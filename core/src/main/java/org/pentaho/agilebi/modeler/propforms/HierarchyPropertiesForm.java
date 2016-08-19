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
