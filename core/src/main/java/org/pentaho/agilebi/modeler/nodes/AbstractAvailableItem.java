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
