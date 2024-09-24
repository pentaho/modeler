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

package org.pentaho.agilebi.modeler.models.annotations.data;

import java.util.Collections;
import java.util.List;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

public class GeneratedbyMemberAnnotation implements IMemberAnnotation {
  public static final String GEBERATED_BY_STRING = "GeneratedBy";
  private String name;

  public GeneratedbyMemberAnnotation( final String name ) {
    this.name = name;
  }

  @Override public String getName() {
    return GEBERATED_BY_STRING;
  }

  @Override public void saveAnnotations( final OlapHierarchyLevel level ) {
  }

  @Override public boolean isValid( final AbstractMetaDataModelNode node ) {
    return node instanceof LevelMetaData;
  }

  @Override public List<String> getValidationMessages( final AbstractMetaDataModelNode node ) {
    return Collections.emptyList();
  }

  @Override
  public void onAttach( AbstractMetaDataModelNode node ) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onDetach( AbstractMetaDataModelNode node ) {
    // TODO Auto-generated method stub
  }

}
