package org.pentaho.agilebi.modeler.models;

import org.pentaho.ui.xul.XulEventSource;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Basic PropertyChangeSupport for use in XulEventSource Binding classes
 */
public class XulEventSourceAdapter implements XulEventSource {

  protected transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

  private PropertyChangeSupport getChangeSupport(){
    if(changeSupport == null){
      changeSupport = new PropertyChangeSupport(this);
    }
    return changeSupport;
  }
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    getChangeSupport().addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    getChangeSupport().addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    getChangeSupport().removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    getChangeSupport().removePropertyChangeListener(propertyName, listener);
  }

  protected void firePropertyChange(String attr, Object previousVal, Object newVal){
    if(previousVal == null && newVal == null){
      return;
    }
    getChangeSupport().firePropertyChange(attr, previousVal, newVal);
  }
}

