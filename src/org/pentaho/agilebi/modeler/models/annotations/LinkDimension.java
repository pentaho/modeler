/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2016 Pentaho Corporation (Pentaho). All rights reserved.
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


import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.util.ISpoonModelerSource;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

public class LinkDimension extends AnnotationType {
  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Dimension Name";
  public static final int NAME_ORDER = 0;

  public static final String SHARED_DIMENSION_ID = "sharedDimension";
  public static final String SHARED_DIMENSION_NAME = "Shared Dimension";
  public static final int SHARED_DIMENSION_ORDER = 1;

  public static final String FIELD_ID = "field";
  public static final String FIELD_NAME = "Field Name";
  public static final int FIELD_ORDER = 2;

  public static final String MDI_GROUP = "LINK_DIMENSION";

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  @Injection( name = MDI_GROUP + "_DIMENSION_NAME", group = MDI_GROUP )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = SHARED_DIMENSION_ID, name = SHARED_DIMENSION_NAME, order = SHARED_DIMENSION_ORDER )
  @Injection( name = MDI_GROUP + "_SHARED_DIMENSION_NAME", group = MDI_GROUP )
  private String sharedDimension;

  @MetaStoreAttribute
  @ModelProperty( id = FIELD_ID, name = FIELD_NAME, order = FIELD_ORDER, hideUI = true )
  @Injection( name = MDI_GROUP + "_FIELD", group = MDI_GROUP )
  private String field;

  private Map<ApplyStatus, List<ModelAnnotation>> sharedApplyStatus;

  @Override public boolean apply(
      final ModelerWorkspace factWorkspace, final IMetaStore metaStore ) throws ModelerException {
    ModelAnnotationManager modelAnnotationManager = new ModelAnnotationManager( true );
    try {
      if ( !modelAnnotationManager.containsGroup( getSharedDimension(), metaStore ) ) {
        return false;
      }
      assignFactTable( factWorkspace );
      ModelAnnotationGroup sharedAnnotations = modelAnnotationManager.readGroup( getSharedDimension(), metaStore );
      List<DataProvider> dataProviders = sharedAnnotations.getDataProviders();
      DataProvider dataProvider = locateDataProvider( dataProviders, factWorkspace, metaStore );
      if ( dataProvider == null ) {
        return false;
      }
      ModelerWorkspace dimensionWorkspace = autoModelSharedDimension( factWorkspace, dataProvider );

      sharedApplyStatus = sharedAnnotations.applyAnnotations( dimensionWorkspace, metaStore );
      if ( sharedApplyStatus.get( ApplyStatus.FAILED ) != null
        && sharedApplyStatus.get( ApplyStatus.FAILED ).size() > 0 ) {
        return false;
      }
      String dimKey = locateDimensionKey( sharedAnnotations );
      if ( Const.isEmpty( dimKey ) ) {
        return false;
      }
      removeAutoLevel( factWorkspace, locateLevel( factWorkspace, field ) );
      removeAutoMeasure( factWorkspace, field );
      moveDimensionToModel( dimensionWorkspace, factWorkspace, field, dimKey );
      factWorkspace.getWorkspaceHelper().populateDomain( factWorkspace );
      return true;
    } catch ( KettlePluginException e ) {
      throw new ModelerException( e );
    } catch ( MetaStoreException e ) {
      throw new ModelerException( e );
    }
  }

  private DataProvider locateDataProvider(
      final List<DataProvider> dataProviders, final ModelerWorkspace workspace, final IMetaStore metaStore )
      throws MetaStoreException, KettlePluginException, ModelerException {
    ModelAnnotationManager manager = new ModelAnnotationManager( true );
    DatabaseMeta factDbMeta = ( (ISpoonModelerSource) workspace.getModelSource() ).getDatabaseMeta();
    for ( DataProvider dataProvider : dataProviders ) {
      DatabaseMeta sharedDbMeta = manager.loadDatabaseMeta( dataProvider.getDatabaseMetaNameRef(), metaStore );
      if ( sharedDbMeta != null && dbMetaEquals( factDbMeta, sharedDbMeta ) ) {
        return dataProvider;
      }
    }
    return null;
  }

  private boolean dbMetaEquals( final DatabaseMeta factDbMeta, final DatabaseMeta sharedDbMeta ) {
    return factDbMeta.getName() != null && factDbMeta.getName().equals( sharedDbMeta.getName() )
        && hostNameEquals( factDbMeta, sharedDbMeta )
        && dbNameEquals( factDbMeta, sharedDbMeta )
        && factDbMeta.getDriverClass() != null && factDbMeta.getDriverClass().equals( sharedDbMeta.getDriverClass() );
  }

  private boolean hostNameEquals( final DatabaseMeta factDbMeta, final DatabaseMeta sharedDbMeta ) {
    return ( factDbMeta.getHostname() != null
        && factDbMeta.environmentSubstitute( factDbMeta.getHostname() ).equals( sharedDbMeta.getHostname() ) )
        || ( factDbMeta.getHostname() == null && sharedDbMeta.getHostname() == null );
  }

  private boolean dbNameEquals( final DatabaseMeta factDbMeta, final DatabaseMeta sharedDbMeta ) {
    return ( factDbMeta.getDatabaseName() != null
        && factDbMeta.environmentSubstitute( factDbMeta.getDatabaseName() ).equals( sharedDbMeta.getDatabaseName() ) )
        || ( factDbMeta.getDatabaseName() == null && sharedDbMeta.getDatabaseName() == null );
  }

  private void assignFactTable( final ModelerWorkspace workspace ) {
    List<LogicalTable> logicalTables = workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables();
    logicalTables.get( 0 ).getPhysicalTable().setProperty( "FACT_TABLE", true );
  }

  private String locateDimensionKey( final ModelAnnotationGroup modelAnnotations ) {
    for ( ModelAnnotation modelAnnotation : modelAnnotations ) {
      if ( modelAnnotation.getType().equals( ModelAnnotation.Type.CREATE_DIMENSION_KEY ) ) {
        return ( (CreateDimensionKey) modelAnnotation.getAnnotation() ).getField();
      }
    }
    return null;
  }

  private void moveDimensionToModel(
      final ModelerWorkspace dimensionWorkspace, final ModelerWorkspace factWorkspace,
      final String factKey, final String dimKey ) throws ModelerException {
    DimensionMetaData dimension = getLastDimension( dimensionWorkspace );
    dimension.setName( getName() );
    removeExistingDimension( factWorkspace );
    factWorkspace.addDimension( dimension );
    LogicalTable dimTable =
        dimensionWorkspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    LogicalTable factTable =
        factWorkspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables().get( 0 );
    LogicalModel logicalModel = factWorkspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    logicalModel.addLogicalTable( dimTable );
    @SuppressWarnings( "unchecked" ) List<SqlPhysicalTable> physicalTables =
        (List<SqlPhysicalTable>) factWorkspace.getDomain().getPhysicalModels().get( 0 ).getPhysicalTables();
    physicalTables.add( (SqlPhysicalTable) dimTable.getPhysicalTable() );
    logicalModel.addLogicalRelationship(
        new LogicalRelationship(
        logicalModel, factTable, dimTable,
        locateLogicalColumn( factWorkspace, factKey ), locateLogicalColumn( dimensionWorkspace, dimKey ) ) );
  }

  private void removeExistingDimension( final ModelerWorkspace factWorkspace ) {
    DimensionMetaDataCollection dimensions = factWorkspace.getModel().getDimensions();
    for ( int i = 0; i < dimensions.size(); i++ ) {
      final DimensionMetaData dimension = dimensions.get( i );
      if ( dimension.getName().equalsIgnoreCase( getName() ) ) {
        dimensions.remove( i );
        return;
      }
    }
  }

  private DimensionMetaData getLastDimension( final ModelerWorkspace dimensionWorkspace ) {
    DimensionMetaDataCollection dimensions = dimensionWorkspace.getModel().getDimensions();
    return dimensions.get( dimensions.size() - 1 );
  }

  private ModelerWorkspace autoModelSharedDimension( final ModelerWorkspace workspace, final DataProvider dataProvider )
      throws MetaStoreException, KettlePluginException, ModelerException {

    DatabaseMeta dbMeta = ( (ISpoonModelerSource) workspace.getModelSource() ).getDatabaseMeta();
    TableModelerSource source =
        new TableModelerSource( dbMeta, dataProvider.getTableName(), dataProvider.getSchemaName() );
    Domain domain = source.generateDomain( new SharedDimensionImportStrategy( dataProvider ) );
    ModelerWorkspace model =
        new ModelerWorkspace(
            new ModelerWorkspaceHelper( workspace.getWorkspaceHelper().getLocale() ), workspace.getGeoContext() );
    model.setModelSource( source );
    model.setDomain( domain );
    model.getWorkspaceHelper().autoModelFlat( model );

    return model;
  }

  @Override public boolean apply( final Document schema ) throws ModelerException {
    return false;
  }

  @Override public void validate() throws ModelerException {
    if ( StringUtils.isBlank( getName() ) ) {
      throw new ModelerException(
          BaseMessages.getString( MSG_CLASS, "Modeler.LinkDimension.validation.DIMENSION_NAME_REQUIRED" ) );
    }
    if ( StringUtils.isBlank( getSharedDimension() ) ) {
      throw new ModelerException( BaseMessages.getString( MSG_CLASS,
          "Modeler.LinkDimension.validation.SHARED_DIMENSION_REQUIRED" ) );
    }
  }

  @Override public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.LINK_DIMENSION;
  }

  @Override public String getSummary() {
    StringBuilder summary = new StringBuilder(
      BaseMessages.getString( MSG_CLASS, "Modeler.LinkDimension.Summary", getName(), getSharedDimension() ) );
    if ( sharedApplyStatus != null ) {
      appendSummaries( summary, ApplyStatus.FAILED, "ModelAnnotation.log.AnnotationFailure" );
      appendSummaries( summary, ApplyStatus.SUCCESS, "ModelAnnotation.log.AnnotationSuccess" );
    }

    return summary.toString();
  }

  private void appendSummaries( final StringBuilder summary, final ApplyStatus status, final String msgKey ) {
    for ( ModelAnnotation modelAnnotation : sharedApplyStatus.get( status ) ) {
      summary.append( "\n    " );
      summary.append( BaseMessages.getString( MSG_CLASS, msgKey,  modelAnnotation.getAnnotation().getSummary() ) );
    }
  }

  @Override public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getSharedDimension() {
    return sharedDimension;
  }

  public void setSharedDimension( String sharedDimension ) {
    this.sharedDimension = sharedDimension;
  }

  @Override
  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }
}
