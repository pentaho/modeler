/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.agilebi.modeler;

public class ModelerMessagesHolder {

  private static IModelerMessages modelerMessages = new DefaultModelerMessages();

  public static IModelerMessages getMessages() {
    return modelerMessages;
  }

  public static void setMessages( IModelerMessages messages ) {
    modelerMessages = messages;
  }

  private static class DefaultModelerMessages implements IModelerMessages {
    public String getString( String key, String... args ) {
      return key;
    }
  }

}
