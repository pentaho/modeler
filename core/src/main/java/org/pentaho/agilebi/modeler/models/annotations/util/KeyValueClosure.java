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


package org.pentaho.agilebi.modeler.models.annotations.util;

import java.io.Serializable;

/**
 * @author Rowell Belen
 */
public interface KeyValueClosure {

  void execute( String key, Serializable serializable );

}
