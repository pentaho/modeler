package org.pentaho.agilebi.modeler;

import org.junit.Before;
import org.junit.BeforeClass;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;

/**
 * User: nbaker
 * Date: 4/8/11
 */
public class AbstractModelerTest {

  protected ModelerWorkspace workspace;
  protected DatabaseMeta databaseMeta;

  private static final String LOCALE = "en-US";

  static{

    System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      KettleEnvironment.init();
    } catch (KettleException e) {
      throw new IllegalStateException("Unable to initialize Kettle.", e);
    }
    Props.init(Props.TYPE_PROPERTIES_EMPTY);
  }
  
  @Before
  public void setUp() throws Exception {

    if(ModelerMessagesHolder.getMessages() == null){
      ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    }
    workspace = new ModelerWorkspace(new ModelerWorkspaceHelper(LOCALE));
    databaseMeta = getDatabaseMeta();
  }

  protected void generateTestDomain() throws ModelerException {
    Domain d = ModelerSourceUtil.generateDomain(getDatabaseMeta(), "", "CUSTOMERS");
    workspace.setDomain(d);
  }

  

  public static DatabaseMeta getDatabaseMeta() {
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseType("Hypersonic");//$NON-NLS-1$
    database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
    database.setDBName("SampleData");//$NON-NLS-1$
    database.setName("SampleData");//$NON-NLS-1$
    return database;
  }
}
