/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
