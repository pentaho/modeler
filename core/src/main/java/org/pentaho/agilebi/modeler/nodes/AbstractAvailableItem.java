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

package org.pentaho.agilebi.modeler.nodes;

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
}
