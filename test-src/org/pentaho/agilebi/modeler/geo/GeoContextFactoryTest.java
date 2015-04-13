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

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/21/11 Time: 9:35 AM To change this template use File | Settings |
 * File Templates.
 */
public class GeoContextFactoryTest {

  private static final String GEO_ROLE_KEY = "geo.roles";
  private static Properties props = null;
  private static final String LOCALE = "en_US";
  private static GeoContextConfigProvider config;

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader( new File( "test-res/geoRoles.properties" ) );
    props = new Properties();
    props.load( propsReader );
    config = new GeoContextPropertiesProvider( props );
  }

  @Test
  public void testCreateWithProps() throws Exception {
    Properties myprops = (Properties) props.clone();
    myprops.setProperty( "geo.country.aliases", "country, ctry" );
    config = new GeoContextPropertiesProvider( myprops );

    GeoContext geo = GeoContextFactory.create( config );

    assertEquals( 6, geo.size() );

    assertNotNull( geo.getLocationRole() );

    // make sure they are in the same order as entered in the props file
    String rolesCsv = props.getProperty( GEO_ROLE_KEY );
    String[] tokens = rolesCsv.split( "," );
    for ( int i = 0; i < tokens.length; i++ ) {
      assertEquals( tokens[ i ].trim(), geo.getGeoRole( i ).getName() );
    }

    GeoRole state = geo.getGeoRoleByName( "state" );
    assertNotNull( state );
    assertEquals( 1, state.getRequiredParentRoles().size() );
    assertEquals( "country", state.getRequiredParentRoles().get( 0 ).getName() );

    GeoRole city = geo.getGeoRoleByName( "city" );
    assertNotNull( city );
    assertEquals( 2, city.getRequiredParentRoles().size() );
    assertEquals( "country", city.getRequiredParentRoles().get( 0 ).getName() );
    assertEquals( "state", city.getRequiredParentRoles().get( 1 ).getName() );

    GeoRole zip = geo.getGeoRoleByName( "postal_code" );
    assertNotNull( zip );
    assertEquals( 1, zip.getRequiredParentRoles().size() );
    assertEquals( "country", zip.getRequiredParentRoles().get( 0 ).getName() );
  }

  @Test( expected = ModelerException.class )
  public void testCreateWithProps_NoRoles() throws Exception {
    Properties myprops = (Properties) props.clone();
    myprops.setProperty( GEO_ROLE_KEY, "" );
    config = new GeoContextPropertiesProvider( myprops );
    GeoContext geo = GeoContextFactory.create( config );
  }

  @Test( expected = ModelerException.class )
  public void testCreateWithProps_NoAliasesForRole() throws Exception {
    Properties myprops = (Properties) props.clone();
    myprops.setProperty( "geo.country.aliases", "" );
    config = new GeoContextPropertiesProvider( myprops );
    GeoContext geo = GeoContextFactory.create( config );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testCreateWithProps_NullProps() throws Exception {
    GeoContext geo = GeoContextFactory.create( null );
  }

}
