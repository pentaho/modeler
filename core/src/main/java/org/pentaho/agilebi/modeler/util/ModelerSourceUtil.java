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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.util;

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerConversionUtil;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.automodel.PhysicalTableImporter.ImportStrategy;
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

import static org.pentaho.metadata.automodel.PhysicalTableImporter.defaultImportStrategy;

public class ModelerSourceUtil {

  public static final String DEFAULT_ROLE_NAME = "Authenticated"; //$NON-NLS-1$
  private static ModelGenerator generator = new ModelGenerator();
  private static Logger logger = LoggerFactory.getLogger( ModelerSourceUtil.class );

  /**
   * PDI allows users to run SQLs where the table names are not quoted.  This results in the
   * DB converting to lower case or upper case depending on the DB vendor.  However, Mondrian
   * will always quote table names so when generating the schema on an unquoted table name,
   * you can use this function to determine what the table name should be if it were quoted.
   * 
   * @param databaseMeta
   * @param schemaName
   * @param tableName
   * @return
   * @throws ModelerException
   */
  public static String[] discoverTableCasing( DatabaseMeta databaseMeta, String schemaName, String tableName )
    throws ModelerException {
    try {
      // First try to see if current casing of tableName matches DB (Ex. MySQL doesn't matter)
      verifyTableExistsAndMayBeQuoted( databaseMeta, schemaName, tableName );
    } catch ( ModelerException e ) {
      try {
        // Second try to see if lower casing will match DB (Ex. Postgres)
        if ( !StringUtils.isBlank( schemaName ) ) {
          schemaName = schemaName.toLowerCase();
        }
        if ( !StringUtils.isBlank( tableName ) ) {
          tableName = tableName.toLowerCase();
        }
        verifyTableExistsAndMayBeQuoted( databaseMeta, schemaName, tableName );
      } catch ( ModelerException e2 ) {
        // Third try to see if upper casing will match DB (Ex. H2, ORACLE)
        if ( !StringUtils.isBlank( schemaName ) ) {
          schemaName = schemaName.toUpperCase();
        }
        if ( !StringUtils.isBlank( tableName ) ) {
          tableName = tableName.toUpperCase();
        }
        verifyTableExistsAndMayBeQuoted( databaseMeta, schemaName, tableName );
      }
    }
    return new String[] { schemaName, tableName };
  }

  public static void verifyTableExistsAndMayBeQuoted( DatabaseMeta databaseMeta, String schemaName, String tableName )
    throws ModelerException {
    Database database = new Database( databaseMeta );
    String quotedSchema = schemaName;
    if ( !StringUtils.isBlank( quotedSchema ) ) {
      quotedSchema = databaseMeta.getStartQuote() + quotedSchema + databaseMeta.getEndQuote();
    }
    String quotedTable = tableName;
    if ( !StringUtils.isBlank( quotedTable ) ) {
      quotedTable = databaseMeta.getStartQuote() + quotedTable + databaseMeta.getEndQuote();
    }
    String schemaTableCombination = databaseMeta.getQuotedSchemaTableCombination( quotedSchema, quotedTable );

    try {
      database.connect();
      database.getTableFields( schemaTableCombination );
    } catch ( KettleDatabaseException e ) {
      throw new ModelerException( BaseMessages.getString( ModelerWorkspace.class,
          "ModelerSourceUtil.FAILED_TO_GET_TABLE_FIELDS", schemaTableCombination ), e ); //$NON-NLS-1$
    } finally {
      database.disconnect();
    }
  }

  public static Domain generateDomain( DatabaseMeta databaseMeta, String schemaName, String tableName )
    throws ModelerException {
    return generateDomain( databaseMeta, schemaName, tableName, tableName, true );
  }

  public static Domain generateDomain( DatabaseMeta databaseMeta, String schemaName, String tableName,
      String datasourceName ) throws ModelerException {
    return generateDomain( databaseMeta, schemaName, tableName, datasourceName, true );
  }

  public static Domain generateDomain( DatabaseMeta databaseMeta, String schemaName, String tableName,
      String datasourceName, boolean dualModelingMode ) throws ModelerException {
    return generateDomain(
      databaseMeta, schemaName, tableName, datasourceName, dualModelingMode, defaultImportStrategy );
  }

  public static Domain generateDomain( DatabaseMeta databaseMeta, String schemaName, String tableName,
                                       String datasourceName, boolean dualModelingMode,
                                       ImportStrategy importStrategy ) throws ModelerException {
    String[] schemaTable = discoverTableCasing( databaseMeta, schemaName, tableName );
    schemaName = schemaTable[0];
    tableName = schemaTable[1];

    Domain domain = null;
    try {
      // modelName, databaseMeta, , "joe", tableOutputMeta.getTablename(), profiles);
      String locale = LocalizedString.DEFAULT_LOCALE;
      generator.setLocale( locale );
      generator.setDatabaseMeta( databaseMeta );
      generator.setModelName( tableName );

      SchemaTable[] tableNames = new SchemaTable[1];
      // TODO: support schema names.
      tableNames[0] = new SchemaTable( schemaName, tableName );
      generator.setTableNames( tableNames );
      domain = generator.generateDomain( importStrategy );
      domain.setId( tableName ); // replaced with user specified name later

      LogicalModel businessModel = domain.getLogicalModels().get( 0 ); // schemaMeta.getActiveModel();
      businessModel.setProperty( "AGILE_BI_GENERATED_SCHEMA", "TRUE" );
      businessModel.setProperty( "DUAL_MODELING_SCHEMA", "" + dualModelingMode );

      // TODO do this with messages
      businessModel.setName( new LocalizedString( locale, tableName ) );
      businessModel.setDescription( new LocalizedString( locale, "This is the data model for " + datasourceName ) );

      LogicalTable businessTable = businessModel.getLogicalTables().get( 0 );
      businessTable.setName( new LocalizedString( locale, businessTable.getPhysicalTable().getName( locale ) ) );

      // if it was requested to generate for dual-mode modeling (relational & olap)
      // duplicate the tables
      LogicalModel olapModel = null;
      if ( dualModelingMode ) {
        olapModel = ModelerConversionUtil.duplicateModelForOlap( businessModel );
        domain.addLogicalModel( olapModel );
      }

      // configuring security is necessary so when publishing a model to the bi-server
      // it can be viewed by everyone. we will eventually have a security UI where this will
      // be configurable in the modeler tool

      // TODO: investigate and replace this magic number with named constant?
      int rights = 31;
      String roleName = System.getProperty( "AGILE_BI_MODEL_ROLE", DEFAULT_ROLE_NAME ); //$NON-NLS-1$
      setRoleAccess( roleName, rights, businessModel );
      if ( olapModel != null ) {
        setRoleAccess( roleName, rights, olapModel );
      }
    } catch ( PentahoMetadataException e ) {
      e.printStackTrace();
      logger.info( e.getLocalizedMessage() );
      throw new ModelerException( e );
    }
    return domain;
  }

  public static void setRoleAccess( String role, int rights, IConcept concept ) {
    SecurityOwner owner = new SecurityOwner( SecurityOwner.OwnerType.ROLE, role );
    Security security = (Security) concept.getProperty( DefaultPropertyID.SECURITY.getId() );
    if ( security == null ) {
      security = new Security();
      concept.setProperty( DefaultPropertyID.SECURITY.getId(), security );
    }
    security.putOwnerRights( owner, rights );
  }

}
