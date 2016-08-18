/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

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
    assertEquals( IOUtils.toString( expectedInput ), IOUtils.toString( actualInput ) );
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
