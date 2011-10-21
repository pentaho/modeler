package org.pentaho.agilebi.modeler.nodes.annotations;

/**
 * Implementations of this interface can be used as Data Type annotations.
 *
 * User: nbaker
 * Date: 10/18/11
 */
public interface IDataRoleAnnotation extends IMemberAnnotation {
  /**
   * Returns the String to be stored as the value of a Data.Role annotation
   * @return
   */
  String getDataType();
}
