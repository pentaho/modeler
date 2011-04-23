/*
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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.strategy;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;

import java.util.HashSet;
import java.util.List;

/**
 * Created: 4/22/11
 *
 * @author rfellows
 */
public class StarSchemaAutoModelStrategy extends SimpleAutoModelStrategy {
  public StarSchemaAutoModelStrategy(String locale) {
    super(locale);
  }

  @Override
  public void autoModelOlap(ModelerWorkspace workspace, MainModelNode mainModel) throws ModelerException {
    mainModel.setName(workspace.getModelName());
    workspace.setModel(mainModel);
    workspace.getModel().getDimensions().clear();
    workspace.getModel().getMeasures().clear();

    final boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true, !mainModel.getSuppressEvents());

    // remove all logical columns from existing logical tables
    for (LogicalTable table : workspace.getDomain().getLogicalModels().get(0).getLogicalTables()) {
      if (table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
        table.getLogicalColumns().clear();
      }
    }

    HashSet<String> existingMeasures = new HashSet<String>();
    List<AvailableTable> tableList = workspace.getAvailableTables().getAsAvailableTablesList();
    for (AvailableTable table : tableList) {
      boolean isFact = table.isFactTable();
      if (isFact) {
        for( AvailableField field : table.getAvailableFields() ) {
          // create measures from the numeric
          DataType dataType = field.getPhysicalColumn().getDataType();
          if( dataType == DataType.NUMERIC) {
            if (!existingMeasures.contains(field.getName())) {
              // create a measure
              MeasureMetaData measure = workspace.createMeasureForNode(field);
              workspace.getModel().getMeasures().add(measure);
              existingMeasures.add(field.getName());
            }
          }
        }
      } else {
        // create a new dimension per table since it is not the fact table
        DimensionMetaData dim = new DimensionMetaData(table.getName());
        dim.setExpanded(false);

        for( AvailableField field : table.getAvailableFields() ) {

          // create a hierarchy per field
          HierarchyMetaData hierarchy = new HierarchyMetaData(field.getName());
          hierarchy.setParent(dim);
          hierarchy.setExpanded(false);
          dim.add(hierarchy);

          // create a level
          LevelMetaData level = workspace.createLevelForParentWithNode(hierarchy, workspace.createColumnBackedNode(field, ModelerPerspective.ANALYSIS));
          if (level != null) {
            hierarchy.add(level);
          }

        }
        workspace.addDimension(dim);
      }

    }
    if (!mainModel.getSuppressEvents()) {
      workspace.setModelIsChanging(prevChangeState);
      workspace.setSelectedNode(workspace.getModel());
    }
  }

}
