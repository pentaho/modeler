package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.nodes.annotations.GeoAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.IAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.MemberAnnotationFactory;
import org.pentaho.metadata.model.olap.OlapAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/21/11
 * Time: 9:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeoContextFactory {


  private static final String LATITUDE = "latitude";
  private static final String LONGITUDE = "longitude";

  /**
   * This factory method creates a GeoContext from a GeoContextConfigProvider.
   * @param configProvider
   * @return
   * @throws ModelerException
   */
  public static GeoContext create(GeoContextConfigProvider configProvider) throws ModelerException {
    GeoContext geo = new GeoContext();

    if (configProvider == null) {
      throw new IllegalArgumentException("GeoContextConfigProvider cannot be null");
    }

    String dimName = configProvider.getDimensionName();
    if (dimName != null && dimName.trim().length() > 0) {
      geo.dimensionName = dimName;
    }

    String rolesCsv = configProvider.getRoles();

    if(rolesCsv != null && rolesCsv.length() > 0) {
      String[] tokens = rolesCsv.split(",");
      ArrayList<String> roleNames = new ArrayList<String>(tokens.length);
      for(String s: tokens) {
        roleNames.add(s.trim());
      }

      // grab the corresponding aliases for each role
      for(String rolename : roleNames) {
        String aliases = configProvider.getRoleAliases(rolename);
        String parents = configProvider.getRoleRequirements(rolename);

        GeoRole role = new GeoRole(rolename, aliases);

        if (role != null) {
          if(parents != null) {
            List<String> requiredParents = GeoRole.parse(parents);

            for(String parentRole : requiredParents) {
              GeoRole pgr = geo.getGeoRoleByName(parentRole);
              if(pgr != null) {
                role.getRequiredParentRoles().add(pgr);
              }
            }
          }
          geo.add(role);
        }
      }

    } else {
      throw new ModelerException("Error while building GeoContext: No GeoRoles found, make sure there is a " + GeoContext.GEO_ROLE_KEY + " property defined.");
    }

    String latAliases = configProvider.getRoleAliases(LATITUDE);
    LatLngRole latRole = new LatLngRole(LATITUDE, latAliases);
    String longAliases = configProvider.getRoleAliases(LONGITUDE);
    LatLngRole longRole = new LatLngRole(LONGITUDE, longAliases);

    LocationRole locationRole = new LocationRole(latRole, longRole);
    geo.addGeoRole(locationRole);

    // Add geo annotation support to the AnnotationFactory
    IAnnotationFactory fact = new GeoAnnotationFactory(geo);
    MemberAnnotationFactory.registerFactory("Geo.Role", fact);
    MemberAnnotationFactory.registerFactory("Data.Role", fact);
    
    return geo;

  }

}
