/**
 * Copyright (c) 2012 - 2025 Kentyou and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.healthcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Health check that compares the scope folders in a file storage root against
 * the scopes the runtime actually has.
 *
 * <p>
 * The file backend lays objects out as
 * {@code <storage root>/<scope>/<registry>/<stage>/}, so a scope name is part
 * of the persistent state and not only of the configuration. Renaming a scope
 * on a volume that already holds data therefore does not move that data: the
 * runtime comes up with an empty scope while the old folder sits next to it,
 * and nothing else in the system says so. That is the case this check exists
 * for.
 * </p>
 *
 * <p>
 * It warns on exactly that signature — a configured scope with no folder of its
 * own <em>while</em> some folder belongs to no configured scope — because
 * either half on its own is ordinary: a freshly added scope has no folder yet,
 * and a retired scope leaves one behind. Unclaimed folders are reported either
 * way, so the old name is always in the output.
 * </p>
 *
 * <p>
 * The built-in {@code atlas} scope is not considered: it only implements the
 * {@code ScopeService} marker, so binding {@link WritableScopeService} leaves
 * it out, which is what we want — its content is the set of globally
 * registered packages and it need not have a folder at all.
 * </p>
 *
 * <p>
 * Configuration-driven on purpose ({@link ConfigurationPolicy#REQUIRE}): only a
 * deployment that stores through the file backend has a storage root to look
 * at, and only it knows where that root is.
 * </p>
 *
 * @since 1.1
 */
@Component(name = StorageScopesHealthCheck.PID, service = HealthCheck.class, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        property = { HealthCheck.NAME + "=Storage Scopes", HealthCheck.TAGS + "=atlas" })
@Designate(ocd = StorageScopesHealthCheck.Config.class)
public class StorageScopesHealthCheck implements HealthCheck {

    /** Configuration PID, short like the other Atlas components so the runtime JSON stays readable. */
    public static final String PID = "StorageScopesHealthCheck";

    @ObjectClassDefinition(name = "Model Atlas Storage Scopes Health Check")
    public @interface Config {

        @AttributeDefinition(name = "Storage Root",
                description = "The file storage root, the same folder the file object storage is configured with. "
                        + "Its direct sub folders are the scopes that have data stored.")
        String storage_root();
    }

    /**
     * Scopes are configuration-driven, so they appear and disappear while this
     * check is active — DYNAMIC and GREEDY for the same reason as
     * {@link ScopesHealthCheck}. Keyed by service so a rebind cannot leave a stale
     * name behind, and holding the name from the service properties rather than
     * {@code getScope()}: it is the very string the configuration sets, which is
     * also what the folder is named after.
     */
    private final Map<WritableScopeService<EObject>, String> scopeNames = new ConcurrentHashMap<>();

    private final Path storageRoot;

    @Activate
    public StorageScopesHealthCheck(Config config) {
        this.storageRoot = Paths.get(config.storage_root());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    void addScopeService(WritableScopeService<EObject> scopeService, Map<String, Object> properties) {
        Object name = properties.getOrDefault(AtlasProperties.ATLAS_SCOPE, properties.get("scope.name"));
        if (name instanceof String scopeName && !scopeName.isBlank()) {
            scopeNames.put(scopeService, scopeName);
        }
    }

    void removeScopeService(WritableScopeService<EObject> scopeService) {
        scopeNames.remove(scopeService);
    }

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();

        if (!Files.isDirectory(storageRoot)) {
            log.info("Storage root {} does not exist yet, nothing is stored", storageRoot);
            return new Result(log);
        }

        Set<String> stored;
        try {
            stored = storedScopes();
        } catch (IOException e) {
            log.warn("Storage root {} could not be read: {}", storageRoot, e.getMessage());
            return new Result(log);
        }

        Set<String> configured = new TreeSet<>(scopeNames.values());

        Set<String> unclaimed = new TreeSet<>(stored);
        unclaimed.removeAll(configured);

        Set<String> withoutData = new TreeSet<>(configured);
        withoutData.removeAll(stored);

        if (!unclaimed.isEmpty() && !withoutData.isEmpty()) {
            log.warn("Scope(s) {} have no data in {}, while {} belong(s) to no configured scope. If a scope was "
                    + "renamed, its data is still stored under the old name: the file backend keys the layout on "
                    + "the scope name and does not move it.", withoutData, storageRoot, unclaimed);
        } else if (!unclaimed.isEmpty()) {
            log.info("Stored scope(s) {} belong to no configured scope", unclaimed);
        }

        log.info("Storage root {}: configured {}, stored {}", storageRoot, configured, stored);

        return new Result(log);
    }

    private Set<String> storedScopes() throws IOException {
        try (Stream<Path> children = Files.list(storageRoot)) {
            return children.filter(Files::isDirectory).map(p -> p.getFileName().toString())
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }
}
