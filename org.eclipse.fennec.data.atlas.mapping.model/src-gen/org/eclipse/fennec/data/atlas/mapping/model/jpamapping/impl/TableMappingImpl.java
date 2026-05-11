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

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Table Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl#getClassName <em>Class Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl#getTableName <em>Table Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl#getSchema <em>Schema</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl#getColumnMappings <em>Column Mappings</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl#getJoinMappings <em>Join Mappings</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TableMappingImpl extends MinimalEObjectImpl.Container implements TableMapping {
	/**
	 * The default value of the '{@link #getClassName() <em>Class Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassName()
	 * @generated
	 * @ordered
	 */
	protected static final String CLASS_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getClassName() <em>Class Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassName()
	 * @generated
	 * @ordered
	 */
	protected String className = CLASS_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTableName()
	 * @generated
	 * @ordered
	 */
	protected static final String TABLE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTableName()
	 * @generated
	 * @ordered
	 */
	protected String tableName = TABLE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSchema() <em>Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSchema()
	 * @generated
	 * @ordered
	 */
	protected static final String SCHEMA_EDEFAULT = "PUBLIC";

	/**
	 * The cached value of the '{@link #getSchema() <em>Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSchema()
	 * @generated
	 * @ordered
	 */
	protected String schema = SCHEMA_EDEFAULT;

	/**
	 * The cached value of the '{@link #getColumnMappings() <em>Column Mappings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumnMappings()
	 * @generated
	 * @ordered
	 */
	protected EList<ColumnMapping> columnMappings;

	/**
	 * The cached value of the '{@link #getJoinMappings() <em>Join Mappings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJoinMappings()
	 * @generated
	 * @ordered
	 */
	protected EList<JoinMapping> joinMappings;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TableMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return JPAMappingPackage.Literals.TABLE_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getClassName() {
		return className;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setClassName(String newClassName) {
		String oldClassName = className;
		className = newClassName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.TABLE_MAPPING__CLASS_NAME, oldClassName, className));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTableName() {
		return tableName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTableName(String newTableName) {
		String oldTableName = tableName;
		tableName = newTableName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.TABLE_MAPPING__TABLE_NAME, oldTableName, tableName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSchema() {
		return schema;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSchema(String newSchema) {
		String oldSchema = schema;
		schema = newSchema;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.TABLE_MAPPING__SCHEMA, oldSchema, schema));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ColumnMapping> getColumnMappings() {
		if (columnMappings == null) {
			columnMappings = new EObjectContainmentEList<ColumnMapping>(ColumnMapping.class, this, JPAMappingPackage.TABLE_MAPPING__COLUMN_MAPPINGS);
		}
		return columnMappings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<JoinMapping> getJoinMappings() {
		if (joinMappings == null) {
			joinMappings = new EObjectContainmentEList<JoinMapping>(JoinMapping.class, this, JPAMappingPackage.TABLE_MAPPING__JOIN_MAPPINGS);
		}
		return joinMappings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case JPAMappingPackage.TABLE_MAPPING__COLUMN_MAPPINGS:
				return ((InternalEList<?>)getColumnMappings()).basicRemove(otherEnd, msgs);
			case JPAMappingPackage.TABLE_MAPPING__JOIN_MAPPINGS:
				return ((InternalEList<?>)getJoinMappings()).basicRemove(otherEnd, msgs);
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
			case JPAMappingPackage.TABLE_MAPPING__CLASS_NAME:
				return getClassName();
			case JPAMappingPackage.TABLE_MAPPING__TABLE_NAME:
				return getTableName();
			case JPAMappingPackage.TABLE_MAPPING__SCHEMA:
				return getSchema();
			case JPAMappingPackage.TABLE_MAPPING__COLUMN_MAPPINGS:
				return getColumnMappings();
			case JPAMappingPackage.TABLE_MAPPING__JOIN_MAPPINGS:
				return getJoinMappings();
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
			case JPAMappingPackage.TABLE_MAPPING__CLASS_NAME:
				setClassName((String)newValue);
				return;
			case JPAMappingPackage.TABLE_MAPPING__TABLE_NAME:
				setTableName((String)newValue);
				return;
			case JPAMappingPackage.TABLE_MAPPING__SCHEMA:
				setSchema((String)newValue);
				return;
			case JPAMappingPackage.TABLE_MAPPING__COLUMN_MAPPINGS:
				getColumnMappings().clear();
				getColumnMappings().addAll((Collection<? extends ColumnMapping>)newValue);
				return;
			case JPAMappingPackage.TABLE_MAPPING__JOIN_MAPPINGS:
				getJoinMappings().clear();
				getJoinMappings().addAll((Collection<? extends JoinMapping>)newValue);
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
			case JPAMappingPackage.TABLE_MAPPING__CLASS_NAME:
				setClassName(CLASS_NAME_EDEFAULT);
				return;
			case JPAMappingPackage.TABLE_MAPPING__TABLE_NAME:
				setTableName(TABLE_NAME_EDEFAULT);
				return;
			case JPAMappingPackage.TABLE_MAPPING__SCHEMA:
				setSchema(SCHEMA_EDEFAULT);
				return;
			case JPAMappingPackage.TABLE_MAPPING__COLUMN_MAPPINGS:
				getColumnMappings().clear();
				return;
			case JPAMappingPackage.TABLE_MAPPING__JOIN_MAPPINGS:
				getJoinMappings().clear();
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
			case JPAMappingPackage.TABLE_MAPPING__CLASS_NAME:
				return CLASS_NAME_EDEFAULT == null ? className != null : !CLASS_NAME_EDEFAULT.equals(className);
			case JPAMappingPackage.TABLE_MAPPING__TABLE_NAME:
				return TABLE_NAME_EDEFAULT == null ? tableName != null : !TABLE_NAME_EDEFAULT.equals(tableName);
			case JPAMappingPackage.TABLE_MAPPING__SCHEMA:
				return SCHEMA_EDEFAULT == null ? schema != null : !SCHEMA_EDEFAULT.equals(schema);
			case JPAMappingPackage.TABLE_MAPPING__COLUMN_MAPPINGS:
				return columnMappings != null && !columnMappings.isEmpty();
			case JPAMappingPackage.TABLE_MAPPING__JOIN_MAPPINGS:
				return joinMappings != null && !joinMappings.isEmpty();
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
		result.append(" (className: ");
		result.append(className);
		result.append(", tableName: ");
		result.append(tableName);
		result.append(", schema: ");
		result.append(schema);
		result.append(')');
		return result.toString();
	}

} //TableMappingImpl
