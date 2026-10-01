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
package org.eclipse.fennec.model.atlas.qvt.gdpr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.m2x.model.imperativeocl.AssignExp;
import org.eclipse.fennec.m2x.model.qvtoperational.MappingOperation;
import org.eclipse.fennec.m2x.model.qvtoperational.ModelParameter;
import org.eclipse.fennec.m2x.model.qvtoperational.ModelType;
import org.eclipse.fennec.m2x.model.qvtoperational.OperationalTransformation;
import org.eclipse.fennec.model.gdprReport.FlowKind;

/**
 * Reads the flows out of a stored {@code CompiledUnit}: which source feature reaches which target
 * feature, in which mapping, and how.
 * <p>
 * It works on the compiled AST and never on the source text - nothing is re-parsed, nothing is
 * recompiled, and a unit that is in the registry can be analysed however its source got there.
 * The walk is a pure function; everything it needs is inside the document.
 *
 * <h2>The two ends are read very differently, and one of them is a name match</h2>
 * <p>
 * The <b>right-hand side</b> is exact. A {@code PropertyCallExp} carries {@code referredProperty},
 * a real reference to the {@code EStructuralFeature}, so the fragment it yields matches the
 * {@code uriFragment} of a {@code FeatureEvaluation} in that metamodel's review outright.
 * <p>
 * The <b>left-hand side</b> is not. The compiler turns {@code comment := …} inside a mapping into
 * an assignment to a satellite {@link Variable} <em>named</em> after the target feature - untyped,
 * with no link back to the feature - so the target is resolved by looking that name up on the
 * mapping's result type. That is a name match and it is the one weak joint in the whole analysis:
 * a name the result type does not have produces no reference to follow. It is never dropped
 * silently; the flow is emitted with {@link FlowKind#OPAQUE} and
 * {@link Flow#targetResolved()} {@code false}, because a flow the analyser could not interpret
 * must be visible and silence must never read as "clean".
 *
 * <h2>What it does not follow</h2>
 * <p>
 * A value that passes through an operation other than string concatenation, through
 * {@code resolve}, or through a mapping call, is recorded as {@link FlowKind#OPAQUE} rather than
 * guessed at. Multi-hop flows through intermediate properties and local variables are a single
 * hop here: each assignment is read on its own. Those flows come out as {@code OPAQUE} or, where
 * the temporary is simply not a feature of the result type, as an unresolved target - loud either
 * way, which is the property that matters. Following them is a later step, and it is a dataflow
 * graph rather than a bigger walk.
 */
final class QvtFlowExtractor {

	/**
	 * What one walk of a unit found.
	 *
	 * @param flows        every flow, in the order the mappings and assignments appear, so two runs
	 *                     over the same unit produce the same report
	 * @param readFeatures {@code <nsURI>#<fragment>} of every feature the unit reads anywhere. It
	 *                     is what a classified feature is checked against to say that no mapping
	 *                     reads it
	 * @param directions   nsURI of each metamodel to the direction the transformation declares it
	 *                     in
	 */
	record Extraction(List<Flow> flows, Set<String> readFeatures, Map<String, Direction> directions) {
	}

	/** Which way a metamodel is used, as the transformation's model parameters declare it. */
	enum Direction {
		IN, OUT, INOUT
	}

	private QvtFlowExtractor() {
	}

	/** Walks one compiled unit. Never throws on a shape it does not know; it records it instead. */
	static Extraction extract(CompiledUnit unit) {
		List<Flow> flows = new ArrayList<>();
		Set<String> read = new LinkedHashSet<>();
		EPackage root = unit.getUnit();
		if (root == null) {
			return new Extraction(List.of(), Set.of(), Map.of());
		}
		Map<String, EPackage> packages = packagesOf(unit);
		for (MappingOperation mapping : mappingsOf(root)) {
			collect(mapping, packages, flows, read);
		}
		return new Extraction(List.copyOf(flows), Set.copyOf(read), directionsOf(root));
	}

	/* ------------------------------------------------------------------ the walk */

	/**
	 * Every mapping of the unit, wherever it sits.
	 * <p>
	 * A whole-tree walk rather than {@code transformation.getEOperations()}: the compiler puts a
	 * transformation's operations on an inner {@code EClass} named after the transformation, and a
	 * unit may hold more than one. What matters for the analysis is that a mapping is reached, not
	 * where the language chose to hang it.
	 */
	private static List<MappingOperation> mappingsOf(EPackage root) {
		List<MappingOperation> mappings = new ArrayList<>();
		TreeIterator<EObject> contents = root.eAllContents();
		while (contents.hasNext()) {
			EObject next = contents.next();
			if (next instanceof MappingOperation mapping) {
				mappings.add(mapping);
				// The body is walked separately and holds no mappings of its own
				contents.prune();
			}
		}
		return mappings;
	}

	private static void collect(MappingOperation mapping, Map<String, EPackage> packages, List<Flow> flows,
			Set<String> read) {
		if (mapping.getBody() == null) {
			return;
		}
		EClassifier resultType = mapping.getResult().isEmpty() ? null : mapping.getResult().get(0).getEType();
		URI targetTypeUri = uriOf(resultType);
		String targetNsURI = nsURIOf(targetTypeUri);
		String targetClass = fragmentOf(targetTypeUri);

		for (AssignExp assign : assignmentsOf(mapping)) {
			String targetName = assignedName(assign);
			String targetFeature = targetName == null || targetClass == null ? null
					: targetClass + "/" + targetName;
			EStructuralFeature target = resolve(packages, targetNsURI, targetClass, targetName);
			boolean resolved = target != null;
			String targetTypeName = target == null || target.getEType() == null ? null
					: target.getEType().getName();

			Sources sources = sourcesOf(assign);
			for (URI feature : sources.features()) {
				read.add(nsURIOf(feature) + "#" + fragmentOf(feature));
			}
			FlowKind kind = resolved ? sources.kind() : FlowKind.OPAQUE;
			if (sources.features().isEmpty()) {
				// An assignment that reads no feature moves no personal data - unless the analyser
				// simply could not follow it, and then saying nothing would be the one thing it
				// must not do.
				if (kind == FlowKind.OPAQUE) {
					flows.add(new Flow(mapping.getName(), null, null, targetNsURI, targetFeature, targetTypeName,
							kind, resolved));
				}
				continue;
			}
			for (URI feature : sources.features()) {
				flows.add(new Flow(mapping.getName(), nsURIOf(feature), fragmentOf(feature), targetNsURI,
						targetFeature, targetTypeName, kind, resolved));
			}
		}
	}

	/** Every assignment in a mapping's body, however deeply nested in blocks and loops. */
	private static List<AssignExp> assignmentsOf(MappingOperation mapping) {
		List<AssignExp> found = new ArrayList<>();
		for (OclExpression content : mapping.getBody().getContent()) {
			if (content instanceof AssignExp assign) {
				found.add(assign);
			}
			TreeIterator<EObject> nested = content.eAllContents();
			while (nested.hasNext()) {
				if (nested.next() instanceof AssignExp assign) {
					found.add(assign);
				}
			}
		}
		return found;
	}

	/**
	 * The name the assignment writes to: the satellite variable's name, which is the target
	 * feature's name and the only link to it there is.
	 */
	private static String assignedName(AssignExp assign) {
		if (!(assign.getLeft() instanceof VariableExp left)) {
			// Not the shape the compiler produces for a property assignment in a mapping. It is
			// still an assignment to something, so it is reported rather than skipped.
			return null;
		}
		Variable variable = left.getReferredVariable();
		return variable == null ? null : variable.getName();
	}

	/** What an assignment's value reads, and what stands between the reads and the target. */
	private record Sources(List<URI> features, FlowKind kind) {
	}

	private static Sources sourcesOf(AssignExp assign) {
		List<URI> features = new ArrayList<>();
		int concatenations = 0;
		boolean opaque = false;
		for (OclExpression value : assign.getValue()) {
			for (Iterator<EObject> it = contentsOf(value); it.hasNext();) {
				EObject node = it.next();
				if (node instanceof PropertyCallExp property) {
					URI uri = uriOf(property.getReferredProperty());
					if (uri != null && !features.contains(uri)) {
						features.add(uri);
					}
				} else if (node instanceof OperationCallExp call) {
					String name = call.getName();
					if (CONCATENATIONS.contains(name)) {
						concatenations++;
					} else if (name != null && !"=".equals(name)) {
						// Anything else transforms the value in a way this analyser does not model:
						// a blackbox, a library call, a mapping call, resolve. The data still
						// travels; what it looks like at the other end is not knowable here.
						opaque = true;
					}
				}
			}
		}
		return new Sources(features, kindOf(features.size(), concatenations, opaque));
	}

	private static FlowKind kindOf(int features, int concatenations, boolean opaque) {
		if (opaque) {
			return FlowKind.OPAQUE;
		}
		if (concatenations > 0) {
			return features > 1 ? FlowKind.CONCATENATION : FlowKind.EXPRESSION;
		}
		return features == 1 ? FlowKind.DIRECT : FlowKind.EXPRESSION;
	}

	/** String concatenation, the one operation whose effect on a value the rules do model. */
	private static final Set<String> CONCATENATIONS = Set.of("+", "concat");

	private static Iterator<EObject> contentsOf(EObject root) {
		List<EObject> all = new ArrayList<>();
		all.add(root);
		TreeIterator<EObject> nested = root.eAllContents();
		while (nested.hasNext()) {
			all.add(nested.next());
		}
		return all.iterator();
	}

	/* ------------------------------------------------------------------ the unit's metamodels */

	/**
	 * The metamodels the unit carries a copy of, by nsURI. A unit compiled against dynamic packages
	 * embeds them, which is what lets the target name be checked without a package registry and
	 * without a ResourceSet.
	 */
	private static Map<String, EPackage> packagesOf(CompiledUnit unit) {
		Map<String, EPackage> packages = new LinkedHashMap<>();
		for (EPackage ePackage : unit.getPackages()) {
			if (ePackage != null && ePackage.getNsURI() != null) {
				packages.put(ePackage.getNsURI(), ePackage);
			}
		}
		return packages;
	}

	/**
	 * The feature of the result type the satellite variable names, or {@code null}.
	 * <p>
	 * Answers {@code null} when the metamodel is not in the unit, which is the honest answer: the
	 * analyser could not check, so it did not check. The flow is then {@code OPAQUE} and says so.
	 */
	private static EStructuralFeature resolve(Map<String, EPackage> packages, String nsURI, String classFragment,
			String featureName) {
		EPackage ePackage = nsURI == null ? null : packages.get(nsURI);
		if (ePackage == null || classFragment == null || featureName == null) {
			return null;
		}
		EClassifier classifier = ePackage.getEClassifier(classFragment.startsWith("//")
				? classFragment.substring(2)
				: classFragment);
		return classifier instanceof EClass eClass ? eClass.getEStructuralFeature(featureName) : null;
	}

	private static Map<String, Direction> directionsOf(EPackage root) {
		if (!(root instanceof OperationalTransformation transformation)) {
			return Map.of();
		}
		Map<String, Direction> directions = new LinkedHashMap<>();
		for (ModelParameter parameter : transformation.getModelParameter()) {
			String nsURI = metamodelOf(parameter);
			if (nsURI == null) {
				continue;
			}
			Direction declared = switch (parameter.getKind()) {
			case OUT -> Direction.OUT;
			case INOUT -> Direction.INOUT;
			// 'in' is the serialisation default, so a parameter with no kind at all is 'in'
			default -> Direction.IN;
			};
			// A metamodel used both ways, by two parameters or by one inout one, is both
			directions.merge(nsURI, declared, (first, second) -> first == second ? first : Direction.INOUT);
		}
		return directions;
	}

	/** The nsURI behind a model parameter: its {@code ModelType} satellite's metamodel. */
	private static String metamodelOf(ModelParameter parameter) {
		if (!(parameter.getEType() instanceof ModelType modelType) || modelType.getMetamodel().isEmpty()) {
			return null;
		}
		EPackage metamodel = modelType.getMetamodel().get(0);
		if (metamodel.eIsProxy()) {
			return nsURIOf(uriOf(metamodel));
		}
		return metamodel.getNsURI();
	}

	/* ------------------------------------------------------------------ addressing */

	/**
	 * Where an object lives, as a URI whose first half is an nsURI and whose fragment addresses the
	 * element inside it.
	 * <p>
	 * A stored unit's cross-metamodel references are proxies until something resolves them, and
	 * whether they are resolved depends on the ResourceSet the unit was read into rather than on
	 * anything about the unit. Both cases have to give the same answer, so a proxy is read off its
	 * URI and a resolved object is addressed from the model: the fragment of an
	 * {@code EStructuralFeature} in a package that is <em>contained</em> in the unit document is
	 * a positional path, not {@code //Patient/fullName}, and a review is indexed by the latter.
	 */
	private static URI uriOf(EObject object) {
		if (object == null) {
			return null;
		}
		if (object.eIsProxy()) {
			return ((InternalEObject) object).eProxyURI();
		}
		if (object instanceof EStructuralFeature feature && feature.getEContainingClass() != null) {
			EClass owner = feature.getEContainingClass();
			EPackage owning = owner.getEPackage();
			return owning == null ? null
					: URI.createURI(owning.getNsURI() + "#//" + owner.getName() + "/" + feature.getName());
		}
		if (object instanceof EClassifier classifier && classifier.getEPackage() != null) {
			return URI.createURI(classifier.getEPackage().getNsURI() + "#//" + classifier.getName());
		}
		return EcoreUtil.getURI(object);
	}

	private static String nsURIOf(URI uri) {
		return uri == null ? null : uri.trimFragment().toString();
	}

	private static String fragmentOf(URI uri) {
		return uri == null ? null : uri.fragment();
	}
}
