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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Artifact Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getArtifactType()
 * @model
 * @generated
 */
@ProviderType
public enum ArtifactType implements Enumerator {
	/**
	 * The '<em><b>XML</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #XML_VALUE
	 * @generated
	 * @ordered
	 */
	XML(0, "XML", "XML"),

	/**
	 * The '<em><b>JSON</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #JSON_VALUE
	 * @generated
	 * @ordered
	 */
	JSON(1, "JSON", "JSON"),

	/**
	 * The '<em><b>XSD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #XSD_VALUE
	 * @generated
	 * @ordered
	 */
	XSD(2, "XSD", "XSD"),

	/**
	 * The '<em><b>ASYNCAPI</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ASYNCAPI_VALUE
	 * @generated
	 * @ordered
	 */
	ASYNCAPI(3, "ASYNCAPI", "ASYNCAPI"),

	/**
	 * The '<em><b>AVRO</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Apache Avro schema
	 * <!-- end-model-doc -->
	 * @see #AVRO_VALUE
	 * @generated
	 * @ordered
	 */
	AVRO(4, "AVRO", "AVRO"),

	/**
	 * The '<em><b>GRAPHQL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #GRAPHQL_VALUE
	 * @generated
	 * @ordered
	 */
	GRAPHQL(5, "GRAPHQL", "GRAPHQL"),

	/**
	 * The '<em><b>KCONNECT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Apache Kafka Connect schema
	 * <!-- end-model-doc -->
	 * @see #KCONNECT_VALUE
	 * @generated
	 * @ordered
	 */
	KCONNECT(6, "KCONNECT", "KCONNECT"),

	/**
	 * The '<em><b>OPENAPI</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OPENAPI_VALUE
	 * @generated
	 * @ordered
	 */
	OPENAPI(7, "OPENAPI", "OPENAPI"),

	/**
	 * The '<em><b>PROTOBUF</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Google protocol buffers schema
	 * <!-- end-model-doc -->
	 * @see #PROTOBUF_VALUE
	 * @generated
	 * @ordered
	 */
	PROTOBUF(8, "PROTOBUF", "PROTOBUF"),

	/**
	 * The '<em><b>WSDL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Web Service Definition Language
	 * <!-- end-model-doc -->
	 * @see #WSDL_VALUE
	 * @generated
	 * @ordered
	 */
	WSDL(9, "WSDL", "WSDL");

	/**
	 * The '<em><b>XML</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #XML
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int XML_VALUE = 0;

	/**
	 * The '<em><b>JSON</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #JSON
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int JSON_VALUE = 1;

	/**
	 * The '<em><b>XSD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #XSD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int XSD_VALUE = 2;

	/**
	 * The '<em><b>ASYNCAPI</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ASYNCAPI
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ASYNCAPI_VALUE = 3;

	/**
	 * The '<em><b>AVRO</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Apache Avro schema
	 * <!-- end-model-doc -->
	 * @see #AVRO
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int AVRO_VALUE = 4;

	/**
	 * The '<em><b>GRAPHQL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #GRAPHQL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int GRAPHQL_VALUE = 5;

	/**
	 * The '<em><b>KCONNECT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Apache Kafka Connect schema
	 * <!-- end-model-doc -->
	 * @see #KCONNECT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int KCONNECT_VALUE = 6;

	/**
	 * The '<em><b>OPENAPI</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OPENAPI
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OPENAPI_VALUE = 7;

	/**
	 * The '<em><b>PROTOBUF</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Google protocol buffers schema
	 * <!-- end-model-doc -->
	 * @see #PROTOBUF
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PROTOBUF_VALUE = 8;

	/**
	 * The '<em><b>WSDL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Web Service Definition Language
	 * <!-- end-model-doc -->
	 * @see #WSDL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WSDL_VALUE = 9;

	/**
	 * An array of all the '<em><b>Artifact Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ArtifactType[] VALUES_ARRAY =
		new ArtifactType[] {
			XML,
			JSON,
			XSD,
			ASYNCAPI,
			AVRO,
			GRAPHQL,
			KCONNECT,
			OPENAPI,
			PROTOBUF,
			WSDL,
		};

	/**
	 * A public read-only list of all the '<em><b>Artifact Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ArtifactType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Artifact Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ArtifactType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ArtifactType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Artifact Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ArtifactType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ArtifactType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Artifact Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ArtifactType get(int value) {
		switch (value) {
			case XML_VALUE: return XML;
			case JSON_VALUE: return JSON;
			case XSD_VALUE: return XSD;
			case ASYNCAPI_VALUE: return ASYNCAPI;
			case AVRO_VALUE: return AVRO;
			case GRAPHQL_VALUE: return GRAPHQL;
			case KCONNECT_VALUE: return KCONNECT;
			case OPENAPI_VALUE: return OPENAPI;
			case PROTOBUF_VALUE: return PROTOBUF;
			case WSDL_VALUE: return WSDL;
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
	private ArtifactType(int value, String name, String literal) {
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
	
} //ArtifactType
