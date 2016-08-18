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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
