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
