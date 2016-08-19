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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.propforms.RelationalModelNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/18/11
 * 
 * @author rfellows
 */
public class RelationalModelNode extends AbstractMetaDataModelNode<CategoryMetaDataCollection> implements IRootModelNode {
  private static final String IMAGE = "images/sm_model_icon.png";
  private static final long serialVersionUID = 818429477176656590L;
  String name = "Untitled";

  private CategoryMetaDataCollection categories = new CategoryMetaDataCollection();

  private transient PropertyChangeListener listener;
  private ModelerWorkspace workspace;
  private static final String CLASSNAME = "pentaho-smallmodelbutton";

  public RelationalModelNode() {
    super( CLASSNAME );
    add( categories );
    setExpanded( true );
    categories.setExpanded( true );
  }

  public RelationalModelNode( ModelerWorkspace workspace ) {
    this();
    this.workspace = workspace;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public String getDisplayName() {
    return ModelerMessagesHolder.getMessages().getString( "Main.Model.Name.Template", getName() ); //$NON-NLS-1$
  }

  @Bindable
  public void setName( String name ) {
    if ( !name.equals( this.name ) ) {
      String oldName = this.name;
      String prevDisplay = getDisplayName();
      this.name = name;
      this.firePropertyChange( "name", oldName, this.name ); //$NON-NLS-1$
      this.firePropertyChange( "displayName", prevDisplay, getName() ); //$NON-NLS-1$
      validateNode();
    }
  }

  @Bindable
  public String getImage() {
    return IMAGE; //$NON-NLS-1$
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this ); //$NON-NLS-1$
  }

  @Override
  public void onAdd( CategoryMetaDataCollection child ) {
    child.addPropertyChangeListener( "children", getListener() ); //$NON-NLS-1$
    child.addPropertyChangeListener( "valid", validListener ); //$NON-NLS-1$
  }

  @Override
  public void onRemove( CategoryMetaDataCollection child ) {
    child.removePropertyChangeListener( getListener() );
    child.removePropertyChangeListener( validListener );
  }

  public CategoryMetaDataCollection getCategories() {
    return categories;
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public void validate() {
    valid = true;
    this.validationMessages.clear();

    if ( this.children.size() != 1 ) {
      valid = false;
      this.validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          "validation.relationalmodel.INVALID_STRUCTURE" ) );
    }
    for ( AbstractMetaDataModelNode child : children ) {
      valid &= child.isValid();
      this.validationMessages.addAll( child.getValidationMessages() );
    }
  }

  @Override
  public Class<RelationalModelNodePropertiesForm> getPropertiesForm() {
    return RelationalModelNodePropertiesForm.class;
  }

  @Bindable
  public boolean isEditingDisabled() {
    return true;
  }

  private PropertyChangeListener getListener() {
    if ( listener == null ) {
      listener = new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent evt ) {
          if ( !suppressEvents ) {
            fireCollectionChanged();
          }
        }
      };
    }
    return listener;
  }

  public void setSupressEvents( boolean suppress ) {
    super.setSupressEvents( suppress );
    if ( !suppress ) {
      firePropertyChange( "valid", !isValid(), isValid() );
    }
  }

  public boolean getSuppressEvents() {
    return suppressEvents;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return false;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    throw new ModelerException( new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString(
        "invalid_drop" ) ) );
  }

  @Override
  public ModelerWorkspace getWorkspace() {
    return workspace;
  }

  public void setWorkspace( ModelerWorkspace workspace ) {
    this.workspace = workspace;
  }
}
