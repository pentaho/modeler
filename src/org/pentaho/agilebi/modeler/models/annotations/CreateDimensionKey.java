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

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

public class CreateDimensionKey extends AnnotationType {

  private static final long serialVersionUID = 1L;

  @ModelProperty( id = "field", name = "Field", order = 1, hideUI = true )
  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = CreateAttribute.DIMENSION_ID, name = CreateAttribute.DIMENSION_NAME, order = 2 )
  private String dimension;

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
  public boolean apply( ModelerWorkspace workspace, String field ) throws ModelerException {
    // TODO will only be factored in when modeling star schema
    return false;
  }

  @Override
  public boolean apply( Document schema, String field ) throws ModelerException {
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
    return BaseMessages.getString( MSG_CLASS, "Modeler.CreateDimensionKey.Summary", getName(), getDimension() );
  }


}
