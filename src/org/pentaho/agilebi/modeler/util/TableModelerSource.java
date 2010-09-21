package org.pentaho.agilebi.modeler.util;

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.util.ThinModelConverter;

/**
 * Provides information to the ModelerModel to support the User Interface. This
 * class also generates the final artifacts from the UI models.
 *
 * @author jdixon
 *
 */
public class TableModelerSource implements ISpoonModelerSource {

	private String tableName;
	private transient DatabaseMeta databaseMeta;
	private String schemaName;
	public static final String SOURCE_TYPE = TableModelerSource.class.getSimpleName();

  public TableModelerSource(){

  }

	public TableModelerSource(DatabaseMeta databaseMeta, String tableName, String schemaName ) {
		this.tableName = tableName;
		this.databaseMeta = databaseMeta;
		this.schemaName = schemaName;
		if( schemaName == null ) {
		  this.schemaName = ""; //$NON-NLS-1$
		}
	}

	public String getDatabaseName() {
		return databaseMeta.getName();
	}

	public Domain generateDomain() throws ModelerException {
		return ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableName);
	}

	public void initialize(Domain domain) throws ModelerException {
	  SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
	  SqlPhysicalTable table = model.getPhysicalTables().get(0);

	  String targetTable = (String) table.getProperty("target_table"); //$NON-NLS-1$
	  if(!StringUtils.isEmpty(targetTable)) {
	    domain.setId(targetTable);
	  }

	  this.databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());
    this.tableName = table.getTargetTable();
	  this.schemaName = table.getTargetSchema();

    if( schemaName == null ) {
      schemaName = ""; //$NON-NLS-1$
    }
	}

  public void serializeIntoDomain(Domain d) {
    LogicalModel lm = d.getLogicalModels().get(0);
    lm.setProperty("source_type", SOURCE_TYPE); //$NON-NLS-1$
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getSchemaName() {
    return schemaName == null ? "" : schemaName; //$NON-NLS-1$
  }

  public void setSchemaName(String schemaName) {
    if( schemaName == null ) {
      schemaName = ""; //$NON-NLS-1$
    }
    this.schemaName = schemaName;
  }

}
