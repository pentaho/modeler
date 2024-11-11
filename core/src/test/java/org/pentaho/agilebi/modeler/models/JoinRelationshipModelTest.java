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

import static org.junit.Assert.*;

public class JoinRelationshipModelTest {
  @Test
  public void testGetName() throws Exception {
    JoinRelationshipModel relModel = new JoinRelationshipModel();
    relModel.setLeftKeyFieldModel( createFieldModel( "field1", "pt1" ) );
    relModel.setRightKeyFieldModel( createFieldModel( "field2", "pt2" ) );

    assertEquals( "pt1.field1 - multitable.INNER_JOIN - pt2.field2", relModel.getName() );
  }

  @Test
  public void testEqualsComparesParentTables() throws Exception {
    JoinRelationshipModel relModel = new JoinRelationshipModel();
    relModel.setLeftKeyFieldModel( createFieldModel( "field1", "pt1" ) );
    relModel.setRightKeyFieldModel( createFieldModel( "field2", "pt2" ) );

    JoinRelationshipModel relModel2 = new JoinRelationshipModel();
    relModel2.setLeftKeyFieldModel( createFieldModel( "field1x", "pt1" ) );
    relModel2.setRightKeyFieldModel( createFieldModel( "field2x", "pt2" ) );

    JoinRelationshipModel relModel3 = new JoinRelationshipModel();
    relModel3.setLeftKeyFieldModel( createFieldModel( "field1", "pt1x" ) );
    relModel3.setRightKeyFieldModel( createFieldModel( "field2", "pt2" ) );

    assertTrue( relModel.equals( relModel2 ) );
    assertFalse( relModel.equals( relModel3 ) );
  }

  private JoinFieldModel createFieldModel( final String fieldName, final String parentTableName ) {
    JoinFieldModel joinField = new JoinFieldModel();
    joinField.setName( fieldName );
    JoinTableModel parentTable1 = new JoinTableModel();
    parentTable1.setName( parentTableName );
    joinField.setParentTable( parentTable1 );
    return joinField;
  }
}
