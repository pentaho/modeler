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

import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.di.core.database.DatabaseMeta;

/**
 * User: nbaker Date: Sep 21, 2010
 */
public interface ISpoonModelerSource extends IModelerSource {
  DatabaseMeta getDatabaseMeta();
}
