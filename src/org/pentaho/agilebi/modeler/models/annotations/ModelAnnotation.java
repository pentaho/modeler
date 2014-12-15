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

import org.pentaho.agilebi.modeler.ModelerWorkspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotation<T extends AnnotationType> implements Serializable {

  private static final long serialVersionUID = 5742135911581602697L;

  private String column;

  private T annotation;

  public ModelAnnotation() {
  }

  public ModelAnnotation( String column, T annotation ) {
    setColumn( column );
    setAnnotation( annotation );
  }

  public String getColumn() {
    return column;
  }

  public void setColumn( String column ) {
    this.column = column;
  }

  public T getAnnotation() {
    return annotation;
  }

  public void setAnnotation( T annotation ) {
    this.annotation = annotation;
  }

  public boolean isMeasure() {

    if ( this.annotation instanceof Measure ) {
      return true;
    }
    return false;
  }

  public boolean isDimension() {

    if ( this.annotation instanceof Dimension ) {
      return true;
    }
    return false;
  }

  public boolean asAttribute() {

    if ( this.annotation != null && this.annotation.getClass().equals( Attribute.class ) ) { // don't match subclass
      return true;
    }
    return false;
  }

  public boolean isHierarchyLevel() {
    if ( this.annotation instanceof HierarchyLevel ) {
      return true;
    }
    return false;
  }

  /**
   * **** Utility methods ******
   */

  public static List<ModelAnnotation<Measure>> getMeasures(
      final List<ModelAnnotation<? extends AnnotationType>> annotations ) {
    return filter( annotations, Measure.class );
  }

  public static List<ModelAnnotation<Attribute>> getAttributes(
      final List<ModelAnnotation<? extends AnnotationType>> annotations ) {
    return filter( annotations, Attribute.class );
  }

  public static List<ModelAnnotation<Dimension>> getDimensions(
      final List<ModelAnnotation<? extends AnnotationType>> annotations ) {
    return filter( annotations, Dimension.class );
  }

  public static List<ModelAnnotation<HierarchyLevel>> getHeirachyLevels(
      final List<ModelAnnotation<? extends AnnotationType>> annotations ) {
    return filter( annotations, HierarchyLevel.class );
  }

  private static <S extends AnnotationType> List<ModelAnnotation<S>> filter(
      final List<ModelAnnotation<? extends AnnotationType>> annotations, Class<S> cls ) {

    List<ModelAnnotation<S>> list = new ArrayList<ModelAnnotation<S>>();
    if ( cls != null && annotations != null && annotations.size() > 0 ) {
      annotations.removeAll( Collections.singleton( null ) ); // remove nulls
      for ( ModelAnnotation<?> annotation : annotations ) {
        if ( annotation.getAnnotation() != null && cls.equals( annotation.getAnnotation().getClass() ) ) {
          list.add( (ModelAnnotation<S>) annotation );
        }
      }
    }

    return list;
  }

  public void apply( final ModelerWorkspace modelerWorkspace ) {
    annotation.apply( modelerWorkspace, getColumn() );
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
}
