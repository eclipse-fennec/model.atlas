/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.mcp.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.mcp.api.MCPTool;
import org.eclipse.fennec.model.atlas.mcp.tools.api.PublishableObject;
import org.eclipse.fennec.model.atlas.publisher.ObjectPublisher;
import org.eclipse.fennec.model.atlas.mcp.tools.api.PublishableObjectSource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * MCP tool storing one agent-supplied object in the model atlas.
 * <p>
 * The counterpart of {@link PostToModelAtlasTool} for instances rather than
 * schemas, and like it the agent never carries the document: it names an id, and
 * the object is fetched from whichever {@link PublishableObjectSource} holds it
 * and serialized here. A report of any size therefore costs the same handful of
 * output tokens as an empty one, and cannot be shortened or mangled on the way
 * out.
 * <p>
 * The agent names only <em>which</em> object and under which id. The scope, the
 * object registry, the stage, the wire format and whether an object already
 * stored under that id may be replaced are the deployment's.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
@Component(name = "PostObjectToModelAtlasTool", service = MCPTool.class, property = "tool.name=post_object_to_model_atlas")
public class PostObjectToModelAtlasTool extends AbstractAtlasTool {

	private static final Logger LOGGER = Logger.getLogger(PostObjectToModelAtlasTool.class.getName());

	@Reference
	ObjectPublisher publisher;

	/**
	 * The sources, newest-ranked first. Dynamic and optional: a deployment may register none, and
	 * the tool then has nothing to publish and says so. Kept sorted here rather than relying on the
	 * order DS hands out, because "asked in ranking order" is part of the contract.
	 */
	private final List<RankedSource> sources = new CopyOnWriteArrayList<>();

	private record RankedSource(PublishableObjectSource source, int ranking, long serviceId) {
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addSource(PublishableObjectSource source, Map<String, Object> properties) {
		List<RankedSource> ordered = new ArrayList<>(sources);
		ordered.add(new RankedSource(source, intValue(properties, Constants.SERVICE_RANKING),
				longValue(properties, Constants.SERVICE_ID)));
		// Highest ranking first; ties by service id, so the older registration wins — the same
		// order the framework itself uses to pick a single service.
		ordered.sort(Comparator.comparingInt(RankedSource::ranking).reversed()
				.thenComparingLong(RankedSource::serviceId));
		sources.clear();
		sources.addAll(ordered);
	}

	void removeSource(PublishableObjectSource source) {
		sources.removeIf(ranked -> ranked.source() == source);
	}

	private static int intValue(Map<String, Object> properties, String key) {
		Object value = properties == null ? null : properties.get(key);
		return value instanceof Number number ? number.intValue() : 0;
	}

	private static long longValue(Map<String, Object> properties, String key) {
		Object value = properties == null ? null : properties.get(key);
		return value instanceof Number number ? number.longValue() : Long.MAX_VALUE;
	}

	@Activate
	void activate() {
		this.name = "post_object_to_model_atlas";
		this.description = "Store one object — an instance, not a metamodel — in this runtime's model atlas, so "
				+ "data assembled in this session is handed over as a stored, versioned object rather than pasted "
				+ "into a reply. Name the object with 'objectId'; the tool fetches it from whichever tool set "
				+ "assembled it, so you never write the document out. That id is the one that tool gave you. "
				+ "The object's type must already be published as a schema in the atlas — use post_to_model_atlas "
				+ "first for a metamodel you authored here — and it must be a type the destination registry "
				+ "accepts, or the atlas refuses it. The scope, the object registry, the stage and whether an "
				+ "existing object may be replaced are fixed by the deployment and are not parameters: if the id "
				+ "is already taken you are told so, and the answer is a free id, not a retry.";
		this.inputSchema = """
				{
					"type": "object",
					"properties": {
						"objectId": {
							"type": "string",
							"description": "The id of the object to store, as given to you by the tool that assembled it. It is also the id the object is stored and read back under. A single name — no '/', and not '.' or '..'."
						},
						"name": {
							"type": "string",
							"description": "Optional human-readable name stored with the object. Omit it unless you have one the assembling tool did not: what it supplies is used when this is absent."
						},
						"version": {
							"type": "string",
							"description": "Optional version string stored with the object, e.g. '1.0.0'. Omit it unless you have one the assembling tool did not."
						}
					},
					"required": ["objectId"]
				}
				""";
	}

	/**
	 * The wire format the sources are asked to produce. It is the deployment's, never the agent's,
	 * and it is no longer in the description either: nothing the agent writes is in that format.
	 */
	private String contentType() {
		return publisher == null ? null : publisher.contentType();
	}

	@Override
	public Mono<McpSchema.CallToolResult> execute(McpAsyncServerExchange exchange, Map<String, Object> arguments) {
		return run(() -> {
			String objectId = requireString(arguments, "objectId");
			// Asked exactly once: find() is a pure read, but it serializes a document that can be
			// tens of kilobytes, and a source is entitled to assume one fetch means one publish.
			Held held = findHolder(objectId);
			PublishableObject object = held.object();

			// The agent may still name these; what the source supplies wins nothing over an
			// explicit argument, but it is what is used when the agent says nothing.
			String name = firstNonBlank(optionalString(arguments, "name"), object.name());
			String version = firstNonBlank(optionalString(arguments, "version"), object.version());

			Object receipt = publisher.publish(objectId, object.content(), name, version);
			notifyPublished(held.source(), objectId);
			return receipt;
		});
	}

	/**
	 * The first source claiming {@code objectId}, asked in ranking order.
	 * <p>
	 * A source that throws fails the call rather than being skipped: a source broken while holding
	 * the id would otherwise be indistinguishable from an id nothing holds, and the agent would be
	 * sent to re-check an id it got right.
	 */
	private Held findHolder(String objectId) {
		Held holder = null;
		for (RankedSource candidate : sources) {
			Optional<PublishableObject> found = find(candidate, objectId);
			if (found.isPresent()) {
				if (holder == null) {
					holder = new Held(candidate.source(), found.get());
				} else {
					LOGGER.fine(() -> "More than one source holds '" + objectId + "'; using the highest-ranked");
					break;
				}
			}
		}
		if (holder == null) {
			throw new ToolException(String.format(
					"No object with id '%s' is held by anything in this runtime, and nothing was published. "
							+ "This tool stores an object another tool assembled; the id is the one that tool "
							+ "gave you. Check you are using that id, and that the call which was meant to "
							+ "produce the object actually succeeded.",
					objectId));
		}
		return holder;
	}

	/** The winning source and what it handed over, so nothing is fetched twice. */
	private record Held(PublishableObjectSource source, PublishableObject object) {
	}

	/**
	 * Ask one source, turning its refusal into something the agent can act on. A source throws to
	 * refuse an object it holds but that is not fit to store — a report with no findings in it, say
	 * — and that reason is the useful part of the answer, so it is not sanitized away.
	 */
	private Optional<PublishableObject> find(RankedSource candidate, String objectId) {
		try {
			return candidate.source().find(objectId, contentType());
		} catch (RuntimeException e) {
			String reason = e.getMessage() == null || e.getMessage().isBlank() ? e.toString() : e.getMessage();
			throw new ToolException(String.format(
					"The object '%s' was not published: the tool set holding it refused to hand it over — %s",
					objectId, reason));
		}
	}

	/**
	 * Tell the source its object is stored — only the one it came from, and never before the atlas
	 * returned a receipt. Anything it throws is logged and swallowed: the object is stored, and a
	 * notification must not turn a successful publish into a failed call.
	 */
	private static void notifyPublished(PublishableObjectSource source, String objectId) {
		try {
			source.published(objectId);
		} catch (RuntimeException e) {
			LOGGER.log(Level.WARNING, e,
					() -> "Source notification failed for '" + objectId + "'; the object is stored regardless");
		}
	}

	private static String firstNonBlank(String preferred, String fallback) {
		return preferred != null && !preferred.isBlank() ? preferred : fallback;
	}
}
