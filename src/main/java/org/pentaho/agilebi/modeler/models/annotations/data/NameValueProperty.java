package org.pentaho.agilebi.modeler.models.annotations.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.metastore.persist.MetaStoreAttribute;

import java.io.Serializable;

/**
 * @author Rowell Belen
 */
public class NameValueProperty implements Serializable {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String value;

  public NameValueProperty() {
  }

  public NameValueProperty( String name, String value ) {
    setName( name );
    setValue( value );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  @Override
  public boolean equals( Object obj ) {
    return EqualsBuilder.reflectionEquals( this, obj );
  }
}
