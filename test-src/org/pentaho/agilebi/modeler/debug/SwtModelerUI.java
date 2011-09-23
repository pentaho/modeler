package org.pentaho.agilebi.modeler.debug;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceUtil;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

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

  public SwtModelerUI (Shell shell,  ModelerWorkspace workspace) throws ModelerException {
    SwtXulLoader loader = null;
    try {
      loader = new SwtXulLoader();
      loader.registerClassLoader(getClass().getClassLoader());
      loader.setOuterContext(shell);
      container = loader.loadXul("org/pentaho/agilebi/modeler/res/panel.xul", new ModelerMessages(ModelerWorkspace.class)); //$NON-NLS-1$
      container.loadOverlay("org/pentaho/agilebi/modeler/debug/panel_overlay.xul"); //$NON-NLS-1$

      controller = new ModelerController(workspace);

      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      container.addEventHandler(controller);
      controller.setBindingFactory(bf);
      ModelerUiHelper.configureControllers(container, workspace, bf, controller, new ColResolverController());

      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(workspace, "valid", "modelValidationStatus", "value", BindingConvertor.boolean2String());

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

    Reader propsReader = null;
    GeoContext geoContext = null;
    try {
      propsReader = new FileReader(new File("test-res/geoRoles.properties"));
      Properties props = new Properties();
      props.load(propsReader);
      geoContext = GeoContextFactory.create(props);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    ModelerWorkspace workspace = new ModelerWorkspace(new ModelerWorkspaceHelper("en-US"), geoContext);


    try {
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);
      Domain d = ModelerSourceUtil.generateDomain(getDatabaseMeta(), "", "CUSTOMERS");

      XmiParser parser = new XmiParser();
      try {
        Domain domain = parser.parseXmi(new FileInputStream(new File("test-res/testMulti.xmi")));
        workspace.setDomain(domain);
      } catch (Exception e) {
        e.printStackTrace();
      }
//      workspace.setDomain(d);
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

  Shell shell = null;
  
  public void setupTestMenuBar() {
    SwtWindow window = (SwtWindow)runner.getXulDomContainers().get(0).getDocumentRoot().getRootElement();
    shell = (Shell)window.getManagedObject();
    Menu menuBar = new Menu(shell, SWT.BAR);
    MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    fileMenuHeader.setText("&File");
    Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
    fileMenuHeader.setMenu(fileMenu);

    MenuItem fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
    fileOpenItem.setText("&Open...");

    
    MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
    fileSaveItem.setText("&Save...");

    MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
    fileExitItem.setText("E&xit");
    shell.setMenuBar(menuBar);
    
    fileOpenItem.addSelectionListener(new FileOpenItemListener());
    fileSaveItem.addSelectionListener(new FileSaveItemListener());
    fileExitItem.addSelectionListener(new FileExitItemListener());
    
  }
  
  public void startDebugWindow(){
    try {
      setupTestMenuBar();
      runner.start();
    } catch (XulException e) {
      e.printStackTrace();
    }
  }
  
  class FileExitItemListener implements SelectionListener {
    public void widgetSelected(SelectionEvent event) {
      widgetDefaultSelected(event);
    }

    public void widgetDefaultSelected(SelectionEvent event) {
      shell.close();
    }
  }
  
  class FileOpenItemListener implements SelectionListener {
    public void widgetSelected(SelectionEvent event) {
      widgetDefaultSelected(event);
    }

    public void widgetDefaultSelected(SelectionEvent event) {
      System.out.println("Open...");
      try {
        String xmi = IOUtils.toString(new FileInputStream("testing.xmi"));
        ModelerWorkspaceUtil.loadWorkspace("testing.xmi", xmi, controller.getModel());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  class FileSaveItemListener implements SelectionListener {
    public void widgetSelected(SelectionEvent event) {
      widgetDefaultSelected(event);
    }

    public void widgetDefaultSelected(SelectionEvent event) {
      System.out.println("Save...");
      try {
        ModelerWorkspaceUtil.saveWorkspace(controller.getModel(), "testing.xmi");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


}
