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
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

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

  @Override public boolean apply( final ModelerWorkspace workspace, final String field ) throws ModelerException {
    return false;
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
