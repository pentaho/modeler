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

    if ( StringUtils.isBlank( getField() ) ) {
      throw new ModelerException( BaseMessages.getString( MSG_CLASS,
        "ModelAnnotation.CreateAttribute.validation.FIELD_NAME " ) );
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
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }
}
