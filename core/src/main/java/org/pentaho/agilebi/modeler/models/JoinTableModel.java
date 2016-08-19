/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
