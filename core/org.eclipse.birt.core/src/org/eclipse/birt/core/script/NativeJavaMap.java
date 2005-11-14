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

package org.eclipse.birt.core.script;

import java.util.Map;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * Represents the scriptable object for Java object which implements the
 * interface <code>Map</code>.
 * 
 * @version $Revision: 1.8 $ $Date: 2005/06/01 07:57:57 $
 */
class NativeJavaMap extends NativeJavaObject
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3988584321233636629L;

	public NativeJavaMap( )
	{
	}

	public NativeJavaMap( Scriptable scope, Object javaObject, Class staticType )
	{
		super( scope, javaObject, staticType );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mozilla.javascript.Scriptable#has(java.lang.String,
	 *      org.mozilla.javascript.Scriptable)
	 */

	public boolean has( String name, Scriptable start )
	{
		return ( (Map) javaObject ).containsKey( name );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mozilla.javascript.Scriptable#get(java.lang.String,
	 *      org.mozilla.javascript.Scriptable)
	 */

	public Object get( String name, Scriptable start )
	{
		if ( has( name, start ) )
		{
			return ( (Map) javaObject ).get( name );
		}
		throw new JavaScriptException( name + " not found", "<unknown>", -1 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mozilla.javascript.Scriptable#put(java.lang.String,
	 *      org.mozilla.javascript.Scriptable, java.lang.Object)
	 */

	public void put( String name, Scriptable start, Object value )
	{
		( (Map) javaObject ).put( name, value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mozilla.javascript.Scriptable#delete(java.lang.String)
	 */

	public void delete( String name )
	{
		( (Map) javaObject ).remove( name );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mozilla.javascript.Scriptable#get(int,
	 *      org.mozilla.javascript.Scriptable)
	 */

	public Object get( int index, Scriptable start )
	{
		if ( has( new Integer( index ).toString( ), start ) )
		{
			return ( (Map) javaObject ).get( new Integer( index ).toString( ) );
		}
		throw new JavaScriptException( index + " not found", "<unknown>", -1 ); //$NON-NLS-1$

	}

	public void put( int index, Scriptable start, Object value )
	{
		( (Map) javaObject ).put( new Integer( index ).toString( ), value );
	}

}