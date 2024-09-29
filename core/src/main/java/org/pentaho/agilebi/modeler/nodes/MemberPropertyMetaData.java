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


package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerException;
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
    return null;
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
