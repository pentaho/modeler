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

  public ModelerMessages() {
  }

  public ModelerMessages( Class pkg ) {
    this.clz = pkg;
  }

  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject( String key ) {
    String result = BaseMessages.getString( clz, key );
    return result;
  }

}
