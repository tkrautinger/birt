
/*******************************************************************************
 * Copyright (c) 2004, 2011 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.data.engine.executor.aggregation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.script.ScriptContext;
import org.eclipse.birt.data.engine.api.IBaseExpression;
import org.eclipse.birt.data.engine.api.IDataScriptEngine;
import org.eclipse.birt.data.engine.api.IScriptExpression;
import org.eclipse.birt.data.engine.api.aggregation.Accumulator;
import org.eclipse.birt.data.engine.api.aggregation.IAggrFunction;
import org.eclipse.birt.data.engine.api.aggregation.IParameterDefn;
import org.eclipse.birt.data.engine.cache.BasicCachedList;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.data.engine.expression.ExprEvaluateUtil;
import org.eclipse.birt.data.engine.i18n.ResourceConstants;
import org.eclipse.birt.data.engine.impl.DataEngineSession;
import org.eclipse.birt.data.engine.odi.IAggrDefnManager;
import org.eclipse.birt.data.engine.odi.IAggrInfo;
import org.eclipse.birt.data.engine.odi.IResultIterator;
import org.eclipse.birt.data.engine.odi.IResultObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * 
 */

public class ProgressiveAggregationHelper 
{
	private IAggrDefnManager manager;
	
	//private ResultSetPopulator populator;
	
	/**
	 * Array to store all calculated aggregate values. aggrValue[i] is a list of
	 * values calculated for expression #i in the associated aggregate table.
	 * The aggregate values are stored in each list as the cursor advances for
	 * the associated ODI result set.
	 */
	private List[] currentRoundAggrValue;

	/**
	 * Array to store current argument values to all aggregates argrArgs[i] is
	 * the argument array to aggregate expression #i
	 */
	private Object[][] aggrArgs;

	// The count of aggregate expression
	private int currentAggrCount;
	


	private Set<String> aggrNames;
	private List<Accumulator> accumulators;
	private Scriptable currentScope;
	private ScriptContext sc;
	private DummyJSResultSetRow jsRow;

	
	/**
	 * For the given odi resultset, calcaulate the value of aggregate from
	 * aggregateTable
	 * 
	 * @param aggrTable
	 * @param odiResult
	 * @throws DataException 
	 */
	public ProgressiveAggregationHelper( IAggrDefnManager manager, String tempDir, Scriptable currentScope, ScriptContext sc ) throws DataException
	{
		this.manager = manager;
		this.currentRoundAggrValue = new List[0];
		this.accumulators = new ArrayList<Accumulator>();
		this.sc = sc;
		try
		{
			this.currentScope = ( (IDataScriptEngine) this.sc.getScriptEngine( IDataScriptEngine.ENGINE_NAME ) ).getJSContext( sc )
					.initStandardObjects( );
		}
		catch ( BirtException e )
		{
			throw DataException.wrap( e );
		}
		this.currentScope.setParentScope( currentScope );
		this.jsRow = new DummyJSResultSetRow();
		this.currentScope.put( "row", this.currentScope, this.jsRow );
		this.populateAggregations( tempDir );
	}

	private void populateAggregations( String tempDir ) throws DataException
	{
			this.aggrNames = new HashSet<String>();
			this.currentAggrCount = manager.getAggrCount( );
			if ( currentAggrCount > 0 )
			{
				currentRoundAggrValue = new List[currentAggrCount];
				aggrArgs = new Object[currentAggrCount][];
				for ( int i = 0; i < this.currentAggrCount; i++ )
				{
					currentRoundAggrValue[i] = new BasicCachedList( tempDir, DataEngineSession.getCurrentClassLoader( ) );
					IAggrInfo aggrInfo = this.manager.getAggrDefn( i );

					// Initialize argument array for this aggregate expression
					aggrArgs[i] = new Object[aggrInfo.getAggregation( )
							.getParameterDefn( ).length];
					this.aggrNames.add( this.manager.getAggrDefn( i ).getName( ) );
					this.accumulators.add(aggrInfo.getAggregation().newAccumulator());
				}
				
			}
	}

	public void onRow( int startingGroupLevel, int endingGroupLevel,
			IResultObject ro, int currentRowIndex )
			throws DataException
	{
		this.jsRow.currentRow = ro;
		for ( int i = 0; i < this.manager.getAggrCount( ); i++ )
		{
			this.onRow( i,
					startingGroupLevel,
					endingGroupLevel,
					ro,
					currentRowIndex );
		}
	}

	public void close () throws DataException
	{
		this.currentScope.delete( "row" );
	}
	/**
	 * Calculate the value by row
	 * 
	 * @param aggrIndex
	 * @param startingGroupLevel
	 * @param endingGroupLevel
	 * @param context
	 * @param scope
	 * @throws DataException
	 */
	private void onRow( int aggrIndex, int startingGroupLevel,
			int endingGroupLevel, IResultObject ro, int currentRowIndex )
			throws DataException
	{
		IAggrInfo aggrInfo = getAggrInfo( aggrIndex );
		Accumulator acc = this.accumulators.get(aggrIndex);
		boolean newGroup = false;
		IParameterDefn[] argDefs = aggrInfo.getAggregation( ).getParameterDefn( );
		if (startingGroupLevel <= aggrInfo.getGroupLevel( )) 
		{
			acc.start();
		}
		
		// Apply filtering on row
		boolean accepted = true;
		if ( aggrInfo.getFilter( ) != null )
		{
			try
			{
				Object filterResult = ExprEvaluateUtil.evaluateValue( aggrInfo.getFilter( ),
						currentRowIndex,
						ro,
						this.currentScope,
						this.sc);
				if ( filterResult == null )
					accepted = true;
				else

					accepted = DataTypeUtil.toBoolean( filterResult )
							.booleanValue( );
			}
			catch ( BirtException e )
			{
				currentRoundAggrValue[aggrIndex].add( e );
			}
		}

		if ( accepted )
		{
			// Calculate arguments to the aggregate aggregationtion
			
			final IBaseExpression[] arguments = aggrInfo.getArgument( );
			if ( !isFunctionCount( aggrInfo )
					&& arguments == null )
			{
				DataException e = new DataException( ResourceConstants.INVALID_AGGR_PARAMETER,
						aggrInfo.getName( ) );
				currentRoundAggrValue[aggrIndex].add( e );
			}
			
			try
			{
				int optionalAgrsNum = 0;
				for ( int i = 0; i < argDefs.length; i++ )
				{
					if ( argDefs[i].isOptional( ) )
					{
						optionalAgrsNum++;
					}
					if ( aggrInfo.getArgument( ) == null
							|| i >= arguments.length + optionalAgrsNum )
					{
						throw new DataException( ResourceConstants.AGGREGATION_ARGUMENT_ERROR,
								new Object[]{
										argDefs[i].getName( ),
										aggrInfo.getName( )
								} );
					}
					if ( isEmptyAggrArgument( aggrInfo ) )
					{
						aggrArgs[aggrIndex] = null;
					}
					else
					{
						evaluateArgsValue( aggrIndex, aggrInfo, i, argDefs[i], currentRowIndex, ro );
					}
				}

				if ( aggrInfo.getArgument( ) == null
						|| !isValidArgumentNumber( aggrInfo.getArgument( ).length, argDefs.length, optionalAgrsNum ) )
				{
					DataException e = new DataException( ResourceConstants.INVALID_AGGR_PARAMETER,
							aggrInfo.getName( ) );
					currentRoundAggrValue[aggrIndex].add( e );
				}
				acc.onRow( aggrArgs[aggrIndex] );
				newGroup = false;
			}
			catch ( DataException e )
			{
				currentRoundAggrValue[aggrIndex].add( e );
			}
		}
		
		//If this is a running aggregate, get value for current row
		boolean isRunning = ( aggrInfo.getAggregation( ).getType( ) == IAggrFunction .RUNNING_AGGR );
		
		if ( isRunning )
		{
			Object value = acc.getValue( );
			currentRoundAggrValue[aggrIndex].add( value );
		}

		if ( endingGroupLevel <= aggrInfo.getGroupLevel( ) )
		{
			// Current group ends for this aggregate; call finish() on
			// accumulator
			acc.finish( );

			// For non-running aggregates, this is the time to call getValue
			if ( !isRunning )
			{
				Object value = acc.getValue( );
				currentRoundAggrValue[aggrIndex].add( value );
			}
		}
	}
	
	/**
	 * Checks whether the arguments number is valid
	 * 
	 * @param aggrArgNumb
	 * @param argDefsLength
	 * @param optionalNum
	 * @return
	 */
	private boolean isValidArgumentNumber( int aggrArgNumb, int argDefsLength,
			int optionalNum )
	{
		return ( aggrArgNumb >= argDefsLength - optionalNum )
				&& ( aggrArgNumb <= argDefsLength );
	}

	/**
	 * Check whether the input aggregation script expression is empty
	 * 
	 * @param aggrInfo
	 * @return
	 */
	private boolean isEmptyAggrArgument( IAggrInfo aggrInfo )
	{
		return aggrInfo.getArgument( ).length == 0
				|| aggrInfo.getArgument( )[0] == null
				|| ( (IScriptExpression)aggrInfo.getArgument( )[0]).getText( ) == null 
				|| ( (IScriptExpression)aggrInfo.getArgument( )[0]).getText( ).trim().length( ) == 0;
	}

	/**
	 * Check whether the number of the aggregation arguments is valid
	 * 
	 * @param aggrInfo
	 * @param argDefs
	 * @return
	 */
	private boolean isInvalidArgumentNum( IAggrInfo aggrInfo,
			IParameterDefn[] argDefs )
	{
		if( aggrInfo.getArgument( ) == null )
		{
			
		}
		//if input argument is null or the 
		return aggrInfo.getArgument( ) == null
				|| ( ( aggrInfo.getArgument( ).length != argDefs.length ) && 
						!( ( aggrInfo.getArgument( ).length == ( argDefs.length - 1 ) ) 
								&& argDefs[0].isOptional( ) ) );
	}

	/**
	 * Get the evaluated result by the ScriptExpression
	 * 
	 * @param aggrIndex
	 * @param aggrInfo
	 * @param i
	 * @throws DataException
	 */
	private void evaluateArgsValue( int aggrIndex, IAggrInfo aggrInfo, int i, IParameterDefn paramDefn, int currentResultIndex, IResultObject currentRo )
			throws DataException
	{
		if( i >= aggrInfo.getArgument( ).length )
		{
			return;
		}
		IBaseExpression argExpr = aggrInfo.getArgument( )[i];
		if ( !paramDefn.isOptional( ) )
		{
			if ( !isFunctionCount( aggrInfo )
					&& isEmptyScriptExpression( argExpr ) )
			{
				throw new DataException( ResourceConstants.AGGREGATION_ARGUMENT_CANNOT_BE_BLANK,
						new Object[]{
								paramDefn.getName( ), aggrInfo.getName( )
						} );
			}
		}
		else if ( argExpr == null
				|| ( (IScriptExpression) argExpr ).getText( ) == null
				|| ( (IScriptExpression) argExpr ).getText( ).trim( ).length( ) == 0 )
		{
			aggrArgs[aggrIndex][i] = null;
			return;
		}
		try
		{
			aggrArgs[aggrIndex][i] = ExprEvaluateUtil.evaluateValue( argExpr,
					currentResultIndex,
					currentRo,
					this.currentScope,
					this.sc); 
		}
		catch ( BirtException e )
		{
			throw DataException.wrap( e );
		}
	}

	/**
	 * Checks whether the ScriptExpression has empty expression text
	 * 
	 * @param argExpr
	 * @return
	 */
	private boolean isEmptyScriptExpression( IBaseExpression argExpr )
	{
		IScriptExpression expr = (IScriptExpression) argExpr;
		return expr == null
				|| expr.getText( ) == null
				|| expr.getText( ).trim( ).length( ) == 0;
	}

	/**
	 * @param aggrInfo
	 * @return
	 */
	private boolean isFunctionCount( IAggrInfo aggrInfo )
	{
		return aggrInfo.getAggregation( ).getParameterDefn( ).length==0;
	}
	
	
	private IAggrInfo getAggrInfo( int i ) throws DataException
	{
		return this.manager.getAggrDefn( i );
	}

	public Object getLatestAggrValue( String name ) throws DataException
	{
		List currentValues = this.currentRoundAggrValue[this.manager.getAggrDefnIndex( name )];
		if( currentValues.isEmpty( ))
			return null;
		return currentValues.get( currentValues.size( )-1 );
	}
	
	/**
	 * Get the aggregate value
	 * @param aggrIndex
	 * @return
	 * @throws DataException
	 */
	public Object getAggrValue( String name, IResultIterator ri ) throws DataException
	{
		IAggrInfo aggrInfo = this.manager.getAggrDefn( name );
		if( this.currentRoundAggrValue[this.manager.getAggrDefnIndex( name )].isEmpty( ))
			return this.manager.getAggrDefn( name ).getAggregation( ).getDefaultValue( );
		/*if ( this.populator.getCache( ).getCount( ) == 0 )
		{
			return aggrInfo.getAggregation( ).getDefaultValue( );
		}*/

		try
		{
			int groupIndex;

			if ( aggrInfo.getAggregation( ).getType( ) == IAggrFunction .SUMMARY_AGGR )
			{
				// Aggregate on the whole list: there is only one group
				if ( aggrInfo.getGroupLevel( ) == 0 )
					groupIndex = 0;
				else
					groupIndex = ri.getCurrentGroupIndex( aggrInfo.getGroupLevel( ));
			}
			else
			{
				groupIndex = ri.getCurrentResultIndex( );
			}

			return this.currentRoundAggrValue[this.manager.getAggrDefnIndex( name )].get( groupIndex );

		}
		catch ( DataException e )
		{
			throw e;
		}
	}
	
	public List getAggrValues( String name ) throws DataException
	{
		return this.currentRoundAggrValue[this.manager.getAggrDefnIndex( name )];
	}
	
	public boolean hasAggr( String name ) throws DataException
	{
		return this.manager.getAggrDefnIndex( name ) != -1;
	}
	
	public Set<String> getAggrNames( ) throws DataException
	{
		return this.aggrNames;
	}

	public IAggrInfo getAggrInfo( String aggrName ) throws DataException
	{
		if( this.hasAggr( aggrName ))
			return this.manager.getAggrDefn( aggrName );
		return null;
	}
	
	private class DummyJSResultSetRow extends ScriptableObject{

		private IResultObject currentRow;

		public Object get( String name, Scriptable start )
		{
			try
			{
				if( this.currentRow!= null )
					return this.currentRow.getFieldValue( name );
			}
			catch ( DataException e )
			{
				return null;
			}
			return null;
		}
		public String getClassName( )
		{
			return "row";
		}
		
	}
	
}