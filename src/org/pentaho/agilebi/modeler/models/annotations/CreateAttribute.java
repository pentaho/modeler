/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package org.pentaho.agilebi.modeler.models.annotations;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.TimeRole;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.olap.OlapDimension;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Rowell Belen
 */
public class CreateAttribute extends AnnotationType {

  private static final long serialVersionUID = 5169827225345800226L;

  public static final String NAME_ID = "name";
  public static final String NAME_NAME = "Attribute Name";

  public static final String UNIQUE_ID = "unique";
  public static final String UNIQUE_NAME = "Is Unique";

  public static final String TIME_FORMAT_ID = "timeFormat";
  public static final String TIME_FORMAT_NAME = "Time Source Format";

  public static final String TIME_TYPE_ID = "timeType";
  public static final String TIME_TYPE_NAME = "Time Level Type";

  public static final String GEO_TYPE_ID = "geoType";
  public static final String GEO_TYPE_NAME = "Geo Type";

  public static final String ORDINAL_FIELD_ID = "ordinalField";
  public static final String ORDINAL_FIELD_NAME = "Ordinal Field";

  public static final String PARENT_ATTRIBUTE_ID = "parentAttribute";
  public static final String PARENT_ATTRIBUTE_NAME = "Parent Attribute";

  public static final String DIMENSION_ID = "dimension";
  public static final String DIMENSION_NAME = "Dimension";

  public static final String HIERARCHY_ID = "hierarchy";
  public static final String HIERARCHY_NAME = "Hierarchy";

  @ModelProperty( id = NAME_ID, name = NAME_NAME )
  private String name;

  @ModelProperty( id = UNIQUE_ID, name = UNIQUE_NAME )
  private boolean unique;

  @ModelProperty( id = TIME_FORMAT_ID, name = TIME_FORMAT_NAME )
  private String timeFormat;

  @ModelProperty( id = TIME_TYPE_ID, name = TIME_TYPE_NAME )
  private ModelAnnotation.TimeType timeType;

  @ModelProperty( id = GEO_TYPE_ID, name = GEO_TYPE_NAME )
  private ModelAnnotation.GeoType geoType;

  @ModelProperty( id = ORDINAL_FIELD_ID, name = ORDINAL_FIELD_NAME )
  private String ordinalField;

  @ModelProperty( id = PARENT_ATTRIBUTE_ID, name = PARENT_ATTRIBUTE_NAME )
  private String parentAttribute;

  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME )
  private String dimension;

  @ModelProperty( id = HIERARCHY_ID, name = HIERARCHY_NAME )
  private String hierarchy;

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

  @Override
  public boolean apply( final ModelerWorkspace workspace, final String column ) throws ModelerException {
    HierarchyMetaData existingHierarchy = locateHierarchy( workspace );
    if ( existingHierarchy == null && getParentAttribute() != null ) {
      return false;
    } else if ( existingHierarchy != null && getParentAttribute() == null ) {
      throw new ModelerException( "Cannot create an attribute at the top of an existing hierarchy" );
    } else if ( existingHierarchy == null ) {
      return createNewHierarchy( workspace, column );
    } else {
      return attachLevel( workspace, existingHierarchy, column );
    }
  }

  private HierarchyMetaData locateHierarchy( final ModelerWorkspace workspace ) {
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      if ( dimensionMetaData.getName().equals( getDimension() ) ) {
        for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
          if ( hierarchyMetaData.getName().equals( getHierarchy() ) ) {
            return hierarchyMetaData;
          }
        }
      }
    }
    return null;
  }

  private boolean createNewHierarchy( final ModelerWorkspace workspace, final String column ) throws ModelerException {
    HierarchyMetaData hierarchyMetaData = new HierarchyMetaData( getHierarchy() );
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      if ( dimensionMetaData.getName().equals( getDimension() ) ) {
        hierarchyMetaData.setParent( dimensionMetaData );
        dimensionMetaData.add( hierarchyMetaData );
      }
    }
    if ( hierarchyMetaData.getParent() == null ) {
      DimensionMetaData dimensionMetaData = new DimensionMetaData( getDimension(), dimensionType() );
      dimensionMetaData.setTimeDimension( getTimeType() != null );
      workspace.getModel().getDimensions().add( dimensionMetaData );
      hierarchyMetaData.setParent( dimensionMetaData );
      dimensionMetaData.add( hierarchyMetaData );
    }
    LevelMetaData existingLevel = locateLevel( workspace, column );
    LevelMetaData levelMetaData = buildLevel( workspace, hierarchyMetaData, existingLevel );
    hierarchyMetaData.add( levelMetaData );
    removeAutoLevel( workspace, existingLevel );
    workspace.getWorkspaceHelper().populateDomain( workspace );
    return true;
  }

  private String dimensionType() {
    if ( getTimeType() != null ) {
      return OlapDimension.TYPE_TIME_DIMENSION;
    }
    return OlapDimension.TYPE_STANDARD_DIMENSION;
  }

  private void removeAutoLevel( final ModelerWorkspace workspace, final LevelMetaData levelMetaData ) {
    HierarchyMetaData hierachy = levelMetaData.getHierarchyMetaData();
    DimensionMetaData dimension = hierachy.getDimensionMetaData();
    if ( hierachy.getLevels().size() > 1 ) {
      return;
    }
    dimension.remove( hierachy );
    if ( dimension.size() > 0 ) {
      return;
    }
    workspace.getModel().getDimensions().remove( dimension );
  }

  private LevelMetaData buildLevel( final ModelerWorkspace workspace,
                                    final HierarchyMetaData hierarchyMetaData, final LevelMetaData existingLevel )
    throws ModelerException {
    LevelMetaData levelMetaData = new LevelMetaData( hierarchyMetaData, getName() );
    levelMetaData.setLogicalColumn( existingLevel.getLogicalColumn() );
    levelMetaData.setUniqueMembers( isUnique() );
    if ( getTimeType() != null ) {
      levelMetaData.setDataRole( TimeRole.fromMondrianAttributeValue( getTimeType().name() ) );
    }
    if ( getTimeFormat() != null ) {
      levelMetaData.setTimeLevelFormat( getTimeFormat() );
    }
    LogicalColumn ordinalColumn = locateLogicalColumn( workspace, getOrdinalField() );
    if ( ordinalColumn != null ) {
      levelMetaData.setLogicalOrdinalColumn( ordinalColumn );
    }
    return levelMetaData;
  }

  private LogicalColumn locateLogicalColumn( final ModelerWorkspace workspace, final String columnName ) {
    LogicalModel logicalModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    logicalModel.getLogicalTables();
    for ( LogicalTable logicalTable : logicalModel.getLogicalTables() ) {
      for ( LogicalColumn logicalColumn : logicalTable.getLogicalColumns() ) {
        if ( logicalColumn.getName( workspace.getWorkspaceHelper().getLocale() ).equalsIgnoreCase( columnName ) ) {
          return logicalColumn;
        }
      }
    }
    return null;
  }

  private LevelMetaData locateLevel( final ModelerWorkspace workspace, final String column ) throws ModelerException {
    workspace.getModel().getDimensions();
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
        for ( LevelMetaData levelMetaData : hierarchyMetaData ) {
          if ( levelMetaData.getLogicalColumn().getName(workspace.getWorkspaceHelper().getLocale() ).equals( column ) ) {
            return levelMetaData;
          }
        }
      }
    }
    throw new ModelerException( "" );
  }

  private boolean attachLevel( final ModelerWorkspace workspace, final HierarchyMetaData existingHierarchy,
                               final String column ) throws ModelerException {
    int parentIndex = parentIndex( existingHierarchy );
    if ( parentIndex < 0 ) {
      return false;
    } else {
      LevelMetaData existingLevel = locateLevel( workspace, column );
      LevelMetaData levelMetaData = buildLevel( workspace, existingHierarchy, existingLevel );
      existingHierarchy.add( parentIndex + 1, levelMetaData );
      removeAutoLevel( workspace, existingLevel );
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
  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.CREATE_ATTRIBUTE;
  }

  @Override public String getSummary() {
    return BaseMessages
        .getString( MSG_CLASS, summaryMsgKey(), getName(), getHierarchy(), getParentAttribute() );
  }

  private String summaryMsgKey() {
    if ( getParentAttribute() == null ) {
      return "Modeler.CreateAttribute.Summary.noparent";
    }
    return "Modeler.CreateAttribute.Summary";
  }

  @Override
  public boolean apply( Document schema, String field ) throws ModelerException {
    throw new UnsupportedOperationException();
  }
}
