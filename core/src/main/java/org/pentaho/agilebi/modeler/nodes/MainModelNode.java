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
import org.pentaho.agilebi.modeler.propforms.MainModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MainModelNode extends AbstractMetaDataModelNode<AbstractMetaDataModelNode> implements IRootModelNode {

  private static final long serialVersionUID = 2399128598598210134L;

  String name = "Untitled"; // BaseMessages.getString(ModelerWorkspace.class, "Main.Model.Name.Untitled");

  private MeasuresCollection measures = new MeasuresCollection();
  private DimensionMetaDataCollection dimensions = new DimensionMetaDataCollection();

  private transient PropertyChangeListener listener;
  private ModelerWorkspace workspace;
  private static final String CLASSNAME = "pentaho-smallmodelbutton";

  public MainModelNode() {
    super( CLASSNAME );
    add( measures );
    add( dimensions );
    setExpanded( true );
    dimensions.setExpanded( true );
  }

  public MainModelNode( ModelerWorkspace workspace ) {
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
    // return ModelerController.MESSAGES.getString("Main.Model.Name.Template", getName());
  }

  @Bindable
  public void setName( String name ) {
    if ( !name.equals( this.name ) ) {
      String oldName = this.name;
      String prevDisplay = getDisplayName();
      this.name = name;
      this.firePropertyChange( "name", oldName, this.name ); //$NON-NLS-1$
      this.firePropertyChange( "displayName", prevDisplay, getName() ); //BaseMessages.getString(ModelerWorkspace.class, "Main.Model.Name.Template", getName())); //$NON-NLS-1$
      validateNode();
    }
  }

  @Bindable
  public String getImage() {
    return "images/sm_model_icon.png"; //$NON-NLS-1$
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this ); //$NON-NLS-1$
  }

  @Override
  public void onAdd( AbstractMetaDataModelNode child ) {
    child.addPropertyChangeListener( "children", getListener() ); //$NON-NLS-1$
    child.addPropertyChangeListener( "valid", validListener ); //$NON-NLS-1$
  }

  @Override
  public void onRemove( AbstractMetaDataModelNode child ) {
    child.removePropertyChangeListener( getListener() );
    child.removePropertyChangeListener( validListener );
  }

  public DimensionMetaDataCollection getDimensions() {
    return dimensions;
  }

  public MeasuresCollection getMeasures() {
    return measures;
  }

  @Bindable
  public boolean isEditingDisabled() {
    return true;
  }

  @Override
  public Class<MainModelerNodePropertiesForm> getPropertiesForm() {
    return MainModelerNodePropertiesForm.class;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return getImage();
  }

  @Override
  public void validate() {
    valid = true;
    this.validationMessages.clear();

    if ( "".equals( this.getName() ) ) {
      valid = false;
      this.validationMessages.add( "Node is emtpy" ); //BaseMessages.getString(ModelerWorkspace.class, "MainModelNode.ModelNameEmpty")); //$NON-NLS-1$
    }

    if ( this.children.size() != 2 ) {
      valid = false;
      this.validationMessages.add( "Invalid Structure" ); //BaseMessages.getString(ModelerWorkspace.class, "MainModelNode.ModelStructureInvalid")); //$NON-NLS-1$
    }
    for ( AbstractMetaDataModelNode child : children ) {
      valid &= child.isValid();
      this.validationMessages.addAll( child.getValidationMessages() );
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( dimensions == null ) ? 0 : dimensions.hashCode() );
    result = prime * result + ( ( listener == null ) ? 0 : listener.hashCode() );
    result = prime * result + ( ( measures == null ) ? 0 : measures.hashCode() );
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    return result;
  }

  public boolean equals( MainModelNode obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    MainModelNode other = obj;
    if ( dimensions == null ) {
      if ( other.dimensions != null ) {
        return false;
      }
    } else if ( !dimensions.equals( other.dimensions ) ) {
      return false;
    }
    if ( listener == null ) {
      if ( other.listener != null ) {
        return false;
      }
    } else if ( !listener.equals( other.listener ) ) {
      return false;
    }
    if ( measures == null ) {
      if ( other.measures != null ) {
        return false;
      }
    } else if ( !measures.equals( other.measures ) ) {
      return false;
    }
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
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
