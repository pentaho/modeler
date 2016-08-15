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

package org.pentaho.agilebi.modeler.geo;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.util.AbstractModelList;

/**
 * GeoContext are intended to be used by AutoModelStrategy implementors to auto-detect geography fields in data sources
 * that contribute to the construction of geography dimensions
 */
public class GeoContext extends AbstractModelList<GeoRole> {
  private static final long serialVersionUID = 7328118800436710452L;
  protected static final String GEO_PREFIX = "geo.";
  protected static final String GEO_DIM_NAME = "geo.dimension.name";
  protected static final String GEO_ROLE_KEY = "geo.roles";
  protected static final String GEO_MATCH_SEPARATOR = "geo.matchSeparator";
  protected static final String ALIAS_SUFFIX = ".aliases";
  protected static final String REQUIRED_PARENTS_SUFFIX = ".required-parents";

  public static final String LATITUDE = "latitude";
  public static final String LONGITUDE = "longitude";
  public static final String ANNOTATION_DATA_ROLE = "Data.Role";
  public static final String ANNOTATION_GEO_ROLE = "Geo.Role";
  public static final String ANNOTATION_GEO_PARENTS = "Geo.RequiredParents";

  protected String dimensionName = "Geography";

  public GeoContext() {
  }

  public GeoRole getGeoRole( int index ) {
    if ( index >= 0 && children.size() >= index ) {
      return children.get( index );
    } else {
      return null;
    }
  }

  public void addGeoRole( GeoRole geoRole ) {
    if ( geoRole != null ) {
      children.add( geoRole );
    }
  }

  public String getDimensionName() {
    return dimensionName;
  }

  public void setDimensionName( String dimensionName ) {
    this.dimensionName = dimensionName;
  }

  public GeoRole matchFieldToGeoRole( AvailableField field ) {
    for ( GeoRole role : this ) {
      if ( role.evaluate( field.getPhysicalColumn().getId() ) ) {
        return role;
      } else if ( field.getPhysicalColumn().getId().startsWith( "pc__" ) ) {
        // sql data sources prefix the column ids with pc__, if that is detected just match with out it
        if ( role.evaluate( field.getPhysicalColumn().getId().substring( 4 ) ) ) {
          return role;
        }
      }
    }
    return null;
  }

  public GeoRole matchColumnToGeoRole( IPhysicalColumn column ) {
    for ( GeoRole role : this ) {
      if ( role.evaluate( column.getId() ) ) {
        return role;
      }
    }
    return null;
  }

  public List<DimensionMetaData> buildDimensions( ModelerWorkspace workspace ) {
    List<DimensionMetaData> geoDims = new ArrayList<DimensionMetaData>();
    List<AvailableTable> tableList = workspace.getAvailableTables().getAsAvailableTablesList();

    // get all roles for the fields
    for ( AvailableTable table : tableList ) {
      if ( table.isFactTable() ) {
        // don't bother looking at fact tables for geographic fields
        continue;
      }
      String dimName;

      if ( tableList.size() == 1 ) {
        dimName = getDimensionName();
      } else {
        // have to name the dimensions in context with the tables they are built from
        dimName = table.getName() + get( 0 ).getMatchSeparator() + getDimensionName();
      }

      // see if the desired name is already the name of a column
      for ( IPhysicalColumn col : table.getPhysicalTable().getPhysicalColumns() ) {
        if ( col.getId().equalsIgnoreCase( getDimensionName() ) ) {
          dimName += "2";
          continue;
        }
      }

      DimensionMetaData dim = new DimensionMetaData( dimName );
      dim.getMemberAnnotations().put( ANNOTATION_DATA_ROLE, new GeoRole() );
      HierarchyMetaData hier = new HierarchyMetaData( dimName );
      hier.getMemberAnnotations().put( ANNOTATION_DATA_ROLE, new GeoRole() );
      ArrayList<LevelMetaData> levels = new ArrayList<LevelMetaData>();

      AvailableField locationField = null;

      LocationRole locationRole = getLocationRole();

      boolean locationFieldDetected = false;
      int latColIndex = 0;
      int lonColIndex = 0;
      int count = 0;
      // must iterate over the physical columns to ensure we process the columns in the proper order, available fields
      // are sorted in available table
      for ( IPhysicalColumn col : table.getPhysicalTable().getPhysicalColumns() ) {
        // go get the field for this physical column so we can work with that
        AvailableField field = table.findFieldByPhysicalColumn( col );

        GeoRole role = matchFieldToGeoRole( field );
        String fieldName = col.getId();

        if ( role != null ) {
          if ( role instanceof LocationRole ) {
            locationFieldDetected = true;
            // if this was matched to a location role. we need to set it as the data role on another level
            // in an existing dimension, but only if we detect both lat & long
            if ( locationRole.evaluateLatitude( fieldName ) ) {
              latColIndex = count;
            } else if ( locationRole.evaluateLongitude( fieldName ) ) {
              lonColIndex = count;
            }
          } else {
            // regular geo field, add it as a level to the dimension
            ColumnBackedNode node = workspace.createColumnBackedNode( field, ModelerPerspective.ANALYSIS );
            LevelMetaData level = workspace.createLevelForParentWithNode( hier, node );
            level.getMemberAnnotations().put( ANNOTATION_DATA_ROLE, role );
            level.getMemberAnnotations().put( ANNOTATION_GEO_ROLE, role );
            levels.add( level );
          }
        }
        count++;
      }

      if ( locationFieldDetected ) {
        locationField =
            determineLocationField( table, locationRole, latColIndex, lonColIndex, workspace.getWorkspaceHelper()
                .getLocale() );
      }

      if ( levels.size() > 0 ) {
        // now that we have the levels of the geo dim, put them in the hierarchy in the correct order
        for ( int i = 0; i < size(); i++ ) {
          GeoRole knownRole = get( i );
          for ( LevelMetaData level : levels ) {
            if ( knownRole.equals( level.getMemberAnnotations().get( ANNOTATION_GEO_ROLE ) ) ) {

              // if one of these levels was identified as the location field, set it's data role properly
              if ( locationFieldDetected && locationField != null && locationRole != null && latColIndex > -1
                  && lonColIndex > -1
                  && locationField.isSameUnderlyingPhysicalColumn( level.getLogicalColumn().getPhysicalColumn() ) ) {

                level.getMemberAnnotations().put( ANNOTATION_DATA_ROLE, locationRole );
                level.getMemberAnnotations().put( ANNOTATION_GEO_ROLE, locationRole );

                // if it is a LocationField we need to make sure the lat & long columns get
                // added as logical columns to the model.
                AvailableField latField =
                    table.findFieldByPhysicalColumn( table.getPhysicalTable().getPhysicalColumns().get( latColIndex ) );
                AvailableField lonField =
                    table.findFieldByPhysicalColumn( table.getPhysicalTable().getPhysicalColumns().get( lonColIndex ) );

                ColumnBackedNode tmp = workspace.createColumnBackedNode( latField, ModelerPerspective.ANALYSIS );
                tmp.getLogicalColumn().setName(
                    new LocalizedString( workspace.getWorkspaceHelper().getLocale(), LATITUDE ) );
                MemberPropertyMetaData memberProp = workspace.createMemberPropertyForParentWithNode( level, tmp );
                memberProp.setName( LATITUDE );
                level.add( memberProp );

                tmp = workspace.createColumnBackedNode( lonField, ModelerPerspective.ANALYSIS );
                tmp.getLogicalColumn().setName(
                    new LocalizedString( workspace.getWorkspaceHelper().getLocale(), LONGITUDE ) );
                memberProp = workspace.createMemberPropertyForParentWithNode( level, tmp );
                memberProp.setName( LONGITUDE );
                level.add( memberProp );

              }
              if ( !hier.contains( level ) ) {
                hier.add( level );
              }
            }
          }
        }

        hier.setParent( dim );
        dim.add( hier );
        geoDims.add( dim );
      }

      // if location was detected, must set an existing level in an existing dimension
      // to be the LocationRole and it must be aware of the fields that provide lat & long
      if ( locationFieldDetected && locationField != null && locationRole != null ) {
        for ( DimensionMetaData existingDim : workspace.getModel().getDimensions() ) {
          for ( HierarchyMetaData existingHier : existingDim ) {
            for ( LevelMetaData existingLevel : existingHier ) {
              if ( locationField
                  .isSameUnderlyingPhysicalColumn( existingLevel.getLogicalColumn().getPhysicalColumn() ) ) {
                existingLevel.getMemberAnnotations().put( ANNOTATION_DATA_ROLE, locationRole );
                existingLevel.getMemberAnnotations().put( ANNOTATION_GEO_ROLE, locationRole );
                // if it is a LocationField we need to make sure the lat & long columns get
                // added as logical columns to the model.

                AvailableField latField =
                    table.findFieldByPhysicalColumn( table.getPhysicalTable().getPhysicalColumns().get( latColIndex ) );
                AvailableField lonField =
                    table.findFieldByPhysicalColumn( table.getPhysicalTable().getPhysicalColumns().get( lonColIndex ) );

                ColumnBackedNode tmp = workspace.createColumnBackedNode( latField, ModelerPerspective.ANALYSIS );
                tmp.getLogicalColumn().setName(
                    new LocalizedString( workspace.getWorkspaceHelper().getLocale(), LATITUDE ) );
                MemberPropertyMetaData memberProp =
                    workspace.createMemberPropertyForParentWithNode( existingLevel, tmp );
                memberProp.setName( LATITUDE );
                existingLevel.add( memberProp );

                tmp = workspace.createColumnBackedNode( lonField, ModelerPerspective.ANALYSIS );
                tmp.getLogicalColumn().setName(
                    new LocalizedString( workspace.getWorkspaceHelper().getLocale(), LONGITUDE ) );
                memberProp = workspace.createMemberPropertyForParentWithNode( existingLevel, tmp );
                memberProp.setName( LONGITUDE );
                existingLevel.add( memberProp );

                continue;
              }
            }
          }
        }
      }

    }

    // if there was only one dimension created, set it's name to the configured value
    if ( geoDims.size() == 1 ) {
      String resetDimName = getDimensionName();
      if ( geoDims.get( 0 ).getName().endsWith( resetDimName + "2" ) ) {
        resetDimName += "2";
      }
      geoDims.get( 0 ).setName( resetDimName );
      geoDims.get( 0 ).get( 0 ).setName( resetDimName );
    }

    return geoDims;
  }

  public LocationRole getLocationRole() {
    for ( int i = size() - 1; i >= 0; i-- ) {
      if ( get( i ) instanceof LocationRole ) {
        return (LocationRole) get( i );
      }
    }
    return null;
  }

  protected AvailableField determineLocationField( AvailableTable table, LocationRole locationRole, int latColIndex,
      int lonColIndex, String locale ) {
    AvailableField locationField = null;
    int count = table.getAvailableFields().size();

    // if the lat&longs where detected with a prefix, use that to try to find a column with a name matching that prefix
    String prefix = locationRole.getPrefix();
    if ( prefix != null && prefix.length() > 0 ) {
      // iterate over the columns, match prefix to column name
      for ( AvailableField field : table.getAvailableFields() ) {
        IPhysicalColumn col = field.getPhysicalColumn();
        if ( prefix.equalsIgnoreCase( col.getName( locale ) ) ) {
          locationField = field;
          continue;
        }
      }
    }

    if ( locationField == null ) {
      int min = Math.min( latColIndex, lonColIndex );
      int max = Math.max( latColIndex, lonColIndex );

      // get the previous column
      if ( min > 0 ) {
        IPhysicalColumn col = table.getPhysicalTable().getPhysicalColumns().get( min - 1 );
        locationField = table.findFieldByPhysicalColumn( col );
      } else if ( max < count ) {
        // get the column immediately following
        IPhysicalColumn col = table.getPhysicalTable().getPhysicalColumns().get( max + 1 );
        locationField = table.findFieldByPhysicalColumn( col );
      }
    }
    return locationField;
  }

  public GeoRole getGeoRoleByName( String name ) {

    for ( GeoRole role : this ) {
      if ( role.getName().equalsIgnoreCase( name ) ) {
        return role;
      }
    }
    return null;
  }

}
