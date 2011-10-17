package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.propforms.MemberPropertyPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;

/**
 * User: rfellows
 * Date: 10/13/11
 * Time: 9:09 AM
 */
public class MemberPropertyMetaData extends BaseColumnBackedMetaData implements Serializable {

  private LevelMetaData parent;
  private static final String IMAGE = "images/sm_member_prop_icon.png";

  public MemberPropertyMetaData() {
    super();
  }

  public MemberPropertyMetaData(LevelMetaData parent, String name) {
    super(name);
    this.parent = parent;
  }

  @Bindable
  public String toString() {
    return "Member Property Name: " + name + "\nColumn Name: " + columnName;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return IMAGE;
  }

  public LevelMetaData getParent() {
    return parent;
  }

  public void setParent(LevelMetaData parent) {
    this.parent = parent;
  }

  @Override
  public boolean acceptsDrop(Object obj) {
    return false;
  }

  @Override
  public Object onDrop(Object data) throws ModelerException {
    throw new ModelerException(new IllegalArgumentException(ModelerMessagesHolder.getMessages().getString("invalid_drop")));
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
     return MemberPropertyPropertiesForm.class;
  }

  @Override
  public String getValidationMessageKey(String key) {
    return "validation.memberprop." + key;
  }

}
