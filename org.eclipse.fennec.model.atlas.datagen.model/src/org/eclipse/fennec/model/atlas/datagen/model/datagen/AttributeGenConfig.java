/*
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Attribute Gen Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Konfiguration fuer die Generierung eines einzelnen Attributwerts.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorKey <em>Generator Key</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorArgs <em>Generator Args</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#isUnique <em>Unique</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getStaticValue <em>Static Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getTemplate <em>Template</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig()
 * @model
 * @generated
 */
@ProviderType
public interface AttributeGenConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name des Attributs in der EClass.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature Name</em>' attribute.
	 * @see #setFeatureName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig_FeatureName()
	 * @model required="true"
	 * @generated
	 */
	String getFeatureName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getFeatureName <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Name</em>' attribute.
	 * @see #getFeatureName()
	 * @generated
	 */
	void setFeatureName(String value);

	/**
	 * Returns the value of the '<em><b>Generator Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Schluessel des Generators (z.B. faker.person.firstName).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Generator Key</em>' attribute.
	 * @see #setGeneratorKey(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig_GeneratorKey()
	 * @model required="true"
	 * @generated
	 */
	String getGeneratorKey();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorKey <em>Generator Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Generator Key</em>' attribute.
	 * @see #getGeneratorKey()
	 * @generated
	 */
	void setGeneratorKey(String value);

	/**
	 * Returns the value of the '<em><b>Generator Args</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JSON-String mit Argumenten fuer den Generator.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Generator Args</em>' attribute.
	 * @see #setGeneratorArgs(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig_GeneratorArgs()
	 * @model
	 * @generated
	 */
	String getGeneratorArgs();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorArgs <em>Generator Args</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Generator Args</em>' attribute.
	 * @see #getGeneratorArgs()
	 * @generated
	 */
	void setGeneratorArgs(String value);

	/**
	 * Returns the value of the '<em><b>Unique</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Ob generierte Werte eindeutig sein muessen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Unique</em>' attribute.
	 * @see #setUnique(boolean)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig_Unique()
	 * @model default="false"
	 * @generated
	 */
	boolean isUnique();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#isUnique <em>Unique</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unique</em>' attribute.
	 * @see #isUnique()
	 * @generated
	 */
	void setUnique(boolean value);

	/**
	 * Returns the value of the '<em><b>Static Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Statischer Wert (ueberschreibt generatorKey wenn gesetzt).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Static Value</em>' attribute.
	 * @see #setStaticValue(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig_StaticValue()
	 * @model
	 * @generated
	 */
	String getStaticValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getStaticValue <em>Static Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Static Value</em>' attribute.
	 * @see #getStaticValue()
	 * @generated
	 */
	void setStaticValue(String value);

	/**
	 * Returns the value of the '<em><b>Template</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Template mit #{key} Platzhaltern fuer zusammengesetzte Werte.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Template</em>' attribute.
	 * @see #setTemplate(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getAttributeGenConfig_Template()
	 * @model
	 * @generated
	 */
	String getTemplate();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getTemplate <em>Template</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Template</em>' attribute.
	 * @see #getTemplate()
	 * @generated
	 */
	void setTemplate(String value);

} // AttributeGenConfig
