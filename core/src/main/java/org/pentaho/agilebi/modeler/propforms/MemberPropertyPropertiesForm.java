/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
