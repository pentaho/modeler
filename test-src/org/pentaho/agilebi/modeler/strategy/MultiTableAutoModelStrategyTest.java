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

package org.pentaho.agilebi.modeler.strategy;

import static junit.framework.Assert.*;

import java.util.HashMap;

import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;

/**
 * Created: 4/19/11
 *
 * @author rfellows
 */
public class MultiTableAutoModelStrategyTest extends AbstractModelerTest {

  private static final String LOCALE = "en-US";

  @Test
  public void testAutoModelOlap() throws Exception {
    // expect one dim per table, one hierarchy per column, one level per hierarchy

    generateMultiStarTestDomain();
    MultiTableAutoModelStrategy strategy = new MultiTableAutoModelStrategy( LOCALE );
    try {
      strategy.autoModelOlap( workspace, new MainModelNode() );
    } catch ( UnsupportedOperationException e ) {
      return;
    }
    fail( "Should have thrown an UnsupportedOperationException" );

  }

  @Test
  public void testAutoModelRelational() throws Exception {
    generateMultiTableTestDomain();
    MultiTableAutoModelStrategy strategy = new MultiTableAutoModelStrategy( LOCALE );

    strategy.autoModelRelational( workspace, new RelationalModelNode() );

    // one category per table
    // one field per table.column

    HashMap<String, Integer> tables = new HashMap<String, Integer>();
    for ( AvailableTable table : workspace.getAvailableTables().getAsAvailableTablesList() ) {
      tables.put( table.getName(), table.getAvailableFields().size() );
    }

    assertEquals( tables.size(), workspace.getRelationalModel().getCategories().size() );
    for ( CategoryMetaData cat : workspace.getRelationalModel().getCategories() ) {
      assertTrue( tables.containsKey( cat.getName() ) );
      assertEquals( tables.get( cat.getName() ).intValue(), cat.size() );
    }

  }

}
