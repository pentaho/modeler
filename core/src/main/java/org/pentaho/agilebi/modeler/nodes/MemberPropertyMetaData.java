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

package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.MemberPropertyPropertiesForm;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * User: rfellows Date: 10/13/11 Time: 9:09 AM
 */
public class MemberPropertyMetaData extends BaseColumnBackedMetaData<MemberPropertyMetaData> {
  private static final long serialVersionUID = 1416688972721247836L;
  private static final String IMAGE = "images/sm_member_prop_icon.png";

  public MemberPropertyMetaData() {
    super();
  }

  public MemberPropertyMetaData( LevelMetaData parent, String name ) {
    super( name );
    setParent( parent );
  }

  @Bindable
  public String toString() {
    return "Member Property Name: " + name + "\nColumn Name: " + columnName;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return false;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    throw new ModelerException( new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString(
        "invalid_drop" ) ) );
  }

  @Override
  public Class<MemberPropertyPropertiesForm> getPropertiesForm() {
    return MemberPropertyPropertiesForm.class;
  }

  @Override
  public String getValidationMessageKey( String key ) {
    return "validation.memberprop." + key;
  }

  @Override
  public IPhysicalTable getTableRestriction() {
    // restrict to the table of the parent Level
    if ( ( (LevelMetaData) getParent() ).getLogicalColumn() != null ) {
      return ( (LevelMetaData) getParent() ).getLogicalColumn().getPhysicalColumn().getPhysicalTable();
    }
    return ( (LevelMetaData) getParent() ).getTableRestriction();
  }
}
