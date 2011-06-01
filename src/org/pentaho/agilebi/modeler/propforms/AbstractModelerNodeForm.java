/*
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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public abstract class AbstractModelerNodeForm<T> extends AbstractXulEventHandler implements ModelerNodePropertiesForm<T>{

  protected BindingFactory bf;
  protected XulDeck deck;
  protected XulVbox panel;
  private String id;
  protected T node;

  protected static BindingConvertor<String, String> validMsgTruncatedBinding = BindingConvertor.truncatedString(45);
  protected static BindingConvertor<String, Boolean> showMsgBinding = new ShowMessagesBindingConvertor(45);

  public AbstractModelerNodeForm(String panelId){
    this.id = panelId;
  }
  
  public void activate(T obj) {
    this.setObject(obj);
    deck.setSelectedIndex(deck.getChildNodes().indexOf(panel));
  }

  public void setBindingFactory(BindingFactory bf){
    this.bf = bf;
  }

  public void init() {
    deck = (XulDeck) document.getElementById("propertiesdeck");
    panel = (XulVbox) document.getElementById(id);
  }

  @Bindable
  public T getNode() {
    return node;
  }

  @Bindable
  public void setNode(T node) {
    this.node = node;
  }

  @Bindable
  public void setValidMessages( String validMessages ) {
    this.firePropertyChange("validMessages", null, validMessages);
  }

  @Bindable
  public abstract String getValidMessages();

  private static class ShowMessagesBindingConvertor extends BindingConvertor<String, Boolean> {
    int length = 100;
    public ShowMessagesBindingConvertor(int length) {
      this.length = length;
    }
    public Boolean sourceToTarget(String value) {
      return value.length() > length;
    }

    public String targetToSource(Boolean value) {
      return "";
    }
  }

}
