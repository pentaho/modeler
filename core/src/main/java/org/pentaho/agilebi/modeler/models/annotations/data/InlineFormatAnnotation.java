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

package org.pentaho.agilebi.modeler.models.annotations.data;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

import java.util.Collections;
import java.util.List;

public class InlineFormatAnnotation implements IMemberAnnotation {
  public static final String INLINE_MEMBER_FORMAT_STRING = "InlineMemberFormatString";
  private String formatString;

  public InlineFormatAnnotation( final String formatString ) {
    this.formatString = formatString;
  }

  @Override public String getName() {
    return INLINE_MEMBER_FORMAT_STRING;
  }

  @Override public void saveAnnotations( final OlapHierarchyLevel level ) {
    level.setFormatter( "org.pentaho.platform.plugin.action.mondrian.formatter.InlineMemberFormatter" );
    level.getAnnotations().removeIf( olapAnnotation -> INLINE_MEMBER_FORMAT_STRING.equals( olapAnnotation.getName() ) );
    level.getAnnotations().add( new OlapAnnotation( INLINE_MEMBER_FORMAT_STRING, formatString ) );
  }

  @Override public boolean isValid( final AbstractMetaDataModelNode node ) {
    return node instanceof LevelMetaData;
  }

  @Override public List<String> getValidationMessages( final AbstractMetaDataModelNode node ) {
    return Collections.emptyList();
  }

  @Override public void onAttach( final AbstractMetaDataModelNode node ) {

  }

  @Override public void onDetach( final AbstractMetaDataModelNode node ) {

  }
}
