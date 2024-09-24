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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.geo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/26/11 Time: 2:09 PM To change this template use File | Settings |
 * File Templates.
 */
public class LatLngRole extends GeoRole implements Serializable {
  private static final long serialVersionUID = 3443044732976689019L;
  private String prefix = "";

  public LatLngRole() {
    super();
  }

  public LatLngRole( String name, List<String> commonAliases ) {
    super( name, commonAliases );
  }

  public LatLngRole( String name, String commonAliases ) {
    super( name, commonAliases );
  }

  @Override
  protected boolean eval( String fieldName, String alias ) {
    if ( super.eval( fieldName, alias ) ) {
      return true;
    } else if ( fieldName.endsWith( getMatchSeparator() + alias ) ) {
      prefix = fieldName.substring( 0, fieldName.indexOf( getMatchSeparator() + alias ) );
      return true;
    }

    return false;
  }

  public String getPrefix() {
    return prefix;
  }

  public LatLngRole clone() {
    List<String> clonedAliases = (ArrayList<String>) ( (ArrayList<String>) getCommonAliases() ).clone();
    LatLngRole clone = new LatLngRole( getName(), clonedAliases );
    clone.prefix = getPrefix();
    return clone;
  }

}
