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

import org.apache.commons.beanutils.PropertyUtils;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */

@MetaStoreElementType( name = "SharedDimensionGroup", description = "Shared Dimension Group" )
public class SharedDimensionGroup extends ModelAnnotationGroup {

  @MetaStoreAttribute
  private String id;

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @MetaStoreAttribute
  private boolean sharedDimension;

  @MetaStoreAttribute
  private List<DataProvider> dataProviders = new ArrayList<DataProvider>();

  @MetaStoreAttribute
  private List<ModelAnnotation> modelAnnotations; // indicate to metastore to persist items (calls the getter/setter)

  public SharedDimensionGroup() {
  }

  public SharedDimensionGroup( ModelAnnotationGroup modelAnnotationGroup ) {
    super();
    try {
      PropertyUtils.copyProperties( this, modelAnnotationGroup );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId( String id ) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription( String description ) {
    this.description = description;
  }

  @Override
  public boolean isSharedDimension() {
    return sharedDimension;
  }

  @Override
  public void setSharedDimension( boolean sharedDimension ) {
    this.sharedDimension = sharedDimension;
  }

  @Override
  public List<DataProvider> getDataProviders() {
    return dataProviders;
  }

  @Override
  public void setDataProviders( List<DataProvider> dataProviders ) {
    this.dataProviders = dataProviders;
  }

  @Override
  public List<ModelAnnotation> getModelAnnotations() {
    return this;
  }

  @Override
  public void setModelAnnotations( List<ModelAnnotation> modelAnnotations ) {
    removeRange( 0, this.size() ); // remove all
    if ( modelAnnotations != null ) {
      addAll( modelAnnotations );
    }
  }
}
