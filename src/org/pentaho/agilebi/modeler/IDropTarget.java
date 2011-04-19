package org.pentaho.agilebi.modeler;

/**
 * User: nbaker
 * Date: 4/19/11
 */
public interface IDropTarget {

  Object onDrop(Object data) throws ModelerException;
}
