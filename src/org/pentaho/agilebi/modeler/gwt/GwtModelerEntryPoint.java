package org.pentaho.agilebi.modeler.gwt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.agilebi.modeler.*;
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

public class GwtModelerEntryPoint implements EntryPoint, IXulLoaderCallback {

  private Domain domain;
  public void onModuleLoad() {
    IModelerServiceAsync service = new GwtModelerServiceImpl();
    service.generateDomain(null, "ORDERS", "GENERIC", null, "testing", new XulServiceCallback<Domain>(){
      public void success( Domain domain) {
        GwtModelerEntryPoint.this.domain = domain;
        AsyncXulLoader.loadXulFromUrl(GWT.getModuleBaseURL() + "panel.xul", GWT.getModuleBaseURL() + "modeler", GwtModelerEntryPoint.this); //$NON-NLS-1$//$NON-NLS-2$
      }

      public void error( String s, Throwable throwable ) {
        Window.alert(s);
        throwable.printStackTrace();
      }
    });
  }

  public void xulLoaded( GwtXulRunner gwtXulRunner ) {
    IModelerMessages messages = new GwtModelerMessages((ResourceBundle) gwtXulRunner.getXulDomContainers().get(0).getResourceBundles().get(0));
    BogoPojo bogo = new BogoPojo();
    XulDomContainer container = gwtXulRunner.getXulDomContainers().get(0);


    GwtModelerWorkspaceHelper helper = new GwtModelerWorkspaceHelper();

    ModelerWorkspace model = new ModelerWorkspace(helper);
    model.setDomain(this.domain);
    ModelerController controller = new ModelerController(model);
    controller.setWorkspaceHelper(helper);

    try {
      ModelerMessagesHolder.setMessages(messages);
    } catch (IllegalStateException e) {
      // ignore, it was already set by someone else
    }

    BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());
    controller.setBindingFactory(bf);
    container.addEventHandler(controller);

    ModelerUiHelper.configureControllers(container, model, bf, controller, new ColResolverController());

    RootPanel.get().add((Widget) container.getDocumentRoot().getRootElement().getManagedObject());
    try {
      gwtXulRunner.initialize();
    } catch (XulException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public void overlayLoaded() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void overlayRemoved() {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
