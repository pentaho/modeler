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



package org.pentaho.agilebi.modeler;

import java.io.Serializable;

public class ModelerException extends Exception implements Serializable {
  private static final long serialVersionUID = 3088017235255752108L;

  public ModelerException() {
  }

  public ModelerException( String msg ) {
    super( msg );
  }

  public ModelerException( String msg, Throwable t ) {
    super( msg, t );
  }

  public ModelerException( Throwable t ) {
    super( t );
  }
}
