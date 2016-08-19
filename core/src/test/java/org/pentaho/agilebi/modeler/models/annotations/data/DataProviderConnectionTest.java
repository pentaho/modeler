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

package org.pentaho.agilebi.modeler.models.annotations.data;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DataProviderConnectionTest {
  @Test
  public void testGetAndSet() throws Exception {
    DataProviderConnection dpc = createDPC();

    assertEquals( "dpcName", dpc.getName() );
    assertEquals( "dpcDatabase", dpc.getDatabaseName() );
    assertEquals( "dpcDisplay", dpc.getDisplayName() );
    assertEquals( "dpcHost", dpc.getHostname() );
    assertEquals( "dpcUser", dpc.getUsername() );
    assertEquals( "dpcPassword", dpc.getPassword() );
    assertEquals( 1, dpc.getAttributeList().size() );
    assertEquals( "aName", dpc.getAttributeList().get( 0 ).getName() );
    assertEquals( "aValue", dpc.getAttributeList().get( 0 ).getValue() );
    assertTrue( dpc.isChanged() );
  }

  @Test
  public void testEquals() throws Exception {
    DataProviderConnection dpc1 = createDPC();
    DataProviderConnection dpc2 = createDPC();
    assertTrue( dpc1.equals( dpc2 ) );
    List<NameValueProperty> aList = Collections.singletonList( new NameValueProperty( "name2", "value2" ) );
    dpc1.setAttributeList( aList );
    assertFalse( dpc1.equals( dpc2 ) );
    dpc2.setAttributeList( aList );
    assertTrue( dpc1.equals( dpc2 ) );
    dpc2.setName( "other" );
    assertFalse( dpc2.equals( dpc1 ) );
  }

  private DataProviderConnection createDPC() {
    DataProviderConnection dpc = new DataProviderConnection();
    dpc.setName( "dpcName" );
    dpc.setChanged( true );
    dpc.setAttributeList( Collections.singletonList( new NameValueProperty( "aName", "aValue" ) ) );
    dpc.setDatabaseName( "dpcDatabase" );
    dpc.setDisplayName( "dpcDisplay" );
    dpc.setHostname( "dpcHost" );
    dpc.setUsername( "dpcUser" );
    dpc.setPassword( "dpcPassword" );
    return dpc;
  }
}
