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
