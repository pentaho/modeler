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

package org.pentaho.agilebi.modeler.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;

/**
 * Created: 4/19/11
 * 
 * @author rfellows
 */
public class SimpleAutoModelStrategy implements AutoModelStrategy {

  private String locale;
  protected GeoContext geoContext;

  public SimpleAutoModelStrategy( String locale ) {
    this( locale, null );
  }

  /**
   * Create a new SimpleAutoModelStrategy with geo-aware context. This context will attempt to identify geographic
   * fields in the model and build appropriate geography dimensions.
   * 
   * @param locale
   * @param geoContext
   */
  public SimpleAutoModelStrategy( String locale, GeoContext geoContext ) {
    this.locale = locale;
    this.geoContext = geoContext;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    this.locale = locale;
  }

  public GeoContext getGeoContext() {
    return geoContext;
  }

  public void setGeoContext( GeoContext geoContext ) {
    this.geoContext = geoContext;
  }

  /**
   * Generates a basic OLAP model consisting of one Dimension/Hierarchy/Level combination per column and one Measure per
   * unique column name. Columns belonging to separate tables have no distinction here
   * 
   * @param workspace
   * @param mainModel
   * @throws ModelerException
   */
  @Override
  public void autoModelOlap( ModelerWorkspace workspace, MainModelNode mainModel ) throws ModelerException {
    mainModel.setName( workspace.getModelName() );
    workspace.setModel( mainModel );
    DimensionMetaDataCollection dims = workspace.getModel().getDimensions();
    dims.clear();
    dims.setExpanded( true );
    MeasuresCollection measures = workspace.getModel().getMeasures();
    measures.setExpanded( false );
    measures.clear();

    final boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging( true, !mainModel.getSuppressEvents() );

    // remove all logical columns from existing logical tables
    for ( LogicalTable table : workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables() ) {
      if ( table.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
        table.getLogicalColumns().clear();
      }
    }

    HashSet<String> existingMeasures = new HashSet<String>();
    List<AvailableTable> tableList = workspace.getAvailableTables().getAsAvailableTablesList();

    for ( AvailableTable table : tableList ) {
      for ( AvailableField field : table.getAvailableFields() ) {

        // only add the field if it is not a geo field, they will be handled separately
        if ( !isGeoField( field ) ) {
          DataType dataType = field.getPhysicalColumn().getDataType();
          if ( dataType == DataType.NUMERIC ) {
            if ( !existingMeasures.contains( field.getName() ) ) {
              // create a measure
              MeasureMetaData measure = workspace.createMeasureForNode( field );
              workspace.getModel().getMeasures().add( measure );
              existingMeasures.add( field.getName() );
            }
          }
          // create a dimension
          workspace.addDimensionFromNode( workspace.createColumnBackedNode( field, ModelerPerspective.ANALYSIS ) );
        }
      }
    }

    addGeoDimensions( dims, workspace );

    for ( DimensionMetaData dim : dims ) {
      dim.setExpanded( false );
    }
    if ( !mainModel.getSuppressEvents() ) {
      workspace.setModelIsChanging( prevChangeState );
      workspace.setSelectedNode( workspace.getModel() );
    }
  }

  /**
   * Generates a basic Relational model consisting of one Category per table and one Field per column within that table
   * 
   * @param workspace
   * @param relationalModelNode
   * @throws ModelerException
   */
  @Override
  public void autoModelRelational( ModelerWorkspace workspace, RelationalModelNode relationalModelNode )
    throws ModelerException {
    relationalModelNode.setName( workspace.getRelationalModelName() );

    workspace.setRelationalModel( relationalModelNode );
    final boolean prevChangeState = workspace.isModelChanging();

    workspace.getRelationalModel().getCategories().clear();

    workspace.setRelationalModelIsChanging( true, !relationalModelNode.getSuppressEvents() );

    // remove all logical columns from existing logical tables
    for ( LogicalTable table : workspace.getDomain().getLogicalModels().get( 0 ).getLogicalTables() ) {
      if ( !table.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
        table.getLogicalColumns().clear();
      }
    }

    List<? extends IPhysicalTable> tables = workspace.getDomain().getPhysicalModels().get( 0 ).getPhysicalTables();
    Set<String> tableIds = new HashSet<String>();

    List<AvailableTable> tablesList = workspace.getAvailableTables().getAsAvailableTablesList();

    for ( IPhysicalTable table : tables ) {
      if ( !tableIds.contains( table.getId() ) ) {
        tableIds.add( table.getId() );
        String catName =
            BaseModelerWorkspaceHelper.getCleanCategoryName( table.getName( locale ), workspace, tableIds.size() );

        CategoryMetaData category = new CategoryMetaData( catName );
        category.setExpanded( true );
        for ( AvailableTable aTable : tablesList ) {
          if ( aTable.isSameUnderlyingPhysicalTable( table ) ) {
            for ( AvailableField field : aTable.getAvailableFields() ) {
              if ( field.getPhysicalColumn().getPhysicalTable().getId().equals( table.getId() ) ) {
                category.add( workspace.createFieldForParentWithNode( category, field ) );
              }
            }
          }
        }

        relationalModelNode.getCategories().add( category );
      }
    }

    if ( !relationalModelNode.getSuppressEvents() ) {
      workspace.setRelationalModelIsChanging( prevChangeState );
      workspace.setSelectedRelationalNode( workspace.getRelationalModel() );
    }
  }

  protected boolean isGeoField( AvailableField field ) {
    if ( geoContext != null ) {
      return geoContext.matchFieldToGeoRole( field ) != null;
    } else {
      return false;
    }
  }

  protected void addGeoDimensions( List<DimensionMetaData> dims, ModelerWorkspace workspace ) {
    if ( geoContext != null ) {
      // get any geographic dimensions detected, add them
      List<DimensionMetaData> geoDims = geoContext.buildDimensions( workspace );
      dims.addAll( geoDims );
      Collections.sort( dims, new Comparator<DimensionMetaData>() {
        @Override
        public int compare( DimensionMetaData dimMeta1, DimensionMetaData dimMeta2 ) {
          return dimMeta1.getDisplayName().compareToIgnoreCase( dimMeta2.getDisplayName() );
        }
      } );
    }
  }

}
