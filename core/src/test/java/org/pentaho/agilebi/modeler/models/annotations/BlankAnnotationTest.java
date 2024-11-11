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

package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Test;

import static org.junit.Assert.*;


public class BlankAnnotationTest {
  @Test
  public void testBlankTypeIsNotNull() throws Exception {
    BlankAnnotation blankAnnotation = new BlankAnnotation();
    assertEquals( ModelAnnotation.Type.BLANK, blankAnnotation.getType() );
  }

  @Test
  public void testBlankAnnotationAlwaysSuccess() throws Exception {
    BlankAnnotation blankAnnotation = new BlankAnnotation();
    assertTrue( blankAnnotation.apply( null ) );
    assertTrue( blankAnnotation.apply( null, null ) );
  }
}
