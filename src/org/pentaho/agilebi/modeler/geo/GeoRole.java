package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.DataRole;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.nodes.annotations.IDataRoleAnnotation;
import org.pentaho.agilebi.modeler.nodes.annotations.IGeoRoleAnnotation;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/15/11
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeoRole extends XulEventSourceAdapter implements DataRole, Serializable, IMemberAnnotation, IDataRoleAnnotation, IGeoRoleAnnotation {
  private String name;
  private List<String> commonAliases;
  private String matchSeparator = "_";
  private List<GeoRole> requiredParentRoles;

  public GeoRole() {}

  public GeoRole(String name, List<String> commonAliases) {
    this.name = name;
    this.commonAliases = commonAliases;
  }

  /**
   * Generates a GeoRole
   * @param name name of the geo role
   * @param commonAliases comma separated list of aliases
   */
  public GeoRole(String name, String commonAliases) {
    this.name = name;

    List<String> aliases = parse(commonAliases);
    if(aliases != null) {
      this.commonAliases = aliases;
    }
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCommonAliases() {
    return commonAliases;
  }

  public void setCommonAliases(List<String> commonAliases) {
    this.commonAliases = commonAliases;
  }

  public String getMatchSeparator() {
    return matchSeparator;
  }

  public void setMatchSeparator(String matchSeparator) {
    this.matchSeparator = matchSeparator;
  }

  public boolean evaluate(String fieldName) {
    if(commonAliases == null || fieldName == null || fieldName.length() == 0) {
      return false;
    }

    for(String alias : commonAliases) {
      String testName = fieldName.toLowerCase();
      String testAlias = alias.toLowerCase();

      if (eval(testName, testAlias)) {
        return true;
      } else if (eval(testName, testAlias.replaceAll(" ", ""))) {
        return true;
      } else if (eval(testName, testAlias.replaceAll(" ", getMatchSeparator()))) {
        return true;
      }

    }
    return false;
  }

  protected boolean eval(String fieldName, String alias) {
    return fieldName.equals(alias);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GeoRole geoRole = (GeoRole) o;

    if (commonAliases != null ? !commonAliases.equals(geoRole.commonAliases) : geoRole.commonAliases != null)
      return false;
    if (matchSeparator != null ? !matchSeparator.equals(geoRole.matchSeparator) : geoRole.matchSeparator != null)
      return false;
    if (name != null ? !name.equals(geoRole.name) : geoRole.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (commonAliases != null ? commonAliases.hashCode() : 0);
    result = 31 * result + (matchSeparator != null ? matchSeparator.hashCode() : 0);
    return result;
  }

  public GeoRole clone() {
    List<String> clonedAliases = (ArrayList<String>)((ArrayList<String>)this.commonAliases).clone();
    GeoRole clone = new GeoRole(this.name, clonedAliases);
    return clone;
  }

  protected static List<String> parse(String csv) {
    if(csv != null && csv.length() > 0) {
      String[] tokens = csv.split(",");
      List<String> aliases = new ArrayList<String>(tokens.length);
      for(String s: tokens) {
        aliases.add(s.trim());
      }
      return aliases;
    } else {
      return null;
    }
  }

  public void setRequiredParentRoles(List<GeoRole> parentRoles) {
    this.requiredParentRoles = parentRoles;
  }

  public List<GeoRole> getRequiredParentRoles() {
    if(requiredParentRoles == null) {
      requiredParentRoles = new ArrayList<GeoRole>();
    }
    return requiredParentRoles;
  }

  @Override
  public void saveAnnotations(Object obj) {

    OlapHierarchyLevel level = (OlapHierarchyLevel) obj;

    level.getAnnotations().add(new OlapAnnotation(GeoContext.ANNOTATION_DATA_ROLE, "Geography"));

    // lat long is set as member properties (add as logical columns to achieve this)
    // geo-role is set on the level as an annotation

    level.getAnnotations().add(new OlapAnnotation(GeoContext.ANNOTATION_GEO_ROLE, getName()));
    if(getRequiredParentRoles().size() > 0) {
      String parents = combineRequiredParents(this);
      level.getAnnotations().add(new OlapAnnotation(GeoContext.ANNOTATION_GEO_PARENTS, parents));
    }
  }

  protected String combineRequiredParents(GeoRole role) {
    if(role.getRequiredParentRoles().size() > 0) {
      StringBuffer sb = new StringBuffer();
      for(GeoRole r : role.getRequiredParentRoles()) {
        if(sb.length() > 0) {
          sb.append(",");
        }
        sb.append(r.getName());
      }
      return sb.toString();
    }
    return null;
  }

  @Override
  public String getDataType() {
    return "Geo.Role";
  }

  @Override
  public String getGeoName() {
    return name;
  }

  @Override
  public boolean isValid(AbstractMetaDataModelNode node) {
    // No validation required, this is just a marker
    return true;
  }

  @Override
  public List<String> getValidationMessages(AbstractMetaDataModelNode node) {
    return Collections.emptyList();
  }

  public void onAttach(AbstractMetaDataModelNode node) {

  }

  public void onDetach(AbstractMetaDataModelNode node) {

  }
}
