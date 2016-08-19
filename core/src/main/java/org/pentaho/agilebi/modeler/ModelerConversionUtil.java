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

package org.pentaho.agilebi.modeler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;

/**
 * User: rfellows Date: 1/26/12
 */
public class ModelerConversionUtil {

  public static double upConvertDomain( Domain domain ) {
    int modelCount = domain.getLogicalModels().size();
    LogicalModel model = domain.getLogicalModels().get( 0 );
    String versionString = (String) model.getProperty( "AGILE_BI_VERSION" );
    String isAgileBiGenerated = (String) model.getProperty( "AGILE_BI_GENERATED_SCHEMA" );
    String mondrianCatRef = (String) model.getProperty( "MondrianCatalogRef" );
    String dualModelingMode = (String) model.getProperty( "DUAL_MODELING_SCHEMA" );

    double currentVersion = Double.parseDouble( BaseModelerWorkspaceHelper.AGILE_BI_VERSION );

    if ( "false".equals( dualModelingMode ) ) {
      return currentVersion;
    }

    // see if it requires upconverting
    if ( modelCount > 1 ) {
      if ( model.getLogicalTables().size() == domain.getLogicalModels().get( 1 ).getLogicalTables().size() ) {
        return currentVersion;
      }
    } else {

      if ( modelCount == 1
          || ( ( isAgileBiGenerated != null && isAgileBiGenerated.equals( "TRUE" ) ) || ( mondrianCatRef != null ) ) ) {

        double versionNumber = 1.0;
        boolean ignored = false;

        try {
          versionNumber = Double.parseDouble( versionString );
        } catch ( NumberFormatException e ) {
          // not a valid version number, assume it was the original
          ignored = true;
        } catch ( NullPointerException npe ) {
          // just use the pre-defined versionNumber
          ignored = true;
        }

        if ( versionNumber < 2.0 ) {
          // original agile-bi model
          LogicalModel olapModel = duplicateModelForOlap( model );
          domain.addLogicalModel( olapModel );

        } else if ( versionNumber == 2.0 ) {
          // agile-bi model that had duplicated tables in the LogicalModel for OLAP
          LogicalModel olapModel = upgradeAndSplitCombinedModel( model );
          domain.addLogicalModel( olapModel );

        }

        return versionNumber;
      }
    }
    return 0;
  }

  protected static LogicalModel upgradeAndSplitCombinedModel( LogicalModel combinedModel ) {

    // create the new OLAP model
    LogicalModel olapModel = new LogicalModel();
    duplicateProperties( combinedModel, olapModel );

    olapModel.setName( appendOlap( combinedModel.getName() ) );
    olapModel.setDescription( appendOlap( combinedModel.getName() ) );
    olapModel.setId( combinedModel.getId() + BaseModelerWorkspaceHelper.OLAP_SUFFIX );
    olapModel.setPhysicalModel( combinedModel.getPhysicalModel() );
    olapModel.setDomain( combinedModel.getDomain() );
    if ( combinedModel.getRowLevelSecurity() != null ) {
      olapModel.setRowLevelSecurity( combinedModel.getRowLevelSecurity() );
    }

    olapModel.setProperty( "AGILE_BI_GENERATED_SCHEMA", "TRUE" );
    olapModel.setProperty( "MODELING_SCHEMA", "OLAP" );
    olapModel.setProperty( "DUAL_MODELING_SCHEMA", "true" );
    olapModel.setProperty( "visible", "false" );

    List<LogicalTable> relationalTables = new ArrayList<LogicalTable>();
    List<LogicalTable> olapTables = new ArrayList<LogicalTable>();
    for ( LogicalTable table : combinedModel.getLogicalTables() ) {
      if ( table.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
        table.setLogicalModel( olapModel );
        olapTables.add( table );
      } else {
        relationalTables.add( table );
      }
    }

    // clear the logical tables
    combinedModel.getLogicalTables().clear();

    // add back in all of the relational tables
    combinedModel.getLogicalTables().addAll( relationalTables );

    // add in all of the olap tables to the new model
    olapModel.getLogicalTables().addAll( olapTables );

    // add relationships if needed
    if ( combinedModel.getLogicalRelationships().size() > 0 ) {
      splitOlapRelationships( combinedModel, olapModel );
    }

    // update the model version number to the current version
    combinedModel.setProperty( "AGILE_BI_VERSION", BaseModelerWorkspaceHelper.AGILE_BI_VERSION );
    olapModel.setProperty( "AGILE_BI_VERSION", BaseModelerWorkspaceHelper.AGILE_BI_VERSION );

    // remove the olap_cubes and olap_dimensions properties from the non-olap model
    combinedModel.removeChildProperty( "olap_cubes" );
    combinedModel.removeChildProperty( "olap_dimensions" );

    return olapModel;
  }

  protected static void splitOlapRelationships( LogicalModel relationalModel, LogicalModel olapModel ) {
    List<LogicalRelationship> relationalRelationships = new ArrayList<LogicalRelationship>();
    List<LogicalRelationship> olapRelationships = new ArrayList<LogicalRelationship>();

    if ( relationalModel.getLogicalRelationships() != null ) {
      for ( LogicalRelationship rel : relationalModel.getLogicalRelationships() ) {

        if ( isOlap( rel.getFromColumn() ) && isOlap( rel.getToColumn() ) && isOlap( rel.getFromTable() )
            && isOlap( rel.getToTable() ) ) {

          LogicalRelationship olapRel = duplicateRelationshipForOlap( rel, olapModel );

          olapRelationships.add( olapRel );

        } else {
          relationalRelationships.add( rel );
        }

      }

      relationalModel.getLogicalRelationships().clear();
      relationalModel.getLogicalRelationships().addAll( relationalRelationships );
      olapModel.getLogicalRelationships().addAll( olapRelationships );
    }
  }

  private static boolean isOlap( Concept concept ) {
    return concept.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX );
  }

  protected static LogicalTable findCorrespondingOlapTable( LogicalTable relationalTable, LogicalModel olapModel ) {
    for ( LogicalTable table : olapModel.getLogicalTables() ) {
      if ( table.getPhysicalTable().getId().equals( relationalTable.getPhysicalTable().getId() ) ) {
        return table;
      }
    }
    return null;
  }

  protected static LogicalColumn findCorrespondingOlapColumn( LogicalColumn relationalColumn, LogicalModel olapModel ) {
    LogicalTable olapTable = findCorrespondingOlapTable( relationalColumn.getLogicalTable(), olapModel );

    if ( olapTable != null ) {

      for ( LogicalColumn col : olapTable.getLogicalColumns() ) {
        if ( col.getPhysicalColumn().getId().equals( relationalColumn.getPhysicalColumn().getId() ) ) {
          return col;
        }
      }

    }
    return null;
  }

  /**
   * Takes any LogicalModel and duplicates it for use in OLAP modeling. This will duplicate all LogicalTables and sufix
   * them with "_OLAP"
   * 
   * @param logicalModel
   * @return
   */
  public static LogicalModel duplicateModelForOlap( LogicalModel logicalModel ) {

    LogicalModel olapModel = new LogicalModel();
    duplicateProperties( logicalModel, olapModel );

    olapModel.setId( logicalModel.getId() + BaseModelerWorkspaceHelper.OLAP_SUFFIX );
    olapModel.setName( appendOlap( logicalModel.getName() ) );
    olapModel.setDescription( appendOlap( logicalModel.getName() ) );
    olapModel.setPhysicalModel( logicalModel.getPhysicalModel() );
    olapModel.setDomain( logicalModel.getDomain() );
    if ( logicalModel.getRowLevelSecurity() != null ) {
      olapModel.setRowLevelSecurity( logicalModel.getRowLevelSecurity() );
    }

    olapModel.setProperty( "AGILE_BI_GENERATED_SCHEMA", "TRUE" );
    olapModel.setProperty( "MODELING_SCHEMA", "OLAP" );
    olapModel.setProperty( "DUAL_MODELING_SCHEMA", "true" );
    olapModel.setProperty( "visible", "false" );

    for ( LogicalTable table : logicalModel.getLogicalTables() ) {
      LogicalTable copiedTable = (LogicalTable) table.clone();
      copiedTable.setId( copiedTable.getId() + BaseModelerWorkspaceHelper.OLAP_SUFFIX );

      List<LogicalColumn> olapColumns = new ArrayList<LogicalColumn>();
      // set up the columns too
      for ( LogicalColumn col : table.getLogicalColumns() ) {
        LogicalColumn olapCol = new LogicalColumn();
        olapCol.setLogicalTable( copiedTable );
        olapCol.setPhysicalColumn( col.getPhysicalColumn() );
        olapCol.setDataType( col.getDataType() );

        if ( col.getPhysicalColumn().getAggregationList() != null ) {
          olapCol.setAggregationList( col.getPhysicalColumn().getAggregationList() );
        }
        if ( col.getPhysicalColumn().getAggregationType() != null ) {
          olapCol.setAggregationType( col.getPhysicalColumn().getAggregationType() );
        } else {
          if ( olapCol.getDataType().equals( DataType.NUMERIC ) ) {
            olapCol.setAggregationType( AggregationType.SUM );
          } else {
            olapCol.setAggregationType( AggregationType.NONE );
          }
        }

        if ( col.getProperty( "mask" ) != null ) {
          olapCol.setProperty( "mask", col.getProperty( "mask" ) );
        } else if ( olapCol.getDataType().equals( DataType.NUMERIC ) ) {
          olapCol.setProperty( "mask", "#" );
        }

        LocalizedString newName = appendOlap( col.getName() );
        olapCol.setName( newName );

        String locale = "en_US";
        if ( logicalModel.getName().getLocales() != null && logicalModel.getName().getLocales().size() > 0 ) {
          for ( String l : logicalModel.getName().getLocales() ) {
            locale = l;
            break;
          }
        }

        String colId =
            "LC_" + ModelerWorkspace.toId( table.getPhysicalTable().getName( locale ) ) + "_"
                + ModelerWorkspace.toId( col.getPhysicalColumn().getId() ) + BaseModelerWorkspaceHelper.OLAP_SUFFIX;

        colId = BaseModelerWorkspaceHelper.uniquify( colId, olapColumns );

        olapCol.setId( colId );
        olapColumns.add( olapCol );
      }

      copiedTable.getLogicalColumns().clear();
      copiedTable.getLogicalColumns().addAll( olapColumns );

      olapModel.addLogicalTable( copiedTable );
    }

    duplicateRelationshipsForOlap( logicalModel, olapModel );

    olapModel.setProperty( "AGILE_BI_VERSION", BaseModelerWorkspaceHelper.AGILE_BI_VERSION );

    // remove the olap_cubes and olap_dimensions properties from the non-olap model
    logicalModel.removeChildProperty( "olap_cubes" );
    logicalModel.removeChildProperty( "olap_dimensions" );

    return olapModel;
  }

  private static void duplicateProperties( LogicalModel relationalModel, LogicalModel olapModel ) {
    Map<String, Object> props = relationalModel.getProperties();
    olapModel.getProperties().clear();
    for ( String key : props.keySet() ) {
      olapModel.setProperty( key, props.get( key ) );
    }
  }

  public static void duplicateRelationshipsForOlap( LogicalModel relationalModel, LogicalModel olapModel ) {
    if ( olapModel.getLogicalRelationships() != null ) {
      olapModel.getLogicalRelationships().clear();
    }
    if ( relationalModel.getLogicalRelationships() != null ) {
      for ( LogicalRelationship rel : relationalModel.getLogicalRelationships() ) {
        olapModel.addLogicalRelationship( duplicateRelationshipForOlap( rel, olapModel ) );
      }
    }

  }

  private static LogicalRelationship duplicateRelationshipForOlap( LogicalRelationship rel, LogicalModel olapModel ) {
    LogicalTable olapFromTable = findCorrespondingOlapTable( rel.getFromTable(), olapModel );
    LogicalTable olapToTable = findCorrespondingOlapTable( rel.getToTable(), olapModel );
    LogicalColumn olapFromCol = findCorrespondingOlapColumn( rel.getFromColumn(), olapModel );
    LogicalColumn olapToCol = findCorrespondingOlapColumn( rel.getToColumn(), olapModel );

    LogicalRelationship olapRel =
        new LogicalRelationship( olapModel, olapFromTable, olapToTable, olapFromCol, olapToCol );

    olapRel.setComplex( rel.isComplex() );
    olapRel.setRelationshipType( rel.getRelationshipType() );
    olapRel.setJoinOrderKey( rel.getJoinOrderKey() );
    olapRel.setComplexJoin( rel.getComplexJoin() );
    olapRel.setRelationshipDescription( rel.getRelationshipDescription() );

    return olapRel;
  }

  private static LocalizedString appendOlap( LocalizedString localizedString ) {
    LocalizedString newString = new LocalizedString();
    for ( String locale : localizedString.getLocaleStringMap().keySet() ) {
      newString.setString( locale, localizedString.getString( locale ) + BaseModelerWorkspaceHelper.OLAP_SUFFIX );
    }
    return newString;
  }

}
