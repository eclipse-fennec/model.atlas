/*
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Attribute Gen Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl#getGeneratorKey <em>Generator Key</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl#getGeneratorArgs <em>Generator Args</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl#isUnique <em>Unique</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl#getStaticValue <em>Static Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl#getTemplate <em>Template</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AttributeGenConfigImpl extends MinimalEObjectImpl.Container implements AttributeGenConfig {
	/**
	 * The default value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected String featureName = FEATURE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getGeneratorKey() <em>Generator Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGeneratorKey()
	 * @generated
	 * @ordered
	 */
	protected static final String GENERATOR_KEY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGeneratorKey() <em>Generator Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGeneratorKey()
	 * @generated
	 * @ordered
	 */
	protected String generatorKey = GENERATOR_KEY_EDEFAULT;

	/**
	 * The default value of the '{@link #getGeneratorArgs() <em>Generator Args</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGeneratorArgs()
	 * @generated
	 * @ordered
	 */
	protected static final String GENERATOR_ARGS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGeneratorArgs() <em>Generator Args</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGeneratorArgs()
	 * @generated
	 * @ordered
	 */
	protected String generatorArgs = GENERATOR_ARGS_EDEFAULT;

	/**
	 * The default value of the '{@link #isUnique() <em>Unique</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isUnique()
	 * @generated
	 * @ordered
	 */
	protected static final boolean UNIQUE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isUnique() <em>Unique</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isUnique()
	 * @generated
	 * @ordered
	 */
	protected boolean unique = UNIQUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getStaticValue() <em>Static Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStaticValue()
	 * @generated
	 * @ordered
	 */
	protected static final String STATIC_VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStaticValue() <em>Static Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStaticValue()
	 * @generated
	 * @ordered
	 */
	protected String staticValue = STATIC_VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getTemplate() <em>Template</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTemplate()
	 * @generated
	 * @ordered
	 */
	protected static final String TEMPLATE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTemplate() <em>Template</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTemplate()
	 * @generated
	 * @ordered
	 */
	protected String template = TEMPLATE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AttributeGenConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DatagenPackage.Literals.ATTRIBUTE_GEN_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureName(String newFeatureName) {
		String oldFeatureName = featureName;
		featureName = newFeatureName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.ATTRIBUTE_GEN_CONFIG__FEATURE_NAME, oldFeatureName, featureName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGeneratorKey() {
		return generatorKey;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGeneratorKey(String newGeneratorKey) {
		String oldGeneratorKey = generatorKey;
		generatorKey = newGeneratorKey;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY, oldGeneratorKey, generatorKey));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGeneratorArgs() {
		return generatorArgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGeneratorArgs(String newGeneratorArgs) {
		String oldGeneratorArgs = generatorArgs;
		generatorArgs = newGeneratorArgs;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS, oldGeneratorArgs, generatorArgs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isUnique() {
		return unique;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnique(boolean newUnique) {
		boolean oldUnique = unique;
		unique = newUnique;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.ATTRIBUTE_GEN_CONFIG__UNIQUE, oldUnique, unique));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStaticValue() {
		return staticValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStaticValue(String newStaticValue) {
		String oldStaticValue = staticValue;
		staticValue = newStaticValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.ATTRIBUTE_GEN_CONFIG__STATIC_VALUE, oldStaticValue, staticValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTemplate() {
		return template;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTemplate(String newTemplate) {
		String oldTemplate = template;
		template = newTemplate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.ATTRIBUTE_GEN_CONFIG__TEMPLATE, oldTemplate, template));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__FEATURE_NAME:
				return getFeatureName();
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY:
				return getGeneratorKey();
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS:
				return getGeneratorArgs();
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__UNIQUE:
				return isUnique();
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__STATIC_VALUE:
				return getStaticValue();
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__TEMPLATE:
				return getTemplate();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__FEATURE_NAME:
				setFeatureName((String)newValue);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY:
				setGeneratorKey((String)newValue);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS:
				setGeneratorArgs((String)newValue);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__UNIQUE:
				setUnique((Boolean)newValue);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__STATIC_VALUE:
				setStaticValue((String)newValue);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__TEMPLATE:
				setTemplate((String)newValue);
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
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__FEATURE_NAME:
				setFeatureName(FEATURE_NAME_EDEFAULT);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY:
				setGeneratorKey(GENERATOR_KEY_EDEFAULT);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS:
				setGeneratorArgs(GENERATOR_ARGS_EDEFAULT);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__UNIQUE:
				setUnique(UNIQUE_EDEFAULT);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__STATIC_VALUE:
				setStaticValue(STATIC_VALUE_EDEFAULT);
				return;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__TEMPLATE:
				setTemplate(TEMPLATE_EDEFAULT);
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
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__FEATURE_NAME:
				return FEATURE_NAME_EDEFAULT == null ? featureName != null : !FEATURE_NAME_EDEFAULT.equals(featureName);
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY:
				return GENERATOR_KEY_EDEFAULT == null ? generatorKey != null : !GENERATOR_KEY_EDEFAULT.equals(generatorKey);
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS:
				return GENERATOR_ARGS_EDEFAULT == null ? generatorArgs != null : !GENERATOR_ARGS_EDEFAULT.equals(generatorArgs);
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__UNIQUE:
				return unique != UNIQUE_EDEFAULT;
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__STATIC_VALUE:
				return STATIC_VALUE_EDEFAULT == null ? staticValue != null : !STATIC_VALUE_EDEFAULT.equals(staticValue);
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG__TEMPLATE:
				return TEMPLATE_EDEFAULT == null ? template != null : !TEMPLATE_EDEFAULT.equals(template);
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
		result.append(" (featureName: ");
		result.append(featureName);
		result.append(", generatorKey: ");
		result.append(generatorKey);
		result.append(", generatorArgs: ");
		result.append(generatorArgs);
		result.append(", unique: ");
		result.append(unique);
		result.append(", staticValue: ");
		result.append(staticValue);
		result.append(", template: ");
		result.append(template);
		result.append(')');
		return result.toString();
	}

} //AttributeGenConfigImpl
