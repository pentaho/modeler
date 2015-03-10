package org.pentaho.agilebi.modeler.models.annotations.data;

import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.io.Serializable;

/**
 * @author Rowell Belen
 */
@MetaStoreElementType( name = "ColumnMapping", description = "ColumnMapping" )
public class ColumnMapping implements Serializable {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String columnName;

  @MetaStoreAttribute
  private DataType columnDataType;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName( String columnName ) {
    this.columnName = columnName;
  }

  public DataType getColumnDataType() {
    return columnDataType;
  }

  public void setColumnDataType( DataType columnDataType ) {
    this.columnDataType = columnDataType;
  }
}
