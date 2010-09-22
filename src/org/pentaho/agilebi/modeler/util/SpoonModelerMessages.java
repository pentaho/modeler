package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;


public class SpoonModelerMessages implements IModelerMessages {

  public String getString(String key, String... args) {
    return BaseMessages.getString(ModelerWorkspace.class, key, args);
  }
}
