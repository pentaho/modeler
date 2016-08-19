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
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class InlineFormatAnnotationTest {
  @Test
  public void testSavesOlapAnnotationToLevel() throws Exception {
    InlineFormatAnnotation memberAnnotation = new InlineFormatAnnotation( "mm-dd-yyyy" );
    OlapHierarchyLevel level = new OlapHierarchyLevel();
    memberAnnotation.saveAnnotations( level );
    assertLevelFormatString( level, "mm-dd-yyyy" );

    memberAnnotation = new InlineFormatAnnotation( "MM/DD/YYYY" );
    memberAnnotation.saveAnnotations( level );
    assertLevelFormatString( level, "MM/DD/YYYY" );
  }

  private void assertLevelFormatString( final OlapHierarchyLevel level, final String formatString ) {
    List<OlapAnnotation> annotations = level.getAnnotations();
    assertEquals( 1, annotations.size() );
    assertEquals( InlineFormatAnnotation.INLINE_MEMBER_FORMAT_STRING, annotations.get( 0 ).getName() );
    assertEquals( formatString, annotations.get( 0 ).getValue() );
    assertEquals( "org.pentaho.platform.plugin.action.mondrian.formatter.InlineMemberFormatter", level.getFormatter() );
  }
}
