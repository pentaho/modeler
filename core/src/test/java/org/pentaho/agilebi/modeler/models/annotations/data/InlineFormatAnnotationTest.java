/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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
