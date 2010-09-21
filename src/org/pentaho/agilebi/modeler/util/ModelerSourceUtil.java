package org.pentaho.agilebi.modeler.util;

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.pms.schema.concept.DefaultPropertyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ModelerSourceUtil {

  public static final String DEFAULT_ROLE_NAME = "Authenticated"; //$NON-NLS-1$
  private static ModelGenerator generator = new ModelGenerator();
  private static Logger logger = LoggerFactory.getLogger(ModelerSourceUtil.class);

  public static void verifyTableExistsAndMayBeQuoted(DatabaseMeta databaseMeta, String schemaName, String tableName) throws
      ModelerException {
    Database database = new Database(databaseMeta);
    String quotedSchema = schemaName;
    if (!StringUtils.isBlank(quotedSchema)) {
      quotedSchema = databaseMeta.getStartQuote() + quotedSchema + databaseMeta.getEndQuote();
    }
    String quotedTable = tableName;
    if (!StringUtils.isBlank(quotedTable)) {
      quotedTable = databaseMeta.getStartQuote() + quotedTable + databaseMeta.getEndQuote();
    }
    String schemaTableCombination =  databaseMeta.getSchemaTableCombination(quotedSchema, quotedTable);

    try {
      database.connect();
      database.getTableFields(schemaTableCombination);
    } catch (KettleDatabaseException e) {
      throw new ModelerException(BaseMessages.getString(ModelerSourceUtil.class, "ModelerSourceUtil.FAILED_TO_GET_TABLE_FIELDS", schemaTableCombination), e); //$NON-NLS-1$
    } finally {
      database.disconnect();
    }
  }

  public static Domain generateDomain(DatabaseMeta databaseMeta, String schemaName, String tableName)
      throws ModelerException {

    // before generating model, let's check that the table exists and can be quoted.
    // Mondrian quotes tables, so we need to force this check.
    //verifyTableExistsAndMayBeQuoted(databaseMeta, schemaName, tableName);

  	Domain domain = null;
  	try {
	    // modelName, databaseMeta, , "joe", tableOutputMeta.getTablename(), profiles);
	    String locale = Locale.getDefault().toString();
	    generator.setLocale(locale);
	    generator.setDatabaseMeta(databaseMeta);
	    generator.setModelName(tableName);

	    SchemaTable tableNames[] = new SchemaTable[1];
	    // TODO: support schema names.
	    tableNames[0] = new SchemaTable(schemaName, tableName);
	    generator.setTableNames(tableNames);
	    domain = generator.generateDomain();
	    domain.setId(tableName); // replaced with user specified name later

	    LogicalModel businessModel = domain.getLogicalModels().get(0); // schemaMeta.getActiveModel();
	    businessModel.setProperty("AGILE_BI_GENERATED_SCHEMA", "TRUE");

	    // TODO do this with messages
	    businessModel.setName(new LocalizedString(locale, tableName));
	    businessModel.setDescription(new LocalizedString(locale, "This is the data model for "
	        + businessModel.getName(locale)));

	    LogicalTable businessTable = businessModel.getLogicalTables().get(0);
	    businessTable.setName(new LocalizedString(locale, "Available Columns"));

	    // configuring security is necessary so when publishing a model to the bi-server
	    // it can be viewed by everyone.  we will eventually have a security UI where this will
	    // be configurable in the modeler tool

	    // TODO: investigate and replace this magic number with named constant?
	    int rights = 31;
	    String roleName = System.getProperty("AGILE_BI_MODEL_ROLE", DEFAULT_ROLE_NAME); //$NON-NLS-1$
	    setRoleAccess(roleName, rights, businessModel);
  	} catch (PentahoMetadataException e) {
      e.printStackTrace();
  		logger.info(e.getLocalizedMessage());
  		throw new ModelerException(e);
  	}
    return domain;
  }

  public static void setRoleAccess( String role, int rights, IConcept concept ) {
    SecurityOwner owner = new SecurityOwner(SecurityOwner.OwnerType.ROLE, role );
    Security security = (Security)concept.getProperty(DefaultPropertyID.SECURITY.getId());
    if( security == null ) {
      security = new Security();
      concept.setProperty(DefaultPropertyID.SECURITY.getId(), security);
    }
    security.putOwnerRights(owner, rights);
  }
}
