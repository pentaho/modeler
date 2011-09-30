package org.pentaho.agilebi.modeler.geo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/26/11
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class LatLngRoleTest {

  @Test
  public void testEval() {
    LatLngRole role = new LatLngRole("latitude", "lat,latitude");

    assertTrue(role.eval("lat", "lat"));
    assertEquals("", role.getPrefix());

    assertTrue(role.eval("customer_lat", "lat"));
    assertEquals("customer", role.getPrefix());

  }

}
