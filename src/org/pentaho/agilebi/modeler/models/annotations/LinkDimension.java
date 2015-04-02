/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2015 Pentaho Corporation (Pentaho). All rights reserved.
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


import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
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

public class LinkDimension extends AnnotationType {
  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Dimension Name";
  public static final int NAME_ORDER = 0;

  public static final String SHARED_DIMENSION_ID = "sharedDimension";
  public static final String SHARED_DIMENSION_NAME = "Shared Dimension";
  public static final int SHARED_DIMENSION_ORDER = 1;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = SHARED_DIMENSION_ID, name = SHARED_DIMENSION_NAME, order = SHARED_DIMENSION_ORDER )
  private String sharedDimension;

  @Override public boolean apply(
      final ModelerWorkspace factWorkspace, final String field, final IMetaStore metaStore ) throws ModelerException {
    ModelAnnotationManager modelAnnotationManager = new ModelAnnotationManager();
    try {
      if ( !modelAnnotationManager.containsGroup( getSharedDimension(), metaStore ) ) {
        return false;
      }
      assignFactTable( factWorkspace );
      ModelAnnotationGroup sharedAnnotations = modelAnnotationManager.readGroup( getSharedDimension(), metaStore );
      DataProvider dataProvider = sharedAnnotations.getDataProviders().get( 0 );
      ModelerWorkspace dimensionWorkspace =
          autoModelSharedDimension( factWorkspace, metaStore, modelAnnotationManager, dataProvider );
      sharedAnnotations.applyAnnotations( dimensionWorkspace, metaStore );
      String dimKey = locateDimensionKey( sharedAnnotations );
      if ( Const.isEmpty( dimKey ) ) {
        return false;
      }
      moveDimensionToModel( dimensionWorkspace, factWorkspace, field, dimKey );
      removeAutoLevel( factWorkspace, locateLevel( factWorkspace, field ) );
      removeAutoMeasure( factWorkspace, field );
      factWorkspace.getWorkspaceHelper().populateDomain( factWorkspace );
      return true;
    } catch ( KettlePluginException e ) {
      throw new ModelerException( e );
    } catch ( MetaStoreException e ) {
      throw new ModelerException( e );
    }
  }

  private void assignFactTable( final ModelerWorkspace workspace ) {
    List<LogicalTable> logicalTables = workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables();
    logicalTables.get( 0 ).getPhysicalTable().setProperty( "FACT_TABLE", true );
  }

  private String locateDimensionKey( final ModelAnnotationGroup modelAnnotations ) {
    for ( ModelAnnotation modelAnnotation : modelAnnotations ) {
      if ( modelAnnotation.getType().equals( ModelAnnotation.Type.CREATE_DIMENSION_KEY ) ) {
        return modelAnnotation.getField();
      }
    }
    return null;
  }

  private void moveDimensionToModel(
      final ModelerWorkspace dimensionWorkspace, final ModelerWorkspace factWorkspace,
      final String factKey, final String dimKey ) {
    DimensionMetaData dimension = locateDimension( dimensionWorkspace );
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

  private DimensionMetaData locateDimension( final ModelerWorkspace dimensionWorkspace ) {
    DimensionMetaDataCollection dimensions = dimensionWorkspace.getModel().getDimensions();
    return dimensions.get( dimensions.size() - 1 );
  }

  private ModelerWorkspace autoModelSharedDimension(
      final ModelerWorkspace workspace, final IMetaStore metaStore,
      final ModelAnnotationManager modelAnnotationManager,
      final DataProvider dataProvider )
      throws MetaStoreException, KettlePluginException, ModelerException {

    String databaseMetaNameRef = dataProvider.getDatabaseMetaNameRef();
    DatabaseMeta dbMeta = modelAnnotationManager.loadDatabaseMeta( databaseMetaNameRef, metaStore );
    TableModelerSource source =
        new TableModelerSource( dbMeta, dataProvider.getTableName(), dataProvider.getSchemaName() );
    Domain domain = source.generateDomain();
    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( workspace.getWorkspaceHelper().getLocale() ) );
    model.setModelSource( source );
    model.setDomain( domain );
    model.getWorkspaceHelper().autoModelFlat( model );

    return model;
  }

  @Override public boolean apply( final Document schema, final String field ) throws ModelerException {
    return false;
  }

  @Override public void validate() throws ModelerException {

  }

  @Override public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.LINK_DIMENSION;
  }

  @Override public String getSummary() {
    return BaseMessages.getString( MSG_CLASS, "Modeler.LinkDimension.Summary", getName(), getSharedDimension() );
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
}
