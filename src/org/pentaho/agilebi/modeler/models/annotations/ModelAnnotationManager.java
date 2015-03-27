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

  private String nameSpace;
  private ModelAnnotationObjectFactory modelAnnotationObjectFactory = new ModelAnnotationObjectFactory();

  public ModelAnnotationManager() {
    this( null );
  }

  public ModelAnnotationManager( String namespace ) {
    this.nameSpace = namespace;
    if ( StringUtils.isBlank( this.nameSpace ) ) {
      this.nameSpace = "pentaho";
    }
  }

  private MetaStoreFactory<ModelAnnotation> getMetaStoreFactory( IMetaStore metastore ) {
    MetaStoreFactory<ModelAnnotation>
        factory =
        new MetaStoreFactory( ModelAnnotation.class, metastore, this.nameSpace );
    factory.setObjectFactory( this.modelAnnotationObjectFactory );
    return factory;
  }

  private MetaStoreFactory<ModelAnnotationGroup> getGroupMetaStoreFactory( IMetaStore metastore ) {
    MetaStoreFactory<ModelAnnotationGroup>
        factory =
        new MetaStoreFactory( ModelAnnotationGroup.class, metastore, this.nameSpace );
    factory.setObjectFactory( this.modelAnnotationObjectFactory );
    return factory;
  }

  public void create( ModelAnnotation modelAnnotation, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getMetaStoreFactory( metastore );
    factory.saveElement( modelAnnotation );
  }

  public void createGroup( final ModelAnnotationGroup modelAnnotationGroup, final IMetaStore metastore )
      throws Exception {
    if ( metastore == null || modelAnnotationGroup == null ) {
      return;
    }

    MetaStoreFactory factory = getGroupMetaStoreFactory( metastore );
    factory.saveElement( modelAnnotationGroup );
  }

  public ModelAnnotation read( String modelAnnotationName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getMetaStoreFactory( metastore );
    return (ModelAnnotation) factory.loadElement( modelAnnotationName );
  }

  public ModelAnnotationGroup readGroup( String groupName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    return (ModelAnnotationGroup) factory.loadElement( groupName );
  }

  public void update( ModelAnnotation modelAnnotation, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getMetaStoreFactory( metastore );
    factory.deleteElement( modelAnnotation.getName() );
    factory.saveElement( modelAnnotation );
  }

  public void updateGroup( ModelAnnotationGroup modelAnnotationGroup, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    factory.deleteElement( modelAnnotationGroup.getName() );
    factory.saveElement( modelAnnotationGroup );
  }

  public void delete( String modelAnnotationName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getMetaStoreFactory( metastore );
    factory.deleteElement( modelAnnotationName );
  }

  public void deleteGroup( String groupName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    factory.deleteElement( groupName );
  }

  public ModelAnnotationGroup list( final IMetaStore metastore ) throws Exception {

    final ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    try {
      MetaStoreFactory factory = getMetaStoreFactory( metastore );
      modelAnnotationGroup.addAll( factory.getElements() );
    } catch ( MetaStoreException e ) {
      throw new RuntimeException( e );
    }

    return modelAnnotationGroup;
  }

  public List<ModelAnnotationGroup> listGroups( final IMetaStore metastore ) throws Exception {
    MetaStoreFactory factory = getGroupMetaStoreFactory( metastore );
    return factory.getElements();
  }

  public List<String> listNames( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getMetaStoreFactory( metastore );
    return factory.getElementNames();
  }

  public List<String> listGroupNames( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory factory = this.getGroupMetaStoreFactory( metastore );
    return factory.getElementNames();
  }

  public boolean contains( String modelAnnotationName, IMetaStore metastore ) throws MetaStoreException {
    if ( metastore == null ) {
      return false;
    }
    for ( String name : listNames( metastore ) ) {
      if ( name.equals( modelAnnotationName ) ) {
        return true;
      }
    }
    return false;
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

  public void deleteAll( IMetaStore metastore ) throws MetaStoreException {
    if ( metastore == null ) {
      return;
    }

    for ( String name : listNames( metastore ) ) {
      this.delete( name, metastore );
    }
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
   * 
   * @param dbMeta
   * @return DatabaseMetaRefName
   * @throws MetaStoreException 
   */
  public String storeDatabaseMeta( DatabaseMeta dbMeta, IMetaStore mstore ) throws MetaStoreException {
    // TODO: what to do about shared objects, variables?
    // check if exists
    boolean exists = false;
    for ( DatabaseMeta stored : DatabaseMetaStoreUtil.getDatabaseElements( mstore ) ) {
      if ( stored.equals( dbMeta ) ) {
        exists = true;
        break;
      }
    }
    // update if exists, create if doesn't
    if ( exists ) {
      IMetaStoreElement dbMetaElement = DatabaseMetaStoreUtil.populateDatabaseElement( mstore, dbMeta );
      // get the type that's actually stored, the one that comes in the element is new from populate
      IMetaStoreElementType properType = getDatabaseMetaType( mstore );
      mstore.updateElement( properType.getNamespace(), properType, dbMetaElement.getId(), dbMetaElement );
    } else {
      // creates type if not there, throws if element exists
      DatabaseMetaStoreUtil.createDatabaseElement( mstore, dbMeta );
    }
    // id == name
    return dbMeta.getName();
  }

  public DatabaseMeta loadDatabaseMeta( String databaseMetaRefName, IMetaStore mstore ) throws MetaStoreException,
      KettlePluginException {
    IMetaStoreElementType dbMetaType = DatabaseMetaStoreUtil.populateDatabaseElementType( mstore );
    IMetaStoreElement element = mstore.getElementByName( dbMetaType.getNamespace(), dbMetaType, databaseMetaRefName );
    if ( element == null ) {
      return null;
    }
    return DatabaseMetaStoreUtil.loadDatabaseMetaFromDatabaseElement( mstore, element );
  }

  private static IMetaStoreElementType getDatabaseMetaType( IMetaStore metaStore ) throws MetaStoreException {
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
