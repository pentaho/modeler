package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.ModelerException;

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
   * This factory method creates a GeoContext from a Properties file. This was originally a constructor
   * of GeoContext, but since Properties aren't available in GWT... this was moved. Now this class can be
   * excluded from the gwt compile.
   * @param props
   * @return
   * @throws ModelerException
   */
  public static GeoContext create(Properties props) throws ModelerException {
    GeoContext geo = new GeoContext();

    geo.geoRoles = new ArrayList<GeoRole>();
    if (props == null) {
      throw new IllegalArgumentException("Properties cannot be null");
    }

    String dimName = props.getProperty(GeoContext.GEO_DIM_NAME);
    if (dimName != null && dimName.trim().length() > 0) {
      geo.dimensionName = dimName;
    }

    String rolesCsv = props.getProperty(GeoContext.GEO_ROLE_KEY);

    if(rolesCsv != null && rolesCsv.length() > 0) {
      String[] tokens = rolesCsv.split(",");
      ArrayList<String> roleNames = new ArrayList<String>(tokens.length);
      for(String s: tokens) {
        roleNames.add(s.trim());
      }

      // grab the corresponding aliases for each role
      for(String rolename : roleNames) {
        String aliases = getRoleAliasesFromProps(props, rolename);
        GeoRole role = new GeoRole(rolename, aliases);
        if (role != null) {
          geo.geoRoles.add(role);
        }
      }

    } else {
      throw new ModelerException("Error while building GeoContext from properties: No GeoRoles found, make sure there is a " + GeoContext.GEO_ROLE_KEY + " property defined.");
    }

    String latAliases = getRoleAliasesFromProps(props, LATITUDE);
    GeoRole latRole = new GeoRole(LATITUDE, latAliases);
    String longAliases = getRoleAliasesFromProps(props, LONGITUDE);
    GeoRole longRole = new GeoRole(LONGITUDE, longAliases);

    LocationRole locationRole = new LocationRole(latRole, longRole);
    geo.addGeoRole(locationRole);
    
    return geo;
  }

  private static String getRoleAliasesFromProps(Properties props, String roleName) throws ModelerException {
    String aliasKey = GeoContext.GEO_PREFIX + roleName + GeoContext.ALIAS_SUFFIX;
    String aliases = props.getProperty(aliasKey);
    if (aliases == null || aliases.trim().length() == 0) {
      throw new ModelerException("Error while building GeoContext from properties: No Aliases found for role  " + roleName + ". Make sure there is a " + aliasKey + " property defined");
    }
    return aliases;
  }


}
