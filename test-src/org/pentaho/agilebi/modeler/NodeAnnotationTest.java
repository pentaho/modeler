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
public class NodeAnnotationTest extends AbstractModelerTest {
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
