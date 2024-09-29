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


package org.pentaho.agilebi.modeler.services.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerService;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.mantle.client.csrf.CsrfUtil;
import org.pentaho.mantle.client.csrf.IConsumer;
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
      delegate = GWT.create( IGwtModelerService.class );
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

    if ( moduleUrl.contains( "content" ) ) {
      String baseUrl = moduleUrl.substring( 0, moduleUrl.indexOf( "content" ) );
      return baseUrl + "gwtrpc/modelerService";
    }
    return moduleUrl + "modelerService";
  }

  public void serializeModels( final Domain domain, final String name, final XulServiceCallback<String> callback ) {
    // Lambda expressions are only supported from GWT 2.8 onwards.
    CsrfUtil.callProtected( getDelegate(), new IConsumer<IGwtModelerServiceAsync>() {
      @Override
      public void accept( IGwtModelerServiceAsync delegate ) {

        delegate.serializeModels( domain, name, new AsyncCallback<String>() {
          public void onFailure( Throwable throwable ) {
            callback.error( "Error saving models", throwable );
          }

          public void onSuccess( String v ) {
            callback.success( v );
          }
        } );
      }
    } );
  }

  public void serializeModels( final Domain domain, final String name, final boolean doOlap,
                               final XulServiceCallback<String> callback ) {
    // Lambda expressions are only supported from GWT 2.8 onwards.
    CsrfUtil.callProtected( getDelegate(), new IConsumer<IGwtModelerServiceAsync>() {
      @Override
      public void accept( IGwtModelerServiceAsync delegate ) {

        delegate.serializeModels( domain, name, doOlap, new AsyncCallback<String>() {
          public void onFailure( Throwable throwable ) {
            callback.error( "Error saving models", throwable );
          }

          public void onSuccess( String v ) {
            callback.success( v );
          }
        } );
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
