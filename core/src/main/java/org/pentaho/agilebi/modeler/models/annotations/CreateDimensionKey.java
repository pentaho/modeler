/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

public class CreateDimensionKey extends AnnotationType {

  private static final long serialVersionUID = 1L;

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Name";
  public static final int NAME_ORDER = 0;

  public static final String DIMENSION_ID = "dimension";
  public static final String DIMENSION_NAME = "Dimension";
  public static final int DIMENSION_ORDER = 1;

  public static final String FIELD_ID = "field";
  public static final String FIELD_NAME = "Field";
  public static final int FIELD_ORDER = 2;

  public static final String MDI_GROUP = "DIMENSION_KEY";

  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER, hideUI = true )
  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME, order = DIMENSION_ORDER )
  @Injection( name = MDI_GROUP + "_DIMENSION", group = MDI_GROUP )
  private String dimension;

  @ModelProperty( id = FIELD_ID, name = FIELD_NAME, order = FIELD_ORDER, hideUI = true )
  @MetaStoreAttribute
  @Injection( name = MDI_GROUP + "_FIELD", group = MDI_GROUP )
  private String field;

  public String getDimension() {
    return dimension;
  }

  public void setDimension( String dimension ) {
    this.dimension = dimension;
  }

  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean apply( ModelerWorkspace workspace, final IMetaStore metaStore ) throws ModelerException {
    // nothing to do at apply time.  Will be used when a corresponding LinkDimension is specified
    return true;
  }

  @Override
  public boolean apply( Document schema ) throws ModelerException {
    // TODO  will only be factored in when modeling star schema
    return false;
  }

  @Override
  public void validate() throws ModelerException {
    if ( StringUtils.isBlank( getName() ) ) {
      // not ui facing, shouldn't happen
      throw new ModelerException(
          BaseMessages.getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.ATTRIBUTE_NAME_REQUIRED" ) );
    }

    if ( StringUtils.isBlank( getDimension() ) ) {
      throw new ModelerException( BaseMessages.getString( MSG_CLASS,
          "ModelAnnotation.CreateAttribute.validation.PARENT_PROVIDED_MISSING_DIMENSION" ) );
    }
  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.CREATE_DIMENSION_KEY;
  }

  @Override
  public String getSummary() {
    return BaseMessages.getString( MSG_CLASS, "Modeler.CreateDimensionKey.Summary", getField(), getDimension() );
  }

  @Override
  public String getField() {
    if ( field == null ) {
      setField( getName() );
    }
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }
}
