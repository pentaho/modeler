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

/**
 * An interface for objects that can open a url. This is useful for popping up (infocenter) help pages.
 * 
 * @author rbouman
 * 
 */
public interface IUriHandler {

  public void openUri( String uri );

}
