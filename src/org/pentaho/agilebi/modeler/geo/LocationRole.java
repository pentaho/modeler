package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.nodes.AvailableField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/22/11
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocationRole extends GeoRole {

  private static final String LOCATION = "location";
  private GeoRole latitudeRole;
  private GeoRole longitudeRole;

  private AvailableField latitudeField = null;
  private AvailableField longitudeField = null;

  public LocationRole() {}
  public LocationRole(GeoRole latitudeRole, GeoRole longitudeRole) {
    this.latitudeRole = latitudeRole;
    this.longitudeRole = longitudeRole;
  }

  @Override
  public String getName() {
    return LOCATION;
  }

  public GeoRole getLatitudeRole() {
    return latitudeRole;
  }

  public void setLatitudeRole(GeoRole latitudeRole) {
    this.latitudeRole = latitudeRole;
  }

  public GeoRole getLongitudeRole() {
    return longitudeRole;
  }

  public void setLongitudeRole(GeoRole longitudeRole) {
    this.longitudeRole = longitudeRole;
  }

  @Override
  public boolean evaluate(String fieldName) {
    boolean result = false;
    if (latitudeRole != null) {
      result = latitudeRole.evaluate(fieldName);
      if (result) return result;
    }
    if (longitudeRole != null) {
      return result || longitudeRole.evaluate(fieldName);
    }
    return result;
  }

  public boolean evaluateLatitude(String fieldName) {
    if (latitudeRole != null) {
      return latitudeRole.evaluate(fieldName);
    }
    return false;
  }
  public boolean evaluateLongitude(String fieldName) {
    if (longitudeRole != null) {
      return longitudeRole.evaluate(fieldName);
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    LocationRole that = (LocationRole) o;

    if (latitudeField != null ? !latitudeField.equals(that.latitudeField) : that.latitudeField != null) return false;
    if (latitudeRole != null ? !latitudeRole.equals(that.latitudeRole) : that.latitudeRole != null) return false;
    if (longitudeField != null ? !longitudeField.equals(that.longitudeField) : that.longitudeField != null)
      return false;
    if (longitudeRole != null ? !longitudeRole.equals(that.longitudeRole) : that.longitudeRole != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (latitudeRole != null ? latitudeRole.hashCode() : 0);
    result = 31 * result + (longitudeRole != null ? longitudeRole.hashCode() : 0);
    result = 31 * result + (latitudeField != null ? latitudeField.hashCode() : 0);
    result = 31 * result + (longitudeField != null ? longitudeField.hashCode() : 0);
    return result;
  }

  @Override
  public GeoRole clone(){
    LocationRole clone = new LocationRole();
    clone.setLatitudeRole(this.latitudeRole == null ? null : this.latitudeRole.clone());
    clone.setLongitudeRole(this.longitudeRole == null ? null : this.longitudeRole.clone());

    clone.setMatchSeparator(getMatchSeparator());
    clone.setName(getName());

    // don't bother cloning aliases or the lat & long fields.

    return clone;
  }

  public AvailableField getLatitudeField() {
    return latitudeField;
  }

  public void setLatitudeField(AvailableField latitudeField) {
    this.latitudeField = latitudeField;
  }

  public AvailableField getLongitudeField() {
    return longitudeField;
  }

  public void setLongitudeField(AvailableField longitudeField) {
    this.longitudeField = longitudeField;
  }
}
