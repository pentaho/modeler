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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/15/11 Time: 4:51 PM To change this template use File | Settings |
 * File Templates.
 */
public class GeoRoleTest {

  @Test
  public void testConstructor() {
    String[] aliases = { "state", "province" };

    GeoRole geoRole = new GeoRole( "State", "state, province" );
    assertEquals( "State", geoRole.getName() );
    assertEquals( 2, geoRole.getCommonAliases().size() );
    assertArrayEquals( aliases, geoRole.getCommonAliases().toArray( new String[] { } ) );
  }

  @Test
  public void testEvaluation() {
    String[] aliases = { "st", "state", "province", "stateProvince", "postal code" };
    GeoRole state = new GeoRole( "State", Arrays.asList( aliases ) );
    assertTrue( state.evaluate( "STATE" ) );
    assertTrue( state.evaluate( "province" ) );
    assertTrue( state.evaluate( "st" ) );
    assertTrue( state.evaluate( "StateProvince" ) );
    assertTrue( state.evaluate( "postalcode" ) );
    assertTrue( state.evaluate( "POSTALCODE" ) );
    assertTrue( state.evaluate( "postal_code" ) );
    assertTrue( state.evaluate( "postal code" ) );

    assertEquals( false, state.evaluate( "sta" ) );
    assertEquals( false, state.evaluate( "prov" ) );
    assertEquals( false, state.evaluate( "provinc" ) );
    assertEquals( false, state.evaluate( "past" ) );
  }

}
