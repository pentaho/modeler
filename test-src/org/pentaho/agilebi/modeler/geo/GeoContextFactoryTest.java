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
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/21/11
 * Time: 9:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeoContextFactoryTest {

  private static final String GEO_ROLE_KEY = "geo.roles";
  private static Properties props = null;
  private static final String LOCALE = "en_US";

  @BeforeClass
  public static void bootstrap() throws IOException {
    Reader propsReader = new FileReader(new File("test-res/geoRoles.properties"));
    props = new Properties();
    props.load(propsReader);
  }


  @Test
  public void testCreateWithProps() throws Exception {
    GeoContext geo = GeoContextFactory.create(props);

    assertEquals(6, geo.size());

    assertNotNull(geo.getLocationRole());

    // make sure they are in the same order as entered in the props file
    String rolesCsv = props.getProperty(GEO_ROLE_KEY);
    String[] tokens = rolesCsv.split(",");
    for(int i = 0; i < tokens.length; i++) {
      assertEquals(tokens[i].trim(), geo.getGeoRole(i).getName());
    }
  }

  @Test(expected = ModelerException.class)
  public void testCreateWithProps_NoRoles() throws Exception {
    Properties myprops = (Properties) props.clone();
    myprops.setProperty(GEO_ROLE_KEY, "");
    GeoContext geo = GeoContextFactory.create(myprops);
  }
  @Test(expected = ModelerException.class)
  public void testCreateWithProps_NoAliasesForRole() throws Exception {
    Properties myprops = (Properties) props.clone();
    myprops.setProperty("geo.country.aliases", "");
    GeoContext geo = GeoContextFactory.create(myprops);
  }
  @Test(expected = IllegalArgumentException.class)
  public void testCreateWithProps_NullProps() throws Exception {
    GeoContext geo = GeoContextFactory.create(null);
  }

}
