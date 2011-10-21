package org.pentaho.agilebi.modeler;

import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.propforms.LevelsPropertiesForm;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import static org.junit.Assert.*;

/**
 * User: nbaker
 * Date: 10/20/11
 */
public class NodeAnnotationTest extends AbstractModelerTest {
  @Test
  public void testAnnotations() throws Exception{
    generateTestDomain();
    ModelerWorkspaceHelper workspaceHelper = new ModelerWorkspaceHelper("en-US");
    workspaceHelper.autoModelFlat(workspace);
    workspaceHelper.autoModelRelationalFlat(workspace);
    
    LevelMetaData level = workspace.getModel().getDimensions().get(0).get(0).get(0);
    level.getMemberAnnotations().put(GeoContext.ANNOTATION_GEO_ROLE, workspace.getGeoContext().getGeoRoleByName("location"));
    level.getMemberAnnotations().put(GeoContext.ANNOTATION_DATA_ROLE, workspace.getGeoContext().getGeoRoleByName("location"));

    assertEquals(workspace.getGeoContext().getGeoRoleByName("location"), level.getMemberAnnotations().get(GeoContext.ANNOTATION_GEO_ROLE));
    assertEquals(level.getMemberAnnotations().get(GeoContext.ANNOTATION_GEO_ROLE).getName(), "location");
    assertEquals(level.getMemberAnnotations().get(GeoContext.ANNOTATION_DATA_ROLE).getName(), "location");

  }
}
