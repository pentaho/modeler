package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.ModelerException;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/27/11
 * Time: 10:14 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GeoContextConfigProvider {

  public String getDimensionName() throws ModelerException;
  public String getRoles() throws ModelerException;
  public String getRoleAliases(String roleName) throws ModelerException;
  public String getRoleRequirements(String roleName) throws ModelerException;

}
