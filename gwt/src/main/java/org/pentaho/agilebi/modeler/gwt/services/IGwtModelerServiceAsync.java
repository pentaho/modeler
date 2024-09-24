/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

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
