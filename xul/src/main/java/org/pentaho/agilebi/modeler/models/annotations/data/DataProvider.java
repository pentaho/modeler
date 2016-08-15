/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
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
