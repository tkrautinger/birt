/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.model.elements.interfaces;

/**
 * The interface for Dimension element to store the constants on it.
 */

public interface IDimensionModel
{

	/**
	 * Name of the property which indicates whether a dimension element is a
	 * special type of Time.
	 */

	static final String IS_TIME_TYPE_PROP = "isTimeType"; //$NON-NLS-1$

	/**
	 * Identifier of the slot that holds a list of Hierarchy elements.
	 */

	static final String HIERARCHIES_PROP = "hierarchies"; //$NON-NLS-1$
}
