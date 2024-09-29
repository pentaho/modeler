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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DimensionPropertiesForm extends AbstractModelerNodeForm<DimensionMetaData> {
  private static final long serialVersionUID = 7467855499137361995L;
  private XulTextbox name;
  private XulCheckbox isTimeDim;
  private XulVbox messageBox;
  private XulLabel messageLabel;

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( !evt.getPropertyName().equals( "valid" ) ) {
        return;
      }
      showValidations();
    }
  };
  private XulButton messageBtn;

  public DimensionPropertiesForm() {
    super( "dimensionprops" );
  }

  public void setObject( DimensionMetaData dim ) {
    if ( getNode() != null ) {
      getNode().removePropertyChangeListener( propListener );
    }
    setNode( dim );
    if ( dim == null ) {
      return;
    }
    dim.addPropertyChangeListener( propListener );
    name.setValue( dim.getName() );
    isTimeDim.setChecked( dim.isTimeDimension() );
    showValidations();
  }

  @Bindable
  private void showValidations() {
    if ( getNode() == null ) {
      return;
    }
    messageBox.setVisible( getNode().getValidationMessages().size() > 0 );
    setValidMessages( getNode().getValidationMessagesString() );

  }

  public void init( ModelerWorkspace workspace ) {
    super.init( workspace );
    name = (XulTextbox) document.getElementById( "dimension_name" );
    bf.createBinding( this, "name", name, "value" );
    isTimeDim = (XulCheckbox) document.getElementById( "is_time_dimension" );
    bf.createBinding( this, "timeDimension", isTimeDim, "checked" );
    messageBox = (XulVbox) document.getElementById( "dimension_message" );
    messageLabel = (XulLabel) document.getElementById( "dimension_message_label" );
    bf.createBinding( this, "validMessages", messageLabel, "value", validMsgTruncatedBinding );
    messageBtn = (XulButton) document.getElementById( "dimension_message_btn" );
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

  @Bindable
  public void setTimeDimension( boolean timeDimension ) {
    if ( getNode() != null ) {
      getNode().setTimeDimension( timeDimension );
    }
    if ( timeDimension == isTimeDim.isChecked() ) {
      return;
    }
    isTimeDim.setChecked( timeDimension );
  }

  @Bindable
  public boolean isTimeDimension() {
    if ( getNode() == null ) {
      return false;
    }
    return getNode().isTimeDimension();
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
