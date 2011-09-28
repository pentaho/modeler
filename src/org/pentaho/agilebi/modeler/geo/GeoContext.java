package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.concept.types.LocalizedString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GeoContext are intended to be used by AutoModelStrategy implementors
 * to auto-detect geography fields in data sources
 * that contribute to the construction of geography dimensions
 */
public class GeoContext implements Iterable<GeoRole>, Serializable {
  protected static final String GEO_PREFIX = "geo.";
  protected static final String GEO_DIM_NAME = "geo.dimension.name";
  protected static final String GEO_ROLE_KEY = "geo.roles";
  protected static final String GEO_MATCH_SEPARATOR = "geo.matchSeparator";
  protected static final String ALIAS_SUFFIX = ".aliases";
  protected static final String REQUIRED_PARENTS_SUFFIX = ".required-parents";

  private static final String LATITUDE = "latitude";
  private static final String LONGITUDE = "longitude";

  protected String dimensionName = "Geography";
  protected List<GeoRole> geoRoles;

  public GeoContext() {
    this.geoRoles = new ArrayList<GeoRole>();
  }

  public GeoRole getGeoRole(int index) {
    if(index >= 0 && geoRoles != null && geoRoles.size() >= index) {
      return geoRoles.get(index);
    } else {
      return null;
    }
  }

  public void addGeoRole(GeoRole geoRole) {
    if (geoRole != null) {
      geoRoles.add(geoRole);
    }
  }

  @Override
  public Iterator<GeoRole> iterator() {
    if (geoRoles != null) {
      return geoRoles.listIterator();
    } else {
      return null;
    }
  }

  public int size() {
    if(geoRoles != null) {
      return geoRoles.size();
    } else {
      return 0;
    }
  }

  public String getDimensionName() {
    return dimensionName;
  }

  public void setDimensionName(String dimensionName) {
    this.dimensionName = dimensionName;
  }

  public GeoRole matchFieldToGeoRole(AvailableField field) {
    for(GeoRole role : this.geoRoles) {
      if (role.evaluate(field.getPhysicalColumn().getId())) {
        return role;
      }
    }
    return null;
  }
  public GeoRole matchColumnToGeoRole(IPhysicalColumn column) {
    for(GeoRole role : this.geoRoles) {
      if (role.evaluate(column.getId())) {
        return role;
      }
    }
    return null;
  }

  public List<DimensionMetaData> buildDimensions(ModelerWorkspace workspace) {
    List<DimensionMetaData> geoDims = new ArrayList<DimensionMetaData>();
    List<AvailableTable> tableList = workspace.getAvailableTables().getAsAvailableTablesList();

    String locale = workspace.getWorkspaceHelper().getLocale();

    // get all roles for the fields
    for (AvailableTable table : tableList) {
      if(table.isFactTable()) {
        // don't bother looking at fact tables for geographic fields
        continue;
      }
      String dimName;
      if (tableList.size() == 1) {
        dimName = getDimensionName();
      } else {
        // have to name the dimensions in context with the tables they are built from
        dimName = table.getName() + geoRoles.get(0).getMatchSeparator() + getDimensionName();
      }
      DimensionMetaData dim = new DimensionMetaData(dimName);
      dim.setDataRole(new GeoRole());
      HierarchyMetaData hier = new HierarchyMetaData(dimName);
      hier.setDataRole(new GeoRole());
      ArrayList<LevelMetaData> levels = new ArrayList<LevelMetaData>();

      AvailableField locationField = null;

      LocationRole locRole = getLocationRole();
      LocationRole locationRole = null;
      if(locRole != null) {
        locationRole = (LocationRole)locRole.clone();
      } else {
        locationRole = new LocationRole();
      }


      boolean locationFieldDetected = false;
      int latColIndex = 0;
      int lonColIndex = 0;
      int count = 0;
      // must iterate over the physical columns to ensure we process the columns in the proper order, available fields are sorted in available table
      for(IPhysicalColumn col : table.getPhysicalTable().getPhysicalColumns()) {
        // go get the field for this physical column so we can work with that
        AvailableField field = table.findFieldByPhysicalColumn(col);

        GeoRole role = matchFieldToGeoRole(field);
        String fieldName = col.getId();

        if (role != null) {
          if (role instanceof LocationRole) {
            locationFieldDetected = true;
            // if this was matched to a location role. we need to set it as the data role on another level
            // in an existing dimension, but only if we detect both lat & long
            if (locationRole.evaluateLatitude(fieldName)) {
              locationRole.setLatitudeField(field);
              latColIndex = count;
            } else if (locationRole.evaluateLongitude(fieldName)) {
              locationRole.setLongitudeField(field);
              lonColIndex = count;
            }
          } else {
            // regular geo field, add it as a level to the dimension
            ColumnBackedNode node = workspace.createColumnBackedNode(field, ModelerPerspective.ANALYSIS);
            LevelMetaData level = workspace.createLevelForParentWithNode(hier, node);
            level.setDataRole(role);
            levels.add(level);
          }
        }
        count++;
      }

      if (locationFieldDetected) {
        locationField = determineLocationField(table, locationRole, latColIndex, lonColIndex, workspace.getWorkspaceHelper().getLocale());
      }

      if (levels.size() > 0) {
        // now that we have the levels of the geo dim, put them in the hierarchy in the correct order
        for(int i = 0; i < geoRoles.size(); i++) {
          GeoRole knownRole = geoRoles.get(i);
          for(LevelMetaData level : levels) {
            if (level.getDataRole().getName().equals(knownRole.getName())) {
              
              // if one of these levels was identified as the location field, set it's data role properly
              if (locationFieldDetected && locationField != null &&
                  locationRole != null && locationRole.getLatitudeField() != null && locationRole.getLongitudeField() != null &&
                  locationField.isSameUnderlyingPhysicalColumn(level.getLogicalColumn().getPhysicalColumn())) {

                level.setDataRole(locationRole);

                // if it is a LocationField we need to make sure the lat & long columns get
                // added as logical columns to the model.
                ColumnBackedNode tmp = workspace.createColumnBackedNode(locationRole.getLatitudeField(), ModelerPerspective.ANALYSIS);
                tmp.getLogicalColumn().setName(new LocalizedString(workspace.getWorkspaceHelper().getLocale(), LATITUDE));

                tmp = workspace.createColumnBackedNode(locationRole.getLongitudeField(), ModelerPerspective.ANALYSIS);
                tmp.getLogicalColumn().setName(new LocalizedString(workspace.getWorkspaceHelper().getLocale(), LONGITUDE));

              }

              hier.add(level);
            }
          }
        }

        hier.setParent(dim);
        dim.add(hier);
        geoDims.add(dim);
      }

      // if location was detected, must set an existing level in an existing dimension
      // to be the LocationRole and it must be aware of the fields that provide lat & long
      if(locationFieldDetected && locationField != null && locationRole != null &&
          locationRole.getLatitudeField() != null && locationRole.getLongitudeField() != null) {
        for(DimensionMetaData existingDim : workspace.getModel().getDimensions()) {
          for(HierarchyMetaData existingHier : existingDim) {
            for(LevelMetaData existingLevel : existingHier) {
              if(locationField.isSameUnderlyingPhysicalColumn(existingLevel.getLogicalColumn().getPhysicalColumn())) {
                existingLevel.setDataRole(locationRole);
                // if it is a LocationField we need to make sure the lat & long columns get
                // added as logical columns to the model.
                ColumnBackedNode tmp = workspace.createColumnBackedNode(locationRole.getLatitudeField(), ModelerPerspective.ANALYSIS);
                tmp.getLogicalColumn().setName(new LocalizedString(workspace.getWorkspaceHelper().getLocale(), LATITUDE));

                tmp = workspace.createColumnBackedNode(locationRole.getLongitudeField(), ModelerPerspective.ANALYSIS);
                tmp.getLogicalColumn().setName(new LocalizedString(workspace.getWorkspaceHelper().getLocale(), LONGITUDE));

                continue;
              }
            }
          }
        }
      }

    }

    // if there was only one dimension created, set it's name to the configured value
    if(geoDims.size() == 1) {
      geoDims.get(0).setName(getDimensionName());
      geoDims.get(0).get(0).setName(getDimensionName());
    }

    return geoDims;
  }

  public LocationRole getLocationRole() {
    for(int i = geoRoles.size() - 1; i >= 0; i--) {
      if (geoRoles.get(i) instanceof LocationRole) {
        return (LocationRole)geoRoles.get(i);
      }
    }
    return null;
  }

  protected AvailableField determineLocationField(AvailableTable table, LocationRole locationRole, int latColIndex, int lonColIndex, String locale) {
    AvailableField locationField = null;
    int count = table.getAvailableFields().size();
 
    // if the lat&longs where detected with a prefix, use that to try to find a column with a name matching that prefix
    String prefix = locationRole.getPrefix();
    if(prefix != null && prefix.length() > 0) {
      // iterate over the columns, match prefix to column name
      for(AvailableField field : table.getAvailableFields()) {
        IPhysicalColumn col = field.getPhysicalColumn();
        if(prefix.equalsIgnoreCase(col.getName(locale))) {
          locationField = field;
          continue;
        }
      }
    }

    if(locationField == null) {
      int min = Math.min(latColIndex, lonColIndex);
      int max = Math.max(latColIndex, lonColIndex);

      // get the previous column
      if (min > 0) {
        IPhysicalColumn col = table.getPhysicalTable().getPhysicalColumns().get(min-1);
        locationField = table.findFieldByPhysicalColumn(col);
      } else if (max < count) {
        // get the column immediately following
        IPhysicalColumn col = table.getPhysicalTable().getPhysicalColumns().get(max+1);
        locationField = table.findFieldByPhysicalColumn(col);
      }
    }
    return locationField;
  }

  public GeoRole getGeoRoleByName(String name) {
    if (this.geoRoles != null) {
      for(GeoRole role : this.geoRoles) {
        if(role.getName().equalsIgnoreCase(name)) {
          return role;
        }
      }
    }
    return null;
  }

}
