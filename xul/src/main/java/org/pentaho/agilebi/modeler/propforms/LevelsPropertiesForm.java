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

package org.pentaho.agilebi.modeler.propforms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.nodes.BaseColumnBackedMetaData;
import org.pentaho.agilebi.modeler.nodes.DataRole;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.TimeRole;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

public class LevelsPropertiesForm extends AbstractModelerNodeForm<BaseColumnBackedMetaData> {
  private static final long serialVersionUID = 7387695478794340469L;
  protected XulTextbox name;
  protected XulCheckbox hasUniqueMembers;
  protected XulLabel sourceLabel;
  protected XulLabel ordinalLabel;
  // protected XulLabel captionLabel;
  protected XulLabel level_message_label;
  protected XulVbox messageBox;
  protected String colName;
  protected String ordinalColName;
  protected XulButton clearOrdinalColumnBtn;
  protected String captionColName;
  protected String locale;
  protected XulButton messageBtn;
  protected XulMenuList geoList;
  protected XulMenuList timeLevelTypeList;
  protected XulMenuList timeLevelFormatList;
  protected String timeLevelFormat;

  protected List<GeoRole> geoRoles = new ArrayList<GeoRole>();
  protected GeoRole selectedGeoRole;
  protected GeoRole dummyGeoRole = new GeoRole( ModelerMessagesHolder.getMessages().getString( "none" ), Collections
      .<String>emptyList() );

  protected List<TimeRole> timeRoles = new ArrayList<TimeRole>();
  protected TimeRole selectedTimeLevelType = TimeRole.DUMMY;

  public LevelsPropertiesForm( String panelId, String locale ) {
    super( panelId );
    this.locale = locale;
  }

  protected PropertyChangeListener validListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      String propertyName = evt.getPropertyName();
      if ( propertyName.equals( "valid" ) || propertyName.equals( "logicalColumn" )
          || propertyName.equals( "logicalOrdinalColumn" ) || propertyName.equals( "ordinalColumnName" )
          || propertyName.equals( "logicalCaptionColumn" ) || propertyName.equals( "timeLevelFormat" ) ) {
        showValidations();
      }
    }
  };

  public LevelsPropertiesForm( String locale ) {
    this( "levelprops", locale );
  }

  public void setObject( BaseColumnBackedMetaData metadata ) {
    LevelMetaData levelMetaData = (LevelMetaData) metadata;
    if ( getNode() != null ) {
      getNode().removePropertyChangeListener( validListener );
    }

    setNode( levelMetaData );
    if ( levelMetaData == null ) {
      return;
    }
    getNode().addPropertyChangeListener( validListener );

    name.setValue( levelMetaData.getName() );

    setColumnName( getColumnNameFromLogicalColumn( levelMetaData.getLogicalColumn() ) );
    ordinalColName = "";
    setOrdinalColumnName( getColumnNameFromLogicalColumn( levelMetaData.getLogicalOrdinalColumn() ) );
    // setCaptionColumnName(getColumnNameFromLogicalColumn(levelMetaData.getLogicalCaptionColumn()));
    hasUniqueMembers.setChecked( levelMetaData.isUniqueMembers() );

    Map<String, IMemberAnnotation> annotations = levelMetaData.getMemberAnnotations();

    if ( levelMetaData.isTimeLevel() ) {
      setGeoLevelElementsVisible( false );
      setTimeLevelElementsVisible( true );
      DataRole dataRole = getNode().getDataRole();
      setSelectedTimeLevelType( dataRole instanceof TimeRole ? ( (TimeRole) dataRole ) : TimeRole.DUMMY );
    } else {
      setGeoLevelElementsVisible( true );
      setTimeLevelElementsVisible( false );
      GeoRole geoRole = (GeoRole) annotations.get( GeoContext.ANNOTATION_GEO_ROLE );
      setSelectedGeoRole( geoRole );
      if ( selectedGeoRole == null ) {
        setSelectedGeoRole( dummyGeoRole );
      }
    }

    showValidations();
  }

  protected void showValidations() {
    if ( getNode() == null ) {
      return;
    }
    setNotValid( !getNode().isValid() );
    LogicalColumn logicalColumn;

    logicalColumn = getNode().getLogicalColumn();
    setBackingColumnAvailable( logicalColumn != null );
    setColumnName( getColumnNameFromLogicalColumn( logicalColumn ) );

    logicalColumn = getNode().getLogicalOrdinalColumn();
    setOrdinalColumnName( getColumnNameFromLogicalColumn( logicalColumn ) );

    messageBox.setVisible( getNode().getValidationMessages().size() > 0 );
    setValidMessages( getNode().getValidationMessagesString() );
  }

  protected void setGeoLevelElementsVisible( boolean visible ) {
    setContainerVisible( "geo_level_elements", visible );
  }

  protected void setTimeLevelElementsVisible( boolean visible ) {
    setContainerVisible( "time_level_elements", visible );
  }

  public void init( ModelerWorkspace workspace ) {
    super.init( workspace );
    bf.createBinding( this, "notValid", "level_message", "visible" );
    name = (XulTextbox) document.getElementById( "level_name" );
    hasUniqueMembers = (XulCheckbox) document.getElementById( "has_unique_members" );
    sourceLabel = (XulLabel) document.getElementById( "level_source_col" );
    ordinalLabel = (XulLabel) document.getElementById( "level_ordinal_col" );
    clearOrdinalColumnBtn = (XulButton) document.getElementById( "clear_ordinal_column" );
    // captionLabel = (XulLabel) document.getElementById("level_source_col");
    level_message_label = (XulLabel) document.getElementById( "level_message_label" );
    messageBox = (XulVbox) document.getElementById( "level_message" );
    bf.createBinding( this, "backingColumnAvailable", "fixLevelColumnsBtn", "!visible" );

    bf.createBinding( this, "columnName", sourceLabel, "value" );
    bf.createBinding( this, "ordinalColumnName", ordinalLabel, "value" );
    bf.createBinding( this, "ordinalColumnName", clearOrdinalColumnBtn, "image",
        new BindingConvertor<String, String>() {

          @Override
          public String sourceToTarget( String value ) {
            return "images/" + ( value == null ? "blank_button" : "remove" ) + ".png";
          }

          @Override
          public String targetToSource( String value ) {
            return null;
          }
        } );

    bf.createBinding( this, "name", name, "value" );
    bf.createBinding( this, "uniqueMembers", hasUniqueMembers, "checked" );
    bf.createBinding( this, "validMessages", level_message_label, "value", validMsgTruncatedBinding );
    messageBtn = (XulButton) document.getElementById( "level_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );

    geoList = (XulMenuList) document.getElementById( "level_geo_role" );
    geoRoles.clear();
    geoRoles.add( dummyGeoRole );
    geoRoles.addAll( workspace.getGeoContext() );
    geoList.setElements( geoRoles );

    bf.createBinding( geoList, "selectedItem", this, "selectedGeoRole" );

    timeLevelTypeList = (XulMenuList) document.getElementById( "time_level_type" );
    timeRoles.clear();
    timeRoles.addAll( TimeRole.getAllRoles() );
    timeLevelTypeList.setElements( timeRoles );

    bf.createBinding( timeLevelTypeList, "selectedItem", this, "selectedTimeLevelType" );

    timeLevelFormatList = (XulMenuList) document.getElementById( "time_level_format" );
    bf.createBinding( timeLevelFormatList, "value", this, "timeLevelFormat" );
  }

  protected String getColumnNameFromLogicalColumn( LogicalColumn col ) {
    String columnName = ""; //$NON-NLS-1$
    if ( col != null ) {
      IPhysicalColumn physicalColumn = col.getPhysicalColumn();
      if ( physicalColumn != null ) {
        columnName = physicalColumn.getName( locale );
      }
    }
    return columnName;
  }

  @Bindable
  public void setColumnName( String name ) {
    String prevName = colName;
    if ( prevName == null && "".equals( name ) ) {
      return;
    }
    if ( prevName != null && name != null && prevName.equals( name ) ) {
      return;
    }
    colName = name;
    this.firePropertyChange( "columnName", prevName, colName ); //$NON-NLS-1$
  }

  @Bindable
  public String getColumnName() {
    return colName;
  }

  @Bindable
  public void setOrdinalColumnName( String name ) {
    String prevName = ordinalColName;
    if ( "".equals( name ) ) {
      name = null;
    }
    if ( prevName == null && name == null ) {
      return;
    }
    if ( prevName != null && name != null && prevName.equals( name ) ) {
      return;
    }
    ordinalColName = name;
    this.firePropertyChange( "ordinalColumnName", prevName, ordinalColName ); //$NON-NLS-1$
  }

  @Bindable
  public String getOrdinalColumnName() {
    return ordinalColName;
  }

  @Bindable
  public void setCaptionColumnName( String name ) {
    String prevName = captionColName;
    if ( prevName == null && "".equals( name ) ) {
      return;
    }
    if ( prevName != null && name != null && prevName.equals( name ) ) {
      return;
    }
    captionColName = name;
    this.firePropertyChange( "captionColumnName", prevName, captionColName ); //$NON-NLS-1$
  }

  @Bindable
  public String geCaptionColumnName() {
    return captionColName;
  }

  @Bindable
  public void setName( String name ) {
    if ( getNode() != null ) {
      getNode().setName( name );
    }
    this.name.setValue( name );
  }

  @Bindable
  public String getName() {
    if ( getNode() == null ) {
      return null;
    }
    return getNode().getName();
  }

  @Bindable
  public void setUniqueMembers( boolean uniqueMembers ) {
    if ( getNode() != null ) {
      getNode().setUniqueMembers( uniqueMembers );
    }
    if ( uniqueMembers == hasUniqueMembers.isChecked() ) {
      return;
    }
    hasUniqueMembers.setChecked( uniqueMembers );
  }

  @Bindable
  public boolean isUniqueMembers() {
    if ( getNode() == null ) {
      return false;
    }
    return getNode().isUniqueMembers();
  }

  @Bindable
  public boolean isNotValid() {
    if ( getNode() != null ) {
      return !getNode().isValid();
    } else {
      return false;
    }
  }

  @Bindable
  public void setNotValid( boolean notValid ) {
    this.firePropertyChange( "notValid", null, notValid );
  }

  @Bindable
  public boolean isBackingColumnAvailable() {
    if ( getNode() != null ) {
      return getNode().getLogicalColumn() != null;
    } else {
      return false;
    }
  }

  @Bindable
  public void setBackingColumnAvailable( boolean available ) {
    this.firePropertyChange( "backingColumnAvailable", null, available );
  }

  @Override
  public String getValidMessages() {
    if ( getNode() != null ) {
      return getNode().getValidationMessagesString();
    } else {
      return null;
    }
  }

  @Bindable
  public GeoRole getSelectedGeoRole() {
    return selectedGeoRole;
  }

  @Bindable
  public void setSelectedGeoRole( GeoRole selectedGeoRole ) {
    GeoRole prevVal = this.selectedGeoRole;
    this.selectedGeoRole = selectedGeoRole;
    if ( selectedGeoRole != null && selectedGeoRole != dummyGeoRole ) {
      getNode().getMemberAnnotations().put( GeoContext.ANNOTATION_GEO_ROLE, selectedGeoRole );
      getNode().getMemberAnnotations().put( GeoContext.ANNOTATION_DATA_ROLE, selectedGeoRole );
    } else {
      getNode().getMemberAnnotations().remove( GeoContext.ANNOTATION_DATA_ROLE );
      getNode().getMemberAnnotations().remove( GeoContext.ANNOTATION_GEO_ROLE );
    }
    getNode().validateNode();
    showValidations();
    firePropertyChange( "selectedGeoRole", prevVal, selectedGeoRole );
  }

  @Bindable
  public TimeRole getSelectedTimeLevelType() {
    return selectedTimeLevelType;
  }

  @Bindable
  public void setSelectedTimeLevelType( TimeRole selectedTimeLevelType ) {
    TimeRole oldTimeLevelType = this.getSelectedTimeLevelType();
    if ( selectedTimeLevelType == null ) {
      selectedTimeLevelType = TimeRole.DUMMY;
    }
    this.selectedTimeLevelType = selectedTimeLevelType;
    getNode().setDataRole( selectedTimeLevelType );
    List<String> formatsList = selectedTimeLevelType.getFormatsList();
    String value = getTimeLevelFormat();
    int selectedIndex = formatsList.indexOf( value );
    timeLevelFormatList.setElements( formatsList );
    if ( selectedIndex == -1 ) {
      if ( value == null ) {
        value = "";
      }
      timeLevelFormatList.setValue( value );
    } else {
      timeLevelFormatList.setSelectedIndex( selectedIndex );
    }
    firePropertyChange( "selectedTimeLevelType", oldTimeLevelType, selectedTimeLevelType );
  }

  @Bindable
  public void setTimeLevelFormat( String format ) {
    if ( "".equals( format ) ) {
      format = null;
    }
    if ( format == null && timeLevelFormat == null ) {
      return;
    }
    if ( format != null && timeLevelFormat != null && format.equals( timeLevelFormat ) ) {
      return;
    }
    if ( getNode() != null ) {
      getNode().setTimeLevelFormat( format );
    }

    LevelMetaData levelMetaData = (LevelMetaData) getNode();
    HierarchyMetaData hierarchyMetaData = levelMetaData.getHierarchyMetaData();
    List<LevelMetaData> levels = hierarchyMetaData.getLevels();
    boolean isDescendant = false;
    for ( LevelMetaData descendant : levels ) {
      if ( !isDescendant ) {
        if ( descendant == levelMetaData ) {
          isDescendant = true;
        }
        continue;
      }
      descendant.validateNode();
    }
    showValidations();
    timeLevelFormat = format;
  }

  @Bindable
  public String getTimeLevelFormat() {
    if ( getNode() == null ) {
      return null;
    }
    return getNode().getTimeLevelFormat();
  }

}
