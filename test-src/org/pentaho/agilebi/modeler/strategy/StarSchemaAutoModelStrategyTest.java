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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.nodes.annotations.IDataRoleAnnotation;
import org.pentaho.metadata.model.concept.types.DataType;

/**
 * Created: 4/22/11
 *
 * @author rfellows
 */
public class StarSchemaAutoModelStrategyTest extends AbstractModelerTest {
  private static final String LOCALE = "en-US";
  private static Properties props = null;
  private static GeoContextConfigProvider config;

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader( new File( "test-res/geoRoles.properties" ) );
    props = new Properties();
    props.load( propsReader );
    config = new GeoContextPropertiesProvider( props );
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    generateMultiStarTestDomain();
  }

  @Test
  public void testAutoModelOlap() throws Exception {
    // expect one dim per non-fact table, one hierarchy per column, one level per hierarchy
    // expect only numeric fields from the fact table to be created as measures

    StarSchemaAutoModelStrategy strategy = new StarSchemaAutoModelStrategy( LOCALE );

    strategy.autoModelOlap( workspace, new MainModelNode() );

    HashMap<String, Integer> dimTables = new HashMap<String, Integer>();
    HashSet<String> numericFactColumns = new HashSet<String>();

    for ( AvailableTable table : workspace.getAvailableTables().getAsAvailableTablesList() ) {
      if ( table.getPhysicalTable().getProperty( "FACT_TABLE" ) == null ) {
        dimTables.put( table.getName(), table.getAvailableFields().size() );
      } else {
        // this is the fact table
        for ( AvailableField field : table.getAvailableFields() ) {
          if ( field.getPhysicalColumn().getDataType() == DataType.NUMERIC
              && !numericFactColumns.contains( field.getName() ) ) {
            numericFactColumns.add( field.getName() );
          }
        }
      }
    }
    assertEquals( workspace.getAvailableTables().size() - 1, dimTables.size() );
    assertEquals( dimTables.size(), workspace.getModel().getDimensions().size() );
    for ( DimensionMetaData dim : workspace.getModel().getDimensions() ) {
      assertTrue( dimTables.containsKey( dim.getName() ) );
      // there should be one hierarchy per column
      assertEquals( dimTables.get( dim.getName() ).intValue(), dim.size() );
      for ( HierarchyMetaData hier : dim ) {
        // should only have one level
        assertEquals( 1, hier.size() );
        // level should be the same name as the hierarchy
        assertEquals( hier.getName(), hier.get( 0 ).getName() );
      }
    }

    // make sure the measures are accounted for too
    assertEquals( numericFactColumns.size(), workspace.getModel().getMeasures().size() );
  }

  @Test
  public void testAutoModelGeo() throws Exception {
    // expect one dim per non-fact table, one hierarchy per column, one level per hierarchy
    // expect only numeric fields from the fact table to be created as measures
    GeoContext geoContext = GeoContextFactory.create( config );
    StarSchemaAutoModelStrategy strategy = new StarSchemaAutoModelStrategy( LOCALE, geoContext );

    strategy.autoModelOlap( workspace, new MainModelNode() );

    HashMap<String, Integer> dimTables = new HashMap<String, Integer>();
    HashSet<String> numericFactColumns = new HashSet<String>();

    int geoFields = 0;

    for ( AvailableTable table : workspace.getAvailableTables().getAsAvailableTablesList() ) {
      if ( table.getPhysicalTable().getProperty( "FACT_TABLE" ) == null ) {
        int fields = 0;
        for ( AvailableField field : table.getAvailableFields() ) {
          if ( geoContext.matchFieldToGeoRole( field ) != null ) {
            // this is a geo field
            geoFields++;
            fields++;
          }
        }
        dimTables.put( table.getName(), table.getAvailableFields().size() - fields );
      } else {
        // this is the fact table
        for ( AvailableField field : table.getAvailableFields() ) {
          if ( field.getPhysicalColumn().getDataType() == DataType.NUMERIC
              && !numericFactColumns.contains( field.getName() ) ) {
            numericFactColumns.add( field.getName() );
          }
        }
      }
    }
    assertEquals( workspace.getAvailableTables().size() - 1, dimTables.size() );
    int actualGeoDims = 0;
    int actualGeoLevels = 0;
    for ( DimensionMetaData dim : workspace.getModel().getDimensions() ) {
      IDataRoleAnnotation dataRole =
          (IDataRoleAnnotation) dim.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE );
      if ( dataRole instanceof GeoRole ) {
        actualGeoDims++;
        assertEquals( 1, dim.size() ); // only one hierarchy per geo dim
        for ( LevelMetaData level : dim.get( 0 ) ) {
          actualGeoLevels++;
        }
      } else {
        assertTrue( dimTables.containsKey( dim.getName() ) );
        // there should be one hierarchy per column
        assertEquals( dimTables.get( dim.getName() ).intValue(), dim.size() );
        for ( HierarchyMetaData hier : dim ) {
          // should only have one level
          assertEquals( 1, hier.size() );
          // level should be the same name as the hierarchy
          assertEquals( hier.getName(), hier.get( 0 ).getName() );
        }
      }
    }

    // make sure the measures are accounted for too
    assertEquals( numericFactColumns.size(), workspace.getModel().getMeasures().size() );
    assertTrue( actualGeoDims > 0 );
    assertEquals( geoFields, actualGeoLevels );
  }

  @Test
  public void testAutoModelRelational() throws Exception {
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
