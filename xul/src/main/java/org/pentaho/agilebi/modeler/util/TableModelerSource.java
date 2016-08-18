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
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.PhysicalTableImporter;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.util.ThinModelConverter;

/**
 * Provides information to the ModelerModel to support the User Interface. This class also generates the final artifacts
 * from the UI models.
 * 
 * @author jdixon
 * 
 */
public class TableModelerSource implements ISpoonModelerSource {

  private String datasourceName;
  private String tableName;
  private transient DatabaseMeta databaseMeta;
  private String schemaName;
  public static final String SOURCE_TYPE = TableModelerSource.class.getSimpleName();

  public TableModelerSource() {

  }

  public TableModelerSource( DatabaseMeta databaseMeta, String tableName, String schemaName ) {
    this( databaseMeta, tableName, schemaName, tableName );
  }

  public TableModelerSource( DatabaseMeta databaseMeta, String tableName, String schemaName, String datasourceName ) {
    this.tableName = tableName;
    this.databaseMeta = databaseMeta;
    this.schemaName = schemaName;
    this.datasourceName = datasourceName;
    if ( schemaName == null ) {
      this.schemaName = "";
    }
  }

  public String getDatabaseName() {
    return databaseMeta.getName();
  }

  public Domain generateDomain() throws ModelerException {
    return ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, datasourceName, true );
  }

  @Override
  public Domain generateDomain( boolean dualModelingMode ) throws ModelerException {
    return ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, datasourceName, dualModelingMode );
  }

  public Domain generateDomain( final PhysicalTableImporter.ImportStrategy importStrategy ) throws ModelerException {
    return ModelerSourceUtil.generateDomain( databaseMeta, schemaName, tableName, datasourceName, true,
      importStrategy );
  }

  public void initialize( Domain domain ) throws ModelerException {
    SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get( 0 );
    SqlPhysicalTable table = model.getPhysicalTables().get( 0 );

    String targetTable = (String) table.getProperty( "target_table" );
    if ( !StringUtils.isEmpty( targetTable ) ) {
      domain.setId( targetTable );
    }

    this.databaseMeta = ThinModelConverter.convertToLegacy( model.getId(), model.getDatasource() );
    this.tableName = table.getTargetTable();
    this.schemaName = table.getTargetSchema();

    if ( schemaName == null ) {
      schemaName = "";
    }
  }

  public void serializeIntoDomain( Domain d ) {
    LogicalModel lm = d.getLogicalModels().get( 0 );
    lm.setProperty( "source_type", SOURCE_TYPE );
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  public String getSchemaName() {
    return schemaName == null ? "" : schemaName;
  }

  public void setSchemaName( String schemaName ) {
    if ( schemaName == null ) {
      schemaName = "";
    }
    this.schemaName = schemaName;
  }

  public void setDatasourceName( String datasourceName ) {
    this.datasourceName = datasourceName;
  }

  public String getDatasourceName() {
    return this.datasourceName;
  }
}
