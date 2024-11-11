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

package org.pentaho.agilebi.modeler.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataFormatHolder implements Cloneable, Serializable {

  public static List<String> DATE_FORMATS = new ArrayList<String>() { {
      add( "yyyyMMdd" );
      add( "dd-MM-yy" );
      add( "dd-MM-yyyy" );
      add( "dd.MM.yy" );
      add( "dd.MM.yyyy" );
      add( "MM/dd/yy" );
      add( "MM/dd/yyyy" );
      add( "yyyy-MM-dd" );
      add( "yyyy.MM.dd" );
      add( "yyyy/MM/dd" );
      add( "dd MMM yyyy" );
      add( "dd MMMM yyyy" );
      add( "yyyyMMddHHmm" );
      add( "yyyyMMdd HHmm" );
      add( "dd-MM-yy HH:mm" );
      add( "dd-MM-yyyy HH:mm" );
      add( "dd.MM.yy HH:mm" );
      add( "dd.MM.yyyy HH:mm" );
      add( "MM/dd/yy HH:mm" );
      add( "MM/dd/yyyy HH:mm" );
      add( "yyyy-MM-dd HH:mm" );
      add( "yyyy.MM.dd HH:mm" );
      add( "yyyy/MM/dd HH:mm" );
      add( "dd MMM yyyy HH:mm" );
      add( "dd MMMM yyyy HH:mm" );
      add( "yyyyMMddHHmmss" );
      add( "yyyyMMdd HHmmss" );
      add( "dd-MM-yy HH:mm:ss" );
      add( "dd-MM-yyyy HH:mm:ss" );
      add( "dd.MM.yy HH:mm:ss" );
      add( "dd.MM.yyyy HH:mm:ss" );
      add( "MM/dd/yy HH:mm:ss" );
      add( "MM/dd/yyyy HH:mm:ss" );
      add( "yyyy-MM-dd HH:mm:ss" );
      add( "yyyy.MM.dd HH:mm:ss" );
      add( "yyyy/MM/dd HH:mm:ss" );
      add( "dd MMM yyyy HH:mm:ss" );
      add( "dd MMMM yyyy HH:mm:ss" );
      add( "dd-MM-yy HH:mm:ss.S" );
      add( "dd-MM-yyyy HH:mm:ss.S" );
      add( "dd.MM.yy HH:mm:ss.S" );
      add( "dd.MM.yyyy HH:mm:ss.S" );
      add( "MM/dd/yy HH:mm:ss.S" );
      add( "MM/dd/yyyy HH:mm:ss.S" );
      add( "yyyy-MM-dd HH:mm:ss.S" );
      add( "yyyy.MM.dd HH:mm:ss.S" );
      add( "yyyy/MM/dd HH:mm:ss.S" );
      add( "dd MMM yyyy HH:mm:ss.S" );
      add( "dd MMMM yyyy HH:mm:ss.S" );
      add( "dd-MM-yy HH:mm:ss.SSS" );
      add( "dd-MM-yyyy HH:mm:ss.SSS" );
      add( "dd.MM.yy HH:mm:ss.SSS" );
      add( "dd.MM.yyyy HH:mm:ss.SSS" );
      add( "MM/dd/yy HH:mm:ss.SSS" );
      add( "MM/dd/yyyy HH:mm:ss.SSS" );
      add( "yyyy-MM-dd HH:mm:ss.SSS" );
      add( "yyyy.MM.dd HH:mm:ss.SSS" );
      add( "yyyy/MM/dd HH:mm:ss.SSS" );
      add( "dd MMM yyyy HH:mm:ss.SSS" );
      add( "dd MMMM yyyy HH:mm:ss.SSS" );
    } };

  public static List<String> NUMBER_FORMATS = new ArrayList<String>() { {
      add( "0.00" );
      add( "0000000000000" );
      add( "#.#" );
      add( "#" );
      add( "###,###,###.#" );
      add( "#######.###" );
      add( "#####.###%" );
    } };

  public static List<String> CONVERSION_FORMATS = new ArrayList<String>() { {
      addAll( DATE_FORMATS );
      addAll( NUMBER_FORMATS );
    } };

}
