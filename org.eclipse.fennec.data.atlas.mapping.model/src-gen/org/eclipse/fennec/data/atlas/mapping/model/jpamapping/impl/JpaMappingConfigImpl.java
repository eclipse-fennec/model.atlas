/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Jpa Mapping Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl#getTargetModelNsUri <em>Target Model Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl#getDataSource <em>Data Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl#getTableMappings <em>Table Mappings</em>}</li>
 * </ul>
 *
 * @generated
 */
public class JpaMappingConfigImpl extends MinimalEObjectImpl.Container implements JpaMappingConfig {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getTargetModelNsUri() <em>Target Model Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetModelNsUri()
	 * @generated
	 * @ordered
	 */
	protected static final String TARGET_MODEL_NS_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTargetModelNsUri() <em>Target Model Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetModelNsUri()
	 * @generated
	 * @ordered
	 */
	protected String targetModelNsUri = TARGET_MODEL_NS_URI_EDEFAULT;

	/**
	 * The cached value of the '{@link #getDataSource() <em>Data Source</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataSource()
	 * @generated
	 * @ordered
	 */
	protected DataSourceConfig dataSource;

	/**
	 * The cached value of the '{@link #getTableMappings() <em>Table Mappings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTableMappings()
	 * @generated
	 * @ordered
	 */
	protected EList<TableMapping> tableMappings;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected JpaMappingConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return JPAMappingPackage.Literals.JPA_MAPPING_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JPA_MAPPING_CONFIG__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTargetModelNsUri() {
		return targetModelNsUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTargetModelNsUri(String newTargetModelNsUri) {
		String oldTargetModelNsUri = targetModelNsUri;
		targetModelNsUri = newTargetModelNsUri;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI, oldTargetModelNsUri, targetModelNsUri));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DataSourceConfig getDataSource() {
		return dataSource;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDataSource(DataSourceConfig newDataSource, NotificationChain msgs) {
		DataSourceConfig oldDataSource = dataSource;
		dataSource = newDataSource;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE, oldDataSource, newDataSource);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataSource(DataSourceConfig newDataSource) {
		if (newDataSource != dataSource) {
			NotificationChain msgs = null;
			if (dataSource != null)
				msgs = ((InternalEObject)dataSource).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE, null, msgs);
			if (newDataSource != null)
				msgs = ((InternalEObject)newDataSource).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE, null, msgs);
			msgs = basicSetDataSource(newDataSource, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE, newDataSource, newDataSource));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<TableMapping> getTableMappings() {
		if (tableMappings == null) {
			tableMappings = new EObjectContainmentEList<TableMapping>(TableMapping.class, this, JPAMappingPackage.JPA_MAPPING_CONFIG__TABLE_MAPPINGS);
		}
		return tableMappings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE:
				return basicSetDataSource(null, msgs);
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TABLE_MAPPINGS:
				return ((InternalEList<?>)getTableMappings()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case JPAMappingPackage.JPA_MAPPING_CONFIG__NAME:
				return getName();
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI:
				return getTargetModelNsUri();
			case JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE:
				return getDataSource();
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TABLE_MAPPINGS:
				return getTableMappings();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case JPAMappingPackage.JPA_MAPPING_CONFIG__NAME:
				setName((String)newValue);
				return;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI:
				setTargetModelNsUri((String)newValue);
				return;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE:
				setDataSource((DataSourceConfig)newValue);
				return;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TABLE_MAPPINGS:
				getTableMappings().clear();
				getTableMappings().addAll((Collection<? extends TableMapping>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case JPAMappingPackage.JPA_MAPPING_CONFIG__NAME:
				setName(NAME_EDEFAULT);
				return;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI:
				setTargetModelNsUri(TARGET_MODEL_NS_URI_EDEFAULT);
				return;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE:
				setDataSource((DataSourceConfig)null);
				return;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TABLE_MAPPINGS:
				getTableMappings().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case JPAMappingPackage.JPA_MAPPING_CONFIG__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI:
				return TARGET_MODEL_NS_URI_EDEFAULT == null ? targetModelNsUri != null : !TARGET_MODEL_NS_URI_EDEFAULT.equals(targetModelNsUri);
			case JPAMappingPackage.JPA_MAPPING_CONFIG__DATA_SOURCE:
				return dataSource != null;
			case JPAMappingPackage.JPA_MAPPING_CONFIG__TABLE_MAPPINGS:
				return tableMappings != null && !tableMappings.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", targetModelNsUri: ");
		result.append(targetModelNsUri);
		result.append(')');
		return result.toString();
	}

} //JpaMappingConfigImpl
