/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.emf.common.ecore;

import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * This implementation of a {@link DynamicEObjectImpl}, can handle EClasses,
 * that become proxies due to a reload of their EPackage.
 * 
 * @author Juergen Albert
 * @since 20 Feb 2025
 */
public class EClassResolvingDynamicEObject extends DynamicEObjectImpl {

    /**
     * Creates a new instance.
     * 
     * @param eClass
     */
    public EClassResolvingDynamicEObject(EClass eClass) {
        super(eClass);
    }

    public EClassResolvingDynamicEObject() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.emf.ecore.impl.DynamicEObjectImpl#eClass()
     */
    @Override
    public EClass eClass() {
        if (eClass.eIsProxy()) {
            eClass = (EClass) EcoreUtil.resolve(eClass, this);
        }
        return eClass;
    }

    /**
     * The map-entry counterpart of this class, resolving its {@link EClass} the same way.
     *
     * <p>
     * EMF's own {@code DynamicEObjectImpl.BasicEMapEntry} is {@code final}, so this
     * cannot be a subclass of it: it re-implements the same contract on top of
     * {@link EClassResolvingDynamicEObject}, which is where the resolving
     * {@link #eClass()} comes from. The behaviour is EMF's — key and value are the
     * {@code key} and {@code value} features of the entry's EClass, and the hash is
     * computed from the key and cached — with the single difference that an EClass
     * turned into a proxy by an EPackage reload is resolved on access.
     * </p>
     *
     * <p>
     * This exists because the factory used to ask for
     * {@code EClassResolvingDynamicEObject.BasicEMapEntry} when no such class was
     * declared here: the name resolved to the inherited EMF one, so it compiled, looked
     * resolving, and left map entries without the reload-resilience the rest of the model
     * had.
     * </p>
     *
     * @param <K> the entry's key type
     * @param <V> the entry's value type
     */
    public static class BasicEMapEntry<K, V> extends EClassResolvingDynamicEObject implements BasicEMap.Entry<K, V> {

        private int hash = -1;

        public BasicEMapEntry() {
            super();
        }

        /**
         * Creates a new instance.
         *
         * @param eClass the map-entry EClass
         */
        public BasicEMapEntry(EClass eClass) {
            super(eClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public K getKey() {
            return (K) eGet(feature("key"));
        }

        @Override
        public void setKey(Object key) {
            eSet(feature("key"), key);
        }

        @Override
        public int getHash() {
            if (hash == -1) {
                Object theKey = getKey();
                hash = theKey == null ? 0 : theKey.hashCode();
            }
            return hash;
        }

        @Override
        public void setHash(int hash) {
            this.hash = hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValue() {
            return (V) eGet(feature("value"));
        }

        @SuppressWarnings("unchecked")
        @Override
        public V setValue(V value) {
            EStructuralFeature valueFeature = feature("value");
            V result = (V) eGet(valueFeature);
            eSet(valueFeature, value);
            return result;
        }

        /**
         * The named feature of the entry's <em>current</em> EClass.
         *
         * <p>
         * EMF's version caches the key and value features when the EClass is set. This
         * one looks them up through {@link #eClass()} on every access instead, because
         * caching is what a reload invalidates: the cached features belong to the EClass
         * this entry had before, and after the swap they are either stale or — if the
         * EClass was set while still a proxy — {@code null}. The lookup is a map access
         * on an EClass that caches it, so the cost is negligible next to being wrong.
         * </p>
         */
        private EStructuralFeature feature(String name) {
            return eClass().getEStructuralFeature(name);
        }
    }
}
