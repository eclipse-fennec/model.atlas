/**
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage
 * @generated
 */
public class DatagenSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static DatagenPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DatagenSwitch() {
		if (modelPackage == null) {
			modelPackage = DatagenPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case DatagenPackage.DATA_GEN_CONFIG: {
				DataGenConfig dataGenConfig = (DataGenConfig)theEObject;
				T result = caseDataGenConfig(dataGenConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DatagenPackage.CLASS_GEN_CONFIG: {
				ClassGenConfig classGenConfig = (ClassGenConfig)theEObject;
				T result = caseClassGenConfig(classGenConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG: {
				AttributeGenConfig attributeGenConfig = (AttributeGenConfig)theEObject;
				T result = caseAttributeGenConfig(attributeGenConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DatagenPackage.REFERENCE_GEN_CONFIG: {
				ReferenceGenConfig referenceGenConfig = (ReferenceGenConfig)theEObject;
				T result = caseReferenceGenConfig(referenceGenConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DatagenPackage.CUSTOM_GENERATOR_DEF: {
				CustomGeneratorDef customGeneratorDef = (CustomGeneratorDef)theEObject;
				T result = caseCustomGeneratorDef(customGeneratorDef);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DatagenPackage.DATA_GEN_RESULT: {
				DataGenResult dataGenResult = (DataGenResult)theEObject;
				T result = caseDataGenResult(dataGenResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Gen Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataGenConfig(DataGenConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Class Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Class Gen Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClassGenConfig(ClassGenConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Attribute Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Attribute Gen Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAttributeGenConfig(AttributeGenConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reference Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reference Gen Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReferenceGenConfig(ReferenceGenConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Custom Generator Def</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Custom Generator Def</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCustomGeneratorDef(CustomGeneratorDef object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Gen Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Gen Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataGenResult(DataGenResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //DatagenSwitch
