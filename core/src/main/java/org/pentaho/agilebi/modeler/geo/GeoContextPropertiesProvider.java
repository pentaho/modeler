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
