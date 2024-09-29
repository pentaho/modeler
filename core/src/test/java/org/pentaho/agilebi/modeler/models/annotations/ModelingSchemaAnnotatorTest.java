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


package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.pentaho.platform.api.repository.RepositoryException;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ModelingSchemaAnnotatorTest {
  @Test
  public void testAppliesAnnotationsFromAFile() throws Exception {
    ModelingSchemaAnnotator annotator = new ModelingSchemaAnnotator();
    InputStream schemaInput = getClass().getResourceAsStream( "resources/simple.mondrian.xml" );
    InputStream annotationsInput = getClass().getResourceAsStream( "resources/annotations.xml" );
    InputStream expectedInput = getClass().getResourceAsStream( "resources/annotated.mondrian.xml" );
    InputStream actualInput = annotator.getInputStream( schemaInput, annotationsInput );
    assertEquals( IOUtils.toString( expectedInput ).replaceAll( "\\r\\n", "\\\n" ), IOUtils.toString( actualInput )
            .replaceAll( "\\r\\n", "\\\n" ) );
  }

  @Test
  public void testExceptionsThrowRepositoryException() throws Exception {
    ModelingSchemaAnnotator annotator = new ModelingSchemaAnnotator();
    InputStream schemaInput = getClass().getResourceAsStream( "resources/simple.mondrian.xml" );
    InputStream annotationsInput = getClass().getResourceAsStream( "resources/annotations.xml" );
    schemaInput.close();  //forcing exception on read
    try {
      annotator.getInputStream( schemaInput, annotationsInput );
    } catch ( RepositoryException e ) {
      return;
    } catch ( Exception e ) {
      fail( "got wrong exception" );
    }
    fail( "should have got exception" );
  }
}
