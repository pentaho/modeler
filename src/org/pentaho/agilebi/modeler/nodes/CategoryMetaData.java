package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.propforms.CategoryPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;

/**
 * Created: 3/18/11
 *
 * @author rfellows
 */
public class CategoryMetaData extends AbstractMetaDataModelNode<FieldMetaData> implements Serializable {

  private static final String IMAGE = "images/sm_folder_icon.png";
  private static final long serialVersionUID = 7879805619425103630L;
  String name;

  public CategoryMetaData() {
  }

  public CategoryMetaData(String name) {
    this.name = name;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public String getDisplayName() {
    return getName();
  }

  @Bindable
  public void setName( String name ) {
    if (!name.equals(this.name)) {
      String oldName = this.name;
      this.name = name;
      this.firePropertyChange("name", oldName, name); //$NON-NLS-1$
      this.firePropertyChange("displayName", oldName, name); //$NON-NLS-1$
      validateNode();
    }
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public void validate() {
    // make sure there is at least one field
    valid = true;
    this.validationMessages.clear();

    if (this.children.size() == 0) {
      valid = false;
      this.validationMessages.add("Categories require at least one Field");
    }
    for (AbstractMetaDataModelNode child : children) {
      valid &= child.isValid();
      this.validationMessages.addAll(child.getValidationMessages());
    }
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return CategoryPropertiesForm.class;
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  @Bindable
  public boolean isEditingDisabled() {
    return false;
  }

  @Override
  public void onAdd( FieldMetaData child ) {
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }

  @Override
  public void onRemove( FieldMetaData child ) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }

  @Override
  public boolean acceptsDrop(Object obj) {
    return obj instanceof AvailableField || obj instanceof FieldMetaData;
  }
}
