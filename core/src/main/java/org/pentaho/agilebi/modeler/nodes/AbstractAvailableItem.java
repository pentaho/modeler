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
 * Copyright (c) 2002-2023 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * Created: 4/12/11
 * 
 * @author rfellows
 */
public class AbstractAvailableItem<T extends IAvailableItem> extends AbstractModelNode<T> implements XulEventSource,
    IAvailableItem {

  private static final long serialVersionUID = 2938604837324271097L;
  private boolean expanded = true;
  private String name;
  private String image;
  private String classname;
  private String altText;

  @Bindable
  public boolean isExpanded() {
    return expanded;
  }

  @Bindable
  public void setExpanded( boolean expanded ) {
    this.expanded = expanded;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return name;
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  @Bindable
  public String getImage() {
    return image;
  }

  @Bindable
  public void setImage( String image ) {
    this.image = image;
  }

  @Bindable
  public String getClassname() {
    return this.classname;
  }

  @Bindable
  public void setClassname( String classname ) {
    this.classname = classname;
  }

  @Bindable
  public String getAltText() {
    return this.altText;
  }

  @Bindable
  public void setAltText( String altText ) {
    this.altText = altText;
  }

  /**
   * Retrieve <code>propertyName</code> from modeler message property and then set alternative text.
   * @param propertyName
   */
  protected void getMessageStringAndSetAltText( String propertyName ) {
    setAltText( getMessageString( propertyName) );
  }

  /**
   * Retrieve <code>propertyName</code> from modeler message property.
   * @param propertyName
   * @param defaultValue
   * @return value of property if <code>propertyName</code> is present, if not then <code>defaultValue</code>
   */
  protected String getMessageString( String propertyName, String defaultValue ) {
    return ModelerMessagesHolder.getMessages().getString( propertyName , defaultValue );
  }

  /**
   * Retrieve <code>propertyName</code> from modeler message property.
   * @param propertyName
   * @return value of property if <code>propertyName</code> is present, if not then ""
   */
  protected String getMessageString( String propertyName ) {
    return getMessageString( propertyName, "" ); // default is empty string
  }
}
