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

package org.pentaho.agilebi.modeler;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;

/**
 * User: nbaker Date: 10/20/11
 */
public class NodeAnnotationIT extends AbstractModelerTest {
  @Test
  public void testAnnotations() throws Exception {
    generateTestDomain();
    ModelerWorkspaceHelper workspaceHelper = new ModelerWorkspaceHelper( "en-US" );
    workspaceHelper.autoModelFlat( workspace );
    workspaceHelper.autoModelRelationalFlat( workspace );

    LevelMetaData level = workspace.getModel().getDimensions().get( 0 ).get( 0 ).get( 0 );
    level.getMemberAnnotations().put( GeoContext.ANNOTATION_GEO_ROLE,
        workspace.getGeoContext().getGeoRoleByName( "location" ) );
    level.getMemberAnnotations().put( GeoContext.ANNOTATION_DATA_ROLE,
        workspace.getGeoContext().getGeoRoleByName( "location" ) );

    assertEquals( workspace.getGeoContext().getGeoRoleByName( "location" ), level.getMemberAnnotations().get(
        GeoContext.ANNOTATION_GEO_ROLE ) );
    assertEquals( level.getMemberAnnotations().get( GeoContext.ANNOTATION_GEO_ROLE ).getName(), "location" );
    assertEquals( level.getMemberAnnotations().get( GeoContext.ANNOTATION_DATA_ROLE ).getName(), "location" );

  }

  @Test
  public void testProcessChange() throws Exception {
    generateTestDomain();
    ModelerWorkspaceHelper workspaceHelper = new ModelerWorkspaceHelper( "en-US" );
    workspaceHelper.autoModelFlat( workspace );
    workspaceHelper.autoModelRelationalFlat( workspace );

    LevelMetaData level = workspace.getModel().getDimensions().get( 0 ).get( 0 ).get( 0 );
    GeoRole geoRole = workspace.getGeoContext().getGeoRoleByName( "location" );
    level.getMemberAnnotations().put( GeoContext.ANNOTATION_GEO_ROLE, geoRole );

    assertEquals( 2, level.size() );
    assertNotNull( level.getLatitudeField() );
    assertNotNull( level.getLongitudeField() );

    level.getMemberAnnotations().remove( geoRole );

    assertEquals( 0, level.size() );
    assertNull( level.getLatitudeField() );
    assertNull( level.getLongitudeField() );

  }
}
