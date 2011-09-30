package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.gwt.widgets.client.controls.schededitor.RecurrenceEditor;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/19/11
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class TimeRole implements DataRole {

  private static final String DEFAULT_NAME = "time";
  public String name;

  public TimeRole(String name) {
    this.name = name;
  }

  public String getName() {
    if(this.name != null) {
      return this.name;
    }
    return DEFAULT_NAME;
  }

  /* basic time roles */
  public static final TimeRole HOUR = new TimeRole("hour");
  public static final TimeRole MINUTE = new TimeRole("minute");
  public static final TimeRole SECOND = new TimeRole("second");

}


