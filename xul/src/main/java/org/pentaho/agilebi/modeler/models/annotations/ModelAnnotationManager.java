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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.util.PentahoDefaults;

import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationManager {

  public static final String DEFAULT_NAMESPACE = "pentaho";
  public static final String SHARED_DIMENSIONS_NAMESPACE = "pentaho.shared.dimensions";

  private boolean sharedDimension;
  private ModelAnnotationObjectFactory modelAnnotationObjectFactory = new ModelAnnotationObjectFactory();

  public ModelAnnotationManager() {
    this( false );
  }

  public ModelAnnotationManager( boolean sharedDimension ) {
    this.sharedDimension = sharedDimension;
  }

  @Deprecated
  public ModelAnnotationManager( String type ) {
    if ( StringUtils.equals( SHARED_DIMENSIONS_NAMESPACE, type ) ) {
      sharedDimension = true;
    }
  }

  private MetaStoreFactory<? extends ModelAnnotationGroup> getGroupMetaStoreFactory( IMetaStore metastore )
      throws MetaStoreException {

    if ( this.sharedDimension ) {
      return getMetaStoreFactory( metastore, SharedDimensionGroup.class );
    } else {
      return getMetaStoreFactory( metastore, ModelAnnotationGroup.class );
    }
  }

  private <T> MetaStoreFactory<T> getMetaStoreFactory( IMetaStore metastore, Class<T> clazz )
      throws MetaStoreException {
    if ( !metastore.namespaceExists( DEFAULT_NAMESPACE ) ) {
      metastore.createNamespace( DEFAULT_NAMESPACE );
    }
    MetaStoreFactory<T> factory = new MetaStoreFactory<T>( clazz, metastore, DEFAULT_NAMESPACE );
    factory.setObjectFactory( this.modelAnnotationObjectFactory );
    return factory;
  }

  private ModelAnnotationGroup augmentGroup( ModelAnnotationGroup modelAnnotationGroup ) {

    if ( this.sharedDimension ) {
      return new SharedDimensionGroup( modelAnnotationGroup );
    } else {
      return modelAnnotationGroup;
    }

  }

  public void createGroup( final ModelAnnotationGroup modelAnnotationGroup, final IMetaStore metastore )
      throws Exception {
    if ( metastore == null || modelAnnotationGroup == null ) {
      return;
    }

    MetaStoreFactory factory = getGroupMetaStoreFactory( metastore );
    factory.saveElement( augmentGroup( modelAnnotationGroup ) );
  }

  public ModelAnnotationGroup readGroup( String groupName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    return (ModelAnnotationGroup) factory.loadElement( groupName );
  }

  public void updateGroup( ModelAnnotationGroup modelAnnotationGroup, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    factory.deleteElement( modelAnnotationGroup.getName() );
    factory.saveElement( augmentGroup( modelAnnotationGroup ) );
  }

  public void deleteGroup( String groupName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    factory.deleteElement( groupName );
  }

  public List<ModelAnnotationGroup> listGroups( final IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = getGroupMetaStoreFactory( metastore );
    return factory.getElements();
  }

  public List<String> listGroupNames( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    return factory.getElementNames();
  }

  public boolean containsGroup( String groupName, IMetaStore metastore ) throws MetaStoreException {
    if ( metastore == null ) {
      return false;
    }
    for ( String name : listGroupNames( metastore ) ) {
      if ( name.equals( groupName ) ) {
        return true;
      }
    }
    return false;
  }

  public void deleteAllGroups( IMetaStore metastore ) throws MetaStoreException {
    if ( metastore == null ) {
      return;
    }

    for ( String name : listGroupNames( metastore ) ) {
      this.deleteGroup( name, metastore );
    }
  }

  /**
   * @param dbMeta
   * @return DatabaseMetaRefName
   * @throws MetaStoreException
   */
  public String storeDatabaseMeta( DatabaseMeta dbMeta, IMetaStore mstore ) throws MetaStoreException {
    // TODO: what to do about shared objects, variables?

    // get the type that's actually stored, the one that comes in the element is new from populate
    IMetaStoreElementType properType = getDatabaseMetaType( mstore );
    IMetaStoreElement dbMetaElement = DatabaseMetaStoreUtil.populateDatabaseElement( mstore, dbMeta );
    IMetaStoreElement dbMetaExisting =
        mstore.getElementByName( properType.getNamespace(), properType, dbMeta.getName() );
    // update if exists, create if doesn't
    if ( dbMetaExisting != null ) {
      mstore.updateElement( properType.getNamespace(), properType, dbMetaExisting.getId(), dbMetaElement );
    } else {
      mstore.createElement( properType.getNamespace(), properType, dbMetaElement );
    }
    return dbMeta.getName();
  }

  public DatabaseMeta loadDatabaseMeta( String databaseMetaRefName, IMetaStore mstore ) throws MetaStoreException,
      KettlePluginException {
    IMetaStoreElementType dbMetaType =
        mstore.getElementTypeByName( PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    IMetaStoreElement element = mstore.getElementByName( dbMetaType.getNamespace(), dbMetaType, databaseMetaRefName );
    if ( element == null ) {
      return null;
    }
    return DatabaseMetaStoreUtil.loadDatabaseMetaFromDatabaseElement( mstore, element );
  }

  private static IMetaStoreElementType getDatabaseMetaType( IMetaStore metaStore ) throws MetaStoreException {
    if ( !metaStore.namespaceExists( PentahoDefaults.NAMESPACE ) ) {
      metaStore.createNamespace( PentahoDefaults.NAMESPACE );
    }
    IMetaStoreElementType elementType =
        metaStore.getElementTypeByName( PentahoDefaults.NAMESPACE,
            PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    if ( elementType == null ) {
      elementType = DatabaseMetaStoreUtil.populateDatabaseElementType( metaStore );
      metaStore.createElementType( PentahoDefaults.NAMESPACE, elementType );
    }
    return elementType;
  }
}
