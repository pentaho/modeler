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

package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

public class HierarchyPropertiesForm extends AbstractModelerNodeForm<HierarchyMetaData> {
  private static final long serialVersionUID = -4214767259175215383L;
  private XulTextbox name;
  private XulVbox messageBox;
  private XulLabel messageLabel;
  private XulButton messageBtn;

  public HierarchyPropertiesForm() {
    super( "hierarchyprops" );
  }

  public void setObject( HierarchyMetaData dim ) {
    setNode( dim );
    if ( getNode() == null ) {
      return;
    }
    name.setValue( dim.getName() );
    showValidations();
  }

  private void showValidations() {
    if ( getNode() == null ) {
      return;
    }
    messageBox.setVisible( getNode().getValidationMessages().size() > 0 );
    setValidMessages( getNode().getValidationMessagesString() );

  }

  public void init( ModelerWorkspace workspace ) {
    super.init( workspace );
    name = (XulTextbox) document.getElementById( "hierarchy_name" );
    messageBox = (XulVbox) document.getElementById( "hierarchy_message" );
    messageLabel = (XulLabel) document.getElementById( "hierarchy_message_label" );
    bf.createBinding( this, "name", name, "value" );
    bf.createBinding( this, "validMessages", messageLabel, "value", validMsgTruncatedBinding );
    messageBtn = (XulButton) document.getElementById( "hierarchy_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );

  }

  @Bindable
  public void setName( String name ) {
    if ( getNode() != null ) {
      getNode().setName( name );
    }
    this.name.setValue( name );
  }

  @Bindable
  public String getName() {
    if ( getNode() == null ) {
      return null;
    }
    return getNode().getName();
  }

  @Override
  public String getValidMessages() {
    if ( getNode() != null ) {
      return getNode().getValidationMessagesString();
    } else {
      return null;
    }
  }

}
