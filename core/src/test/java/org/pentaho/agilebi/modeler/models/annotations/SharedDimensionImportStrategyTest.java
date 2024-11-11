/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.di.core.row.ValueMeta;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SharedDimensionImportStrategyTest {
  @Test
  public void testDefaultsToValueMetaName() throws Exception {
    DataProvider dataProvider = new DataProvider();
    ArrayList<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();
    dataProvider.setColumnMappings( columnMappings );
    SharedDimensionImportStrategy importStrategy = new SharedDimensionImportStrategy( dataProvider );
    ValueMeta valueMeta = new ValueMeta();
    valueMeta.setName( "c1" );
    assertEquals( "c1", importStrategy.displayName( valueMeta ) );
  }

  @Test
  public void testGetsDisplayNameFromDataProvider() throws Exception {
    DataProvider dataProvider = new DataProvider();
    ArrayList<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();
    ColumnMapping mapping1 = new ColumnMapping();
    mapping1.setName( "Column One" );
    mapping1.setColumnName( "c1" );
    columnMappings.add( mapping1 );
    dataProvider.setColumnMappings( columnMappings );
    SharedDimensionImportStrategy importStrategy = new SharedDimensionImportStrategy( dataProvider );
    ValueMeta valueMeta = new ValueMeta();
    valueMeta.setName( "c1" );
    assertTrue( importStrategy.shouldInclude( valueMeta ) );
    assertEquals( "Column One", importStrategy.displayName( valueMeta ) );
  }
}
