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
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

public class UpdateAttribute extends AnnotationType {
  private static final long serialVersionUID = 1201984935190601808L;
  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "New Attribute Name";
  public static final int NAME_ORDER = 0;

  public static final String CUBE_ID = "cube";
  public static final String CUBE_NAME = "Cube";
  public static final int CUBE_ORDER = 1;

  public static final String DIMENSION_ID = "dimension";
  public static final String DIMENSION_NAME = "Dimension";
  public static final int DIMENSION_ORDER = 2;

  public static final String HIERARCHY_ID = "hierarchy";
  public static final String HIERARCHY_NAME = "Hierarchy";
  public static final int HIERARCHY_ORDER = 3;

  public static final String LEVEL_ID = "level";
  public static final String LEVEL_NAME = "Level";
  public static final int LEVEL_ORDER = 4;

  public static final String FORMAT_STRING_ID = "formatString";
  public static final String FORMAT_STRING_NAME = "Format String";
  public static final int FORMAT_STRING_ORDER = 5;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  protected String name;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER )
  protected String cube;

  @MetaStoreAttribute
  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME, order = DIMENSION_ORDER )
  protected String dimension;

  @MetaStoreAttribute
  @ModelProperty( id = HIERARCHY_ID, name = HIERARCHY_NAME, order = HIERARCHY_ORDER )
  protected java.lang.String hierarchy;

  @MetaStoreAttribute
  @ModelProperty( id = LEVEL_ID, name = LEVEL_NAME, order = LEVEL_ORDER )
  protected String level;

  @MetaStoreAttribute
  @ModelProperty( id = FORMAT_STRING_ID, name = FORMAT_STRING_NAME, order = FORMAT_STRING_ORDER )
  protected String formatString;

  @Override public boolean apply( final ModelerWorkspace workspace, final IMetaStore metaStore )
    throws ModelerException {
    return false;
  }

  @Override public boolean apply( final Document schema ) throws ModelerException {
    MondrianSchemaHandler schemaHandler = new MondrianSchemaHandler( schema );
    boolean captioned = schemaHandler.captionLevel( getCube(), getDimension(), getHierarchy(), getLevel(), getName() );
    if ( captioned ) {
      if ( !StringUtils.isBlank( getFormatString() ) ) {
        return schemaHandler.formatLevel( getCube(), getDimension(), getHierarchy(), getLevel(), getFormatString() );
      } else {
        return schemaHandler.removeFormatting( getCube(), getDimension(), getHierarchy(), getLevel() );
      }
    }
    return false;
  }

  @Override public void validate() throws ModelerException {
    if ( StringUtils.isBlank( getCube() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.UpdateAttribute.validation.CUBE_NAME_REQUIRED" ) );
    }
    if ( StringUtils.isBlank( getDimension() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.UpdateAttribute.validation.DIMENSION_NAME_REQUIRED" ) );
    }
    if ( StringUtils.isBlank( getHierarchy() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.UpdateAttribute.validation.HIERARCHY_NAME_REQUIRED" ) );
    }
    if ( StringUtils.isBlank( getLevel() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.UpdateAttribute.validation.LEVEL_NAME_REQUIRED" ) );
    }
    if ( StringUtils.isBlank( getName() ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "ModelAnnotation.UpdateAttribute.validation.NAME_REQUIRED" ) );
    }

  }

  @Override public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.UPDATE_ATTRIBUTE;
  }

  @Override public String getSummary() {
    return BaseMessages.getString( MSG_CLASS, "Modeler.UpdateAttribute.Summary", level, hierarchy, dimension, cube );
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

  public void setDimension( final String dimension ) {
    this.dimension = dimension;
  }

  public void setHierarchy( final java.lang.String hierarchy ) {
    this.hierarchy = hierarchy;
  }

  public void setLevel( final String level ) {
    this.level = level;
  }

  public String getCube() {
    return cube;
  }

  public String getDimension() {
    return dimension;
  }

  public String getHierarchy() {
    return hierarchy;
  }

  public String getLevel() {
    return level;
  }

  public String getFormatString() {
    return formatString;
  }

  public void setFormatString( final String formatString ) {
    this.formatString = formatString;
  }
}

