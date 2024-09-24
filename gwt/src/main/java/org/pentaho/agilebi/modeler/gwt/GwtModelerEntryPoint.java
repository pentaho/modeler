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

package org.pentaho.agilebi.modeler.gwt;

import org.pentaho.agilebi.modeler.ColResolverController;
import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.agilebi.modeler.ModelerController;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerUiHelper;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class GwtModelerEntryPoint implements EntryPoint, IXulLoaderCallback {

  private Domain domain;

  public void onModuleLoad() {
    IModelerServiceAsync service = new GwtModelerServiceImpl();
    service.generateDomain( null, "ORDERS", "GENERIC", null, "testing", new XulServiceCallback<Domain>() {
      public void success( Domain domain ) {
        GwtModelerEntryPoint.this.domain = domain;
        AsyncXulLoader.loadXulFromUrl(
            GWT.getModuleBaseURL() + "panel.xul", GWT.getModuleBaseURL() + "modeler", GwtModelerEntryPoint.this ); //$NON-NLS-1$//$NON-NLS-2$
      }

      public void error( String s, Throwable throwable ) {
        Window.alert( s );
        throwable.printStackTrace();
      }
    } );
  }


  public void xulLoaded( GwtXulRunner gwtXulRunner ) {
    IModelerMessages messages =
        new GwtModelerMessages( (ResourceBundle) gwtXulRunner.getXulDomContainers().get( 0 ).getResourceBundles().get(
            0 ) );
    BogoPojo bogo = new BogoPojo();
    XulDomContainer container = gwtXulRunner.getXulDomContainers().get( 0 );

    GwtModelerWorkspaceHelper helper = new GwtModelerWorkspaceHelper();

    ModelerWorkspace model = new ModelerWorkspace( helper );
    model.setDomain( this.domain );
    ModelerController controller = new ModelerController( model );
    controller.setWorkspaceHelper( helper );

    try {
      ModelerMessagesHolder.setMessages( messages );
    } catch ( IllegalStateException e ) {
      // ignore, it was already set by someone else
      boolean ignore = true;
    }

    BindingFactory bf = new GwtBindingFactory( container.getDocumentRoot() );
    controller.setBindingFactory( bf );
    container.addEventHandler( controller );

    ModelerUiHelper.configureControllers( container, model, bf, controller, new ColResolverController() );

    RootPanel.get().add( (Widget) container.getDocumentRoot().getRootElement().getManagedObject() );
    try {
      gwtXulRunner.initialize();
    } catch ( XulException e ) {
      e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
    }
  }

  public void overlayLoaded() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  public void overlayRemoved() {
    // To change body of implemented methods use File | Settings | File Templates.
  }
}
