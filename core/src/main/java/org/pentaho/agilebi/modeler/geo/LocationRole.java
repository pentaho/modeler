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

package org.pentaho.agilebi.modeler.geo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/22/11 Time: 10:50 AM To change this template use File | Settings |
 * File Templates.
 */
public class LocationRole extends GeoRole implements Serializable {
  private static final long serialVersionUID = 308145806472677473L;
  private static final String LOCATION = "location";
  private LatLngRole latitudeRole;
  private LatLngRole longitudeRole;

  public LocationRole() {
  }

  public LocationRole( LatLngRole latitudeRole, LatLngRole longitudeRole ) {
    this.latitudeRole = latitudeRole;
    this.longitudeRole = longitudeRole;
  }

  public LocationRole( LatLngRole latitudeRole, String displayName, LatLngRole longitudeRole ) {
    this.latitudeRole = latitudeRole;
    this.displayName = displayName;
    this.longitudeRole = longitudeRole;

  }

  @Override
  public String getName() {
    return LOCATION;
  }

  public LatLngRole getLatitudeRole() {
    return latitudeRole;
  }

  public void setLatitudeRole( LatLngRole latitudeRole ) {
    this.latitudeRole = latitudeRole;
  }

  public LatLngRole getLongitudeRole() {
    return longitudeRole;
  }

  public void setLongitudeRole( LatLngRole longitudeRole ) {
    this.longitudeRole = longitudeRole;
  }

  @Override
  public boolean evaluate( String fieldName ) {
    boolean result = false;

    if ( latitudeRole != null ) {
      result = latitudeRole.evaluate( fieldName );
      if ( result ) {
        return result;
      }
    }
    if ( longitudeRole != null ) {
      return result || longitudeRole.evaluate( fieldName );
    }

    return result;
  }

  public boolean evaluateLatitude( String fieldName ) {
    if ( latitudeRole != null ) {
      return latitudeRole.evaluate( fieldName );
    }
    return false;
  }

  public boolean evaluateLongitude( String fieldName ) {
    if ( longitudeRole != null ) {
      return longitudeRole.evaluate( fieldName );
    }
    return false;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    if ( !super.equals( o ) ) {
      return false;
    }

    LocationRole that = (LocationRole) o;

    if ( latitudeRole != null ? !latitudeRole.equals( that.latitudeRole ) : that.latitudeRole != null ) {
      return false;
    }

    if ( longitudeRole != null ? !longitudeRole.equals( that.longitudeRole ) : that.longitudeRole != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + ( latitudeRole != null ? latitudeRole.hashCode() : 0 );
    result = 31 * result + ( longitudeRole != null ? longitudeRole.hashCode() : 0 );
    return result;
  }

  @Override
  public GeoRole clone() {
    LocationRole clone = new LocationRole();
    clone.setLatitudeRole( this.latitudeRole == null ? null : this.latitudeRole.clone() );
    clone.setLongitudeRole( this.longitudeRole == null ? null : this.longitudeRole.clone() );

    clone.setMatchSeparator( getMatchSeparator() );
    clone.setName( getName() );

    // don't bother cloning aliases or the lat & long fields.

    return clone;
  }

  public String getPrefix() {
    if ( latitudeRole != null && longitudeRole != null ) {
      String prefix = latitudeRole.getPrefix();
      if ( prefix.equalsIgnoreCase( longitudeRole.getPrefix() ) ) {
        return prefix;
      }
    }
    return "";
  }

  @Override
  public String getDataType() {
    return "LOCATION_ROLE";
  }

  @Override
  public boolean isValid( AbstractMetaDataModelNode node ) {
    if ( node instanceof LevelMetaData ) {
      LevelMetaData level = (LevelMetaData) node;
      boolean latFound = false;
      boolean lonFound = false;
      for ( MemberPropertyMetaData member : level ) {
        if ( member.getName().equals( GeoContext.LATITUDE ) ) {
          latFound = true;
        } else if ( member.getName().equals( GeoContext.LONGITUDE ) ) {
          lonFound = true;
        }
      }
      return latFound & lonFound;
    } else {
      return super.isValid( node );
    }
  }

  @Override
  public List<String> getValidationMessages( AbstractMetaDataModelNode node ) {
    List<String> messages = new ArrayList<String>();

    if ( node instanceof LevelMetaData ) {
      LevelMetaData level = (LevelMetaData) node;
      boolean latFound = false;
      boolean lonFound = false;
      for ( MemberPropertyMetaData member : level ) {
        if ( member.getName().equals( GeoContext.LATITUDE ) ) {
          latFound = true;
        } else if ( member.getName().equals( GeoContext.LONGITUDE ) ) {
          lonFound = true;
        }
      }
      if ( !latFound ) {
        messages.add( ModelerMessagesHolder.getMessages().getString( "validation.level.MISSING_LAT" ) );
      }
      if ( !lonFound ) {
        messages.add( ModelerMessagesHolder.getMessages().getString( "validation.level.MISSING_LON" ) );
      }
    }
    return messages;
  }

  @Override
  public void onAttach( AbstractMetaDataModelNode node ) {
    super.onAttach( node );
    if ( !( node instanceof LevelMetaData ) ) {
      return;
    }
    LevelMetaData level = (LevelMetaData) node;
    if ( level.getLatitudeField() == null ) {
      level.add( new MemberPropertyMetaData( level, GeoContext.LATITUDE ) );
    }
    if ( level.getLongitudeField() == null ) {
      level.add( new MemberPropertyMetaData( level, GeoContext.LONGITUDE ) );
    }
  }

  public void onDetach( AbstractMetaDataModelNode node ) {
    super.onDetach( node );
    if ( !( node instanceof LevelMetaData ) ) {
      return;
    }
    LevelMetaData level = (LevelMetaData) node;
    MemberPropertyMetaData lat = level.getLatitudeField();
    if ( lat != null && lat.getLogicalColumn() == null ) {
      level.remove( lat );
    }
    MemberPropertyMetaData lon = level.getLongitudeField();
    if ( lon != null && lon.getLogicalColumn() == null ) {
      level.remove( lon );
    }
  }
}
