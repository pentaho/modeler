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


package org.pentaho.agilebi.modeler.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class JoinTableModel extends XulEventSourceAdapter implements Serializable {
  private static final long serialVersionUID = -8964972286672462570L;
  private String name;
  private AbstractModelList<JoinFieldModel> fields;

  public JoinTableModel() {
    this.fields = new AbstractModelList<JoinFieldModel>();
  }

  @Bindable
  public String getName() {
    return this.name;
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  @Bindable
  public AbstractModelList<JoinFieldModel> getFields() {
    return this.fields;
  }

  @Bindable
  public void setFields( AbstractModelList<JoinFieldModel> fields ) {
    this.fields = fields;
  }

  public List<JoinFieldModel> processTableFields( List<String> fields ) {

    List<JoinFieldModel> fieldModels = new ArrayList<JoinFieldModel>();
    for ( String field : fields ) {
      JoinFieldModel fieldModel = new JoinFieldModel();
      fieldModel.setName( field );
      fieldModel.setParentTable( this );
      fieldModels.add( fieldModel );
    }
    return fieldModels;
  }

  public void reset() {
    fields.clear();
  }

  @Override
  public boolean equals( Object o ) {
    if ( o instanceof JoinTableModel == false ) {
      return false;
    }
    return ( (JoinTableModel) o ).getName().equals( this.getName() );
  }
}
