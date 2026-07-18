/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;

public class SpoonModelerMessages implements IModelerMessages {

  public String getString( String key, String... args ) {
    return BaseMessages.getString( ModelerWorkspace.class, key, args );
  }
}
