package org.pentaho.agilebi.modeler;

public class ModelerMessagesHolder {

  private static IModelerMessages modelerMessages;

  public static IModelerMessages getMessages() {
    if (modelerMessages == null) {
      throw new IllegalStateException("IModelerMessages is not set"); //$NON-NLS-1$
    }
    return modelerMessages;
  }

  public static void setMessages(IModelerMessages messages) {
    if (modelerMessages != null) {
      throw new IllegalStateException("IModelerMessages is already set"); //$NON-NLS-1$
    }
    modelerMessages = messages;
  }

}
