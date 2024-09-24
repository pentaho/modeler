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

public class IncompatibleModelerException extends ModelerException {

  private static final long serialVersionUID = 959187252916433860L;

  public IncompatibleModelerException() {
    super( "Not a valid Model" ); //$NON-NLS-1$
  }

}
