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
