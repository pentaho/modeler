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

package org.pentaho.agilebi.modeler.nodes.annotations;

/**
 * Implementations of this interface can be used as Data Type annotations.
 * 
 * User: nbaker Date: 10/18/11
 */
public interface IDataRoleAnnotation extends IMemberAnnotation {
  /**
   * Returns the String to be stored as the value of a Data.Role annotation
   * 
   * @return
   */
  String getDataType();
}
