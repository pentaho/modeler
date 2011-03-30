package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created: 3/18/11
 *
 * @author rfellows
 */
public class CategoryPropertiesForm extends AbstractModelerNodeForm<CategoryMetaData> {

  private static final String ID = "categoryprops";

  private CategoryMetaData cat;
  private XulVbox messageBox;
  private XulLabel messageLabel;
  private XulTextbox name;

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (!evt.getPropertyName().equals("valid")) {
        return;
      }
      showValidations();
    }
  };


  public CategoryPropertiesForm() {
    super(ID);
  }

  public void setObject(CategoryMetaData categoryMetaData) {
    if (this.cat != null) {
      this.cat.removePropertyChangeListener(propListener);
    }
    this.cat = categoryMetaData;
    if (cat == null) {
      return;
    }
    cat.addPropertyChangeListener(propListener);
    name.setValue(cat.getName());
    showValidations();
  }


  public void init() {
    super.init();
    name = (XulTextbox) document.getElementById("category_name");
    messageBox = (XulVbox) document.getElementById("category_message");
    messageLabel = (XulLabel) document.getElementById("category_message_label");
    bf.createBinding(this, "name", name, "value");
  }

  @Bindable
  private void showValidations() {
    if (cat == null) {
      return;
    }
    messageLabel.setValue(cat.getValidationMessagesString());
    messageBox.setVisible(cat.getValidationMessages().size() > 0);
  }

  @Bindable
  public void setName( String name ) {
    if (cat != null) {
      cat.setName(name);
    }
    this.name.setValue(name);
  }

  @Bindable
  public String getName() {
    if (cat == null) {
      return null;
    }
    return cat.getName();
  }


}
