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

import java.util.HashSet;
import java.util.List;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;

/**
 * Created: 4/22/11
 * 
 * @author rfellows
 */
public class StarSchemaAutoModelStrategy extends SimpleAutoModelStrategy {
  public StarSchemaAutoModelStrategy( String locale ) {
    super( locale );
  }

  public StarSchemaAutoModelStrategy( String locale, GeoContext geoContext ) {
    super( locale, geoContext );
  }

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
      table.getLogicalColumns().clear();
    }

    HashSet<String> existingMeasures = new HashSet<String>();
    List<AvailableTable> tableList = workspace.getAvailableTables().getAsAvailableTablesList();
    for ( AvailableTable table : tableList ) {
      boolean isFact = table.isFactTable();
      if ( isFact ) {
        for ( AvailableField field : table.getAvailableFields() ) {
          // create measures from the numeric
          DataType dataType = field.getPhysicalColumn().getDataType();
          if ( dataType == DataType.NUMERIC ) {
            if ( !existingMeasures.contains( field.getName() ) ) {
              // create a measure
              MeasureMetaData measure = workspace.createMeasureForNode( field );
              workspace.getModel().getMeasures().add( measure );
              existingMeasures.add( field.getName() );
            }
          } else {
            // make sure the logical column for this gets added
            workspace.createColumnBackedNode( field, ModelerPerspective.ANALYSIS );
          }
        }
      } else {
        // create a new dimension per table since it is not the fact table
        DimensionMetaData dim = new DimensionMetaData( table.getName() );
        dim.setExpanded( false );

        for ( AvailableField field : table.getAvailableFields() ) {
          if ( !isGeoField( field ) ) {
            // create a hierarchy per field
            HierarchyMetaData hierarchy = new HierarchyMetaData( field.getName() );
            hierarchy.setParent( dim );
            hierarchy.setExpanded( false );
            dim.add( hierarchy );

            // create a level
            LevelMetaData level =
                workspace.createLevelForParentWithNode( hierarchy, workspace.createColumnBackedNode( field,
                    ModelerPerspective.ANALYSIS ) );
            if ( level != null ) {
              hierarchy.add( level );
            }
          }
        }
        // only add the dimension if it has hierarchies
        if ( dim.size() > 0 ) {
          workspace.addDimension( dim );
        }
      }
    }

    addGeoDimensions( workspace.getModel().getDimensions(), workspace );

    if ( !mainModel.getSuppressEvents() ) {
      workspace.setModelIsChanging( prevChangeState );
      workspace.setSelectedNode( workspace.getModel() );
    }
  }

}
