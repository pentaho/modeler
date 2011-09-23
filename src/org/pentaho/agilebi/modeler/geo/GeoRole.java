package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.nodes.DataRole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/15/11
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeoRole implements DataRole, Serializable {
  private String name;
  private List<String> commonAliases;
  private String matchSeparator = "_";

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

    if(commonAliases != null && commonAliases.length() > 0) {
      String[] tokens = commonAliases.split(",");
      List<String> aliases = new ArrayList<String>(tokens.length);
      for(String s: tokens) {
        aliases.add(s.trim());
      }
      this.commonAliases = aliases;
    }
  }

  public String getName() {
    return name;
  }

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

  private boolean eval(String fieldName, String alias) {
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
}
