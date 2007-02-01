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

package org.eclipse.birt.report.model.util;

import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.model.api.elements.structures.Action;
import org.eclipse.birt.report.model.api.elements.structures.FilterCondition;
import org.eclipse.birt.report.model.api.elements.structures.HideRule;
import org.eclipse.birt.report.model.api.elements.structures.HighlightRule;
import org.eclipse.birt.report.model.api.elements.structures.MapRule;
import org.eclipse.birt.report.model.api.elements.structures.ParamBinding;
import org.eclipse.birt.report.model.api.elements.structures.SearchKey;
import org.eclipse.birt.report.model.api.elements.structures.SortKey;
import org.eclipse.birt.report.model.api.elements.structures.TOC;
import org.eclipse.birt.report.model.api.extension.ICompatibleReportItem;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.core.Module;
import org.eclipse.birt.report.model.elements.Cell;
import org.eclipse.birt.report.model.elements.DataItem;
import org.eclipse.birt.report.model.elements.ExtendedItem;
import org.eclipse.birt.report.model.elements.GridItem;
import org.eclipse.birt.report.model.elements.GroupElement;
import org.eclipse.birt.report.model.elements.ImageItem;
import org.eclipse.birt.report.model.elements.Label;
import org.eclipse.birt.report.model.elements.ListGroup;
import org.eclipse.birt.report.model.elements.ListItem;
import org.eclipse.birt.report.model.elements.ListingElement;
import org.eclipse.birt.report.model.elements.ReportItem;
import org.eclipse.birt.report.model.elements.ScalarParameter;
import org.eclipse.birt.report.model.elements.TableColumn;
import org.eclipse.birt.report.model.elements.TableGroup;
import org.eclipse.birt.report.model.elements.TableItem;
import org.eclipse.birt.report.model.elements.TableRow;
import org.eclipse.birt.report.model.elements.TemplateReportItem;
import org.eclipse.birt.report.model.elements.TextDataItem;
import org.eclipse.birt.report.model.elements.TextItem;
import org.eclipse.birt.report.model.elements.interfaces.ICellModel;
import org.eclipse.birt.report.model.elements.interfaces.IDataItemModel;
import org.eclipse.birt.report.model.elements.interfaces.IExtendedItemModel;
import org.eclipse.birt.report.model.elements.interfaces.IGroupElementModel;
import org.eclipse.birt.report.model.elements.interfaces.IImageItemModel;
import org.eclipse.birt.report.model.elements.interfaces.ILabelModel;
import org.eclipse.birt.report.model.elements.interfaces.IListingElementModel;
import org.eclipse.birt.report.model.elements.interfaces.IReportItemModel;
import org.eclipse.birt.report.model.elements.interfaces.IScalarParameterModel;
import org.eclipse.birt.report.model.elements.interfaces.IStyleModel;
import org.eclipse.birt.report.model.elements.interfaces.ITableColumnModel;
import org.eclipse.birt.report.model.elements.interfaces.ITableRowModel;
import org.eclipse.birt.report.model.elements.interfaces.ITextDataItemModel;
import org.eclipse.birt.report.model.elements.interfaces.ITextItemModel;

/**
 * The utility to provide the way to visit all expressions for the given
 * element. If the element is data container such as table/list, also visit
 * elements inside.
 */

public abstract class BoundColumnsMgr
{

	/**
	 * Creates bound columns for report item.
	 * 
	 * @param element
	 *            the report item
	 * @param module
	 *            the root of the report item
	 */

	protected void dealReportItem( ReportItem element, Module module )
	{
		dealStyle( element, module );

		TOC toc = (TOC) element.getLocalProperty( module,
				IReportItemModel.TOC_PROP );
		if ( toc != null )
			handleBoundsForValue( element, module, toc.getExpression( ) );

		String value = (String) element.getLocalProperty( module,
				IReportItemModel.BOOKMARK_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		value = (String) element.getLocalProperty( module,
				IReportItemModel.ON_CREATE_METHOD );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		List paramBindings = (List) element.getLocalProperty( module,
				IReportItemModel.PARAM_BINDINGS_PROP );
		if ( paramBindings != null && paramBindings.size( ) > 0 )
		{
			for ( int i = 0; i < paramBindings.size( ); i++ )
			{
				ParamBinding paramValue = (ParamBinding) paramBindings.get( i );
				handleBoundsForParamBinding( element, module, paramValue
						.getExpression( ) );
			}
		}

		List hideRules = (List) element.getLocalProperty( module,
				IReportItemModel.VISIBILITY_PROP );
		if ( hideRules != null && hideRules.size( ) > 0 )
		{
			for ( int i = 0; i < hideRules.size( ); i++ )
			{
				HideRule paramValue = (HideRule) hideRules.get( i );
				handleBoundsForValue( element, module, paramValue
						.getExpression( ) );
			}
		}
	}

	/**
	 * Creates bound columns for the given element.
	 * 
	 * @param element
	 *            the report item
	 * @param module
	 *            the root of the report item
	 */

	protected void dealDataContainerReportItem( ListingElement element,
			Module module )
	{
		if ( element instanceof TableItem )
			dealTable( (TableItem) element, module );
		else if ( element instanceof ListItem )
			dealList( (ListItem) element, module );

	}

	/**
	 * Creates bound columns for the given element.
	 * 
	 * @param element
	 *            the report item
	 * @param module
	 *            the root of the report item
	 */

	protected void dealNonDataContainerReportItem( ReportItem element,
			Module module )
	{
		if ( element instanceof TextDataItem )
		{
			dealTextData( (TextDataItem) element, module );
		}
		if ( element instanceof Label )
		{
			dealLabel( (Label) element, module );
		}
		if ( element instanceof DataItem )
		{
			dealData( (DataItem) element, module );
		}
		if ( element instanceof ImageItem )
		{
			dealImage( (ImageItem) element, module );
		}
		if ( element instanceof GridItem )
		{
			dealGrid( (GridItem) element, module );
		}
		if ( element instanceof ExtendedItem )
		{
			dealExtendedItem( (ExtendedItem) element, module );
		}
	}

	/**
	 * Creates bound columns for extended item.
	 * 
	 * @param element
	 *            the extended item
	 * @param module
	 *            the root of the report item
	 */

	protected void dealExtendedItem( ExtendedItem element, Module module )
	{
		dealReportItem( element, module );
		List values = (List) element.getLocalProperty( module,
				IExtendedItemModel.FILTER_PROP );
		if ( !( values == null || values.size( ) < 1 ) )
		{
			for ( int i = 0; i < values.size( ); i++ )
			{
				FilterCondition struct = (FilterCondition) values.get( i );
				handleBoundsForValue( element, module, struct.getExpr( ) );
				handleBoundsForValue( element, module, struct.getValue1( ) );
				handleBoundsForValue( element, module, struct.getValue2( ) );
			}
		}

		Object reportItem = element.getExtendedElement( );

		if ( reportItem != null && reportItem instanceof ICompatibleReportItem )
		{
			List jsExprs = ( (ICompatibleReportItem) reportItem )
					.getRowExpressions( );
			for ( int i = 0; i < jsExprs.size( ); i++ )
				handleBoundsForValue( element, module, (String) jsExprs.get( i ) );

			Map updatedExprs = DataBoundColumnUtil.handleJavaExpression(
					jsExprs, element, module, null );
			( (ICompatibleReportItem) reportItem )
					.updateRowExpressions( updatedExprs );
		}
	}

	/**
	 * Creates bound columns for list/table group.
	 * 
	 * @param element
	 *            the list/table group
	 * @param module
	 *            the root of the report item
	 */

	private void dealListingGroup( GroupElement element, Module module )
	{
		String value = (String) element.getLocalProperty( module,
				IGroupElementModel.KEY_EXPR_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		value = (String) element.getLocalProperty( module,
				IGroupElementModel.TOC_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		List values = (List) element.getLocalProperty( module,
				IGroupElementModel.FILTER_PROP );
		if ( !( values == null || values.size( ) < 1 ) )
		{
			for ( int i = 0; i < values.size( ); i++ )
			{
				FilterCondition struct = (FilterCondition) values.get( i );
				handleBoundsForValue( element, module, struct.getExpr( ) );
				handleBoundsForValue( element, module, struct.getValue1( ) );
				handleBoundsForValue( element, module, struct.getValue2( ) );
			}
		}

		values = (List) element.getLocalProperty( module,
				IGroupElementModel.SORT_PROP );
		if ( !( values == null || values.size( ) < 1 ) )
		{
			for ( int i = 0; i < values.size( ); i++ )
			{
				SortKey struct = (SortKey) values.get( i );
				handleBoundsForValue( element, module, struct.getKey( ) );
			}
		}
	}

	/**
	 * Creates bound columns for list/table.
	 * 
	 * @param element
	 *            the list/table
	 * @param module
	 *            the root of the report item
	 */

	private void dealListing( ListingElement element, Module module )
	{
		dealReportItem( element, module );
		List values = (List) element.getLocalProperty( module,
				IListingElementModel.FILTER_PROP );
		if ( !( values == null || values.size( ) < 1 ) )
		{
			for ( int i = 0; i < values.size( ); i++ )
			{
				FilterCondition struct = (FilterCondition) values.get( i );
				handleBoundsForValue( element, module, struct.getExpr( ) );
				handleBoundsForValue( element, module, struct.getValue1( ) );
				handleBoundsForValue( element, module, struct.getValue2( ) );
			}
		}

		values = (List) element.getLocalProperty( module,
				IListingElementModel.SORT_PROP );
		if ( !( values == null || values.size( ) < 1 ) )
		{
			for ( int i = 0; i < values.size( ); i++ )
			{
				SortKey struct = (SortKey) values.get( i );
				handleBoundsForValue( element, module, struct.getKey( ) );
			}
		}
	}

	/**
	 * Creates bound columns for grid.
	 * 
	 * @param element
	 *            the grid
	 * @param module
	 *            the root of grid
	 */

	protected void dealGrid( GridItem element, Module module )
	{
		dealReportItem( element, module );
		LevelContentIterator contents = new LevelContentIterator( module,
				element, 3 );
		while ( contents.hasNext( ) )
		{
			DesignElement child = (DesignElement) contents.next( );
			if ( child instanceof TableColumn )
			{
				dealColumn( (TableColumn) child, module );
			}
			if ( child instanceof TableRow )
			{
				dealRow( (TableRow) child, module );
			}
			if ( child instanceof ReportItem )
			{
				if ( child instanceof ListingElement )
					dealListing( (ListingElement) child, module );
				else
					dealNonDataContainerReportItem( (ReportItem) child, module );
			}
			if ( child instanceof Cell )
			{
				dealCell( (Cell) child, module );
			}
		}
	}

	/**
	 * Creates bound columns for the list.
	 * 
	 * @param element
	 *            the list
	 * @param module
	 *            the root of list
	 */

	protected void dealList( ListItem element, Module module )
	{
		dealListing( element, module );
		LevelContentIterator contents = new LevelContentIterator( module,
				element, 1 );
		while ( contents.hasNext( ) )
		{
			DesignElement child = (DesignElement) contents.next( );
			if ( child instanceof ListGroup )
			{
				dealListingGroup( (ListGroup) child, module );
				LevelContentIterator grandChildren = new LevelContentIterator(
						module, child, 1 );
				while ( grandChildren.hasNext( ) )
				{
					DesignElement grandChild = (DesignElement) grandChildren
							.next( );
					if ( grandChild instanceof ListingElement )
						dealListing( (ListingElement) grandChild, module );
					else
						dealNonDataContainerReportItem(
								(ReportItem) grandChild, module );
				}
			}
			if ( child instanceof ReportItem )
			{
				if ( child instanceof ListingElement )
					dealListing( (ListingElement) child, module );
				else
					dealNonDataContainerReportItem( (ReportItem) child, module );
			}
		}
	}

	/**
	 * Creates bound columns for the table.
	 * 
	 * @param element
	 *            the table
	 * @param module
	 *            the root of table
	 */

	protected void dealTable( TableItem element, Module module )
	{
		dealListing( element, module );
		for ( int i = 0; i < element.getDefn( ).getSlotCount( ); i++ )
		{
			int level = 3;

			if ( i == IListingElementModel.GROUP_SLOT )
				level = 1;

			LevelContentIterator contents = new LevelContentIterator( module,
					element, level );
			while ( contents.hasNext( ) )
			{
				DesignElement child = (DesignElement) contents.next( );
				if ( child instanceof TableGroup )
				{
					dealListingGroup( (TableGroup) child, module );
					LevelContentIterator grandChildren = new LevelContentIterator(
							module, child, 3 );
					while ( grandChildren.hasNext( ) )
					{
						DesignElement grandChild = (DesignElement) grandChildren
								.next( );
						if ( grandChild instanceof TableRow )
						{
							dealRow( (TableRow) grandChild, module );
						}
						if ( grandChild instanceof Cell )
						{
							dealCell( (Cell) grandChild, module );
						}
						if ( grandChild instanceof ReportItem )
						{
							if ( grandChild instanceof ListingElement )
								dealListing( (ListingElement) grandChild,
										module );
							else
								dealNonDataContainerReportItem(
										(ReportItem) grandChild, module );
						}
					}
				}
				if ( child instanceof TableColumn )
				{
					dealColumn( (TableColumn) child, module );
				}
				if ( child instanceof TableRow )
				{
					dealRow( (TableRow) child, module );
				}
				if ( child instanceof ReportItem )
				{
					if ( child instanceof ListingElement )
						dealListing( (ListingElement) child, module );
					else
						dealNonDataContainerReportItem( (ReportItem) child,
								module );
				}
				if ( child instanceof Cell )
				{
					dealCell( (Cell) child, module );
				}
			}
		}
	}

	/**
	 * Creates bound columns for the row.
	 * 
	 * @param element
	 *            the row
	 * @param module
	 *            the root of row
	 */

	private void dealRow( TableRow element, Module module )
	{
		String value = (String) element.getLocalProperty( module,
				ITableRowModel.BOOKMARK_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		value = (String) element.getLocalProperty( module,
				ITableRowModel.ON_CREATE_METHOD );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		List hideRules = (List) element.getLocalProperty( module,
				ITableRowModel.VISIBILITY_PROP );
		if ( hideRules == null || hideRules.size( ) < 1 )
			return;

		for ( int i = 0; i < hideRules.size( ); i++ )
		{
			HideRule paramValue = (HideRule) hideRules.get( i );
			handleBoundsForValue( element, module, paramValue.getExpression( ) );
		}
	}

	/**
	 * Creates bound columns for the row.
	 * 
	 * @param element
	 *            the row
	 * @param module
	 *            the root of row
	 */

	private void dealCell( Cell element, Module module )
	{
		String value = (String) element.getLocalProperty( module,
				ICellModel.ON_CREATE_METHOD );
		if ( value != null )
			handleBoundsForValue( element, module, value );
	}

	/**
	 * Creates bound columns for the image.
	 * 
	 * @param element
	 *            the image
	 * @param module
	 *            the root of image
	 */

	protected void dealImage( ImageItem element, Module module )
	{

		dealReportItem( element, module );

		String value = (String) element.getLocalProperty( module,
				IImageItemModel.URI_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );
		value = (String) element.getLocalProperty( module,
				IImageItemModel.VALUE_EXPR_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );
		value = (String) element.getLocalProperty( module,
				IImageItemModel.TYPE_EXPR_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		Action action = (Action) element.getLocalProperty( module,
				IImageItemModel.ACTION_PROP );
		dealAction( element, module, action );
	}

	/**
	 * Creates bound columns for the label.
	 * 
	 * @param element
	 *            the label
	 * @param module
	 *            the root of label
	 */

	protected void dealLabel( Label element, Module module )
	{
		dealReportItem( element, module );
		Action action = (Action) element.getLocalProperty( module,
				ILabelModel.ACTION_PROP );
		dealAction( element, module, action );
	}

	/**
	 * Creates bound columns for the data item.
	 * 
	 * @param element
	 *            the data item
	 * @param module
	 *            the root of the data item
	 */

	protected void dealData( DataItem element, Module module )
	{
		dealReportItem( element, module );
		Action action = (Action) element.getLocalProperty( module,
				IDataItemModel.ACTION_PROP );
		dealAction( element, module, action );
	}

	/**
	 * Creates bound columns for the action.
	 * 
	 * @param element
	 *            the data item
	 * @param module
	 *            the root of the data item
	 * @param action
	 *            the action object
	 */

	private void dealAction( ReportItem element, Module module, Action action )
	{
		if ( action == null )
			return;

		String value = (String) action.getProperty( module, Action.URI_MEMBER );
		if ( value != null )
			handleBoundsForValue( element, module, value );
		value = (String) action.getProperty( module,
				Action.TARGET_BOOKMARK_MEMBER );
		if ( value != null )
			handleBoundsForValue( element, module, value );

		List paramBindings = (List) action.getLocalProperty( module,
				Action.PARAM_BINDINGS_MEMBER );
		if ( paramBindings != null && paramBindings.size( ) > 0 )
		{

			for ( int i = 0; i < paramBindings.size( ); i++ )
			{
				ParamBinding paramValue = (ParamBinding) paramBindings.get( i );
				handleBoundsForParamBinding( element, module, paramValue
						.getExpression( ) );
			}
		}

		List searchKeys = (List) action.getLocalProperty( module,
				Action.SEARCH_MEMBER );
		if ( searchKeys == null || searchKeys.size( ) < 1 )
			return;

		for ( int i = 0; i < searchKeys.size( ); i++ )
		{
			SearchKey searchKey = (SearchKey) searchKeys.get( i );
			handleBoundsForValue( element, module, searchKey.getExpression( ) );
		}
	}

	/**
	 * Creates bound columns for the private style.
	 * 
	 * @param element
	 *            the element
	 * @param module
	 *            the root of the element
	 */

	private void dealStyle( ReportItem element, Module module )
	{
		List values = (List) element.getLocalProperty( module,
				IStyleModel.MAP_RULES_PROP );
		if ( values != null )
			for ( int i = 0; i < values.size( ); i++ )
			{
				MapRule struct = (MapRule) values.get( i );
				handleBoundsForValue( element, module, struct
						.getTestExpression( ) );
				handleBoundsForValue( element, module, struct.getValue1( ) );
				handleBoundsForValue( element, module, struct.getValue2( ) );
			}

		values = (List) element.getLocalProperty( module,
				IStyleModel.HIGHLIGHT_RULES_PROP );
		if ( values != null )
			for ( int i = 0; i < values.size( ); i++ )
			{
				HighlightRule struct = (HighlightRule) values.get( i );
				handleBoundsForValue( element, module, struct
						.getTestExpression( ) );
				handleBoundsForValue( element, module, struct.getValue1( ) );
				handleBoundsForValue( element, module, struct.getValue2( ) );
			}
	}

	/**
	 * Creates bound columns for the text data.
	 * 
	 * @param element
	 *            the text data
	 * @param module
	 *            the root of the text data
	 */

	protected void dealTextData( TextDataItem element, Module module )
	{
		dealReportItem( element, module );
		String value = (String) element.getLocalProperty( module,
				ITextDataItemModel.VALUE_EXPR_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );
	}

	/**
	 * Creates bound columns for the text.
	 * 
	 * @param element
	 *            the text
	 * @param module
	 *            the root of the text
	 */

	protected void dealText( TextItem element, Module module )
	{
		dealReportItem( element, module );

		String content = (String) element.getLocalProperty( module,
				ITextItemModel.CONTENT_PROP );
		if ( StringUtil.isBlank( content ) )
			return;

		List jsExprs = DataBoundColumnUtil.getExpressions( content, element,
				module );
		DataBoundColumnUtil.handleJavaExpression( jsExprs, element, module,
				null );
	}

	/**
	 * Creates bound columns for the scalar parameter.
	 * 
	 * @param element
	 *            the scalar parameter
	 * @param module
	 *            the root of the scalar parameter
	 */

	protected void dealScalarParameter( ScalarParameter element, Module module )
	{
		String value = (String) element.getLocalProperty( module,
				IScalarParameterModel.VALUE_EXPR_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );
		value = (String) element.getLocalProperty( module,
				IScalarParameterModel.LABEL_EXPR_PROP );
		if ( value != null )
			handleBoundsForValue( element, module, value );
	}

	/**
	 * Creates bound columns for the scalar parameter.
	 * 
	 * @param element
	 *            the scalar parameter
	 * @param module
	 *            the root of the scalar parameter
	 */

	protected void dealTemplateReportItem( TemplateReportItem element,
			Module module )
	{
		List hideRules = (List) element.getLocalProperty( module,
				ITableRowModel.VISIBILITY_PROP );
		if ( hideRules == null || hideRules.size( ) < 1 )
			return;

		for ( int i = 0; i < hideRules.size( ); i++ )
		{
			HideRule paramValue = (HideRule) hideRules.get( i );
			handleBoundsForValue( element, module, paramValue.getExpression( ) );
		}
	}

	/**
	 * Creates bound columns for the column.
	 * 
	 * @param element
	 *            the column
	 * @param module
	 *            the root of the column
	 */

	private void dealColumn( TableColumn element, Module module )
	{
		List hideRules = (List) element.getLocalProperty( module,
				ITableColumnModel.VISIBILITY_PROP );
		if ( hideRules == null || hideRules.size( ) < 1 )
			return;

		for ( int i = 0; i < hideRules.size( ); i++ )
		{
			HideRule paramValue = (HideRule) hideRules.get( i );
			handleBoundsForValue( element, module, paramValue.getExpression( ) );
		}
	}

	/**
	 * Creates bound columns for the given element.
	 * 
	 * @param element
	 *            the element
	 * @param module
	 *            the root of the element
	 * @param propValue
	 *            the value from which to create bound columns
	 */

	abstract protected void handleBoundsForValue( DesignElement element,
			Module module, String propValue );

	/**
	 * Creates bound columns for the given value of the given element.
	 * 
	 * @param element
	 *            the element
	 * @param module
	 *            the root of the element
	 * @param propValue
	 *            the value from which to create bound columns
	 */

	abstract protected void handleBoundsForParamBinding( DesignElement element,
			Module module, String propValue );
}
