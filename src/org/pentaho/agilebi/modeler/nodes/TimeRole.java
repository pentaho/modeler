package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.gwt.widgets.client.controls.schededitor.RecurrenceEditor;
import java.util.List;
import java.util.Arrays;

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
  
  @Bindable
  public String getName() {
    if(this.name != null) {
      return this.name;
    }
    return DEFAULT_NAME;
  }
  
  @Bindable
  public void setName(String name) {
    this.name = name;
  }
  
  public String toString() {
    return getName();
  }
  
  /**
   *  Returns the Mondrian Schema constant (as String) that represents this role
   *  See: http://mondrian.pentaho.com/documentation/schema.php#Time_dimensions
   *  and http://mondrian.pentaho.com/api/mondrian/olap/LevelType.html
   **/
  @Bindable
  public String getMondrianAttributeValue() {
    String name = this.name;
    return name.substring(0, 1).toUpperCase() + name.substring(1) + "s";
  }

  public boolean equals(Object obj) {
    boolean eq;
    if (obj instanceof TimeRole) {
      TimeRole timeRoleObject = (TimeRole)obj;
      eq = getName().equals(timeRoleObject.getName());
    }
    else {
      eq = false;
    }
    return eq;
  }
  
  /* basic time roles */
  public static final TimeRole DUMMY = new TimeRole("");
  public static final TimeRole YEAR = new TimeRole("year");
  public static final TimeRole HALFYEAR = new TimeRole("halfYear");
  public static final TimeRole QUARTER= new TimeRole("quarter");
  public static final TimeRole MONTH= new TimeRole("month");
  public static final TimeRole WEEK= new TimeRole("week");
  public static final TimeRole DAY= new TimeRole("day");
  public static final TimeRole HOUR = new TimeRole("hour");
  public static final TimeRole MINUTE = new TimeRole("minute");
  public static final TimeRole SECOND = new TimeRole("second");
  
  public static final TimeRole[] ALL_ROLES = new TimeRole[]{
    DUMMY,
    YEAR,
    HALFYEAR,
    QUARTER,
    MONTH,
    WEEK,
    DAY,
    HOUR,
    MINUTE,
    SECOND
  };

  public static final List getAllRoles(){
    return Arrays.asList(ALL_ROLES);
  }
}


