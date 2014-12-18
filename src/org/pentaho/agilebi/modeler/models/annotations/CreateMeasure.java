/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package org.pentaho.agilebi.modeler.models.annotations;

import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Rowell Belen
 */
public class CreateMeasure extends AnnotationType {

  private static final long serialVersionUID = -2487305952482463126L;

  protected static final String AGGREGATE_TYPE_ID = "aggregateType";
  protected static final String AGGREGATE_TYPE_NAME = "Aggregation Type";

  protected static final String FORMAT_STRING_ID = "formatString";
  protected static final String FORMAT_STRING_NAME = "Format String";

  protected static final String EXPRESSION_ID = "expression";
  protected static final String EXPRESSION_NAME = "MDX Expression";

  @ModelProperty( id = AGGREGATE_TYPE_ID, name = AGGREGATE_TYPE_NAME )
  private AggregationType aggregateType;

  @ModelProperty( id = FORMAT_STRING_ID, name = FORMAT_STRING_NAME )
  private String formatString;

  @ModelProperty( id = EXPRESSION_ID, name = EXPRESSION_NAME )
  private String expression;

  public AggregationType getAggregateType() {
    return aggregateType;
  }

  public void setAggregateType( AggregationType aggregateType ) {
    this.aggregateType = aggregateType;
  }

  public String getFormatString() {
    return formatString;
  }

  public void setFormatString( String formatString ) {
    this.formatString = formatString;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression( String expression ) {
    this.expression = expression;
  }

  @Override
  public void apply( final ModelerWorkspace workspace, final String column ) {
    List<LogicalTable> logicalTables = workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables();
    for ( LogicalTable logicalTable : logicalTables ) {
      List<LogicalColumn> logicalColumns = logicalTable.getLogicalColumns();
      for ( LogicalColumn logicalColumn : logicalColumns ) {
        if ( column
            .equals( logicalColumn.getPhysicalColumn().getProperty( SqlPhysicalColumn.TARGET_COLUMN ) ) ) {
          MeasureMetaData measureMetaData =
              new MeasureMetaData( column, getFormatString(), getName(), workspace.getWorkspaceHelper().getLocale() );
          measureMetaData.setLogicalColumn( logicalColumn );
          measureMetaData.setDefaultAggregation( getAggregateType() );
          measureMetaData.setName( getName() );
          workspace.addMeasure( measureMetaData );
          return;
        }
      }

    }
  }

  @Override
  public void populate( final Map<String, Serializable> propertiesMap ) {

    super.populate( propertiesMap ); // let base class handle primitives, etc.

    // correctly convert aggregate type
    if ( propertiesMap.containsKey( AGGREGATE_TYPE_ID ) ) {
      Serializable value = propertiesMap.get( AGGREGATE_TYPE_ID );
      if ( value != null ) {
        setAggregateType( AggregationType.valueOf( value.toString() ) );
      }
    }
  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.CREATE_MEASURE;
  }
}
