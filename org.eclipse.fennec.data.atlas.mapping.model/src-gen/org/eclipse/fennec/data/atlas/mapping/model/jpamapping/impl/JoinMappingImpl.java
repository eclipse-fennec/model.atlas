/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Join Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl#getReferenceName <em>Reference Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl#getJoinColumn <em>Join Column</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl#getJoinType <em>Join Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl#getCascadeType <em>Cascade Type</em>}</li>
 * </ul>
 *
 * @generated
 */
public class JoinMappingImpl extends MinimalEObjectImpl.Container implements JoinMapping {
	/**
	 * The default value of the '{@link #getReferenceName() <em>Reference Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferenceName()
	 * @generated
	 * @ordered
	 */
	protected static final String REFERENCE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReferenceName() <em>Reference Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferenceName()
	 * @generated
	 * @ordered
	 */
	protected String referenceName = REFERENCE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getJoinColumn() <em>Join Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJoinColumn()
	 * @generated
	 * @ordered
	 */
	protected static final String JOIN_COLUMN_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJoinColumn() <em>Join Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJoinColumn()
	 * @generated
	 * @ordered
	 */
	protected String joinColumn = JOIN_COLUMN_EDEFAULT;

	/**
	 * The default value of the '{@link #getJoinType() <em>Join Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJoinType()
	 * @generated
	 * @ordered
	 */
	protected static final JoinType JOIN_TYPE_EDEFAULT = JoinType.FOREIGN_KEY;

	/**
	 * The cached value of the '{@link #getJoinType() <em>Join Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJoinType()
	 * @generated
	 * @ordered
	 */
	protected JoinType joinType = JOIN_TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getCascadeType() <em>Cascade Type</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCascadeType()
	 * @generated
	 * @ordered
	 */
	protected EList<CascadeType> cascadeType;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected JoinMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return JPAMappingPackage.Literals.JOIN_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getReferenceName() {
		return referenceName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReferenceName(String newReferenceName) {
		String oldReferenceName = referenceName;
		referenceName = newReferenceName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JOIN_MAPPING__REFERENCE_NAME, oldReferenceName, referenceName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJoinColumn() {
		return joinColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJoinColumn(String newJoinColumn) {
		String oldJoinColumn = joinColumn;
		joinColumn = newJoinColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JOIN_MAPPING__JOIN_COLUMN, oldJoinColumn, joinColumn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public JoinType getJoinType() {
		return joinType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJoinType(JoinType newJoinType) {
		JoinType oldJoinType = joinType;
		joinType = newJoinType == null ? JOIN_TYPE_EDEFAULT : newJoinType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, JPAMappingPackage.JOIN_MAPPING__JOIN_TYPE, oldJoinType, joinType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<CascadeType> getCascadeType() {
		if (cascadeType == null) {
			cascadeType = new EDataTypeUniqueEList<CascadeType>(CascadeType.class, this, JPAMappingPackage.JOIN_MAPPING__CASCADE_TYPE);
		}
		return cascadeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case JPAMappingPackage.JOIN_MAPPING__REFERENCE_NAME:
				return getReferenceName();
			case JPAMappingPackage.JOIN_MAPPING__JOIN_COLUMN:
				return getJoinColumn();
			case JPAMappingPackage.JOIN_MAPPING__JOIN_TYPE:
				return getJoinType();
			case JPAMappingPackage.JOIN_MAPPING__CASCADE_TYPE:
				return getCascadeType();
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
			case JPAMappingPackage.JOIN_MAPPING__REFERENCE_NAME:
				setReferenceName((String)newValue);
				return;
			case JPAMappingPackage.JOIN_MAPPING__JOIN_COLUMN:
				setJoinColumn((String)newValue);
				return;
			case JPAMappingPackage.JOIN_MAPPING__JOIN_TYPE:
				setJoinType((JoinType)newValue);
				return;
			case JPAMappingPackage.JOIN_MAPPING__CASCADE_TYPE:
				getCascadeType().clear();
				getCascadeType().addAll((Collection<? extends CascadeType>)newValue);
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
			case JPAMappingPackage.JOIN_MAPPING__REFERENCE_NAME:
				setReferenceName(REFERENCE_NAME_EDEFAULT);
				return;
			case JPAMappingPackage.JOIN_MAPPING__JOIN_COLUMN:
				setJoinColumn(JOIN_COLUMN_EDEFAULT);
				return;
			case JPAMappingPackage.JOIN_MAPPING__JOIN_TYPE:
				setJoinType(JOIN_TYPE_EDEFAULT);
				return;
			case JPAMappingPackage.JOIN_MAPPING__CASCADE_TYPE:
				getCascadeType().clear();
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
			case JPAMappingPackage.JOIN_MAPPING__REFERENCE_NAME:
				return REFERENCE_NAME_EDEFAULT == null ? referenceName != null : !REFERENCE_NAME_EDEFAULT.equals(referenceName);
			case JPAMappingPackage.JOIN_MAPPING__JOIN_COLUMN:
				return JOIN_COLUMN_EDEFAULT == null ? joinColumn != null : !JOIN_COLUMN_EDEFAULT.equals(joinColumn);
			case JPAMappingPackage.JOIN_MAPPING__JOIN_TYPE:
				return joinType != JOIN_TYPE_EDEFAULT;
			case JPAMappingPackage.JOIN_MAPPING__CASCADE_TYPE:
				return cascadeType != null && !cascadeType.isEmpty();
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
		result.append(" (referenceName: ");
		result.append(referenceName);
		result.append(", joinColumn: ");
		result.append(joinColumn);
		result.append(", joinType: ");
		result.append(joinType);
		result.append(", cascadeType: ");
		result.append(cascadeType);
		result.append(')');
		return result.toString();
	}

} //JoinMappingImpl
