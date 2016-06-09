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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import mondrian.olap.MondrianDef;
import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.automodel.PhysicalTableImporter;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

//import static org.pentaho.metadata.automodel..*;


/**
 * @author Rowell Belen
 */
public class CreateMeasure extends AnnotationType {

  private static final long serialVersionUID = -2487305952482463126L;

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Measure Name";
  public static final int NAME_ORDER = 0;

  public static final String AGGREGATE_TYPE_ID = "aggregateType";
  public static final String AGGREGATE_TYPE_NAME = "Aggregation Type";
  public static final int AGGREGATE_TYPE_ORDER = 1;

  public static final String FORMAT_STRING_ID = "formatString";
  public static final String FORMAT_STRING_NAME = "Format";
  public static final int FORMAT_STRING_ORDER = 2;

  public static final String DESCRIPTION_ID = "description";
  public static final String DESCRIPTION_NAME = "Description";
  public static final int DESCRIPTION_ORDER = 3;

  public static final String BUSINESS_GROUP_ID = "businessGroup";
  public static final String BUSINESS_GROUP_NAME = "Business Group";
  public static final int BUSINESS_GROUP_ORDER = 4;

  public static final String FIELD_ID = "field";
  public static final String FIELD_NAME = "Field Name";
  public static final int FIELD_ORDER = 5;

  public static final String LEVEL_ID = "level";
  public static final String LEVEL_NAME = "Level Name";
  public static final int LEVEL_ORDER = 6;

  public static final String MEASURE_ID = "measure";
  public static final String MEASURE_NAME = "Measure";
  public static final int MEASURE_ORDER = 7;

  public static final String CUBE_ID = "cube";
  public static final String CUBE_NAME = "Cube Name";
  public static final int CUBE_ORDER = 8;

  public static final String HIDDEN_ID = "hidden";
  public static final String HIDDEN_NAME = "Hidden";
  public static final int HIDDEN_ORDER = 9;

  public static final String MDI_GROUP = "MEASURE";

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  @Injection( name = MDI_GROUP + "_NAME", group = MDI_GROUP )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = AGGREGATE_TYPE_ID, name = AGGREGATE_TYPE_NAME, order = AGGREGATE_TYPE_ORDER )
  @Injection( name = MDI_GROUP + "_AGGREGATION_TYPE", group = MDI_GROUP )
  private AggregationType aggregateType = AggregationType.SUM;

  @MetaStoreAttribute
  @ModelProperty( id = FORMAT_STRING_ID, name = FORMAT_STRING_NAME, order = FORMAT_STRING_ORDER )
  @Injection( name = MDI_GROUP + "_FORMAT_STRING", group = MDI_GROUP )
  private String formatString;

  @MetaStoreAttribute
  @ModelProperty( id = DESCRIPTION_ID, name = DESCRIPTION_NAME, order = DESCRIPTION_ORDER )
  @Injection( name = MDI_GROUP + "_DESCRIPTION", group = MDI_GROUP )
  private String description;

  @MetaStoreAttribute
  @ModelProperty( id = FIELD_ID, name = FIELD_NAME, order = FIELD_ORDER, hideUI = true  )
  @Injection( name = MDI_GROUP + "_FIELD", group = MDI_GROUP )
  private String field;

  @MetaStoreAttribute
  @ModelProperty( id = LEVEL_ID, name = LEVEL_NAME, order = LEVEL_ORDER, hideUI = true  )
  private String level;

  @MetaStoreAttribute
  @ModelProperty( id = MEASURE_ID, name = MEASURE_NAME, order = MEASURE_ORDER, hideUI = true  )
  private String measure;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER, hideUI = true  )
  private String cube;

  @MetaStoreAttribute
  // Do not expose business group in the UI (for now)
  //@ModelProperty( id = BUSINESS_GROUP_ID, name = BUSINESS_GROUP_NAME, order = BUSINESS_GROUP_ORDER )
  private String businessGroup;

  @MetaStoreAttribute
  @ModelProperty( id = HIDDEN_ID, name = HIDDEN_NAME, order = HIDDEN_ORDER )
  @Injection( name = MDI_GROUP + "_IS_HIDDEN", group = MDI_GROUP )
  private boolean hidden;

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

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getBusinessGroup() {
    return businessGroup;
  }

  public void setBusinessGroup( String businessGroup ) {
    this.businessGroup = businessGroup;
  }

  @Override
  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel( String level ) {
    this.level = level;
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

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  @Override
  public boolean apply(
      final ModelerWorkspace workspace, final IMetaStore metaStore ) throws ModelerException {
    List<LogicalTable> logicalTables = workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables();
    for ( LogicalTable logicalTable : logicalTables ) {
      List<LogicalColumn> logicalColumns = logicalTable.getLogicalColumns();
      for ( LogicalColumn logicalColumn : logicalColumns ) {
        if ( columnMatches( workspace, resolveField( workspace ), logicalColumn )
            || columnMatches( workspace, PhysicalTableImporter.beautifyName( resolveField( workspace ) ), logicalColumn ) ) {
          String targetColumn =
              (String) logicalColumn.getPhysicalColumn().getProperty( SqlPhysicalColumn.TARGET_COLUMN );
          MeasureMetaData measureMetaData =
              new MeasureMetaData( targetColumn,
                getFormatString(),
                getName(),
                workspace.getWorkspaceHelper().getLocale() );

          LogicalColumn columnClone = (LogicalColumn) logicalColumn.clone();
          columnClone.setId( BaseModelerWorkspaceHelper.uniquify( columnClone.getId(), logicalColumns ) );
          measureMetaData.setLogicalColumn( columnClone );
          measureMetaData.setName( getName() );
          measureMetaData.setDefaultAggregation( getAggregateType() );
          measureMetaData.setHidden( isHidden() );
          if ( getDescription() != null ) {
            measureMetaData.setDescription( getDescription() );
          }
          removeAutoMeasure( workspace, resolveField( workspace ) );
          removeMeasure( workspace, getName() );
          workspace.getModel().getMeasures().add( measureMetaData );
          removeAutoLevel( workspace, locateLevel( workspace, resolveField( workspace ) ) );
          workspace.getWorkspaceHelper().populateDomain( workspace );
          return true;
        }
      }

    }
    throw new ModelerException( "Unable to apply Create Measure annotation: Column not found" );
  }

  /**
   * Resolves the field for {@link ModelerWorkspace}.
   *
   * @param workspace Workspace to search
   * @return the found field otherwise null
   * @throws ModelerException
   */
  private String resolveField( final ModelerWorkspace workspace ) throws ModelerException {
    String field = getField();
    if ( StringUtils.isBlank( field ) ) {
      if ( !StringUtils.isBlank( getLevel() ) && !StringUtils.isBlank( getCube() ) ) {
        field = resolveFieldFromLevel( workspace, getLevel(), getCube() );
        setField( field );
      } else if ( !StringUtils.isBlank( getMeasure() ) ) {
        field = resolveFieldFromMeasure( workspace, getMeasure(), getCube() );
        setField( field );
      } else {
        throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_FIELD" )
        );
      }
    }

    return field;
  }

  /**
   * Resolves the field for {@link Document} schema.
   *
   * @param schema Schema to search
   * @return returns the field otherwise null
   * @throws ModelerException
   */
  private String resolveField( final Document schema ) throws ModelerException {
    String field = getField();
    if ( StringUtils.isBlank( field ) ) {
      if ( !StringUtils.isBlank( measure ) ) {
        field = resolveFieldFromMeasure( schema, getMeasure() );
        setField( field );
      } else {
        throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_FIELD" )
        );
      }
    }

    return field;
  }

  private boolean columnMatches( final ModelerWorkspace workspace, final String column,
                                 final LogicalColumn logicalColumn ) {
    return column.equalsIgnoreCase(
        logicalColumn.getName( workspace.getWorkspaceHelper().getLocale() ) );
  }

  @Override
  protected void removeAutoMeasure( final ModelerWorkspace workspace, final String column ) {
    LogicalColumn logicalColumn = locateLogicalColumn( workspace, column );
    String locale = workspace.getWorkspaceHelper().getLocale();
    for ( MeasureMetaData measure : workspace.getModel().getMeasures() ) {
      if ( measureNameEquals( column, measure )
          && measure.getLogicalColumn().getPhysicalColumn().getName( locale ).equals(
          logicalColumn.getPhysicalColumn().getName( locale ) )
          && measure.getDefaultAggregation().equals( AggregationType.SUM ) ) {
        workspace.getModel().getMeasures().remove( measure );
        break;
      }
    }
  }

  private boolean measureNameEquals( String column, MeasureMetaData measure ) {
    return measure.getName().equalsIgnoreCase( column )
        || measure.getName().equalsIgnoreCase( PhysicalTableImporter.beautifyName( column ) );
  }

  private void removeMeasure( final ModelerWorkspace workspace, final String measureName ) {
    for ( MeasureMetaData measure : workspace.getModel().getMeasures() ) {
      if ( measure.getName().equals( measureName ) ) {
        workspace.getModel().getMeasures().remove( measure );
        break;
      }
    }
  }

  @Override
  public boolean apply( Document doc ) throws ModelerException {
    // Surgically add the measure into the cube...
    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( doc );

    MondrianDef.Measure measure = new MondrianDef.Measure();
    measure.aggregator = MondrianModelExporter.convertToMondrian( getAggregateType() );
    measure.name = this.getName();

    measure.column = resolveField( doc );

    measure.formatString = this.formatString;

    mondrianSchemaHandler.addMeasure( null, measure );

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
    if ( getAggregateType() != null ) {
      return BaseMessages.getString( MSG_CLASS, "Modeler.CreateMeasure.Summary", getName(), getAggregateType().name() );
    } else {
      return BaseMessages.getString( MSG_CLASS, "Modeler.CreateMeasure.NoAggregateSummary", getName() );
    }
  }

  @Override
  public void validate() throws ModelerException {

    if ( StringUtils.isBlank( getField() )
        && ( StringUtils.isBlank( getLevel() ) || StringUtils.isBlank( getCube() ) )
        && ( StringUtils.isBlank( getMeasure() ) || StringUtils.isBlank( getCube() ) ) ) {
      throw new ModelerException( BaseMessages
        .getString( MSG_CLASS, "ModelAnnotation.CreateMeasure.validation.FIELD_OR_LEVEL_NOT_PROVIDED" ) );
    }

    if ( StringUtils.isBlank( getName() ) ) {
      throw new ModelerException( BaseMessages
          .getString( MSG_CLASS, "ModelAnnotation.CreateMeasure.validation.MEASURE_NAME_REQUIRED" ) );
    }
  }
}
