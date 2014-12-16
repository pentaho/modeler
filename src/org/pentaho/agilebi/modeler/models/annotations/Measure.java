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

import java.util.List;

/**
 * @author Rowell Belen
 */
public class Measure extends AnnotationType {

  private static final long serialVersionUID = -2487305952482463126L;

  @ModelProperty( id = "aggregateType", name = "Aggregate Type" )
  private AggregationType aggregateType;

  @ModelProperty( id = "formatString", name = "Format String" )
  private String formatString;

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

  @Override
  public void apply( final ModelerWorkspace workspace, final String column ) {
    List<LogicalTable> logicalTables = workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables();
    for ( LogicalTable logicalTable : logicalTables ) {
      List<LogicalColumn> logicalColumns = logicalTable.getLogicalColumns();
      for ( LogicalColumn logicalColumn : logicalColumns ) {
        if ( column
            .equals( logicalColumn.getPhysicalColumn().getProperty( SqlPhysicalColumn.TARGET_COLUMN ).getValue() ) ) {
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
  public AnnotationSubType getType() {
    return AnnotationSubType.MEASURE;
  }
}
