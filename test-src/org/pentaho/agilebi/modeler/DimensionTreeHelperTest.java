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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.LogicalTable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created: 4/18/11
 *
 * @author rfellows
 */
public class DimensionTreeHelperTest extends AbstractModelerTest {

  DimensionTreeHelper helper;

  @Before
  public void getReady() throws Exception {
    generateTestDomain();
    ModelerWorkspaceHelper workspaceHelper = new ModelerWorkspaceHelper( "en-US" );
    workspaceHelper.autoModelFlat( workspace );
    workspaceHelper.autoModelRelationalFlat( workspace );
    helper = new DimensionTreeHelper();
    helper.setWorkspace( workspace );
  }

  // clearing the dimension tree should not remove links to OLAP logical columns
  @Test
  public void testClearTreeModel() throws Exception {
    int dimensionCount = workspace.getModel().getDimensions().size();
    int measureCount = workspace.getModel().getMeasures().size();
    TableCountInfo info = getTableCountInfo();

    assertTrue( dimensionCount > 0 );
    assertTrue( measureCount > 0 );
    assertTrue( info.tableCount > 0 );
    assertTrue( info.columnCount > 0 );

    helper.clearTreeModel();

    TableCountInfo infoAfter = getTableCountInfo();

    assertEquals( 0, workspace.getModel().getDimensions().size() );
    assertEquals( 0, workspace.getModel().getMeasures().size() );
    assertEquals( info.tableCount, infoAfter.tableCount );
    assertEquals( info.columnCount, infoAfter.columnCount );

  }

  // removing an olap measure/dimension should not remove it's backing logical column
  @Test
  public void testRemoveField_Measure() {
    int measureCount = workspace.getModel().getMeasures().size();
    MeasureMetaData measure = workspace.getModel().getMeasures().get( 0 );
    TableCountInfo info = getTableCountInfo();
    helper.setSelectedTreeItem( measure );
    helper.removeField();

    // make sure the logical column has not been removed
    assertEquals( info, getTableCountInfo() );

    // make sure the measure is gone
    assertEquals( measureCount - 1, workspace.getModel().getMeasures().size() );
  }

  @Test
  public void testRemoveField_Dimension() {
    int dimCount = workspace.getModel().getDimensions().size();
    DimensionMetaData dim = workspace.getModel().getDimensions().get( 0 );
    TableCountInfo info = getTableCountInfo();
    helper.setSelectedTreeItem( dim );
    helper.removeField();

    // make sure the logical column has not been removed
    assertEquals( info, getTableCountInfo() );

    // make sure the dim is gone
    assertEquals( dimCount - 1, workspace.getModel().getDimensions().size() );
  }

  @Test
  public void testRemoveField_Hierarchy() {
    int hierCount = workspace.getModel().getDimensions().get( 0 ).size();
    HierarchyMetaData hier = workspace.getModel().getDimensions().get( 0 ).get( 0 );
    TableCountInfo info = getTableCountInfo();
    helper.setSelectedTreeItem( hier );
    helper.removeField();

    // make sure the logical column has not been removed
    assertEquals( info, getTableCountInfo() );

    // make sure the hierarchy is gone
    assertEquals( hierCount - 1, workspace.getModel().getDimensions().get( 0 ).size() );
  }

  @Test
  public void testRemoveField_Level() {
    int levelCount = workspace.getModel().getDimensions().get( 0 ).get( 0 ).size();
    LevelMetaData level = workspace.getModel().getDimensions().get( 0 ).get( 0 ).get( 0 );
    TableCountInfo info = getTableCountInfo();
    helper.setSelectedTreeItem( level );
    helper.removeField();

    // make sure the logical column has not been removed
    assertEquals( info, getTableCountInfo() );

    // make sure the hierarchy is gone
    assertEquals( levelCount - 1, workspace.getModel().getDimensions().get( 0 ).get( 0 ).size() );
  }

  private TableCountInfo getTableCountInfo() {
    TableCountInfo info = new TableCountInfo();
    for ( LogicalTable table : workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables() ) {
      if ( table.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) {
        info.tableCount++;
        info.columnCount += table.getLogicalColumns().size();
      }
    }
    return info;
  }

  class TableCountInfo {
    int tableCount = 0;
    int columnCount = 0;

    @Override
    public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }

      TableCountInfo that = (TableCountInfo) o;

      if ( columnCount != that.columnCount ) {
        return false;
      }
      if ( tableCount != that.tableCount ) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = tableCount;
      result = 31 * result + columnCount;
      return result;
    }
  }

}
