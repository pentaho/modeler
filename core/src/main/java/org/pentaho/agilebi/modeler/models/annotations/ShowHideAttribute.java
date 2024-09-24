/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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

public class ShowHideAttribute extends AnnotationType {
  protected static final Class<?> MSG_CLASS = BaseModelerWorkspaceHelper.class;

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Level Name";
  public static final int NAME_ORDER = 0;

  public static final String CUBE_ID = "cube";
  public static final String CUBE_NAME = "Cube Name";
  public static final int CUBE_ORDER = 1;

  public static final String DIMENSION_ID = "dimension";
  public static final String DIMENSION_NAME = "Dimension Name";
  public static final int DIMENSION_ORDER = 2;

  public static final String HIERARCHY_ID = "hierarchy";
  public static final String HIERARCHY_NAME = "Hierarchy Name";
  public static final int HIERARCHY_ORDER = 3;

  public static final String VISIBLE_ID = "visible";
  public static final String VISIBLE_NAME = "Visible";
  public static final int VISIBLE_ORDER = 4;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  protected String name;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER, hideUI = true  )
  protected String cube;

  @MetaStoreAttribute
  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME, order = DIMENSION_ORDER )
  protected String dimension;

  @MetaStoreAttribute
  @ModelProperty( id = HIERARCHY_ID, name = HIERARCHY_NAME, order = HIERARCHY_ORDER )
  protected String hierarchy;



  @MetaStoreAttribute
  @ModelProperty( id = VISIBLE_ID, name = VISIBLE_NAME, order = VISIBLE_ORDER )
  protected boolean visible = false;

  @Override public boolean apply( final ModelerWorkspace workspace, final IMetaStore metaStore )
    throws ModelerException {
    return false;
  }

  @Override public boolean apply( final Document schema ) throws ModelerException {
    MondrianSchemaHandler schemaHandler = new MondrianSchemaHandler( schema );
    return schemaHandler.showHideAttribute( getCube(), getDimension(), getHierarchy(), getName(), isVisible() );
  }

  @Override public void validate() throws ModelerException {
    if ( isBlank( getCube() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.ShowHide.validation.CUBE_NAME_REQUIRED" ) );
    }
    if ( isBlank( getDimension() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.ShowHide.validation.DIMENSION_NAME_REQUIRED" ) );
    }
    if ( isBlank( getHierarchy() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.ShowHide.validation.HIERARCHY_NAME_REQUIRED" ) );
    }
    if ( isBlank( getName() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.ShowHide.validation.LEVEL_NAME_REQUIRED" ) );
    }

  }

  @Override public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.SHOW_HIDE_ATTRIBUTE;
  }

  @Override public String getSummary() {
    return BaseMessages
      .getString( MSG_CLASS, isVisible() ? "Modeler.ShowAttribute.Summary" : "Modeler.HideAttribute.Summary",
        getName(), getHierarchy(), getDimension(), getCube() );
  }

  @Override public String getName() {
    return name;
  }

  @Override public String getField() {
    return null;
  }

  public void setCube( final String cube ) {
    this.cube = cube;
  }

  public void setDimension( final String dimension ) {
    this.dimension = dimension;
  }

  public void setHierarchy( final String hierarchy ) {
    this.hierarchy = hierarchy;
  }

  public void setName( final String name ) {
    this.name = name;
  }

  public String getHierarchy() {
    return hierarchy;
  }

  public String getDimension() {
    return dimension;
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
