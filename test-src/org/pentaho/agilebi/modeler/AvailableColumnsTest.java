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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.metadata.model.LogicalModel;

/**
 * User: nbaker Date: 4/8/11
 */
public class AvailableColumnsTest extends AbstractModelerTest {

  @Test
  public void testAvailableColumnPossibleMeasure() throws Exception {
    super.generateMultiStarTestDomain();
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get( 0 );
    List<AvailableTable> tablesList = workspace.getAvailableTables().getAsAvailableTablesList();
    int fields = tablesList.get( 0 ).getAvailableFields().size();
    assertTrue( fields > 2 );

    // replicating work that would be done by the auto-modeler
    AvailableField field1 = tablesList.get( 0 ).getAvailableFields().get( 0 );

    workspace.getDomain().getLogicalModels().get( 0 ).setProperty( "AGILE_BI_VERSION", "2.0" );
    tablesList.get( 0 ).getPhysicalTable().setProperty( "FACT_TABLE", true );

    AvailableField field2 = tablesList.get( 0 ).getAvailableFields().get( 1 );

    // Add the first available field as a measure. This should work
    workspace.getModel().getMeasures().clear();
    ModelerController controller = new ModelerController( workspace );
    controller.setSelectedFields( new Object[] { field1 } );
    controller.getDimTreeHelper().setSelectedTreeItem( workspace.getModel().getMeasures() );
    controller.addField();
    assertEquals( 1, workspace.getModel().getMeasures().size() );
    assertEquals( field1.getPhysicalColumn(), workspace.getModel().getMeasures().get( 0 ).getLogicalColumn()
        .getPhysicalColumn() );

    // Attempt to add the second field as a measure. It should fail and the list remain the same
    controller.setSelectedFields( new Object[] { field2 } );
    tablesList.get( 0 ).getPhysicalTable().setProperty( "FACT_TABLE", false );
    controller.addField();
    assertEquals( 1, workspace.getModel().getMeasures().size() );

  }
}
