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
package org.eclipse.fennec.data.atlas.jpa.datasource.api;

/**
 * 
 * @author ilenia
 * @since Apr 28, 2026
 */
public class ConnectionCheckException extends RuntimeException {

	/** serialVersionUID */
	private static final long serialVersionUID = -4379424543392755146L;
	
	public ConnectionCheckException(String message, Throwable cause) {
		super(message, cause);		
	}

}
