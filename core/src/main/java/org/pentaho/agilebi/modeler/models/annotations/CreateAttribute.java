/*! ******************************************************************************
 *
 * Pentaho Community Edition Project: pentaho-modeler
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ********************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.models.annotations.data.GeneratedbyMemberAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.data.InlineFormatAnnotation;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.agilebi.modeler.nodes.TimeRole;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.pentaho.agilebi.modeler.geo.GeoContext.*;

/**
 * @author Rowell Belen
 */
public class CreateAttribute extends AnnotationType {

  private static final long serialVersionUID = 5169827225345800226L;
  public static final String MDI_GROUP = "ATTRIBUTE";
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );
  private static final String DEFAULT_AUTO_GEO_DIMENSION_NAME = "Geography";

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Attribute Name";
  public static final int NAME_ORDER = 0;

  public static final String TIME_TYPE_ID = "timeType";
  public static final String TIME_TYPE_NAME = "Time Level Type";
  public static final int TIME_TYPE_ORDER = 1;

  public static final String TIME_FORMAT_ID = "timeFormat";
  public static final String TIME_FORMAT_NAME = "Time Source Format";
  public static final int TIME_FORMAT_ORDER = 2;

  public static final String GEO_TYPE_ID = "geoType";
  public static final String GEO_TYPE_NAME = "Geo Type";
  public static final int GEO_TYPE_ORDER = 3;

  public static final String LATITUDE_FIELD_ID = "latitude";
  public static final String LATITUDE_FIELD_NAME = "Latitude";
  public static final int LATITUDE_FIELD_ORDER = 4;

  public static final String LONGITUDE_FIELD_ID = "longitude";
  public static final String LONGITUDE_FIELD_NAME = "Longitude";
  public static final int LONGITUDE_FIELD_ORDER = 5;

  public static final String ORDINAL_FIELD_ID = "ordinalField";
  public static final String ORDINAL_FIELD_NAME = "Ordinal Field";
  public static final int ORDINAL_FIELD_ORDER = 6;

  public static final String FORMAT_STRING_ID = "formatString";
  public static final String FORMAT_STRING_NAME = "Format";
  public static final int FORMAT_STRING_ORDER = 7;

  public static final String DESCRIPTION_ID = "description";
  public static final String DESCRIPTION_NAME = "Description";
  public static final int DESCRIPTION_ORDER = 8;

  public static final String BUSINESS_GROUP_ID = "businessGroup";
  public static final String BUSINESS_GROUP_NAME = "Business Group";
  public static final int BUSINESS_GROUP_ORDER = 9;

  public static final String PARENT_ATTRIBUTE_ID = "parentAttribute";
  public static final String PARENT_ATTRIBUTE_NAME = "Parent Attribute";
  public static final int PARENT_ATTRIBUTE_ORDER = 10;

  public static final String DIMENSION_ID = "dimension";
  public static final String DIMENSION_NAME = "Dimension";
  public static final int DIMENSION_ORDER = 11;

  public static final String HIERARCHY_ID = "hierarchy";
  public static final String HIERARCHY_NAME = "Hierarchy";
  public static final int HIERARCHY_ORDER = 12;

  public static final String UNIQUE_ID = "unique";
  public static final String UNIQUE_NAME = "Is Unique";
  public static final int UNIQUE_ORDER = 13;

  public static final String FIELD_ID = "field";
  public static final String FIELD_NAME = "Field";
  public static final int FIELD_ORDER = 14;

  public static final String LEVEL_ID = "level";
  public static final String LEVEL_NAME = "Level";
  public static final int LEVEL_ORDER = 15;

  public static final String CUBE_ID = "cube";
  public static final String CUBE_NAME = "Cube";
  public static final int CUBE_ORDER = 16;

  public static final String HIDDEN_ID = "hidden";
  public static final String HIDDEN_NAME = "Hidden";
  public static final int HIDDEN_ORDER = 17;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  @Injection( name = MDI_GROUP + "_NAME", group = MDI_GROUP )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = UNIQUE_ID, name = UNIQUE_NAME, order = UNIQUE_ORDER )
  @Injection( name = MDI_GROUP + "_IS_UNIQUE", group = MDI_GROUP )
  private boolean unique;

  @MetaStoreAttribute
  @ModelProperty( id = TIME_FORMAT_ID, name = TIME_FORMAT_NAME, order = TIME_FORMAT_ORDER )
  @Injection( name = MDI_GROUP + "_TIME_FORMAT", group = MDI_GROUP )
  private String timeFormat;

  @MetaStoreAttribute
  @ModelProperty( id = TIME_TYPE_ID, name = TIME_TYPE_NAME, order = TIME_TYPE_ORDER )
  @Injection( name = MDI_GROUP + "_TIME_TYPE", group = MDI_GROUP )
  private ModelAnnotation.TimeType timeType;

  @MetaStoreAttribute
  @ModelProperty( id = GEO_TYPE_ID, name = GEO_TYPE_NAME, order = GEO_TYPE_ORDER )
  @Injection( name = MDI_GROUP + "_GEO_TYPE", group = MDI_GROUP )
  private ModelAnnotation.GeoType geoType;

  @MetaStoreAttribute
  @ModelProperty( id = LATITUDE_FIELD_ID, name = LATITUDE_FIELD_NAME, order = LATITUDE_FIELD_ORDER )
  @Injection( name = MDI_GROUP + "_LATITUDE_FIELD", group = MDI_GROUP )
  private String latitudeField;

  @MetaStoreAttribute
  @ModelProperty( id = LONGITUDE_FIELD_ID, name = LONGITUDE_FIELD_NAME, order = LONGITUDE_FIELD_ORDER )
  @Injection( name = MDI_GROUP + "_LONGITUDE_FIELD", group = MDI_GROUP )
  private String longitudeField;

  @MetaStoreAttribute
  @ModelProperty( id = ORDINAL_FIELD_ID, name = ORDINAL_FIELD_NAME, order = ORDINAL_FIELD_ORDER )
  @Injection( name = MDI_GROUP + "_ORDINAL_FIELD", group = MDI_GROUP )
  private String ordinalField;

  @MetaStoreAttribute
  @ModelProperty( id = FORMAT_STRING_ID, name = FORMAT_STRING_NAME, order = FORMAT_STRING_ORDER,
    appliesTo = { ModelProperty.AppliesTo.Numeric, ModelProperty.AppliesTo.Time } )
  @Injection( name = MDI_GROUP + "_FORMAT_STRING", group = MDI_GROUP )
  private String formatString;

  @MetaStoreAttribute
  @ModelProperty( id = PARENT_ATTRIBUTE_ID, name = PARENT_ATTRIBUTE_NAME, order = PARENT_ATTRIBUTE_ORDER )
  @Injection( name = MDI_GROUP + "_PARENT", group = MDI_GROUP )
  private String parentAttribute;

  @MetaStoreAttribute
  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME, order = DIMENSION_ORDER )
  @Injection( name = MDI_GROUP + "_DIMENSION", group = MDI_GROUP )
  private String dimension;

  @MetaStoreAttribute
  @ModelProperty( id = HIERARCHY_ID, name = HIERARCHY_NAME, order = HIERARCHY_ORDER )
  @Injection( name = MDI_GROUP + "_HIERARCHY", group = MDI_GROUP )
  private String hierarchy;

  @MetaStoreAttribute
  @ModelProperty( id = DESCRIPTION_ID, name = DESCRIPTION_NAME, order = DESCRIPTION_ORDER )
  @Injection( name = MDI_GROUP + "_DESCRIPTION", group = MDI_GROUP )
  private String description;

  @MetaStoreAttribute
  // Do not expose business group in the UI (for now)
  //@ModelProperty( id = BUSINESS_GROUP_ID, name = BUSINESS_GROUP_NAME, order = BUSINESS_GROUP_ORDER )
  private String businessGroup;

  @MetaStoreAttribute
  @ModelProperty( id = FIELD_ID, name = FIELD_NAME, order = FIELD_ORDER, hideUI = true )
  @Injection( name = MDI_GROUP + "_FIELD", group = MDI_GROUP )
  private String field;

  @MetaStoreAttribute
  @ModelProperty( id = LEVEL_ID, name = LEVEL_NAME, order = LEVEL_ORDER, hideUI = true  )
  private String level;

  @MetaStoreAttribute
  @ModelProperty( id = CUBE_ID, name = CUBE_NAME, order = CUBE_ORDER, hideUI = true )
  private String cube;

  @MetaStoreAttribute
  @ModelProperty( id = HIDDEN_ID, name = HIDDEN_NAME, order = HIDDEN_ORDER )
  @Injection( name = MDI_GROUP + "_IS_HIDDEN", group = MDI_GROUP )
  private boolean hidden;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique( boolean unique ) {
    this.unique = unique;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  public void setTimeFormat( String timeFormat ) {
    this.timeFormat = timeFormat;
  }

  public ModelAnnotation.TimeType getTimeType() {
    return timeType;
  }

  public void setTimeType( ModelAnnotation.TimeType timeType ) {
    this.timeType = timeType;
  }

  public ModelAnnotation.GeoType getGeoType() {
    return geoType;
  }

  public void setGeoType( ModelAnnotation.GeoType geoType ) {
    this.geoType = geoType;
  }

  public String getOrdinalField() {
    return ordinalField;
  }

  public void setOrdinalField( String ordinalField ) {
    this.ordinalField = ordinalField;
  }

  public String getFormatString() {
    return formatString;
  }

  public void setFormatString( final String formatString ) {
    this.formatString = formatString;
  }

  public String getParentAttribute() {
    return parentAttribute;
  }

  public void setParentAttribute( final String parentAttribute ) {
    this.parentAttribute = parentAttribute;
  }

  public String getDimension() {
    return dimension;
  }

  public void setDimension( final String dimension ) {
    this.dimension = dimension;
  }

  public String getHierarchy() {
    return hierarchy;
  }

  public void setHierarchy( final String hierarchy ) {
    this.hierarchy = hierarchy;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getBusinessGroup() {
    return businessGroup;
  }

  public void setBusinessGroup( String businessGroup ) {
    this.businessGroup = businessGroup;
  }

  @Override
  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel( String level ) {
    this.level = level;
  }

  public String getCube() {
    return cube;
  }

  public void setCube( String cube ) {
    this.cube = cube;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public String getLatitudeField() {
    return latitudeField;
  }

  public void setLatitudeField( String latitudeField ) {
    this.latitudeField = latitudeField;
  }

  public String getLongitudeField() {
    return longitudeField;
  }

  public void setLongitudeField( String longitudeField ) {
    this.longitudeField = longitudeField;
  }

  @Override
  public boolean apply(
      final ModelerWorkspace workspace, final IMetaStore metaStore ) throws ModelerException {
    HierarchyMetaData existingHierarchy = locateHierarchy( workspace, getHierarchy() );
    if ( existingHierarchy == null && !Const.isEmpty( getParentAttribute() ) ) {
      return false;
    } else if ( existingHierarchy != null && Const.isEmpty( getParentAttribute() ) ) {
      removeHierarchy( existingHierarchy );
      return createNewHierarchy( workspace, resolveField( workspace ) );
    } else if ( existingHierarchy == null ) {
      return createNewHierarchy( workspace, resolveField( workspace ) );
    } else {
      return attachLevel( workspace, existingHierarchy, resolveField( workspace ) );
    }
  }

  private String resolveField( final ModelerWorkspace workspace ) throws ModelerException {
    String field = getField();
    if ( StringUtils.isBlank( field ) ) {
      if ( !StringUtils.isBlank( getLevel() ) && !StringUtils.isBlank( getCube() ) ) {
        field = resolveFieldFromLevel( workspace, getLevel(), getCube() );
        setField( field );
      } else {
        throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_FIELD" )
        );
      }
    }

    return field;
  }

  private void removeHierarchy( final HierarchyMetaData hierarchy ) {
    DimensionMetaData dimension = hierarchy.getDimensionMetaData();
    if ( dimension.contains( hierarchy ) ) {
      dimension.remove( hierarchy );
    }
  }

  private HierarchyMetaData locateHierarchy( final ModelerWorkspace workspace, final String name ) {
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      if ( dimensionMetaData.getName().equals( getDimension() ) ) {
        for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
          if ( hierarchyMetaData.getName().equals( Const.isEmpty( name ) ? getDimension() : name ) ) {
            return hierarchyMetaData;
          }
        }
      }
    }
    return null;
  }

  private boolean isAutoModeled( final ModelerWorkspace workspace ) {
    try {
      if ( ( getGeoType() != null ) && StringUtils
          .equals( workspace.getGeoContext().getDimensionName(), getDimension() ) ) {
        return true;
      }
      HierarchyMetaData hierarchy = locateHierarchy( workspace, getDimension() );
      return hierarchy != null
        && hierarchy.size() == 1
        && hierarchy.getLevels().get( 0 ).getName().equals( hierarchy.getName() );
    } catch ( Exception e ) {
      return false;
    }
  }

  private boolean createNewHierarchy( final ModelerWorkspace workspace, final String column ) throws ModelerException {
    HierarchyMetaData hierarchyMetaData
        = new HierarchyMetaData( Const.isEmpty( getHierarchy() ) ? getDimension() : getHierarchy() );
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      if ( dimensionMetaData.getName().equals( getDimension() ) && !isAutoModeled( workspace ) ) {
        hierarchyMetaData.setParent( dimensionMetaData );
        if ( dimensionMetaData.isEmpty() ) {
          dimensionMetaData.setDimensionType( dimensionType() );
        }
        dimensionMetaData.add( hierarchyMetaData );
      }
    }
    if ( hierarchyMetaData.getParent() == null ) {
      DimensionMetaData dimensionMetaData = new DimensionMetaData( getDimension(), dimensionType() );
      dimensionMetaData.setTimeDimension( getTimeType() != null );
      dimensionMetaData.getMemberAnnotations().put( GeneratedbyMemberAnnotation.GEBERATED_BY_STRING,
          new GeneratedbyMemberAnnotation( dimensionMetaData.getName() ) );
      workspace.getModel().getDimensions().add( dimensionMetaData );
      hierarchyMetaData.setParent( dimensionMetaData );
      dimensionMetaData.add( hierarchyMetaData );
    }
    LevelMetaData existingLevel = locateLevel( workspace, column );
    LevelMetaData ordinalAutoLevel = locateLevel( workspace, getOrdinalField() );
    LogicalColumn logicalColumn = locateLogicalColumn( workspace, column );
    if ( logicalColumn == null ) {
      return false;
    }
    LevelMetaData levelMetaData = new LevelMetaData( hierarchyMetaData, getName() );
    hierarchyMetaData.add( levelMetaData );
    fillLevelProperties( workspace, logicalColumn, levelMetaData );
    removeAutoLevel( workspace, existingLevel );
    removeAutoMeasure( workspace, column );
    removeAutoLevel( workspace, ordinalAutoLevel );
    removeAutoMeasure( workspace, getOrdinalField() );
    workspace.getWorkspaceHelper().populateDomain( workspace );
    return true;
  }

  private String dimensionType() {
    if ( getTimeType() != null ) {
      return OlapDimension.TYPE_TIME_DIMENSION;
    }
    return OlapDimension.TYPE_STANDARD_DIMENSION;
  }

  private void fillLevelProperties( final ModelerWorkspace workspace, final LogicalColumn logicalColumn,
      final LevelMetaData levelMetaData ) throws ModelerException {
    levelMetaData.setLogicalColumn( logicalColumn );
    levelMetaData.setUniqueMembers( isUnique() );
    levelMetaData.setHidden( isHidden() );
    if ( getTimeType() != null ) {
      levelMetaData.setDataRole( TimeRole.fromMondrianAttributeValue( getTimeType().name() ) );
    }
    if ( !Const.isEmpty( getTimeFormat() ) ) {
      levelMetaData.setTimeLevelFormat( getTimeFormat() );
    }
    LogicalColumn ordinalColumn = locateLogicalColumn( workspace, getOrdinalField() );
    if ( ordinalColumn != null ) {
      levelMetaData.setLogicalOrdinalColumn( ordinalColumn );
    }
    if ( getGeoType() != null ) {
      if ( Const.isEmpty( getParentAttribute() ) ) {
        removeAutoGeo( workspace );
      }
      GeoRole geoRole = workspace.getGeoContext().getGeoRoleByName( getGeoType().name() );
      levelMetaData.getMemberAnnotations().put( ANNOTATION_DATA_ROLE, geoRole );

      // If this is a Lat/Long Location Geo Type then try use the User defined Lat/Long values. If the User has not
      // defined Lat/Long fields then try and find them.
      if ( ModelAnnotation.GeoType.Location.equals( getGeoType() ) ) {
        if ( !StringUtils.isEmpty( latitudeField ) && !StringUtils.isEmpty( longitudeField ) ) {
          LevelMetaData oldLocationMetaData = locateLocationLevel( workspace );
          MemberPropertyMetaData latitudeMetaData = null;
          MemberPropertyMetaData longitudeMetaData = null;

          if ( null != oldLocationMetaData  ) {
            MemberPropertyMetaData tmpLatitudeMetaData = oldLocationMetaData.getLatitudeField();
            MemberPropertyMetaData tmpLongitudeMetaData = oldLocationMetaData.getLongitudeField();
            if ( tmpLatitudeMetaData.getColumnName().equalsIgnoreCase( latitudeField )
                && tmpLongitudeMetaData.getColumnName().equalsIgnoreCase( longitudeField ) ) {
              latitudeMetaData = tmpLatitudeMetaData;
              longitudeMetaData = tmpLongitudeMetaData;
            }
          }

          if ( null != latitudeMetaData && null != longitudeMetaData ) {
            moveGeoLocationFields( oldLocationMetaData, levelMetaData );
          } else {
            workspace.getGeoContext().setLocationFields( workspace, levelMetaData, latitudeField, longitudeField );
          }
        } else {
          LevelMetaData oldLocationMetaData = locateLocationLevel( workspace );
          if ( null != oldLocationMetaData ) {
            moveGeoLocationFields( oldLocationMetaData, levelMetaData );
          } else {
            workspace.getGeoContext().setLocationFields( workspace, levelMetaData );
          }
        }
      }
    }
    if ( getDescription() != null ) {
      levelMetaData.setDescription( getDescription() );
    }
    if ( !StringUtils.isBlank( getFormatString() ) ) {
      levelMetaData.getMemberAnnotations().put(
        InlineFormatAnnotation.INLINE_MEMBER_FORMAT_STRING,  new InlineFormatAnnotation( getFormatString() ) );
    }
  }

  private void removeAutoGeo( final ModelerWorkspace workspace ) {
    DimensionMetaDataCollection dimensions = workspace.getModel().getDimensions();
    DimensionMetaData toRemove = null;
    GeoContext geoContext = workspace.getGeoContext();
    for ( DimensionMetaData dimensionMetaData : dimensions ) {
      if ( geoContext != null && dimensionMetaData.getName().equals( geoContext.getDimensionName() ) ) {
        for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
          if ( hierarchyMetaData.getName().equals( geoContext.getDimensionName() ) ) {
            for ( LevelMetaData levelMetaData : hierarchyMetaData ) {
              if ( levelMetaData.getMemberAnnotations().get( "Data.Role" ) != null ) {
                toRemove = dimensionMetaData;
                break;
              }
            }
          }
        }
      }
    }
    if ( toRemove != null ) {
      dimensions.remove( toRemove );
    }
  }

  private void moveGeoLocationFields( LevelMetaData oldLocationMetaData, LevelMetaData newLocationMetaData ) {
    MemberPropertyMetaData latitudeMetaData = oldLocationMetaData.getLatitudeField();
    MemberPropertyMetaData longitudeMetaData = oldLocationMetaData.getLongitudeField();
    newLocationMetaData.add( latitudeMetaData );
    newLocationMetaData.add( longitudeMetaData );
    oldLocationMetaData.remove( latitudeMetaData );
    oldLocationMetaData.remove( longitudeMetaData );

    Map oldMemberAnnotations = oldLocationMetaData.getMemberAnnotations();
    Map newMemberAnnotations = newLocationMetaData.getMemberAnnotations();
    newMemberAnnotations.put( ANNOTATION_GEO_ROLE, oldMemberAnnotations.remove( ANNOTATION_GEO_ROLE ) );
    newMemberAnnotations.put( ANNOTATION_DATA_ROLE, oldMemberAnnotations.remove( ANNOTATION_DATA_ROLE ) );
  }

  private boolean attachLevel( final ModelerWorkspace workspace, final HierarchyMetaData existingHierarchy,
      final String column ) throws ModelerException {
    int parentIndex = parentIndex( existingHierarchy );
    if ( parentIndex < 0 ) {
      return false;
    } else {
      LevelMetaData existingLevel = locateLevel( workspace, column );
      LevelMetaData ordinalAutoLevel = locateLevel( workspace, getOrdinalField() );
      LogicalColumn logicalColumn = locateLogicalColumn( workspace, column );
      if ( logicalColumn == null ) {
        return false;
      }
      LevelMetaData levelMetaData = new LevelMetaData( existingHierarchy, getName() );
      existingHierarchy.add( parentIndex + 1, levelMetaData );
      fillLevelProperties( workspace, logicalColumn, levelMetaData );
      removeAutoLevel( workspace, existingLevel );
      removeAutoMeasure( workspace, column );
      removeAutoLevel( workspace, ordinalAutoLevel );
      removeAutoMeasure( workspace, getOrdinalField() );
      workspace.getWorkspaceHelper().populateDomain( workspace );
      return true;
    }
  }

  private int parentIndex( final HierarchyMetaData existingHierarchy ) {
    List<LevelMetaData> levels = existingHierarchy.getLevels();
    for ( int i = 0; i < levels.size(); i++ ) {
      LevelMetaData levelMetaData = levels.get( i );
      if ( levelMetaData.getName().equals( getParentAttribute() ) ) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void populate( final Map<String, Serializable> propertiesMap ) {

    super.populate( propertiesMap ); // let base class handle primitives, etc.

    try {
      // correctly convert time type
      if ( propertiesMap.containsKey( TIME_TYPE_ID ) ) {
        Serializable value = propertiesMap.get( TIME_TYPE_ID );
        if ( value != null ) {
          setTimeType( ModelAnnotation.TimeType.valueOf( value.toString() ) );
        }
      }

      // correctly convert geo type
      if ( propertiesMap.containsKey( GEO_TYPE_ID ) ) {
        Serializable value = propertiesMap.get( GEO_TYPE_ID );
        if ( value != null ) {
          setGeoType( ModelAnnotation.GeoType.valueOf( value.toString() ) );
        }
      }
    } catch ( Exception e ) {
      // ignore
      logger.warning( e.getLocalizedMessage() );
    }
  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.CREATE_ATTRIBUTE;
  }

  @Override public String getSummary() {
    return BaseMessages
        .getString( MSG_CLASS, summaryMsgKey(), getName(), Const.isEmpty( getHierarchy() ) ? "" : " " + getHierarchy(),
            getParentAttribute() );
  }

  private String summaryMsgKey() {
    if ( Const.isEmpty( getParentAttribute() ) ) {
      return "Modeler.CreateAttribute.Summary.noparent";
    }
    return "Modeler.CreateAttribute.Summary";
  }

  @Override
  public boolean apply( Document schema ) throws ModelerException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void validate() throws ModelerException {

    if ( StringUtils.isBlank( getName() ) ) {
      throw new ModelerException( BaseMessages
          .getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.ATTRIBUTE_NAME_REQUIRED" ) );
    }

    if ( StringUtils.isNotBlank( getLatitudeField() ) && StringUtils.isNotBlank( getLongitudeField() )
        && StringUtils.equals( getLatitudeField(), getLongitudeField() ) ) {
      throw new ModelerException( BaseMessages
          .getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.LATITUDE_EQUALS_LONGITUDE" ) );
    }

    if ( ( StringUtils.isBlank( getLatitudeField() ) && StringUtils.isNotBlank( getLongitudeField() ) )
        || ( StringUtils.isBlank( getLongitudeField() ) && StringUtils.isNotBlank( getLatitudeField() ) )  ) {
      throw new ModelerException( BaseMessages
          .getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.LATITUDE_OR_LONGITUDE_NOT_PROVIDED" ) );
    }

    if ( StringUtils.isBlank( getField() ) && ( StringUtils.isBlank( getLevel() )
        || StringUtils.isBlank( getCube() ) ) ) {
      throw new ModelerException( BaseMessages
        .getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.FIELD_OR_LEVEL_NOT_PROVIDED" ) );
    }

    if ( StringUtils.isBlank( getDimension() ) ) {
      throw new ModelerException( BaseMessages
          .getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.PARENT_PROVIDED_MISSING_DIMENSION" ) );
    }

    if ( StringUtils.isNotBlank( getParentAttribute() ) && StringUtils.isBlank( getDimension() ) ) {
      throw new ModelerException( BaseMessages
          .getString( MSG_CLASS, "ModelAnnotation.CreateAttribute.validation.PARENT_PROVIDED_MISSING_DIMENSION" ) );
    }
  }

  /**
   * CreateAttribute objects are considered logically equal if the name, dimension, and hierarchy are equal to
   * the equivalent fields in the object under comparison
   * @param obj
   * @return
   */
  @Override
  public boolean equalsLogically( AnnotationType obj ) {
    if ( obj == null || obj.getClass() != getClass() ) {
      return false;
    }
    CreateAttribute that = (CreateAttribute) obj;

    EqualsBuilder eq = new EqualsBuilder();

    // by default just see if the name is the same
    String thatName = that.getName() == null ? null : that.getName().toLowerCase();
    String myName = getName() == null ? null : getName().toLowerCase();
    eq.append( myName, thatName );

    String thatDimension = that.getDimension() == null ? null : that.getDimension().toLowerCase();
    String myDimension = getDimension() == null ? null : getDimension().toLowerCase();
    eq.append( myDimension, thatDimension );

    String thatHierarchy = that.getHierarchy() == null ? null : that.getHierarchy().toLowerCase();
    String myHierarchy = getHierarchy() == null ? null : getHierarchy().toLowerCase();

    // fall back to default hierarchy name equal to dimension name
    if ( thatHierarchy == null ) {
      thatHierarchy = thatDimension;
    }
    if ( myHierarchy == null ) {
      myHierarchy = myDimension;
    }

    eq.append( myHierarchy, thatHierarchy );

    return eq.isEquals();

  }
}
