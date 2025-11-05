/*
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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.governance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>System Component Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Defines the type of a system component.
 * <!-- end-model-doc -->
 * @see org.gecko.mac.governance.GovernancePackage#getSystemComponentType()
 * @model
 * @generated
 */
@ProviderType
public enum SystemComponentType implements Enumerator {
	/**
	 * The '<em><b>UNKNOWN</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #UNKNOWN_VALUE
	 * @generated
	 * @ordered
	 */
	UNKNOWN(0, "UNKNOWN", "UNKNOWN"),

	/**
	 * The '<em><b>SENSOR</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A physical or virtual sensor.
	 * <!-- end-model-doc -->
	 * @see #SENSOR_VALUE
	 * @generated
	 * @ordered
	 */
	SENSOR(1, "SENSOR", "SENSOR"),

	/**
	 * The '<em><b>GATEWAY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A gateway device, e.g., LoRaWAN Gateway.
	 * <!-- end-model-doc -->
	 * @see #GATEWAY_VALUE
	 * @generated
	 * @ordered
	 */
	GATEWAY(2, "GATEWAY", "GATEWAY"),

	/**
	 * The '<em><b>BROKER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A message broker, e.g., MQTT.
	 * <!-- end-model-doc -->
	 * @see #BROKER_VALUE
	 * @generated
	 * @ordered
	 */
	BROKER(3, "BROKER", "BROKER"),

	/**
	 * The '<em><b>DATABASE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A database system.
	 * <!-- end-model-doc -->
	 * @see #DATABASE_VALUE
	 * @generated
	 * @ordered
	 */
	DATABASE(4, "DATABASE", "DATABASE"),

	/**
	 * The '<em><b>INTERFACE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An API or interface.
	 * <!-- end-model-doc -->
	 * @see #INTERFACE_VALUE
	 * @generated
	 * @ordered
	 */
	INTERFACE(5, "INTERFACE", "INTERFACE"),

	/**
	 * The '<em><b>BACKEND</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A backend application or service.
	 * <!-- end-model-doc -->
	 * @see #BACKEND_VALUE
	 * @generated
	 * @ordered
	 */
	BACKEND(6, "BACKEND", "BACKEND"),

	/**
	 * The '<em><b>FRONTEND</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A frontend application.
	 * <!-- end-model-doc -->
	 * @see #FRONTEND_VALUE
	 * @generated
	 * @ordered
	 */
	FRONTEND(7, "FRONTEND", "FRONTEND"),

	/**
	 * The '<em><b>AI SYSTEM</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An Artificial Intelligence system, e.g., a model generator.
	 * <!-- end-model-doc -->
	 * @see #AI_SYSTEM_VALUE
	 * @generated
	 * @ordered
	 */
	AI_SYSTEM(8, "AI_SYSTEM", "AI_SYSTEM"),

	/**
	 * The '<em><b>FRAMEWORK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A service or framework.
	 * <!-- end-model-doc -->
	 * @see #FRAMEWORK_VALUE
	 * @generated
	 * @ordered
	 */
	FRAMEWORK(9, "FRAMEWORK", "FRAMEWORK"),

	/**
	 * The '<em><b>ACTOR</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A physical or virtual actor.
	 * <!-- end-model-doc -->
	 * @see #ACTOR_VALUE
	 * @generated
	 * @ordered
	 */
	ACTOR(10, "ACTOR", "ACTOR");

	/**
	 * The '<em><b>UNKNOWN</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #UNKNOWN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNKNOWN_VALUE = 0;

	/**
	 * The '<em><b>SENSOR</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A physical or virtual sensor.
	 * <!-- end-model-doc -->
	 * @see #SENSOR
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SENSOR_VALUE = 1;

	/**
	 * The '<em><b>GATEWAY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A gateway device, e.g., LoRaWAN Gateway.
	 * <!-- end-model-doc -->
	 * @see #GATEWAY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int GATEWAY_VALUE = 2;

	/**
	 * The '<em><b>BROKER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A message broker, e.g., MQTT.
	 * <!-- end-model-doc -->
	 * @see #BROKER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BROKER_VALUE = 3;

	/**
	 * The '<em><b>DATABASE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A database system.
	 * <!-- end-model-doc -->
	 * @see #DATABASE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DATABASE_VALUE = 4;

	/**
	 * The '<em><b>INTERFACE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An API or interface.
	 * <!-- end-model-doc -->
	 * @see #INTERFACE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INTERFACE_VALUE = 5;

	/**
	 * The '<em><b>BACKEND</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A backend application or service.
	 * <!-- end-model-doc -->
	 * @see #BACKEND
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BACKEND_VALUE = 6;

	/**
	 * The '<em><b>FRONTEND</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A frontend application.
	 * <!-- end-model-doc -->
	 * @see #FRONTEND
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FRONTEND_VALUE = 7;

	/**
	 * The '<em><b>AI SYSTEM</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An Artificial Intelligence system, e.g., a model generator.
	 * <!-- end-model-doc -->
	 * @see #AI_SYSTEM
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int AI_SYSTEM_VALUE = 8;

	/**
	 * The '<em><b>FRAMEWORK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A service or framework.
	 * <!-- end-model-doc -->
	 * @see #FRAMEWORK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FRAMEWORK_VALUE = 9;

	/**
	 * The '<em><b>ACTOR</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A physical or virtual actor.
	 * <!-- end-model-doc -->
	 * @see #ACTOR
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ACTOR_VALUE = 10;

	/**
	 * An array of all the '<em><b>System Component Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final SystemComponentType[] VALUES_ARRAY =
		new SystemComponentType[] {
			UNKNOWN,
			SENSOR,
			GATEWAY,
			BROKER,
			DATABASE,
			INTERFACE,
			BACKEND,
			FRONTEND,
			AI_SYSTEM,
			FRAMEWORK,
			ACTOR,
		};

	/**
	 * A public read-only list of all the '<em><b>System Component Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<SystemComponentType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>System Component Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SystemComponentType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SystemComponentType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>System Component Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SystemComponentType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SystemComponentType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>System Component Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SystemComponentType get(int value) {
		switch (value) {
			case UNKNOWN_VALUE: return UNKNOWN;
			case SENSOR_VALUE: return SENSOR;
			case GATEWAY_VALUE: return GATEWAY;
			case BROKER_VALUE: return BROKER;
			case DATABASE_VALUE: return DATABASE;
			case INTERFACE_VALUE: return INTERFACE;
			case BACKEND_VALUE: return BACKEND;
			case FRONTEND_VALUE: return FRONTEND;
			case AI_SYSTEM_VALUE: return AI_SYSTEM;
			case FRAMEWORK_VALUE: return FRAMEWORK;
			case ACTOR_VALUE: return ACTOR;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private SystemComponentType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
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
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //SystemComponentType
