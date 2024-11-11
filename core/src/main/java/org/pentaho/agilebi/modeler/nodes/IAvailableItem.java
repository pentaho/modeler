/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.nodes;

/**
 * Created: 4/11/11
 * 
 * @author rfellows
 */
public interface IAvailableItem {
  String getName();

  String getDisplayName();

  String getImage();

  String getClassname();

  default String getAltText() {
    throw new UnsupportedOperationException( "getAltText() not implemented" );
  }
}
