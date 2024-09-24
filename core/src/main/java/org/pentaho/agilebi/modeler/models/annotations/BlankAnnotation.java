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
