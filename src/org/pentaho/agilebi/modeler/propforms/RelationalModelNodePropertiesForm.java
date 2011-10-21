package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulButton;
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
  private XulVbox messageBox;
  private XulLabel messageLabel;
  private XulButton messageBtn;

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
    if (getNode() == relationalModelNode) {
      return;
    }
    if (getNode() != null) {
      getNode().removePropertyChangeListener(propListener);
    }
    setNode(relationalModelNode);
    if (getNode() == null) {
      return;
    }
    ;
    name.setValue(getNode().getName());
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(getNode(), "name", name, "value");

    showValidations();
    getNode().addPropertyChangeListener(propListener);
  }

  private void showValidations() {
    if (getNode() != null) {
      messageBox.setVisible(getNode().getValidationMessages().size() > 0);
      setValidMessages(getNode().getValidationMessagesString());
    }
  }

  public void init(ModelerWorkspace workspace) {
    super.init(workspace);
    name = (XulTextbox) document.getElementById("relational_name");
    messageBox = (XulVbox) document.getElementById("relational_message");
    messageLabel = (XulLabel) document.getElementById("relational_message_label");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(this, "name", name, "value");
    bf.createBinding(this, "validMessages", messageLabel, "value", validMsgTruncatedBinding);
    messageBtn = (XulButton) document.getElementById("relational_message_btn");
    bf.createBinding(this, "validMessages", messageBtn, "visible", showMsgBinding);

  }

  @Bindable
  public void setName( String name ) {
    if (getNode() != null) {
      getNode().setName(name);
    }
    this.name.setValue(name);
  }

  @Bindable
  public String getName() {
    if (getNode() == null) {
      return null;
    }
    return getNode().getName();
  }

  @Override
  public String getValidMessages()  {
    if (getNode() != null) {
      return getNode().getValidationMessagesString();
    } else {
      return null;
    }
  }
}
