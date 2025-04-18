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


package org.pentaho.agilebi.modeler.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.agilebi.modeler.IDropTarget;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelNode;

public abstract class AbstractMetaDataModelNode<T extends AbstractMetaDataModelNode> extends AbstractModelNode<T>
    implements IDropTarget {

  private static final long serialVersionUID = 1547202580713108254L;

  protected boolean valid = true;
  protected transient Set<String> validationMessages = new HashSet<String>();

  protected String image;
  protected boolean suppressEvents;
  protected boolean expanded;
  protected DataRole dataRole;
  protected Map<String, IMemberAnnotation> annotations = new AnnotationMap();

  protected String classname;
  protected String validClassname;
  protected String invalidClassname = "pentaho-warningbutton";
  protected String altText;

  protected transient PropertyChangeListener validListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent arg0 ) {
      validateNode();
    }
  };

  protected transient PropertyChangeListener nameListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent arg0 ) {
      validateNode();
    }
  };

  protected transient PropertyChangeListener childrenListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent evt ) {
      fireCollectionChanged();
    }
  };

  public AbstractMetaDataModelNode( String classname ) {
    this.image = getInvalidImage();
    this.classname = classname;
    this.validClassname = classname;
  }

  public AbstractMetaDataModelNode( String classname, String altText ) {
    this.image = getInvalidImage();
    this.classname = classname;
    this.validClassname = classname;
    this.altText = altText;
  }

  @Override
  public void onAdd( T child ) {
    child.addPropertyChangeListener( "name", nameListener );
    child.addPropertyChangeListener( "valid", validListener );
    child.addPropertyChangeListener( "children", childrenListener );
    validateTree();
  }

  @Override
  public void onRemove( T child ) {
    child.removePropertyChangeListener( validListener );
    child.removePropertyChangeListener( nameListener );
    child.removePropertyChangeListener( childrenListener );
    validateNode();
  }

  @Bindable
  public String getValidationMessagesString() {
    StringBuilder str = new StringBuilder(); //$NON-NLS-1$
    int i = 0;
    for ( String msg : validationMessages ) {
      if ( i > 0 ) {
        str.append( ", " ); //$NON-NLS-1$
      }
      str.append( msg );
      i++;
    }
    return str.toString();
  }

  @Bindable
  public void setDataRole( DataRole dataRole ) {
    if ( this.dataRole == null && dataRole == null ) {
      return;
    }
    if ( this.dataRole != null && dataRole != null && this.dataRole.equals( dataRole ) ) {
      return;
    }
    DataRole oldDataRole = this.dataRole;
    this.dataRole = dataRole;
    if ( suppressEvents ) {
      return;
    }
    firePropertyChange( "dataRole", oldDataRole, dataRole );
    validateNode();
  }

  @Bindable
  public DataRole getDataRole() {
    return dataRole;
  }

  @Bindable
  public Set<String> getValidationMessages() {
    return validationMessages;
  }

  @Override
  protected void fireCollectionChanged() {
    if ( this.suppressEvents == false ) {
      super.fireCollectionChanged();
    }
  }

  @Bindable
  public void setImage( String image ) {
    if ( this.image == null || !this.image.equals( image ) ) {
      String oldimg = this.image;
      this.image = image;
      if ( suppressEvents == false ) {
        this.firePropertyChange( "image", oldimg, image ); //$NON-NLS-1$
      }
    }
  }

  public abstract String getValidImage();

  @Bindable
  public final String getInvalidImage() {
    return "images/warning.png"; //$NON-NLS-1$
  }

  @Bindable
  public String getImage() {
    return ( this.valid ) ? getValidImage() : getInvalidImage();
  }

  @Bindable
  public String getClassname() {
    return this.classname;
  }

  @Bindable
  public void setClassname( String classname ) {
    if ( this.classname == null || !this.classname.equals( classname ) ) {
      String oldClassname = this.classname;
      this.classname = classname;
      if ( !suppressEvents ) {
        this.firePropertyChange( "classname", oldClassname, classname ); //$NON-NLS-1$
      }
    }
  }

  @Bindable
  public String getAltText() {
    return altText;
  }

  @Bindable
  public void setAltText( String altText ) {
    if ( this.altText == null || !this.altText.equals( altText ) ) {
      String oldAltText = this.altText;
      this.altText = altText;
      if ( !suppressEvents ) {
        this.firePropertyChange( "altText", oldAltText, altText ); //$NON-NLS-1$
      }
    }
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

  public abstract void validate();

  public void validateNode() {
    boolean prevValid = valid;
    String prevMessages = getValidationMessagesString();

    validate();

    for ( IMemberAnnotation anno : annotations.values() ) {
      if ( anno == null ) {
        continue;
      }
      valid &= anno.isValid( this );
      List<String> messages = anno.getValidationMessages( this );
      if ( messages != null ) {
        validationMessages.addAll( messages );
      }
    }

    if ( suppressEvents == false ) {
      this.firePropertyChange( "validationMessagesString", prevMessages, getValidationMessagesString() );
      this.firePropertyChange( "valid", prevValid, valid );
    }

    if ( valid ) {
      setImage( getValidImage() );
      setClassname( this.validClassname );
    } else {
      setImage( getInvalidImage() );
      setClassname( this.invalidClassname );
    }

    if ( prevValid != valid ) {
      // changing of one element could cause others to become valid or invalid
      AbstractModelNode root = getRoot();
      if ( root != null && root instanceof AbstractMetaDataModelNode ) {
        AbstractMetaDataModelNode rootNode = (AbstractMetaDataModelNode) root;
        rootNode.validateTree();
      }
    }
  }

  public void validateTree() {
    for ( T t : this ) {
      ( (AbstractMetaDataModelNode) t ).validateTree();
    }
    validateNode();
  }

  public boolean isTreeValid() {
    if ( !isValid() ) {
      return false;
    }
    for ( T t : this ) {
      if ( !( (AbstractMetaDataModelNode) t ).isValid() ) {
        return false;
      }
    }
    return true;
  }

  public boolean isValid() {
    return valid;
  }

  public void invalidate() {
    boolean prevValid = this.valid;
    this.valid = false;
    if ( suppressEvents == false ) {
      this.firePropertyChange( "valid", prevValid, valid );
    }
  }

  public abstract Class<? extends ModelerNodePropertiesForm> getPropertiesForm();

  public void setSupressEvents( boolean suppress ) {
    this.suppressEvents = suppress;
    for ( T child : this ) {
      child.setSupressEvents( suppress );
    }
  }

  @Bindable
  public boolean isExpanded() {
    return expanded;
  }

  @Bindable
  public void setExpanded( boolean expanded ) {
    this.expanded = expanded;
  }

  public abstract boolean acceptsDrop( Object obj );

  private ModelerWorkspace workspace;

  public ModelerWorkspace getWorkspace() {
    if ( workspace == null ) {
      AbstractModelNode parent = getRoot();
      if ( parent != null ) {
        workspace = ( (IRootModelNode) parent ).getWorkspace();
      }
    }
    return workspace;
  }

  protected AbstractModelNode getRoot() {
    AbstractModelNode parent = this.getParent();
    while ( parent != null ) {
      if ( parent.getParent() == null ) {
        break;
      }
      parent = parent.getParent();
    }
    return parent;
  }

  public Map<String, IMemberAnnotation> getMemberAnnotations() {
    return annotations;
  }

  private class AnnotationMap extends HashMap<String, IMemberAnnotation> {
    private static final long serialVersionUID = -578588442907941576L;

    @Override
    public IMemberAnnotation put( String s, IMemberAnnotation iMemberAnnotation ) {
      IMemberAnnotation prevVal = get( s );
      if ( prevVal != null && prevVal != iMemberAnnotation ) {
        prevVal.onDetach( AbstractMetaDataModelNode.this );
      }
      if ( ( prevVal == null || prevVal != iMemberAnnotation ) && iMemberAnnotation != null ) {
        iMemberAnnotation.onAttach( AbstractMetaDataModelNode.this );
      }
      return super.put( s, iMemberAnnotation );
    }

    @Override
    public void putAll( Map<? extends String, ? extends IMemberAnnotation> map ) {
      for ( String s : map.keySet() ) {
        IMemberAnnotation prevVal = get( s );
        if ( prevVal != null && prevVal != map.get( s ) ) {
          prevVal.onDetach( AbstractMetaDataModelNode.this );
        }
        if ( prevVal == null || prevVal != map.get( s ) && map.get( s ) != null ) {
          map.get( s ).onAttach( AbstractMetaDataModelNode.this );
        }
      }
      super.putAll( map );
    }

    @Override
    public IMemberAnnotation remove( Object o ) {
      if ( o instanceof IMemberAnnotation ) {
        ( (IMemberAnnotation) o ).onDetach( AbstractMetaDataModelNode.this );
      }
      return super.remove( o );
    }
  }

}
