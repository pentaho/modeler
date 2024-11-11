/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
