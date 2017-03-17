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

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.nodes.annotations.GeoAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.IAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.MemberAnnotationFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/21/11 Time: 9:28 AM To change this template use File | Settings |
 * File Templates.
 */
public class GeoContextFactory {

  private static final String LATITUDE = "latitude";
  private static final String LONGITUDE = "longitude";

  /**
   * This factory method creates a GeoContext from a GeoContextConfigProvider.
   * 
   * @param configProvider
   * @return
   * @throws ModelerException
   */
  public static GeoContext create( GeoContextConfigProvider configProvider ) throws ModelerException {
    GeoContext geo = new GeoContext();

    if ( configProvider == null ) {
      throw new IllegalArgumentException( "GeoContextConfigProvider cannot be null" );
    }

    String dimName = configProvider.getDimensionName();
    if ( dimName != null && dimName.trim().length() > 0 ) {
      geo.dimensionName = dimName;
    }

    String rolesCsv = configProvider.getRoles();

    if ( rolesCsv != null && rolesCsv.length() > 0 ) {
      String[] tokens = rolesCsv.split( "," );
      ArrayList<String> roleNames = new ArrayList<String>( tokens.length );
      for ( String s : tokens ) {
        roleNames.add( s.trim() );
      }

      // grab the corresponding aliases for each role
      for ( String rolename : roleNames ) {
        String aliases = configProvider.getRoleAliases( rolename );
        String parents = configProvider.getRoleRequirements( rolename );
        String displayName = ModelerMessagesHolder.getMessages().getString( "geo." + rolename );
        if ( StringUtils.isEmpty( displayName ) ) {
          displayName = rolename;
        }
        GeoRole role = new GeoRole( rolename, displayName, aliases );

        if ( role != null ) {
          if ( parents != null ) {
            List<String> requiredParents = GeoRole.parse( parents );

            for ( String parentRole : requiredParents ) {
              GeoRole pgr = geo.getGeoRoleByName( parentRole );
              if ( pgr != null ) {
                role.getRequiredParentRoles().add( pgr );
              }
            }
          }
          geo.add( role );
        }
      }

    } else {
      throw new ModelerException( "Error while building GeoContext: No GeoRoles found, make sure there is a "
          + GeoContext.GEO_ROLE_KEY + " property defined." );
    }

    String latAliases = configProvider.getRoleAliases( LATITUDE );
    LatLngRole latRole = new LatLngRole( LATITUDE, latAliases );
    String longAliases = configProvider.getRoleAliases( LONGITUDE );
    LatLngRole longRole = new LatLngRole( LONGITUDE, longAliases );

    String displayName = ModelerMessagesHolder.getMessages().getString( "geo.location" );
    if ( StringUtils.isEmpty( displayName ) ) {
      displayName = "location";
    }
    LocationRole locationRole = new LocationRole( latRole, displayName, longRole );
    geo.addGeoRole( locationRole );

    // Add geo annotation support to the AnnotationFactory
    IAnnotationFactory fact = new GeoAnnotationFactory( geo );
    MemberAnnotationFactory.registerFactory( "Geo.Role", fact );
    MemberAnnotationFactory.registerFactory( "Data.Role", fact );
    MemberAnnotationFactory.registerFactory( "Geo.RequiredParents", fact );

    return geo;

  }

}
