package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.ui.xul.binding.Binding;
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
public class RelationalModelNodePropertiesForm extends AbstractModelerNodeForm<RelationalModelNode> {

  private XulTextbox name;
  private RelationalModelNode relationalModel;
  private XulVbox messageBox;
  private XulLabel messageLabel;

  public RelationalModelNodePropertiesForm() {
    super("relationalmodelprops");
  }

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if (!evt.getPropertyName().equals("valid")) {
        return;
      }
      showValidations();
    }
  };

  public void setObject(RelationalModelNode relationalModelNode) {
    if (this.relationalModel == relationalModelNode) {
      return;
    }
    if (this.relationalModel != null) {
      this.relationalModel.removePropertyChangeListener(propListener);
    }
    this.relationalModel = relationalModelNode;
    if (relationalModel == null) {
      return;
    }
    ;
    name.setValue(relationalModel.getName());
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(relationalModel, "name", name, "value");

    showValidations();
    relationalModel.addPropertyChangeListener(propListener);
  }

  private void showValidations() {
    if (relationalModel != null) {
      messageLabel.setValue(relationalModel.getValidationMessagesString());
      messageBox.setVisible(relationalModel.getValidationMessages().size() > 0);
    }
  }

  public void init() {
    super.init();
    name = (XulTextbox) document.getElementById("relational_name");
    messageBox = (XulVbox) document.getElementById("relational_message");
    messageLabel = (XulLabel) document.getElementById("relational_message_label");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(this, "name", name, "value");
  }

  @Bindable
  public void setName( String name ) {
    if (relationalModel != null) {
      relationalModel.setName(name);
    }
    this.name.setValue(name);
  }

  @Bindable
  public String getName() {
    if (relationalModel == null) {
      return null;
    }
    return relationalModel.getName();
  }

}
