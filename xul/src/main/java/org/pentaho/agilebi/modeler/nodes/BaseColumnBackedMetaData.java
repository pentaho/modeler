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

package org.pentaho.agilebi.modeler.nodes;

import java.util.List;
import java.util.Map;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.nodes.annotations.AnalyzerDateFormatAnnotation;
import org.pentaho.agilebi.modeler.nodes.annotations.AnalyzerDateFormatAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.IAnalyzerDateFormatAnnotation;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.propforms.LevelsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/18/11
 * 
 * @author rfellows
 */
public class BaseColumnBackedMetaData<T extends AbstractMetaDataModelNode> extends AbstractMetaDataModelNode<T>
    implements ColumnBackedNode {
  private static final long serialVersionUID = -7342401951588541248L;
  protected String name;
  protected String columnName;
  protected boolean hidden;
  protected transient LogicalColumn logicalColumn;
  protected transient LogicalColumn logicalOrdinalColumn;
  protected transient LogicalColumn logicalCaptionColumn;
  protected boolean uniqueMembers = false;
  protected String timeLevelFormat;
  private static final String IMAGE = "images/sm_level_icon.png";
  private String description = "";
  private static final String CLASSNAME = "pentaho-smalllevelbutton";

  public BaseColumnBackedMetaData() {
    super( CLASSNAME );
  }

  public BaseColumnBackedMetaData( String name ) {
    this();
    this.name = name;
    this.columnName = name;
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Override
  public IPhysicalTable getTableRestriction() {
    return null;
  }

  @Bindable
  public String getDisplayName() {
    return getName();
  }

  @Bindable
  public void setName( String name ) {
    if ( !name.equals( this.name ) ) {
      String oldName = this.name;
      this.name = name;
      this.firePropertyChange( "name", oldName, name ); //$NON-NLS-1$
      this.firePropertyChange( "displayName", oldName, name ); //$NON-NLS-1$
      validateNode();
    }
  }

  @Bindable
  public String getDescription() {
    if ( getLogicalColumn() != null && !description.equals( getLogicalColumn().getId() ) ) {
      return description;
    } else {
      return "";
    }
  }

  @Bindable
  public void setDescription( String description ) {
    if ( !description.equals( this.description ) ) {
      String oldDesc = this.description;
      this.description = description;
      this.firePropertyChange( "description", oldDesc, description ); //$NON-NLS-1$
      validateNode();
    }
  }

  @Bindable
  public String getColumnName() {
    return columnName;
  }

  @Bindable
  public void setColumnName( String columnName ) {
    this.columnName = columnName;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  @Bindable
  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn( LogicalColumn col ) {
    LogicalColumn prevVal = this.logicalColumn;
    this.logicalColumn = col;
    validateNode();
    firePropertyChange( "logicalColumn", prevVal, col );
  }

  @Bindable
  public LogicalColumn getLogicalOrdinalColumn() {
    return logicalOrdinalColumn;
  }

  public void setLogicalOrdinalColumn( LogicalColumn col ) {
    LogicalColumn prevVal = this.logicalOrdinalColumn;
    this.logicalOrdinalColumn = col;
    validateNode();
    firePropertyChange( "logicalOrdinalColumn", prevVal, col );
  }

  @Bindable
  public LogicalColumn getLogicalCaptionColumn() {
    return logicalCaptionColumn;
  }

  @Bindable
  public void setLogicalCaptionColumn( LogicalColumn col ) {
    LogicalColumn prevVal = this.logicalCaptionColumn;
    this.logicalCaptionColumn = col;
    validateNode();
    firePropertyChange( "logicalCaptionColumn", prevVal, col );
  }

  @Bindable
  public void setUniqueMembers( boolean uniqueMembers ) {
    boolean oldUniqueMembers = this.uniqueMembers;
    if ( oldUniqueMembers == uniqueMembers ) {
      return;
    }
    this.uniqueMembers = uniqueMembers;
    firePropertyChange( "uniqueMembers", oldUniqueMembers, uniqueMembers );
  }

  @Bindable
  public boolean isUniqueMembers() {
    return uniqueMembers;
  }

  /**
   * Derive the AnalyzerDateFormatAnnotation value by looking at parent levels.
   **/
  public String getFullTimeLevelFormat() {
    String format = null;
    HierarchyMetaData hierarchyMetaData = (HierarchyMetaData) getParent();
    List<LevelMetaData> levels = hierarchyMetaData.getLevels();
    for ( LevelMetaData ancestorOrSelf : levels ) {
      format =
          ( format == null ? "" : format + IAnalyzerDateFormatAnnotation.SEPARATOR )
              + AnalyzerDateFormatAnnotation.quoteTimeLevelFormat( ancestorOrSelf.getTimeLevelFormat() );
      if ( ancestorOrSelf == this ) {
        break;
      }
    }
    return format;
  }

  /*
   * Update AnalyzerDateFormatAnnotations of descendant levels.
   */
  public void updateDescendantAnalyzerDateFormatAnnotations() {
    HierarchyMetaData hierarchyMetaData = (HierarchyMetaData) getParent();
    List<LevelMetaData> levels = hierarchyMetaData.getLevels();
    String format = null, timeLevelFormat;
    AnalyzerDateFormatAnnotation annotation;
    Map<String, IMemberAnnotation> annotations;
    String key = IAnalyzerDateFormatAnnotation.NAME;
    boolean isDescendant = false;
    for ( LevelMetaData descendant : levels ) {
      timeLevelFormat = descendant.getTimeLevelFormat();
      format =
          ( format == null ? "" : format + IAnalyzerDateFormatAnnotation.SEPARATOR )
              + AnalyzerDateFormatAnnotation.quoteTimeLevelFormat( timeLevelFormat );
      if ( !isDescendant ) {
        if ( descendant == this ) {
          isDescendant = true;
        }
        continue;
      }
      annotations = descendant.annotations;
      if ( !annotations.containsKey( key ) ) {
        continue;
      }
      annotation = (AnalyzerDateFormatAnnotation) annotations.get( key );
      annotation.setValue( format );
    }
  }

  /**
   * This is the format string that describes only this level's column values. This appears as the last part in
   * AnalyzerDateFormatAnnotations
   **/
  @Bindable
  public String getTimeLevelFormat() {
    return timeLevelFormat;
  }

  /**
   * This sets the format string that describes only this level's column values. This will automatically create a
   * correct AnalyzerDateFormatAnnotation for this level and update the annotations for the levels below this level.
   **/
  @Bindable
  public void setTimeLevelFormat( String timeLevelFormat ) {
    if ( "".equals( timeLevelFormat ) ) {
      timeLevelFormat = null;
    }
    String oldTimeLevelFormat = this.timeLevelFormat;
    if ( timeLevelFormat == null && oldTimeLevelFormat == null ) {
      return;
    }
    if ( timeLevelFormat != null && oldTimeLevelFormat != null && timeLevelFormat.equals( oldTimeLevelFormat ) ) {
      return;
    }
    this.timeLevelFormat = timeLevelFormat;
    String key = IAnalyzerDateFormatAnnotation.NAME;
    AnalyzerDateFormatAnnotation annotation = (AnalyzerDateFormatAnnotation) annotations.get( key );
    if ( annotation == null && timeLevelFormat == null ) {
      return;
    }
    if ( annotation == null && timeLevelFormat != null ) {
      annotation =
          (AnalyzerDateFormatAnnotation) AnalyzerDateFormatAnnotationFactory.instance.create( (LevelMetaData) this );
      annotation.setValue( timeLevelFormat );
      annotations.put( key, annotation );
    } else if ( annotation != null && timeLevelFormat == null ) {
      annotations.remove( key );
      annotation = null;
    } else {
      String newAnalyzerDateFormatValue = getFullTimeLevelFormat();
      if ( annotation.getValue().equals( newAnalyzerDateFormatValue ) ) {
        return;
      }
      annotation.setValue( newAnalyzerDateFormatValue );
    }
    updateDescendantAnalyzerDateFormatAnnotations();
    firePropertyChange( "timeLevelFormat", oldTimeLevelFormat, timeLevelFormat );
    validateNode();
  }

  /**
   * This method is meant to be called only from the annotation's onAttach handler. onAttach is called right *before*
   * the annotation is actually added to the annotations collection. This makes it very complicated (impossible?) to use
   * update timeLevelFormat using the standard setter (because that setter has logic to update annotations accordingly)
   */
  public void updateTimeLevelFormat( AnalyzerDateFormatAnnotation annotation ) {
    timeLevelFormat = annotation.getTimeLevelFormat();
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if ( name == null || "".equals( name ) ) {
      validationMessages
          .add( ModelerMessagesHolder.getMessages().getString( getValidationMessageKey( "MISSING_NAME" ) ) );
      valid = false;
    }

    if ( logicalColumn == null ) {
      validationMessages.add( ModelerMessagesHolder.getMessages().getString(
          getValidationMessageKey( "MISSING_BACKING_COLUMN" ), getName() ) );
      valid = false;
    } else {
      LogicalTable logicalTable = logicalColumn.getLogicalTable();
      if ( logicalOrdinalColumn != null && !logicalOrdinalColumn.getLogicalTable().equals( logicalTable ) ) {
        validationMessages.add( ModelerMessagesHolder.getMessages().getString(
            getValidationMessageKey( "INVALID_TABLE_FOR_ORDINAL_COLUMN" ), getName() ) );
      }
      if ( logicalCaptionColumn != null && !logicalCaptionColumn.getLogicalTable().equals( logicalTable ) ) {
        validationMessages.add( ModelerMessagesHolder.getMessages().getString(
            getValidationMessageKey( "INVALID_TABLE_FOR_CAPTION_COLUMN" ), getName() ) );
      }
    }
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return LevelsPropertiesForm.class;
  }

  @Bindable
  public boolean isUiExpanded() {
    return true;
  }

  @Bindable
  public boolean isEditingDisabled() {
    return false;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    return false;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    throw new ModelerException( new IllegalArgumentException( ModelerMessagesHolder.getMessages().getString(
        "invalid_drop" ) ) );
  }

  public String getValidationMessageKey( String key ) {
    return "validation.measure." + key;
  }
}
