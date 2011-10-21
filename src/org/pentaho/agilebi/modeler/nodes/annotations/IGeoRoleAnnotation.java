package org.pentaho.agilebi.modeler.nodes.annotations;

/**
 *
 * Implementations of this interface can be used as Geo Type annotations.
 *
 * User: nbaker
 * Date: 10/18/11
 */
public interface IGeoRoleAnnotation extends IMemberAnnotation {
  /**
   * returns the String form of the Geo Role to be stored as the value of the Geo.Role annotation
   * @return
   */
  String getGeoName();
}
