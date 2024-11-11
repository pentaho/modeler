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

/**
 * User: nbaker Date: 4/19/11
 */
public interface IDropTarget {

  Object onDrop( Object data ) throws ModelerException;
}
