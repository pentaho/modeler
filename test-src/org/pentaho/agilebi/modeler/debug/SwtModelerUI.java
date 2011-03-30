package org.pentaho.agilebi.modeler.debug;

import org.codehaus.groovy.tools.shell.Shell;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.propforms.*;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created: 3/15/11
 *
 * @author rfellows
 */
public class SwtModelerUI {
  private static Logger logger = LoggerFactory.getLogger(SwtModelerUI.class);
  private XulDomContainer container;
  private XulRunner runner;
  ModelerController controller;

  public SwtModelerUI (Shell shell,  ModelerWorkspace model) throws ModelerException {
    SwtXulLoader loader = null;
    try {
      loader = new SwtXulLoader();
      loader.registerClassLoader(getClass().getClassLoader());
      loader.setOuterContext(shell);
      container = loader.loadXul("org/pentaho/agilebi/modeler/res/panel.xul", new ModelerMessages(ModelerWorkspace.class)); //$NON-NLS-1$

      controller = new ModelerController(model);
      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      container.addEventHandler(controller);
      controller.setBindingFactory(bf);
      controller.setWorkspaceHelper(new ModelerWorkspaceHelper("en-US"));

      AbstractModelerNodeForm propController = new MeasuresPropertiesForm(LocalizedString.DEFAULT_LOCALE);
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();

      propController = new DimensionPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();

      propController = new LevelsPropertiesForm(LocalizedString.DEFAULT_LOCALE );
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();


      propController = new HierarchyPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();

      propController = new MainModelerNodePropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();


      propController = new GenericPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();

      propController = new CategoryPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();

      propController = new FieldsPropertiesForm(LocalizedString.DEFAULT_LOCALE);
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();

      propController = new RelationalModelNodePropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();


      ColResolverController colController = new ColResolverController();
      container.addEventHandler(colController);
      controller.setColResolver(colController);

      runner = new SwtXulRunner();
      runner.addContainer(container);
      runner.initialize();
      container.loadPerspective("ov1");
      
    } catch (XulException e) {
      logger.info("error initializing", e);
      throw new ModelerException(e);
    }
  }

  /*
   * Be sure to include a project reference to Kettle, otherwise libraries like log4j and swt won't be available
   */
  public static void main(String[] args) {
    System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    ModelerWorkspace workspace = new ModelerWorkspace(new ModelerWorkspaceHelper("en-US"));
    try {
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);
      Domain d = ModelerSourceUtil.generateDomain(getDatabaseMeta(), "", "CUSTOMERS");
      workspace.setDomain(d);
    } catch (ModelerException e) {
      e.printStackTrace();
    } catch (KettleException e) {
      e.printStackTrace();
    }

    try {
      new SwtModelerUI(null, workspace).startDebugWindow();
    } catch (ModelerException e) {
      e.printStackTrace();
    }
  }

  private static DatabaseMeta getDatabaseMeta() {
    DatabaseMeta database = new DatabaseMeta();
    //database.setDatabaseInterface(new HypersonicDatabaseMeta());
    database.setDatabaseType("Hypersonic");//$NON-NLS-1$
    //database.setUsername("sa");//$NON-NLS-1$
    //database.setPassword("");//$NON-NLS-1$
    database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
    //database.setHostname(".");
    database.setDBName("SampleData");//$NON-NLS-1$
    //database.setDBPort("9001");//$NON-NLS-1$
    database.setName("SampleData");//$NON-NLS-1$
    return database;
  }

  public void startDebugWindow(){
    try {
      runner.start();
    } catch (XulException e) {
      e.printStackTrace();
    }
  }

}
