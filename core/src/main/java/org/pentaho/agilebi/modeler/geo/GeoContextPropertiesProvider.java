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

import org.pentaho.agilebi.modeler.ModelerException;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/27/11 Time: 10:23 AM To change this template use File | Settings |
 * File Templates.
 */
public class GeoContextPropertiesProvider implements GeoContextConfigProvider {

  private Properties props;

  public GeoContextPropertiesProvider( Properties props ) {
    this.props = props;
  }

  @Override
  public String getDimensionName() throws ModelerException {
    return props.getProperty( GeoContext.GEO_DIM_NAME );
  }

  @Override
  public String getRoles() throws ModelerException {
    return props.getProperty( GeoContext.GEO_ROLE_KEY );
  }

  @Override
  public String getRoleAliases( String roleName ) throws ModelerException {
    String aliasKey = GeoContext.GEO_PREFIX + roleName + GeoContext.ALIAS_SUFFIX;
    String aliases = props.getProperty( aliasKey );
    if ( aliases == null || aliases.trim().length() == 0 ) {
      throw new ModelerException( "Error while building GeoContext from properties: No Aliases found for role  "
          + roleName + ". Make sure there is a " + aliasKey + " property defined" );
    }
    return aliases;
  }

  @Override
  public String getRoleRequirements( String roleName ) throws ModelerException {
    String key = GeoContext.GEO_PREFIX + roleName + GeoContext.REQUIRED_PARENTS_SUFFIX;
    return props.getProperty( key );
  }

}
