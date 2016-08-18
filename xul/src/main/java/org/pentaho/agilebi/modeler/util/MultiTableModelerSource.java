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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerConversionUtil;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMode;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.strategy.MultiTableAutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.StarSchemaAutoModelStrategy;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.RelationshipType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTableModelerSource implements ISpoonModelerSource {

  private ModelGenerator generator;
  private DatabaseMeta databaseMeta;
  private SchemaModel schemaModel;
  private List<String> selectedTables;
  private String datasourceName;

  private GeoContext geoContext;

  public static final String SOURCE_TYPE = MultiTableModelerSource.class.getSimpleName();
  private static Logger logger = LoggerFactory.getLogger( MultiTableModelerSource.class );

  public MultiTableModelerSource( DatabaseMeta databaseMeta, SchemaModel schemaModel, String datasourceName,
      List<String> selectedTables ) {
    this( databaseMeta, schemaModel, datasourceName, selectedTables, null );
  }

  public MultiTableModelerSource( DatabaseMeta databaseMeta, SchemaModel schemaModel, String datasourceName,
      List<String> selectedTables, GeoContext geoContext ) {
    this.datasourceName = datasourceName;
    this.databaseMeta = databaseMeta;
    this.schemaModel = schemaModel;
    this.selectedTables = selectedTables;
    this.generator = new ModelGenerator();
    this.geoContext = geoContext;
  }

  @Override
  public Domain generateDomain( boolean doOlap ) throws ModelerException {

    Domain domain = null;
    try {
      // Generate domain based on the table names.

      String locale = LocalizedString.DEFAULT_LOCALE;
      this.generator.setLocale( locale );
      this.generator.setDatabaseMeta( databaseMeta );
      this.generator.setModelName( datasourceName );

      Set<String> usedTables = new HashSet<String>();
      List<SchemaTable> schemas = new ArrayList<SchemaTable>();
      if ( selectedTables.size() == 1 ) { // special single table story BISERVER-5806

        String singleTable = selectedTables.get( 0 );
        if ( !usedTables.contains( singleTable ) ) {
          schemas.add( createSchemaTable( singleTable ) );
        }

      } else {
        for ( JoinRelationshipModel joinModel : schemaModel.getJoins() ) {

          String fromTable = joinModel.getLeftKeyFieldModel().getParentTable().getName();
          if ( !usedTables.contains( fromTable ) ) {
            schemas.add( createSchemaTable( fromTable ) );
            usedTables.add( fromTable );
          }

          String toTable = joinModel.getRightKeyFieldModel().getParentTable().getName();
          if ( !usedTables.contains( toTable ) ) {
            schemas.add( createSchemaTable( toTable ) );
            usedTables.add( toTable );
          }

        }
      }
      SchemaTable[] tableNames = new SchemaTable[schemas.size()];
      tableNames = schemas.toArray( tableNames );
      this.generator.setTableNames( tableNames );
      domain = this.generator.generateDomain();
      domain.setId( datasourceName );

      ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper( locale );
      if ( selectedTables.size() == 1 ) { // single table mode
        helper.setAutoModelStrategy( new SimpleAutoModelStrategy( locale, geoContext ) );
      } else if ( doOlap ) {
        helper.setAutoModelStrategy( new StarSchemaAutoModelStrategy( locale, geoContext ) );
      } else {
        helper.setAutoModelStrategy( new MultiTableAutoModelStrategy( locale ) );
      }

      domain.getLogicalModels().get( 0 ).setProperty( "DUAL_MODELING_SCHEMA", "" + doOlap );

      ModelerWorkspace workspace = new ModelerWorkspace( helper, geoContext );
      workspace.setDomain( domain );

      LogicalModel logicalModel = workspace.getLogicalModel( ModelerPerspective.REPORTING );
      logicalModel.setProperty( "AGILE_BI_GENERATED_SCHEMA", "TRUE" );
      logicalModel.setName( new LocalizedString( locale, datasourceName ) );
      logicalModel.setDescription( new LocalizedString( locale, "This is the data model for " + datasourceName ) );
      workspace.setModelName( datasourceName );
      helper.autoModelRelationalFlat( workspace );
      LogicalModel olapModel = null;

      if ( doOlap ) {

        olapModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
        if ( olapModel.getLogicalRelationships().size() != logicalModel.getLogicalRelationships().size() ) {
          ModelerConversionUtil.duplicateRelationshipsForOlap( logicalModel, olapModel );
        }
        String factTableName = getSchemaTablePair( schemaModel.getFactTable().getName() )[1];
        LogicalTable factTable = findFactTable( factTableName, olapModel );
        if ( factTable == null ) {
          throw new IllegalStateException( "Fact table not found" );
        } else {
          factTable.getPhysicalTable().setProperty( "FACT_TABLE", true );
          workspace.getAvailableTables().setFactTable( factTable.getPhysicalTable() );
        }
        // somehow the strategy is getting set to simple. setting it back again.
        if ( selectedTables.size() == 1 ) { // single table mode
          helper.setAutoModelStrategy( new SimpleAutoModelStrategy( locale, geoContext ) );
        } else {
          helper.setAutoModelStrategy( new StarSchemaAutoModelStrategy( locale, geoContext ) );
        }
        helper.autoModelFlat( workspace );
        workspace.setModellingMode( ModelerMode.ANALYSIS_AND_REPORTING );
      } else {
        workspace.setModellingMode( ModelerMode.REPORTING_ONLY );
      }

      // Create and add LogicalRelationships to the LogicalModel from the
      // domain.
      generateLogicalRelationships( logicalModel, false );
      if ( doOlap ) {
        generateLogicalRelationships( olapModel, true );
      }

      helper.populateDomain( workspace );

      for ( LogicalTable businessTable : logicalModel.getLogicalTables() ) {
        businessTable.setName( new LocalizedString( locale, businessTable.getPhysicalTable().getName( locale ) ) );
      }

      if ( olapModel != null ) {
        for ( LogicalTable businessTable : olapModel.getLogicalTables() ) {
          businessTable.setName( new LocalizedString( locale, businessTable.getPhysicalTable().getName( locale ) ) );
        }
      }

    } catch ( Exception e ) {
      logger.debug( e.getLocalizedMessage(), e );
      logger.info( e.getLocalizedMessage() );
      throw new ModelerException( e.getLocalizedMessage(), e );
    }
    return domain;
  }

  private SchemaTable createSchemaTable( String table ) {
    String schemaName = "";
    String tableName = table;
    if ( table.indexOf( "." ) > 0 ) {
      String[] pair = getSchemaTablePair( table );
      schemaName = pair[0];
      tableName = pair[1];
    }
    return new SchemaTable( schemaName, tableName );
  }

  private String[] getSchemaTablePair( String table ) {
    if ( table.indexOf( "." ) < 0 ) {
      return new String[] { "", table };
    }
    String[] pair = new String[2];
    String[] parts = table.split( "\\." );
    pair[0] = parts[0];
    pair[1] = StringUtils.join( Arrays.copyOfRange( parts, 1, parts.length ), "." );
    return pair;
  }

  private LogicalTable findFactTable( String table, LogicalModel logicalModel ) {
    LogicalTable factTable = null;
    for ( LogicalTable lTable : logicalModel.getLogicalTables() ) {
      if ( lTable.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
        Object prop = lTable.getPhysicalTable().getProperty( "target_table" );
        if ( prop != null && prop.equals( table ) ) {
          factTable = lTable;
          break;
        }
      }
    }
    return factTable;
  }

  private void generateLogicalRelationships( LogicalModel logicalModel, boolean doOlap ) throws IllegalStateException {
    for ( JoinRelationshipModel joinModel : schemaModel.getJoins() ) {
      String lTable = joinModel.getLeftKeyFieldModel().getParentTable().getName();
      String rTable = joinModel.getRightKeyFieldModel().getParentTable().getName();
      lTable = getSchemaTablePair( lTable )[1];
      rTable = getSchemaTablePair( rTable )[1];

      LogicalTable fromTable = null;
      LogicalColumn fromColumn = null;
      LogicalTable toTable = null;
      LogicalColumn toColumn = null;

      for ( LogicalTable logicalTable : logicalModel.getLogicalTables() ) {

        if ( doOlap && !logicalTable.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
          continue;
        } else if ( !doOlap && logicalTable.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
          continue;
        }

        if ( logicalTable.getPhysicalTable().getProperty( "target_table" ).equals( lTable ) ) {
          fromTable = logicalTable;

          for ( LogicalColumn logicalColumn : fromTable.getLogicalColumns() ) {
            if ( logicalColumn.getPhysicalColumn().getProperty( "target_column" ).equals(
                joinModel.getLeftKeyFieldModel().getName() ) ) {
              fromColumn = logicalColumn;
              break;
            }
          }
        }
        if ( logicalTable.getPhysicalTable().getProperty( "target_table" ).equals( rTable ) ) {
          toTable = logicalTable;

          for ( LogicalColumn logicalColumn : toTable.getLogicalColumns() ) {
            if ( logicalColumn.getPhysicalColumn().getProperty( "target_column" ).equals(
                joinModel.getRightKeyFieldModel().getName() ) ) {
              toColumn = logicalColumn;
              break;
            }
          }
        }
      }

      if ( fromTable == null || fromColumn == null || toTable == null || toColumn == null ) {
        throw new IllegalStateException( "Invalid Relationship" );
      }

      LogicalRelationship logicalRelationship = new LogicalRelationship();
      logicalRelationship.setRelationshipType( RelationshipType._1_1 );
      logicalRelationship.setFromTable( fromTable );
      logicalRelationship.setFromColumn( fromColumn );
      logicalRelationship.setToTable( toTable );
      logicalRelationship.setToColumn( toColumn );
      logicalModel.addLogicalRelationship( logicalRelationship );
    }
  }

  @Override
  public String getDatabaseName() {
    String name = null;
    if ( this.databaseMeta != null ) {
      name = this.databaseMeta.getDatabaseName();
    }
    return name;
  }

  @Override
  public void initialize( Domain domain ) throws ModelerException {
  }

  @Override
  public void serializeIntoDomain( Domain d ) {
    LogicalModel lm = d.getLogicalModels().get( 0 );
    lm.setProperty( "source_type", SOURCE_TYPE );
  }

  @Override
  public String getSchemaName() {
    return null;
  }

  @Override
  public String getTableName() {
    return null;
  }

  @Override
  public DatabaseMeta getDatabaseMeta() {
    return this.databaseMeta;
  }

  public Domain generateDomain() throws ModelerException {
    return generateDomain( false );
  }
}
