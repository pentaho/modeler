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
import org.pentaho.ui.xul.XulServiceCallback;

/**
 * User: nbaker Date: Jun 23, 2010
 */
public interface IModelerServiceAsync {
  void generateDomain( String connectionName, String tableName, String dbType, String query, String datasourceName,
      XulServiceCallback<Domain> callback );

  void serializeModels( Domain domain, String name, XulServiceCallback<String> callback );

  void loadDomain( String id, XulServiceCallback<Domain> callback );

  void gwtWorkaround( BogoPojo pojo, XulServiceCallback<BogoPojo> callback );

  void serializeModels( Domain domain, String modelName,
                        boolean doOlap, XulServiceCallback<String> xulServiceCallback );
}
