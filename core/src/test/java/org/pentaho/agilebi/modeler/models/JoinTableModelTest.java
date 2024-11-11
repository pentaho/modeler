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


package org.pentaho.agilebi.modeler.models;

import org.junit.Test;
import org.pentaho.ui.xul.util.AbstractModelList;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class JoinTableModelTest {
  @Test
  public void testEqualsOnName() throws Exception {
    JoinTableModel model1 = new JoinTableModel();
    model1.setName( "model1" );
    JoinTableModel model2 = new JoinTableModel();
    model2.setName( "model2" );
    assertFalse( model1.equals( model2 ) );
    model2.setName( "model1" );
    assertTrue( model1.equals( model2 ) );
    assertFalse( model1.equals( "model1" ) );
  }

  @Test
  public void testProcessTableFields() throws Exception {
    JoinTableModel model = new JoinTableModel();
    model.setName( "m1" );
    List<JoinFieldModel> joinFieldModels = model.processTableFields( Arrays.asList( "field1", "field2", "field3" ) );
    assertEquals( 3, joinFieldModels.size() );
    assertEquals( "field1", joinFieldModels.get( 0 ).getName() );
    assertEquals( "field2", joinFieldModels.get( 1 ).getName() );
    assertEquals( "field3", joinFieldModels.get( 2 ).getName() );
    assertEquals( model, joinFieldModels.get( 0 ).getParentTable() );
    assertEquals( model, joinFieldModels.get( 1 ).getParentTable() );
    assertEquals( model, joinFieldModels.get( 2 ).getParentTable() );
  }

  @Test
  public void testFields() throws Exception {
    JoinTableModel model = new JoinTableModel();
    JoinFieldModel fieldModel1 = new JoinFieldModel();
    JoinFieldModel fieldModel2 = new JoinFieldModel();
    AbstractModelList<JoinFieldModel> fields = new AbstractModelList<>();
    fields.add( fieldModel1 );
    fields.add( fieldModel2 );
    model.setFields( fields );
    assertEquals( fields, model.getFields() );
    model.reset();
    assertEquals( 0, model.getFields().size() );
  }
}
