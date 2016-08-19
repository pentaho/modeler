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
