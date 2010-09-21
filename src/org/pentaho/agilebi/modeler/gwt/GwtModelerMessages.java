package org.pentaho.agilebi.modeler.gwt;

import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;

/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public class GwtModelerMessages implements IModelerMessages {
  private ResourceBundle messages;
  public GwtModelerMessages( ResourceBundle messages ){
    this.messages = messages;
  }
  public String getString( String key, String... args ) {
    return messages.getString(key, key, args);
  }
}
