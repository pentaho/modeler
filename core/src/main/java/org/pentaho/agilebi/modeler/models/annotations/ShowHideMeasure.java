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

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ShowHideMeasure extends AnnotationType {
  protected static final Class<?> MSG_CLASS = BaseModelerWorkspaceHelper.class;

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Measure Name";
  public static final int NAME_ORDER = 0;

  public static final String CUBE_ID = "cube";
  public static final String CUBE_NAME = "Cube Name";
  public static final int CUBE_ORDER = 1;

  public static final String VISIBLE_ID = "visible";
  public static final String VISIBLE_NAME = "Visible";
  public static final int VISIBLE_ORDER = 2;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  protected String name;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER )
  protected String cube;

  @MetaStoreAttribute
  @ModelProperty( id = VISIBLE_ID, name = VISIBLE_NAME, order = VISIBLE_ORDER )
  protected boolean visible = false;

  @Override public boolean apply( final ModelerWorkspace workspace, final IMetaStore metaStore )
    throws ModelerException {
    return false;
  }

  @Override public boolean apply( final Document schema ) throws ModelerException {
    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schema );
    return mondrianSchemaHandler.showHideMeasure( getCube(), getName(), isVisible() );
  }

  @Override public void validate() throws ModelerException {
    if ( isBlank( getCube() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.ShowHide.validation.CUBE_NAME_REQUIRED" ) );
    }
    if ( isBlank( getName() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.ShowHide.validation.MEASURE_NAME_REQUIRED" ) );
    }
  }

  @Override public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.SHOW_HIDE_MEASURE;
  }

  @Override public String getSummary() {
    return BaseMessages.getString(
      MSG_CLASS, isVisible() ? "Modeler.ShowMeasure.Summary" : "Modeler.HideMeasure.Summary", getName(), getCube() );
  }

  @Override public String getName() {
    return name;
  }

  @Override public String getField() {
    return null;
  }

  public void setName( final String name ) {
    this.name = name;
  }

  public void setCube( final String cube ) {
    this.cube = cube;
  }

  public String getCube() {
    return cube;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible( final boolean visible ) {
    this.visible = visible;
  }
}
