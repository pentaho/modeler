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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.BaseAggregationMetaDataNode;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.nodes.TimeRole;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.strategy.AutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;

/**
 * User: nbaker Date: Jul 16, 2010
 */
public abstract class BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper {

  private static final List<AggregationType> DEFAULT_AGGREGATION_LIST = new ArrayList<AggregationType>();
  private static final List<AggregationType> DEFAULT_NON_NUMERIC_AGGREGATION_LIST = new ArrayList<AggregationType>();
  private static String locale;
  public static final String OLAP_SUFFIX = "_OLAP";

  // public static final String AGILE_BI_VERSION = "2.0" // Relational & OLAP models are in one LogicalModel. OLAP uses
  // tables with _OLAP suffix
  public static final String AGILE_BI_VERSION = "3.0"; // Relational Model & OLAP models seperated into 2 Logical Models
                                                       // in the Domain

  private AutoModelStrategy autoModelStrategy;

  static {
    DEFAULT_AGGREGATION_LIST.add( AggregationType.NONE );
    DEFAULT_AGGREGATION_LIST.add( AggregationType.SUM );
    DEFAULT_AGGREGATION_LIST.add( AggregationType.AVERAGE );
    DEFAULT_AGGREGATION_LIST.add( AggregationType.MINIMUM );
    DEFAULT_AGGREGATION_LIST.add( AggregationType.MAXIMUM );
    DEFAULT_AGGREGATION_LIST.add( AggregationType.COUNT );
    DEFAULT_AGGREGATION_LIST.add( AggregationType.COUNT_DISTINCT );

    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add( AggregationType.NONE );
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add( AggregationType.COUNT );
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add( AggregationType.COUNT_DISTINCT );

  }

  public BaseModelerWorkspaceHelper( String locale ) {
    BaseModelerWorkspaceHelper.locale = locale;
    autoModelStrategy = new SimpleAutoModelStrategy( locale );
  }

  public void populateDomain( ModelerWorkspace model ) throws ModelerException {
    Domain domain = model.getDomain();
    domain.setId( model.getModelName() );

    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.REPORTING );

    if ( model.getModelSource() != null ) {
      model.getModelSource().serializeIntoDomain( domain );
    }

    logicalModel.setId( "MODEL_1" );
    logicalModel.setName( new LocalizedString( locale, model.getModelName() ) );
    logicalModel.setProperty( "AGILE_BI_VERSION", AGILE_BI_VERSION );

    populateCategories( model );

    // =========================== OLAP ===================================== //
    if ( model.supportsOlap( domain ) ) {
      logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
      logicalModel.setId( "MODEL_1" + BaseModelerWorkspaceHelper.OLAP_SUFFIX );
      logicalModel
          .setName( new LocalizedString( locale, model.getModelName() + BaseModelerWorkspaceHelper.OLAP_SUFFIX ) );
      logicalModel.setProperty( "AGILE_BI_VERSION", AGILE_BI_VERSION );
    }

    MainModelNode mainModelNode = model.getModel();
    if ( mainModelNode == null ) {
      return;
    }
    DimensionMetaDataCollection dimensions = mainModelNode.getDimensions();
    if ( dimensions.size() == 0 ) {
      return;
    }

    LogicalTable factTable = null;
    // check to see if there's only one effective table
    if ( logicalModel.getLogicalTables().size() == 1 ) {
      factTable = logicalModel.getLogicalTables().get( 0 );
    } else { // otherwise we're in a multi-table situation, find the table flagged as the fact table
      Object prop;
      for ( LogicalTable lTable : logicalModel.getLogicalTables() ) {
        prop = lTable.getPhysicalTable().getProperty( "FACT_TABLE" );
        if ( prop == null ) {
          continue;
        }
        if ( ( (Boolean) prop ).booleanValue() == false ) {
          continue;
        }
        factTable = lTable;
        break;
      }
    }

    if ( factTable == null ) {
      throw new IllegalStateException( "Fact table is missing." );
    }
    List<OlapDimensionUsage> usages = new ArrayList<OlapDimensionUsage>();
    List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
    List<OlapMeasure> measures = new ArrayList<OlapMeasure>();

    for ( DimensionMetaData dim : dimensions ) {

      OlapDimension dimension = new OlapDimension();
      String dimTitle = dim.getName();

      dimension.setName( dimTitle );
      boolean isTimeDimension = dim.isTimeDimension();
      dimension.setTimeDimension( isTimeDimension );

      List<OlapHierarchy> hierarchies = new ArrayList<OlapHierarchy>();

      for ( HierarchyMetaData hier : dim ) {
        OlapHierarchy hierarchy = new OlapHierarchy( dimension );
        hierarchy.setName( hier.getName() );
        List<OlapHierarchyLevel> levels = new ArrayList<OlapHierarchyLevel>();

        for ( LevelMetaData lvl : hier ) {
          OlapHierarchyLevel level = new OlapHierarchyLevel( hierarchy );
          level.setName( lvl.getName() );
          if ( isTimeDimension ) {
            TimeRole timeRole = (TimeRole) lvl.getDataRole();
            if ( timeRole != null ) {
              level.setLevelType( timeRole.getMondrianAttributeValue() );
            }
          }
          LogicalColumn lCol = lvl.getLogicalColumn();

          if ( lCol != null ) {

            // Due to a bug in LogicalTable's clone() logical columns will be a child of an OLAP while reporting a
            // different parent.
            LogicalTable supposedLTable = lCol.getLogicalTable();
            LogicalTable olapCloneLTable = findOlapCloneForTableInDomain( supposedLTable, domain );

            hierarchy.setLogicalTable( olapCloneLTable );
            if ( !olapCloneLTable.getLogicalColumns().contains( lCol ) ) {
              olapCloneLTable.addLogicalColumn( lCol );
            }

            for ( IMemberAnnotation anno : lvl.getMemberAnnotations().values() ) {
              if ( anno != null ) {
                anno.saveAnnotations( level );
              }
            }

            level.setReferenceColumn( lCol );
            hierarchy.setLogicalTable( olapCloneLTable );
            if ( logicalModel.getLogicalTables().size() > 1 ) { // only do this for multi-table situations
              hierarchy.setPrimaryKey( findPrimaryKeyFor( logicalModel, factTable, olapCloneLTable ) );
            }

            lCol = lvl.getLogicalOrdinalColumn();
            if ( lCol != null ) {
              level.setReferenceOrdinalColumn( lCol );
            }

            lCol = lvl.getLogicalCaptionColumn();
            if ( lCol != null ) {
              level.setReferenceCaptionColumn( lCol );
            }
          }

          for ( MemberPropertyMetaData memberProp : lvl ) {
            LogicalColumn lc = memberProp.getLogicalColumn();
            if ( lc != null && !level.getLogicalColumns().contains( lc ) ) {
              if ( memberProp.getDescription() != null ) {
                lc.setDescription( new LocalizedString( getLocale(), memberProp.getDescription() ) );
              }
              level.getLogicalColumns().add( lc );
            }
          }
          if ( lvl.getDescription() != null && !lvl.getDescription().equals( "" ) ) {
            OlapAnnotation description = new OlapAnnotation();
            description.setName( "description." + getLocale() );
            description.setValue( lvl.getDescription() );
            level.getAnnotations().add( description );
          }
          level.setHavingUniqueMembers( lvl.isUniqueMembers() );
          level.setHidden( lvl.isHidden() );
          levels.add( level );
        }

        hierarchy.setHierarchyLevels( levels );
        hierarchies.add( hierarchy );
      }

      if ( hierarchies.isEmpty() ) {
        // create a default hierarchy
        OlapHierarchy defaultHierarchy = new OlapHierarchy( dimension );

        defaultHierarchy.setLogicalTable( factTable ); // TODO: set this to what???

        hierarchies.add( defaultHierarchy );
      }

      dimension.setHierarchies( hierarchies );

      olapDimensions.add( dimension );
      OlapDimensionUsage usage = new OlapDimensionUsage( dimension.getName(), dimension );
      usages.add( usage );

    }

    List<OlapCube> existingCubes = (List<OlapCube>) logicalModel.getProperty( "olap_cubes" );
    OlapCube cube = existingCubes == null || existingCubes.isEmpty() ? new OlapCube() : existingCubes.get( 0 );
    cube.setLogicalTable( factTable );
    // TODO find a better way to generate default names
    //cube.setName( BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.CubeName", model.getModelName() ) ); //$NON-NLS-1$
    cube.setName( model.getModelName() ); //$NON-NLS-1$
    cube.setOlapDimensionUsages( usages );

    Map<String, LogicalColumn> backingColumns = new HashMap<String, LogicalColumn>();
    for ( MeasureMetaData f : model.getModel().getMeasures() ) {
      LogicalColumn lCol = f.getLogicalColumn();
      if ( f.getDescription() != null && !f.getDescription().equals( "" ) ) {
        lCol.setDescription( new LocalizedString( getLocale(), f.getDescription() ) );
      }
      LogicalTable lTable = lCol.getLogicalTable();
      OlapMeasure measure = new OlapMeasure();

      String colKey = lTable.getId() + "." + lCol.getId();
      // see if any measures already are using this LogicalColumn. if so, clone it.
      if ( backingColumns.containsKey( colKey ) ) {
        // already used, duplicate it
        LogicalColumn clone = (LogicalColumn) lCol.clone();
        clone.setId( uniquify( clone.getId(), lTable.getLogicalColumns() ) );
        lCol = clone;
      } else {
        backingColumns.put( colKey, lCol );
      }

      if ( !lTable.getLogicalColumns().contains( lCol ) ) {
        lTable.addLogicalColumn( lCol );
      }

      if ( f.getDefaultAggregation() != null ) {
        lCol.setAggregationType( f.getDefaultAggregation() );
      }

      setLogicalColumnFormat( f.getFormat(), lCol );

      measure.setName( f.getName() );

      measure.setLogicalColumn( lCol );

      measure.setHidden( f.isHidden() );

      measures.add( measure );
    }

    cube.setOlapMeasures( measures );

    if ( olapDimensions.size() > 0 ) { // Metadata OLAP generator doesn't like empty lists.
      logicalModel.setProperty( "olap_dimensions", olapDimensions ); //$NON-NLS-1$
    }
    List<OlapCube> cubes = new ArrayList<OlapCube>();
    cubes.add( cube );
    logicalModel.setProperty( "olap_cubes", cubes ); //$NON-NLS-1$

  }

  private LogicalTable findOlapCloneForTableInDomain( LogicalTable supposedLTable, Domain domain ) {
    if ( supposedLTable.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
      return supposedLTable;
    }
    for ( LogicalTable table : domain.getLogicalModels().get( 1 ).getLogicalTables() ) {
      if ( table.getId().equals( supposedLTable.getId() + BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
        return table;
      }
    }
    throw new IllegalStateException( "Unable to find a OLAP copy for table: " + supposedLTable.getId() );
  }

  private LogicalColumn findPrimaryKeyFor( LogicalModel model, LogicalTable factTable, LogicalTable dimTable ) {
    LogicalRelationship ship = model.findRelationshipUsing( dimTable, factTable );
    if ( ship == null ) {
      throw new IllegalStateException( "Unable to find a primary key for table: " + dimTable.getId() );
    }

    if ( ship.getFromTable().equals( dimTable ) ) {
      return ship.getFromColumn();
    } else {
      return ship.getToColumn();
    }
  }

  public static final String uniquify( final String id, final List<? extends IConcept> concepts ) {
    boolean gotNew = false;
    boolean found = false;
    int conceptNr = 1;
    String newId = id;
    while ( !gotNew ) {
      for ( IConcept concept : concepts ) {
        if ( concept.getId().equalsIgnoreCase( newId ) ) {
          found = true;
          break;
        }
      }
      if ( found ) {
        conceptNr++;
        newId = id + "_" + conceptNr; //$NON-NLS-1$
        found = false;
      } else {
        gotNew = true;
      }
    }
    return newId;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    BaseModelerWorkspaceHelper.locale = locale;
  }

  protected void populateCategories( ModelerWorkspace workspace ) {
    RelationalModelNode model = workspace.getRelationalModel();
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get( 0 );
    logicalModel.getCategories().clear();

    for ( CategoryMetaData catMeta : model.getCategories() ) {
      Category cat = new Category();
      cat.setName( new LocalizedString( this.getLocale(), catMeta.getName() ) );
      cat.setId( catMeta.getName() );

      for ( FieldMetaData fieldMeta : catMeta ) {
        LogicalColumn lCol = fieldMeta.getLogicalColumn();
        LogicalTable lTable = lCol.getLogicalTable();

        if ( !lTable.getLogicalColumns().contains( lCol ) ) {
          lTable.addLogicalColumn( lCol );
        }

        lCol.setName( new LocalizedString( locale, fieldMeta.getName() ) );
        AggregationType type = fieldMeta.getDefaultAggregation();
        lCol.setAggregationType( type );

        setLogicalColumnFormat( fieldMeta.getFormat(), lCol );

        Set<AggregationType> possibleAggs = new HashSet<AggregationType>();
        possibleAggs.add( fieldMeta.getDefaultAggregation() );
        possibleAggs.addAll( fieldMeta.getSelectedAggregations() );
        lCol.setAggregationList( Arrays.<AggregationType>asList( possibleAggs
            .toArray( new AggregationType[possibleAggs.size()] ) ) );
        cat.addLogicalColumn( lCol );

      }
      logicalModel.addCategory( cat );
    }
  }

  private void setLogicalColumnFormat( String format, LogicalColumn lCol ) {
    String formatMask = format;
    if ( BaseAggregationMetaDataNode.FORMAT_NONE.equals( formatMask )
        || ( formatMask == null || formatMask.equals( "" ) ) ) {
      formatMask = null;
    }
    if ( formatMask != null ) {
      lCol.setProperty( "mask", formatMask ); //$NON-NLS-1$
    } else {
      // remove old mask that might have been set
      if ( lCol.getChildProperty( "mask" ) != null ) { //$NON-NLS-1$
        lCol.removeChildProperty( "mask" ); //$NON-NLS-1$
      }
    }

  }

  /**
   * Builds an OLAP model that is attribute based.
   * 
   * @param workspace
   */
  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {
    autoModelStrategy.autoModelOlap( workspace, getMainModelNode( workspace ) );
  }

  /**
   * Builds an OLAP model that is attribute based.
   * 
   * @param workspace
   */
  public void autoModelFlatInBackground( final ModelerWorkspace workspace ) throws ModelerException {
    autoModelFlat( workspace );
  }

  public void sortFields( List<AvailableField> availableFields ) {
    Collections.sort( availableFields, new Comparator<AvailableField>() {
      public int compare( AvailableField o1, AvailableField o2 ) {
        if ( o1 == null && o2 == null ) {
          return 0;
        } else if ( o1 == null ) {
          return -1;
        } else if ( o2 == null ) {
          return 1;
        }
        String name1 = o1.getDisplayName();
        String name2 = o2.getDisplayName();
        if ( name1 == null && name2 == null ) {
          return 0;
        } else if ( name1 == null ) {
          return -1;
        } else if ( name2 == null ) {
          return 1;
        }
        return name1.compareToIgnoreCase( name2 );
      }
    } );
  }

  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * 
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlat( ModelerWorkspace workspace ) throws ModelerException {
    autoModelStrategy.autoModelRelational( workspace, getRelationalModelNode( workspace ) );
  }

  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * 
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlatInBackground( ModelerWorkspace workspace ) throws ModelerException {
    autoModelRelationalFlat( workspace );
  }

  public static String getCleanCategoryName( String name, ModelerWorkspace workspace, int index ) {
    if ( name == null ) {
      return "Category " + index;
    } else if ( name.equals( "LOGICAL_TABLE_1" ) || name.equals( "INLINE_SQL_1" ) ) {
      if ( workspace.getModel().getName() != null ) {
        return workspace.getModel().getName();
      } else {
        return "Category " + index;
      }
    } else {
      return name;
    }
  }

  protected abstract MainModelNode getMainModelNode( ModelerWorkspace workspace );

  protected abstract RelationalModelNode getRelationalModelNode( ModelerWorkspace workspace );

  public AutoModelStrategy getAutoModelStrategy() {
    return autoModelStrategy;
  }

  public void setAutoModelStrategy( AutoModelStrategy autoModelStrategy ) {
    this.autoModelStrategy = autoModelStrategy;
  }
}
