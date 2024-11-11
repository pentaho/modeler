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


package org.pentaho.agilebi.modeler.gwt.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.metadata.model.Domain;

/**
 * Created by IntelliJ IDEA. User: nbaker Date: Jun 18, 2010 Time: 4:13:28 PM To change this template use File |
 * Settings | File Templates.
 */
public interface IGwtModelerServiceAsync {
  void generateDomain( String connectionName, String tableName, String dbType, String query, String datasourceName,
      AsyncCallback<Domain> domain );

  /**
   * This is a method for the Gwt workaround. This should not be used by any client at all
   * 
   * @return BogoPojo
   */
  void gwtWorkaround( BogoPojo pojo, AsyncCallback<BogoPojo> callback );

  void serializeModels( Domain domain, String name, AsyncCallback<String> callback );

  void serializeModels( Domain domain, String name, boolean doOlap, AsyncCallback<String> callback );

  void loadDomain( String domainId, AsyncCallback<Domain> callback );
}
