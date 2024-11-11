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


package org.pentaho.agilebi.modeler.services;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.metadata.model.Domain;

/**
 * User: nbaker Date: Jun 18, 2010
 */
public interface IModelerService {
  Domain generateDomain( String connectionName, String tableName, String dbType, String query, String datasourceName )
    throws Exception;

  BogoPojo gwtWorkaround( BogoPojo pojo );

  String serializeModels( Domain domain, String name ) throws Exception;

  String serializeModels( Domain domain, String name, boolean doOlap ) throws Exception;

  Domain loadDomain( String id ) throws Exception;
}
