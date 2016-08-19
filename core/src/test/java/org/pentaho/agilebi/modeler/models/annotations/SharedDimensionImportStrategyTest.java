/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2015 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
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
