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
package org.pentaho.agilebi.modeler.models.annotations;

import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.metadata.automodel.PhysicalTableImporter;

import java.util.List;

public class SharedDimensionImportStrategy implements PhysicalTableImporter.ImportStrategy {
  private DataProvider dataProvider;

  public SharedDimensionImportStrategy( DataProvider dataProvider ) {
    this.dataProvider = dataProvider;
  }

  @Override public boolean shouldInclude( ValueMetaInterface valueMeta ) {
//    List<ColumnMapping> columnMappings = dataProvider.getColumnMappings();
//    for ( ColumnMapping columnMapping : columnMappings ) {
//      if ( columnMapping.getColumnName().equalsIgnoreCase( valueMeta.getName() ) ) {
//        return true;
//      }
//    }
    return true;
  }

  @Override public String displayName( ValueMetaInterface valueMeta ) {
    List<ColumnMapping> columnMappings = dataProvider.getColumnMappings();
    for ( ColumnMapping columnMapping : columnMappings ) {
      if ( columnMapping.getColumnName().equalsIgnoreCase( valueMeta.getName() ) ) {
        return columnMapping.getName();
      }
    }
    return valueMeta.getName();
  }
}
