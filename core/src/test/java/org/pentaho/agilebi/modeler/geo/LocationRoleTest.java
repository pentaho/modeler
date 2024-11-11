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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/26/11 Time: 1:49 PM To change this template use File | Settings |
 * File Templates.
 */
public class LocationRoleTest {

  private LatLngRole latRole;
  private LatLngRole longRole;

  @Before
  public void setUp() throws Exception {
    latRole = new LatLngRole( "latitude", "lat,latitude" );
    longRole = new LatLngRole( "latitude", "lng,long,longitude" );
  }

  @Test
  public void testEvaluatePrefixedFields() {
    LocationRole role = new LocationRole( latRole, longRole );

    assertTrue( role.evaluate( "customer_lat" ) );
    assertTrue( role.evaluate( "customer_latitude" ) );
    assertTrue( role.evaluate( "customer_lng" ) );
    assertTrue( role.evaluate( "customer_long" ) );
    assertTrue( role.evaluate( "customer_longitude" ) );

  }

}
