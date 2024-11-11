/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.nodes;

import java.util.Arrays;
import java.util.List;

import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/19/11 Time: 11:08 AM To change this template use File | Settings |
 * File Templates.
 */
public class TimeRole implements DataRole {

  private static final long serialVersionUID = -1386985942519602868L;

  private static final String DEFAULT_NAME = "time";
  public String name;
  public String[] formats;
  public List<String> formatsList;

  public TimeRole( String name, String[] formats ) {
    this.name = name;
    this.formats = formats;
    this.formatsList = Arrays.asList( formats );
  }

  @Bindable
  public String getName() {
    if ( this.name != null ) {
      return this.name;
    }
    return DEFAULT_NAME;
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  public String toString() {
    return getName();
  }

  public List<String> getFormatsList() {
    return this.formatsList;
  }

  /**
   * Returns the Mondrian Schema constant (as String) that represents this role See:
   * http://mondrian.pentaho.com/documentation/schema.php#Time_dimensions and
   * http://mondrian.pentaho.com/api/mondrian/olap/LevelType.html
   **/
  @Bindable
  public String getMondrianAttributeValue() {
    return "Time" + this.name;
  }

  /**
   * 
   */
  public static String mondrianAttributeValueToRoleName( String mondrianAttributeValue ) {
    return mondrianAttributeValue.substring( "Time".length() );
  }

  public static TimeRole fromMondrianAttributeValue( String mondrianAttributeValue ) {
    for ( TimeRole role : allRoles ) {
      if ( role.getMondrianAttributeValue().equals( mondrianAttributeValue ) ) {
        return role;
      }
    }
    return null;
  }

  public boolean equals( Object obj ) {
    boolean eq;
    if ( obj instanceof TimeRole ) {
      TimeRole timeRoleObject = (TimeRole) obj;
      eq = getName().equals( timeRoleObject.getName() );
    } else {
      eq = false;
    }
    return eq;
  }

  /* basic time roles */
  public static final TimeRole DUMMY = new TimeRole( "", new String[] {} );
  public static final TimeRole YEARS = new TimeRole( "Years", new String[] { "yy", "yyyy" } );
  public static final TimeRole HALFYEARS = new TimeRole( "HalfYears", new String[] {} );
  public static final TimeRole QUARTERS = new TimeRole( "Quarters", new String[] { "Q", "QQ", "QQQ" } );
  public static final TimeRole MONTHS = new TimeRole( "Months", new String[] { "M", "MM", "MMM" } );
  public static final TimeRole WEEKS = new TimeRole( "Weeks", new String[] { "w", "ww", "W" } );
  public static final TimeRole DAYS = new TimeRole( "Days", new String[] { "d", "dd", "D", "DDD", "yyyy-MM-dd" } );
  public static final TimeRole HOURS = new TimeRole( "Hours", new String[] { "k", "kk", "H", "HH", "K", "KK" } );
  public static final TimeRole MINUTES = new TimeRole( "Minutes", new String[] { "m", "mm" } );
  public static final TimeRole SECONDS = new TimeRole( "Seconds", new String[] { "s", "ss" } );

  public static final TimeRole[] ALL_ROLES =
      new TimeRole[] { DUMMY, YEARS, HALFYEARS, QUARTERS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS };

  public static TimeRole findRoleByName( String name ) {
    for ( TimeRole role : ALL_ROLES ) {
      if ( role.getName().equals( name ) ) {
        return role;
      }
    }
    return null;
  }

  private static final List<TimeRole> allRoles = Arrays.asList( ALL_ROLES );

  public static final List<TimeRole> getAllRoles() {
    return allRoles;
  }
}
