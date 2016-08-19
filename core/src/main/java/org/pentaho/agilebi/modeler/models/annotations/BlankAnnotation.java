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

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;

/**
 * @author Brandon Groves on 5/22/15.
 */
public class BlankAnnotation extends AnnotationType {
  private String field;

  @Override
  public boolean apply( ModelerWorkspace workspace, IMetaStore metaStore )
    throws ModelerException {
    return true;
  }

  @Override
  public boolean apply( Document schema ) throws ModelerException {
    return true;
  }

  @Override
  public void validate() throws ModelerException {

  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.BLANK;
  }

  @Override
  public String getSummary() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }
}
