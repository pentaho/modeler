package org.pentaho.agilebi.modeler.debug;

import org.pentaho.di.i18n.BaseMessages;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created: 3/17/11
 *
 * @author rfellows
 */
public class ModelerMessages extends ResourceBundle {

  private static ResourceBundle lafBundle;
  private Class clz = this.getClass();

  public ModelerMessages(){
  }

  public ModelerMessages(Class pkg){
    this.clz = pkg;
  }

  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject(String key) {
    String result = BaseMessages.getString(clz, key);
    return result;
  }

}
