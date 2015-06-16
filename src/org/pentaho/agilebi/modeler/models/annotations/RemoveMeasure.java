/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Document;

import java.util.logging.Logger;

/**
 * Created by pminutillo on 3/17/15.
 */
@MetaStoreElementType( name = "RemoveMeasure", description = "RemoveMeasure Annotation" )
public class RemoveMeasure extends AnnotationType {
  private static final long serialVersionUID = 5169827225345800226L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );

  private static final String NAME_ID = "name";
  private static final String NAME_NAME = "Name";
  private static final int NAME_ORDER = 0;

  private static final String DIMENSION_ID = "dimension";
  private static final String DIMENSION_NAME = "Dimension";
  private static final int DIMENSION_ORDER = 0;

  private static final String CUBE_ID = "cube";
  private static final String CUBE_NAME = "Cube";
  private static final int CUBE_ORDER = 0;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME, order = DIMENSION_ORDER )
  private String dimension;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER )
  private String cube;

  @Override public boolean apply( ModelerWorkspace workspace, IMetaStore metaStore )
    throws ModelerException {
    return false;
  }

  @Override public boolean apply( Document schema ) throws ModelerException {
    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schema );
    mondrianSchemaHandler.removeMeasure( cube, name );
    return true;
  }

  @Override public void validate() throws ModelerException {

  }

  @Override public ModelAnnotation.Type getType() {
    return null;
  }

  @Override public String getSummary() {
    return null;
  }

  @Override public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDimension() {
    return dimension;
  }

  public void setDimension( String dimension ) {
    this.dimension = dimension;
  }

  public String getCube() {
    return cube;
  }

  public void setCube( String cube ) {
    this.cube = cube;
  }

  public String getField() {
    return null;
  }
}
