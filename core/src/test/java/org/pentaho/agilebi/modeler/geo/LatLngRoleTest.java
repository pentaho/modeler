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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/26/11 Time: 2:20 PM To change this template use File | Settings |
 * File Templates.
 */
public class LatLngRoleTest {

  @Test
  public void testEval() {
    LatLngRole role = new LatLngRole( "latitude", "lat,latitude" );

    assertTrue( role.eval( "lat", "lat" ) );
    assertEquals( "", role.getPrefix() );

    assertTrue( role.eval( "customer_lat", "lat" ) );
    assertEquals( "customer", role.getPrefix() );

  }

}
