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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Rowell Belen
 */
public class CreateMeasure extends AnnotationType {

  private static final long serialVersionUID = -2487305952482463126L;

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Measure Name";

  public static final String AGGREGATE_TYPE_ID = "aggregateType";
  public static final String AGGREGATE_TYPE_NAME = "Aggregation Type";

  public static final String FORMAT_STRING_ID = "formatString";
  public static final String FORMAT_STRING_NAME = "Format String";

  public static final String EXPRESSION_ID = "expression";
  public static final String EXPRESSION_NAME = "MDX Expression";

  @ModelProperty( id = NAME_ID, name = NAME_NAME )
  private String name;

  @ModelProperty( id = AGGREGATE_TYPE_ID, name = AGGREGATE_TYPE_NAME )
  private AggregationType aggregateType;

  @ModelProperty( id = FORMAT_STRING_ID, name = FORMAT_STRING_NAME )
  private String formatString;

  @ModelProperty( id = EXPRESSION_ID, name = EXPRESSION_NAME )
  private String expression;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

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
  public boolean apply( final ModelerWorkspace workspace, final String column ) throws ModelerException {
    List<LogicalTable> logicalTables = workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables();
    for ( LogicalTable logicalTable : logicalTables ) {
      List<LogicalColumn> logicalColumns = logicalTable.getLogicalColumns();
      for ( LogicalColumn logicalColumn : logicalColumns ) {
        if ( column
            .equalsIgnoreCase(
              logicalColumn.getName( workspace.getWorkspaceHelper().getLocale() ) ) ) {
          String targetColumn =
              (String) logicalColumn.getPhysicalColumn().getProperty( SqlPhysicalColumn.TARGET_COLUMN );
          MeasureMetaData measureMetaData =
              new MeasureMetaData( targetColumn, getFormatString(), getName(), workspace.getWorkspaceHelper().getLocale() );
          measureMetaData.setLogicalColumn( (LogicalColumn) logicalColumn.clone() );
          measureMetaData.setName( getName() );
          measureMetaData.setDefaultAggregation( getAggregateType() );
          workspace.getModel().getMeasures().add( measureMetaData );
          workspace.getWorkspaceHelper().populateDomain( workspace );
          return true;
        }
      }

    }
    throw new ModelerException( "Unable to apply Create Measure annotation: Column not found" );
  }

  @Override
  public boolean apply( Document doc, String field ) throws ModelerException {
    // Surgically add the measure into the cube...
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      StringBuffer xPathExpr = new StringBuffer();
      xPathExpr.append( "/Schema/Cube" ); // TODO: Handle multiple cubes...
      XPathExpression xPathExpression = xPath.compile( xPathExpr.toString() );
      Node cube = (Node) xPathExpression.evaluate( doc, XPathConstants.NODE );
      Element measureElement = null;
      measureElement = doc.createElement( "Measure" );
      cube.appendChild( measureElement ); // TODO: Measures need to come after calculated measures
      measureElement.setAttribute( "name", getName() );
      measureElement.setAttribute( "column", field );
      measureElement.setAttribute( "aggregator", MondrianModelExporter.convertToMondrian( getAggregateType() ) );
    } catch ( XPathExpressionException e ) {
      throw new ModelerException( e );
    }
    return true;
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

  @Override public String getSummary() {
    return BaseMessages.getString( MSG_CLASS, "Modeler.CreateMeasure.Summary", getName(), getAggregateType().name() );
  }
}
