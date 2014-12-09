/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package org.pentaho.agilebi.modeler.models.annotations;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotation<T extends AnnotationType> implements Serializable {

  private static final long serialVersionUID = 5742135911581602697L;

  private String column;
  private List<T> annotations;

  public String getColumn() {
    return column;
  }

  public void setColumn( String column ) {
    this.column = column;
  }

  public List<T> getAnnotations() {
    return annotations;
  }

  public void setAnnotations( List<T> annotations ) {
    this.annotations = annotations;
  }

  public static enum Actions {
    CREATE_HEIRARCHY_LEVEL,
    UPDATE_HEIRARCHY_LEVEL,
    REMOVE_HEIRARCHY_LEVEL,
    CREATE_MEASURE,
    UPDATE_MEASURE,
    REMOVE_MEASURE,
    HIDE_UNHIDE_MEASURE,
    LINK_DIMENSION,
    CREATE_ATTRIBUTE,
    UPDATE_ATTRIBUTE,
    REMOVE_ATTRIBUTE,
    HIDE_UNHIDE_ATTRIBUTE
  }

  public static enum LevelType {
    Regular,
    TimeYears,
    TimeHalfYears,
    TimeQuarters,
    TimeMonths,
    TimeWeeks,
    TimeDays,
    TimeHours,
    TimeMinutes,
    TimeSeconds,
    TimeUndefined,
    Null
  }

  public static enum AttributeType {
    KEY,
    NAME,
    ORDINAL,
    PROPERTY
  }

  public static enum AggregateType {
    SUM,
    AVERAGE,
    MIN,
    MAX,
    COUNT_DISTINCT,
    COUNT_ANY,
    COUNT_ALL
  }
}
