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

package org.pentaho.agilebi.modeler.models.annotations.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.metastore.persist.MetaStoreAttribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class DataProvider implements Serializable {

  private static final long serialVersionUID = -2098838998948067999L;

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String schemaName;

  @MetaStoreAttribute
  private String tableName;

  @MetaStoreAttribute
  private String databaseMetaNameRef;

  @MetaStoreAttribute
  private List<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>(  );

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  public String getDatabaseMetaNameRef() {
    return databaseMetaNameRef;
  }

  public void setDatabaseMetaNameRef( String databaseMetaNameRef ) {
    this.databaseMetaNameRef = databaseMetaNameRef;
  }

  public List<ColumnMapping> getColumnMappings() {
    return columnMappings;
  }

  public void setColumnMappings( List<ColumnMapping> columnMappings ) {
    this.columnMappings = columnMappings;
  }

  @Override
  public boolean equals( Object obj ) {

    try {
      if ( !EqualsBuilder.reflectionEquals( this, obj ) ) {
        return false;
      }

      // manually check columnMappings
      DataProvider toCompare = (DataProvider) obj;
      List<ColumnMapping> thisMap = this.getColumnMappings();
      List<ColumnMapping> toCompareMap = toCompare.getColumnMappings();

      if ( thisMap != null && toCompareMap != null ) {
        if ( thisMap.size() != toCompareMap.size() ) {
          return false;
        }

        for ( int i = 0; i < thisMap.size(); i++ ) {
          if ( !thisMap.get( i ).equals( toCompareMap.get( i ) ) ) {
            return false;
          }
        }
      }

      return true;
    } catch ( Exception e ) {
      return false;
    }
  }
}
