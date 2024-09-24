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
