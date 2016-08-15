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
import org.pentaho.agilebi.modeler.nodes.BaseColumnBackedMetaData;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * User: rfellows Date: 10/13/11 Time: 1:29 PM
 */
public class MemberPropertyPropertiesForm extends LevelsPropertiesForm {
  private static final long serialVersionUID = 7028408198072291535L;

  private static final String ID = "memberprops";

  private XulTextbox desc;

  public MemberPropertyPropertiesForm( String panelId, String locale ) {
    super( panelId, locale );
  }

  public MemberPropertyPropertiesForm( String locale ) {
    super( ID, locale );
  }

  public void init( ModelerWorkspace workspace ) {
    this.workspace = workspace;
    deck = (XulDeck) document.getElementById( "propertiesdeck" );
    panel = (XulVbox) document.getElementById( ID );

    bf.createBinding( this, "notValid", "memberprops_message", "visible" );
    name = (XulTextbox) document.getElementById( "memberprops_name" );
    sourceLabel = (XulLabel) document.getElementById( "memberprops_source_col" );
    level_message_label = (XulLabel) document.getElementById( "memberprops_message_label" );
    messageBox = (XulVbox) document.getElementById( "memberprops_message" );
    desc = (XulTextbox) document.getElementById( "memberprops_desc" );

    bf.createBinding( this, "backingColumnAvailable", "fixLevelColumnsBtn", "!visible" );

    bf.createBinding( this, "columnName", sourceLabel, "value" );
    bf.createBinding( this, "name", name, "value" );
    bf.createBinding( this, "validMessages", level_message_label, "value", validMsgTruncatedBinding );
    messageBtn = (XulButton) document.getElementById( "memberprops_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );
    bf.createBinding( this, "description", desc, "value" );
  }

  @Bindable
  public void setName( String name ) {
    if ( getNode() != null ) {
      getNode().setName( name );
      if ( getNode().getLogicalColumn() != null ) {
        getNode().getLogicalColumn().setName( new LocalizedString( locale, name ) );
      }
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

  @Bindable
  public void setDescription( String desc ) {
    if ( getNode() != null ) {
      getNode().setDescription( desc );
      if ( getNode().getLogicalColumn() != null ) {
        getNode().getLogicalColumn().setDescription( new LocalizedString( locale, desc ) );
      }
    }
    this.desc.setValue( desc );
  }

  @Bindable
  public String getDescription() {
    if ( getNode() == null ) {
      return null;
    }
    return getNode().getDescription();
  }

  @Override
  public void setObject( BaseColumnBackedMetaData dim ) {
    if ( getNode() != null ) {
      getNode().removePropertyChangeListener( validListener );
    }

    setNode( dim );
    if ( dim == null ) {
      return;
    }
    getNode().addPropertyChangeListener( validListener );

    name.setValue( dim.getName() );
    setColumnName( getColumnNameFromLogicalColumn( dim.getLogicalColumn() ) );
    desc.setValue( dim.getDescription() );
    showValidations();
  }
}
