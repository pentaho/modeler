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
    return "Time" + this.name;
  }
  
  /**
   * 
   */
  public static String mondrianAttributeValueToRoleName(String mondrianAttributeValue){
    return mondrianAttributeValue.substring("Time".length());
  }
  
  public static TimeRole fromMondrianAttributeValue(String mondrianAttributeValue){
    for (TimeRole role : allRoles) {
      if (role.getMondrianAttributeValue().equals(mondrianAttributeValue)) return role;
    }
    return null;
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
  public static final TimeRole YEARS = new TimeRole("Years");
  public static final TimeRole HALFYEARS = new TimeRole("HalfYears");
  public static final TimeRole QUARTERS = new TimeRole("Quarters");
  public static final TimeRole MONTHS = new TimeRole("Months");
  public static final TimeRole WEEKS = new TimeRole("Weeks");
  public static final TimeRole DAYS = new TimeRole("Days");
  public static final TimeRole HOURS = new TimeRole("Hours");
  public static final TimeRole MINUTES = new TimeRole("Minutes");
  public static final TimeRole SECONDS = new TimeRole("Seconds");
  
  public static final TimeRole[] ALL_ROLES = new TimeRole[]{
    DUMMY,
    YEARS,
    HALFYEARS,
    QUARTERS,
    MONTHS,
    WEEKS,
    DAYS,
    HOURS,
    MINUTES,
    SECONDS
  };

  private static final List<TimeRole> allRoles = Arrays.asList(ALL_ROLES);
  public static final List<TimeRole> getAllRoles(){
    return allRoles;
  }
}


