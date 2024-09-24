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

package org.pentaho.agilebi.modeler.gwt;

import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;

/**
 * User: nbaker Date: Jul 14, 2010
 */
public class GwtModelerMessages implements IModelerMessages {
  private ResourceBundle messages;

  public GwtModelerMessages( ResourceBundle messages ) {
    this.messages = messages;
  }

  public String getString( String key, String... args ) {
    return messages.getString( key, key, args );
  }
}
