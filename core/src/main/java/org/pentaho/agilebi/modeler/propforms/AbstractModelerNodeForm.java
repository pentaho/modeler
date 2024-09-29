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
import org.pentaho.ui.xul.XulContainer;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public abstract class AbstractModelerNodeForm<T> extends AbstractXulEventHandler implements
    ModelerNodePropertiesForm<T> {
  private static final long serialVersionUID = -3087453988349932488L;
  protected BindingFactory bf;
  protected XulDeck deck;
  protected XulVbox panel;
  private String id;
  protected T node;

  protected static BindingConvertor<String, String> validMsgTruncatedBinding = BindingConvertor.truncatedString( 45 );
  protected static BindingConvertor<String, Boolean> showMsgBinding = new ShowMessagesBindingConvertor( 45 );
  protected ModelerWorkspace workspace;

  /**
   * This will listen for changes to all property values except for validation messages and flag the model as dirty.
   */
  protected PropertyChangeListener propertyValueListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( !( "validMessages".equals( evt.getPropertyName() ) || "valid".equals( evt.getPropertyName() ) ) ) {
        // Set the model's dirty flag to true when anything except valid messages are changed
        workspace.setDirty( true );
      }
    }
  };

  public AbstractModelerNodeForm( String panelId ) {
    this.id = panelId;
  }

  public void activate( T obj ) {
    this.setObject( obj );
    deck.setSelectedIndex( deck.getChildNodes().indexOf( panel ) );
  }

  public void setBindingFactory( BindingFactory bf ) {
    this.bf = bf;
  }

  /**
   * Utility method to show/hide a container with specific components appropriate only in particular circumstances. For
   * example: only time dimension levels need time level type and format, but not geo type
   * 
   * @param containerId
   * @param visible
   */
  protected void setContainerVisible( String containerId, boolean visible ) {
    XulContainer container = (XulContainer) document.getElementById( containerId );
    container.setVisible( visible );
  }

  public void init( ModelerWorkspace workspace ) {
    this.workspace = workspace;
    deck = (XulDeck) document.getElementById( "propertiesdeck" );
    panel = (XulVbox) document.getElementById( id );
  }

  @Bindable
  public T getNode() {
    return node;
  }

  @Bindable
  public void setNode( T node ) {
    if ( this.node instanceof XulEventSource ) {
      ( (XulEventSource) this.node ).removePropertyChangeListener( propertyValueListener );
    }
    if ( node instanceof XulEventSource ) {
      ( (XulEventSource) node ).addPropertyChangeListener( propertyValueListener );
    }
    this.node = node;
  }

  @Bindable
  public void setValidMessages( String validMessages ) {
    this.firePropertyChange( "validMessages", null, validMessages );
  }

  @Bindable
  public abstract String getValidMessages();

  private static class ShowMessagesBindingConvertor extends BindingConvertor<String, Boolean> {
    int length = 100;

    public ShowMessagesBindingConvertor( int length ) {
      this.length = length;
    }

    public Boolean sourceToTarget( String value ) {
      return value.length() > length;
    }

    public String targetToSource( Boolean value ) {
      return "";
    }
  }

}
