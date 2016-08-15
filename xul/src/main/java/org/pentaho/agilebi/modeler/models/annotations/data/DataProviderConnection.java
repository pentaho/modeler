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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.models.annotations.data;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Rowell Belen
 *         Metastore-compatible wrapper for DatabaseMeta
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

  public DatabaseMeta createDatabaseMeta() {

    try {
      DatabaseMeta databaseMeta = new DatabaseMeta();
      BeanUtils.copyProperties( databaseMeta, this );

      if ( this.getAttributeList() != null && !this.getAttributeList().isEmpty() ) {
        Properties properties = new Properties();
        for ( NameValueProperty nameValueProperty : this.getAttributeList() ) {
          properties.setProperty( nameValueProperty.getName(), nameValueProperty.getValue() );
        }
        databaseMeta.setAttributes( properties );
      }

      return databaseMeta;
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return null;
  }

  public void populateFrom( DatabaseMeta databaseMeta ) {
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

  public void persistDatabaseMeta( IMetaStore metaStore, DatabaseMeta databaseMeta ) {

    if ( metaStore == null ) {
      return;
    }

    try {
      DatabaseMetaStoreUtil.createDatabaseElement( metaStore, databaseMeta );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public DatabaseMeta load( final IMetaStore metaStore ) {

    try {

      if ( metaStore == null ) {
        return null;
      }

      List<DatabaseMeta> databaseMetaList = DatabaseMetaStoreUtil.getDatabaseElements( metaStore );
      for ( DatabaseMeta databaseMeta : databaseMetaList ) {
        if ( databaseMeta.getName() != null && StringUtils.isNotBlank( databaseMeta.getName() )
            && StringUtils.equals( databaseMeta.getName(), getName() ) ) {
          populateFrom( databaseMeta );
          return databaseMeta;
        }
      }

    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public boolean equals( Object obj ) {

    try {
      if ( !EqualsBuilder.reflectionEquals( this, obj ) ) {
        return false;
      }

      // manually check columnMappings
      DataProviderConnection toCompare = (DataProviderConnection) obj;
      List<NameValueProperty> thisAttributeList = this.getAttributeList();
      List<NameValueProperty> toCompareAttributeList = toCompare.getAttributeList();

      if ( thisAttributeList != null && toCompareAttributeList != null ) {
        if ( thisAttributeList.size() != toCompareAttributeList.size() ) {
          return false;
        }

        for ( int i = 0; i < thisAttributeList.size(); i++ ) {
          if ( !thisAttributeList.get( i ).equals( toCompareAttributeList.get( i ) ) ) {
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
