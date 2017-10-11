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

package org.pentaho.agilebi.modeler.services.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerService;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

/**
 * User: nbaker Date: Jun 18, 2010
 */
public class GwtModelerServiceImpl implements IModelerServiceAsync {
  IGwtModelerServiceAsync delegate;

  public void gwtWorkaround( BogoPojo bogo, final XulServiceCallback<BogoPojo> callback ) {
    getDelegate().gwtWorkaround( bogo, new AsyncCallback<BogoPojo>() {
      public void onFailure( Throwable throwable ) {
        callback.error( "Error saving models", throwable );
      }

      public void onSuccess( BogoPojo v ) {
        callback.success( v );
      }
    } );

  }

  private IGwtModelerServiceAsync getDelegate() {
    if ( delegate == null ) {
      delegate = (IGwtModelerServiceAsync) GWT.create( IGwtModelerService.class );
      ServiceDefTarget endpoint = (ServiceDefTarget) delegate;
      endpoint.setServiceEntryPoint( getBaseUrl() );
    }
    return delegate;
  }

  public void generateDomain( String connectionName, String tableName, String dbType, String query,
      String datasourceName, final XulServiceCallback<Domain> callback ) {
    getDelegate().generateDomain( connectionName, tableName, dbType, query, datasourceName,
        new AsyncCallback<Domain>() {
          public void onFailure( Throwable throwable ) {
            callback.error( "Error generating Metadata Domain", throwable );
          }

          public void onSuccess( Domain domain ) {
            callback.success( domain );
          }
        } );
  }

  private static String getBaseUrl() {
    String moduleUrl = GWT.getModuleBaseURL();

    if ( moduleUrl.indexOf( "content" ) > -1 ) { //$NON-NLS-1$
      String baseUrl = moduleUrl.substring( 0, moduleUrl.indexOf( "content" ) ); //$NON-NLS-1$
      return baseUrl + "gwtrpc/modelerService"; //$NON-NLS-1$
    }
    return moduleUrl + "modelerService"; //$NON-NLS-1$
  }

  public void serializeModels( Domain domain, String name, final XulServiceCallback<String> callback ) {
    getDelegate().serializeModels( domain, name, new AsyncCallback<String>() {
      public void onFailure( Throwable throwable ) {
        callback.error( "Error saving models", throwable );
      }

      public void onSuccess( String v ) {
        callback.success( v );
      }
    } );
  }

  public void serializeModels( Domain domain, String name, boolean doOlap, final XulServiceCallback<String> callback ) {
    getDelegate().serializeModels( domain, name, doOlap, new AsyncCallback<String>() {
      public void onFailure( Throwable throwable ) {
        callback.error( "Error saving models", throwable );
      }

      public void onSuccess( String v ) {
        callback.success( v );
      }
    } );
  }

  public void loadDomain( String id, final XulServiceCallback<Domain> callback ) {
    getDelegate().loadDomain( id, new AsyncCallback<Domain>() {
      public void onFailure( Throwable throwable ) {
        callback.error( "Error loading domain", throwable );
      }

      public void onSuccess( Domain domain ) {
        callback.success( domain );
      }
    } );
  }
}
