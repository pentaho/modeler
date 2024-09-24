/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
public class MultiTableAutoModelStrategyIT extends AbstractModelerTest {

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
