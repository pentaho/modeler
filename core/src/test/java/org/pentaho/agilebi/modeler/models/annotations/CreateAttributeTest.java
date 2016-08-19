/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 6/8/16.
 */
public class CreateAttributeTest {

  private CreateAttribute buildCreateAttributeAnnotations( String name, String dimension, String hierarchy, String parent, String description, boolean isUnique ) {
    CreateAttribute ca = new CreateAttribute();
    ca.setName( name );
    ca.setDimension( dimension );
    ca.setHierarchy( hierarchy );
    ca.setParentAttribute( parent );
    ca.setUnique( isUnique );
    ca.setDescription( description );
    return ca;
  }

  @Test
  public void equalsLogically_caseInsensitive() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", "sales", "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", "ignore", "ignore", true );

    assertTrue( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_differentName() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "total", "sales", "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", null, null, false );

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_differentDimension() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", "Money", "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", null, null, false );

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_differentHierarchy() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", "sales", "Money", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", "Sales", "Sales", null, null, false );

    assertFalse( left.equalsLogically( right ) );
  }

  @Test
  public void equalsLogically_nulls() throws Exception {
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", null, "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "Sales", null, "Sales", null, null, false );

    assertTrue( left.equalsLogically( right ) );

    left.setHierarchy( null );
    assertFalse( left.equalsLogically( right ) );

    assertFalse( left.equalsLogically( null ) );
  }

  @Test
  public void equalsLogically_UsingDefaultAssumptions() throws Exception {
    // can't assume dimension
    CreateAttribute left = buildCreateAttributeAnnotations( "sales", null, "sales", null, null, false );
    CreateAttribute right = buildCreateAttributeAnnotations( "sales", "sales", "sales", null, null, false );
    assertFalse( left.equalsLogically( right ) );

    // but we can assume hierarchy is the dimension name
    left.setDimension( "Sales" );
    left.setHierarchy( null );
    assertTrue( left.equalsLogically( right ) );
  }


}
