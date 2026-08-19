/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.eobject.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;

/**
 * Records every write; the engine's behavior is asserted purely through this recording.
 */
class RecordingWriter implements EObjectRegistryWriter {

	enum Kind {
		PUT, REMOVE, SYNC
	}

	record Op(Kind kind, String source, String key, EObject object, Map<String, Object> properties,
			List<EObjectRegistryEntry> entries) {
	}

	final List<Op> ops = new ArrayList<>();

	List<Op> ops(Kind kind) {
		return ops.stream().filter(op -> op.kind() == kind).toList();
	}

	@Override
	public EObjectRegistry getRegistry() {
		throw new UnsupportedOperationException("not used by the engine");
	}

	@Override
	public void put(String source, String key, EObject object, Map<String, Object> properties) {
		ops.add(new Op(Kind.PUT, source, key, object, properties, null));
	}

	@Override
	public void remove(String source, String key) {
		ops.add(new Op(Kind.REMOVE, source, key, null, null, null));
	}

	@Override
	public void sync(String source, Collection<EObjectRegistryEntry> entries) {
		ops.add(new Op(Kind.SYNC, source, null, null, null, List.copyOf(entries)));
	}
}
