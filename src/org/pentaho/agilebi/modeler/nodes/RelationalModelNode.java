package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.agilebi.modeler.propforms.RelationalModelNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * Created: 3/18/11
 *
 * @author rfellows
 */
public class RelationalModelNode extends AbstractMetaDataModelNode<CategoryMetaData> implements Serializable {
  private static final String IMAGE = "images/sm_model_icon.png";
  private static final long serialVersionUID = 818429477176656590L;
  String name = "Untitled";
  private transient PropertyChangeListener listener;

  public RelationalModelNode() {
    setExpanded(true);
    add(new CategoryMetaData("Category 1"));
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public String getDisplayName() {
    return ModelerMessagesHolder.getMessages().getString("Main.Model.Name.Template", getName()); //$NON-NLS-1$
  }

  @Bindable
  public void setName( String name ) {
    if (!name.equals(this.name)) {
      String oldName = this.name;
      String prevDisplay = getDisplayName();
      this.name = name;
      this.firePropertyChange("name", oldName, this.name); //$NON-NLS-1$
      this.firePropertyChange("displayName", prevDisplay,
          getName()); //$NON-NLS-1$
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
    this.changeSupport.firePropertyChange("children", null, this); //$NON-NLS-1$
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public void validate() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return RelationalModelNodePropertiesForm.class;
  }
  
  @Bindable
  public boolean isEditingDisabled() {
    return true;
  }

  private PropertyChangeListener getListener(){
    if(listener == null){
      listener = new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent evt ) {
          fireCollectionChanged();
        }
      };
    }
    return listener;
  }

  public void setSupressEvents( boolean suppress ) {
    super.setSupressEvents(suppress);
    firePropertyChange("valid", !isValid(), isValid());

  }
}
