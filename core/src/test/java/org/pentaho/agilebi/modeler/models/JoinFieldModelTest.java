/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.agilebi.modeler.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class JoinFieldModelTest {
  @Test
  public void testGetAndSet() throws Exception {
    JoinFieldModel fieldModel = new JoinFieldModel();
    fieldModel.setName( "myFieldModel" );
    JoinTableModel tableModel = new JoinTableModel();
    tableModel.setName( "myTableModel" );
    fieldModel.setParentTable( tableModel );
    assertEquals( "myFieldModel", fieldModel.getName() );
    assertEquals( tableModel, fieldModel.getParentTable() );
  }
}
