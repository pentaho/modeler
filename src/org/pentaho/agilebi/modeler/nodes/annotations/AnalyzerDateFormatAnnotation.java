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

package org.pentaho.agilebi.modeler.nodes.annotations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

public class AnalyzerDateFormatAnnotation implements IAnalyzerDateFormatAnnotation, PropertyChangeListener {

  protected String value;

  public AnalyzerDateFormatAnnotation( String value ) {
    this.value = value;
  }

  public AnalyzerDateFormatAnnotation( LevelMetaData levelMetaData ) {
    this( levelMetaData.getFullTimeLevelFormat() );
  }

  @Override
  public String getName() {
    return IAnalyzerDateFormatAnnotation.NAME;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public void setValue( LevelMetaData levelMetaData ) {
    setValue( levelMetaData.getFullTimeLevelFormat() );
  }

  /**
   * This pinches of the last part of the full formatstring. That last part is what we should be storing in the
   * LevelMetaData node.
   */
  public String getTimeLevelFormat() {
    String value = getValue();
    if ( value == null ) {
      return null;
    }
    String[] parts = value.split( "\\" + IAnalyzerDateFormatAnnotation.SEPARATOR );
    int numParts = parts.length;
    String part = parts[numParts == 0 ? 0 : numParts - 1];
    if ( part.startsWith( IAnalyzerDateFormatAnnotation.MEMBER_START_QUOTE )
        && part.endsWith( IAnalyzerDateFormatAnnotation.MEMBER_END_QUOTE ) ) {
      part = part.substring( 1, part.length() - 1 );
    }
    return part;
  }

  public static String quoteTimeLevelFormat( String timeLevelFormat ) {
    return IAnalyzerDateFormatAnnotation.MEMBER_START_QUOTE + timeLevelFormat
        + IAnalyzerDateFormatAnnotation.MEMBER_END_QUOTE;
  }

  @Override
  public void saveAnnotations( OlapHierarchyLevel level ) {
    List<OlapAnnotation> annotations = level.getAnnotations();
    // remove the current AnalyzerDateFormatAnnotation if it exists
    OlapAnnotation existingAnnotation = null;
    for ( OlapAnnotation annotation : annotations ) {
      if ( !annotation.getName().equals( getName() ) ) {
        continue;
      }
      existingAnnotation = annotation;
      break;
    }
    if ( existingAnnotation == null ) {
      annotations.add( new OlapAnnotation( getName(), getValue() ) );
    } else {
      existingAnnotation.setValue( getValue() );
    }
  }

  @Override
  public boolean isValid( AbstractMetaDataModelNode node ) {
    LevelMetaData levelMetaData = (LevelMetaData) node;
    HierarchyMetaData hierarchyMetaData = levelMetaData.getHierarchyMetaData();
    List<LevelMetaData> levels = hierarchyMetaData.getLevels();
    for ( LevelMetaData ancestor : levels ) {
      if ( ancestor == node ) {
        return true;
      }
      if ( ancestor.getTimeLevelFormat() == null ) {
        return false;
      }
    }
    return true;
  }

  @Override
  public List<String> getValidationMessages( AbstractMetaDataModelNode node ) {
    List<String> messages = null;
    LevelMetaData levelMetaData = (LevelMetaData) node;
    HierarchyMetaData hierarchyMetaData = levelMetaData.getHierarchyMetaData();
    List<LevelMetaData> levels = hierarchyMetaData.getLevels();
    for ( LevelMetaData ancestor : levels ) {
      if ( ancestor == node ) {
        break;
      }
      if ( ancestor.getTimeLevelFormat() != null ) {
        continue;
      }
      if ( messages == null ) {
        messages = new ArrayList<String>();
      }
      messages.add( "Missing Time Level Format in level " + ancestor.getName() );
    }
    return messages;
  }

  @Override
  public void onAttach( AbstractMetaDataModelNode node ) {
    if ( !( node instanceof LevelMetaData ) ) {
      return;
    }
    LevelMetaData levelMetaData = (LevelMetaData) node;
    levelMetaData.updateTimeLevelFormat( this );
    levelMetaData.addPropertyChangeListener( "timeLevelFormat", this );
  }

  @Override
  public void onDetach( AbstractMetaDataModelNode node ) {
    if ( !( node instanceof LevelMetaData ) ) {
      return;
    }
    LevelMetaData levelMetaData = (LevelMetaData) node;
    levelMetaData.removePropertyChangeListener( this );
  }

  @Override
  public void propertyChange( PropertyChangeEvent evt ) {
    if ( !"timeLevelFormat".equals( evt.getPropertyName() ) ) {
      return;
    }
    Object source = evt.getSource();
    if ( !( source instanceof LevelMetaData ) ) {
      return;
    }
    LevelMetaData levelMetaData = (LevelMetaData) source;
    String value = levelMetaData.getFullTimeLevelFormat();
    if ( this.value == null && value == null ) {
      return;
    }
    if ( this.value != null && value != null && this.value.equals( value ) ) {
      return;
    }
    this.value = value;
  }

}
