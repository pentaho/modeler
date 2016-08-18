/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
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

import mondrian.olap.MondrianDef;
import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Document;

import java.util.logging.Logger;

/**
 * @author Brandon Groves
 */
@MetaStoreElementType( name = "UpdateMeasure", description = "UpdateMeasure Annotation" )
public class UpdateMeasure extends AnnotationType {

  private static final long serialVersionUID = -8365062607663928537L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );

  private static final String NAME_ID = "name";
  private static final String NAME_NAME = "Name";
  private static final int NAME_ORDER = 0;

  private static final String CUBE_ID = "cube";
  private static final String CUBE_NAME = "Cube";
  private static final int CUBE_ORDER = 1;

  private static final String AGGREGATION_TYPE_ID = "aggregationType";
  private static final String AGGREGATION_TYPE_NAME = "Aggregation Type";
  private static final int AGGREGATION_TYPE_ORDER = 2;

  private static final String FORMAT_ID = "format";
  private static final String FORMAT_NAME = "Format";
  private static final int FORMAT_ORDER = 3;

  private static final String MEASURE_ID = "measure";
  private static final String MEASURE_NAME = "Measure";
  private static final int MEASURE_ORDER = 4;

  private static final String CAPTION_ID = "caption";
  private static final String CAPTION_NAME = "Caption";
  private static final int CAPTION_ORDER = 5;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = MEASURE_ID, name = MEASURE_NAME, order = MEASURE_ORDER )
  private String measure;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER )
  private String cube;

  @MetaStoreAttribute
  @ModelProperty( id = AGGREGATION_TYPE_ID, name = AGGREGATION_TYPE_NAME, order = AGGREGATION_TYPE_ORDER )
  private AggregationType aggregationType;

  @MetaStoreAttribute
  @ModelProperty( id = FORMAT_ID, name = FORMAT_NAME, order = FORMAT_ORDER )
  private String format;

  @MetaStoreAttribute
  @ModelProperty( id = CAPTION_ID, name = CAPTION_NAME, order = CAPTION_ORDER )
  private String caption;

  /**
   * Retrieves the measure based on the formula.
   *
   * @param workspace Workspace to search for formula
   * @param formula Formula to search for (ex [MEASURES].[SALES])
   * @return Measure otherwise null
   */
  private MeasureMetaData locateMeasureFromFormula( final ModelerWorkspace workspace, final String formula ) {
    if ( formula == null || workspace == null ) {
      return null;
    }

    for ( MeasureMetaData measureMetaData : workspace.getModel().getMeasures() ) {
      StringBuilder formulaBuilder = new StringBuilder();
      formulaBuilder.append( "[" + MEASURES_DIMENSION + "].[" );
      formulaBuilder.append( measureMetaData.getName() );
      formulaBuilder.append( "]" );

      if ( formula.equals( formulaBuilder.toString() ) ) {
        return measureMetaData;
      }
    }

    return null;
  }

  @Override
  public boolean apply(
      final ModelerWorkspace workspace, final IMetaStore metaStore ) throws ModelerException {
    if ( workspace == null ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "MondrianSchemaHelper.updateMeasure.UNABLE_TO_FIND_MEASURE" )
      );
    }

    MeasureMetaData existingMeasure = locateMeasureFromFormula( workspace, measure );

    if ( existingMeasure == null ) {
      return false;
    }

    // Check to see the name is already be used for another measure
    StringBuilder newMeasureFormula = new StringBuilder();
    newMeasureFormula.append( "[" + MEASURES_DIMENSION + "].[" );
    newMeasureFormula.append( name );
    newMeasureFormula.append( "]" );
    MeasureMetaData prexistingMeasure = locateMeasureFromFormula( workspace, newMeasureFormula.toString() );
    // ignore if the name doesn't change
    if ( !newMeasureFormula.toString().equals( measure ) && prexistingMeasure != null ) {
      return false;
    }

    if ( aggregationType != null ) {
      existingMeasure.setDefaultAggregation( aggregationType );
    }

    // No null or empty check so that default format can be set. See getFormat().
    existingMeasure.setFormat( format );

    if ( !StringUtils.isBlank( name ) ) {
      existingMeasure.setName( name );
    }

    workspace.getWorkspaceHelper().populateDomain( workspace );

    return true;
  }

  @Override
  public boolean apply( final Document schema ) throws ModelerException {
    if ( schema == null ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "MondrianSchemaHelper.updateMeasure.UNABLE_TO_FIND_MEASURE" )
      );
    }

    String mondrianAggregationType = null;
    if ( aggregationType != null ) {
      mondrianAggregationType = MondrianModelExporter.convertToMondrian( aggregationType );
    }
    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schema );
    if ( mondrianSchemaHandler.isCalculatedMeasure( cube, measure ) ) {
      return mondrianSchemaHandler.updateCalculatedMeasure( cube, measure, caption, format );
    } else {
      MondrianDef.Measure updatedMeasure = new MondrianDef.Measure();
      updatedMeasure.name = name;
      updatedMeasure.aggregator = mondrianAggregationType;
      updatedMeasure.formatString = format;
      updatedMeasure.caption = caption;
      return mondrianSchemaHandler.updateMeasure( cube, measure, updatedMeasure );
    }
  }

  @Override
  public void validate() throws ModelerException {
    if ( StringUtils.isBlank( measure ) ) {
      throw new ModelerException( BaseMessages.getString( MSG_CLASS,
        "ModelAnnotation.UpdateMeasure.validation.MEASURE_NAME_REQUIRED" ) );
    }
  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.UPDATE_MEASURE;
  }

  private String summaryMsgKey() {
    return "Modeler.UpdateMeasure.Summary";
  }

  @Override
  public String getSummary() {
    return BaseMessages
      .getString( MSG_CLASS, summaryMsgKey(), getMeasure(), getName() );
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getMeasure() {
    return measure;
  }

  public void setMeasure( String measure ) {
    this.measure = measure;
  }

  public String getCube() {
    return cube;
  }

  public void setCube( String cube ) {
    this.cube = cube;
  }

  @Override
  public String getField() {
    return null;
  }

  public AggregationType getAggregationType() {
    return aggregationType;
  }

  public void setAggregationType( AggregationType aggregationType ) {
    this.aggregationType = aggregationType;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption( final String caption ) {
    this.caption = caption;
  }
}

