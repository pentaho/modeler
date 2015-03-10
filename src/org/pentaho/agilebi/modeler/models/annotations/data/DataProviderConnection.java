package org.pentaho.agilebi.modeler.models.annotations.data;

import org.apache.commons.beanutils.BeanUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Rowell Belen
 *         Metastore-compatible wrapper
 */
@MetaStoreElementType( name = "DataProviderConnection", description = "DataProviderConnection" )
public class DataProviderConnection implements Serializable {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String displayName;

  @MetaStoreAttribute
  private String hostname;

  @MetaStoreAttribute
  private String databaseName;

  @MetaStoreAttribute
  private String username;

  @MetaStoreAttribute
  private String password;

  @MetaStoreAttribute
  private boolean changed;

  @MetaStoreAttribute
  private List<NameValueProperty> attributeList = new ArrayList<NameValueProperty>();

  @Deprecated
  private Properties attributes;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName( String displayName ) {
    this.displayName = displayName;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName( String databaseName ) {
    this.databaseName = databaseName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public boolean isChanged() {
    return changed;
  }

  public void setChanged( boolean changed ) {
    this.changed = changed;
  }

  @Deprecated
  public Properties getAttributes() {
    return attributes;
  }

  @Deprecated
  public void setAttributes( Properties attributes ) {
    this.attributes = attributes;
  }

  public List<NameValueProperty> getAttributeList() {
    return attributeList;
  }

  public void setAttributeList( List<NameValueProperty> attributeList ) {
    this.attributeList = attributeList;
  }

  public void populate( DatabaseMeta databaseMeta ) {
    try {
      BeanUtils.copyProperties( this, databaseMeta );

      if ( this.getAttributes() != null ) {

        if ( this.getAttributeList() == null ) {
          this.setAttributeList( new ArrayList<NameValueProperty>() );
        } else {
          this.getAttributeList().clear();
        }

        Enumeration e = this.getAttributes().propertyNames();
        while ( e.hasMoreElements() ) {
          String key = (String) e.nextElement();
          this.getAttributeList().add( new NameValueProperty( key, this.getAttributes().getProperty( key ) ) );
        }
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
}
