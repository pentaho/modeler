/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.i18n.LanguageChoice;

public class CreateDimensionKeyTest {

  @Test
  public void testValidateOk() throws Exception {
    CreateDimensionKey createKey = new CreateDimensionKey();
    createKey.setName( "field1" );
    createKey.setDimension( "dim1" );
    createKey.validate();
  }

  @Test
  public void testValidateNoDimension() throws Exception {
    CreateDimensionKey createKey = new CreateDimensionKey();
    createKey.setName( "field1" );
    try {
      createKey.validate();
      fail( "no exception" );
    } catch ( ModelerException e ) {
      //
    }
  }

  @Test
  public void testValidateNoName() throws Exception {
    CreateDimensionKey createKey = new CreateDimensionKey();
    createKey.setDimension( "d" );
    try {
      createKey.validate();
      fail( "no exception" );
    } catch ( ModelerException e ) {
      //
    }
  }

  @Test
  public void testSummary() throws Exception {
    CreateDimensionKey createKey = new CreateDimensionKey();
    createKey.setName( "field1" );
    createKey.setDimension( "dim1" );
    LanguageChoice.getInstance().setDefaultLocale( Locale.US );
    final String summary = createKey.getSummary();
    assertEquals( "field1 is key for dimension dim1", summary );
  }

  @Test
  public void testApplyIsAlwaysTrue() throws Exception {
    assertEquals( true, new CreateDimensionKey().apply( null, null, null ) );
  }
}
