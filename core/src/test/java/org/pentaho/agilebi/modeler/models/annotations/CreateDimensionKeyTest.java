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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.pentaho.agilebi.modeler.models.annotations.CreateDimensionKey.FIELD_ID;

import java.util.List;
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
    createKey.setField( "field1" );
    createKey.validate();
  }

  @Test
  public void testValidateNoDimension() throws Exception {
    CreateDimensionKey createKey = new CreateDimensionKey();
    createKey.setName( "field1" );
    createKey.setField( "field1" );
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
    createKey.setField( "field1" );
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
    createKey.setName( "name" );
    createKey.setField( "field1" );
    createKey.setDimension( "dim1" );
    LanguageChoice.getInstance().setDefaultLocale( Locale.US );
    assertEquals( "field1 is key for dimension dim1", createKey.getSummary() );
    createKey.setField( null );
    assertEquals( "name is key for dimension dim1", createKey.getSummary() );
  }

  @Test
  public void testApplyIsAlwaysTrue() throws Exception {
    assertEquals( true, new CreateDimensionKey().apply( null, null ) );
  }

  @Test
  public void testFieldIsHiddenProperty() throws Exception {
    CreateDimensionKey createDimensionKey = new CreateDimensionKey();
    List<ModelProperty> modelProperties = createDimensionKey.getModelProperties();
    int assertCount = 0;
    for ( ModelProperty modelProperty : modelProperties ) {
      String id = modelProperty.id();
      if ( FIELD_ID.equals( id ) ) {
        assertTrue( modelProperty.hideUI() );
        assertCount++;
      }
    }
    assertEquals( 1, assertCount );
  }
}
