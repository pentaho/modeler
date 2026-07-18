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



package org.pentaho.agilebi.modeler.nodes.annotations;

/**
 * 
 * Implementations of this interface can be used as Geo Type annotations.
 * 
 * User: nbaker Date: 10/18/11
 */
public interface IGeoRoleAnnotation extends IMemberAnnotation {
  /**
   * returns the String form of the Geo Role to be stored as the value of the Geo.Role annotation
   * 
   * @return
   */
  String getGeoName();
}
