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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.w3c.dom.Document;

/**
 * @author Rowell Belen
 */
public class ModelAnnotation<T extends AnnotationType> implements Serializable {

  private static final long serialVersionUID = 5742135911581602697L;

  private String field;

  private String cube;
  private String hierarchy;
  private String name;

  private T annotation;

  public ModelAnnotation() {
  }

  public ModelAnnotation( final String field, final T annotation ) {
    setField( field );
    setAnnotation( annotation );
  }

  public ModelAnnotation( final String cube, final String hierarchy, final String name, final T annotation ) {
    setCube( cube );
    setHierarchy( hierarchy );
    setName( name );
    setAnnotation( annotation );
  }

  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }

  public String getCube() {
    return cube;
  }

  public void setCube( String cube ) {
    this.cube = cube;
  }

  public String getHierarchy() {
    return hierarchy;
  }

  public void setHierarchy( String hierarchy ) {
    this.hierarchy = hierarchy;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public T getAnnotation() {
    return annotation;
  }

  public void setAnnotation( T annotation ) {
    this.annotation = annotation;
  }

  /**
   * **** Utility methods ******
   */

  public static List<ModelAnnotation<CreateMeasure>> getMeasures(
      final List<ModelAnnotation<? extends org.pentaho.agilebi.modeler.models.annotations.AnnotationType>> annotations ) {
    return filter( annotations, CreateMeasure.class );
  }

  public static List<ModelAnnotation<CreateAttribute>> getAttributes(
      final List<ModelAnnotation<? extends org.pentaho.agilebi.modeler.models.annotations.AnnotationType>> annotations ) {
    return filter( annotations, CreateAttribute.class );
  }

  private static <S extends org.pentaho.agilebi.modeler.models.annotations.AnnotationType> List<ModelAnnotation<S>> filter(
      final List<ModelAnnotation<? extends org.pentaho.agilebi.modeler.models.annotations.AnnotationType>> annotations, Class<S> cls ) {

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

  public boolean apply( final ModelerWorkspace modelerWorkspace ) throws ModelerException {
    if ( getField() != null ) {
      return annotation.apply( modelerWorkspace, getField() );
    } else {
      return annotation.apply( modelerWorkspace, getCube(), getHierarchy(), getName() );
    }
  }

  public boolean apply( final Document schema ) throws ModelerException {
    if ( getField() != null ) {
      return annotation.apply( schema, getField() );
    } else {
      return annotation.apply( schema, getCube(), getHierarchy(), getName() );
    }
  }

  public org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation.Type getType() {
    if ( annotation != null ) {
      return annotation.getType();
    }

    return null;
  }

  public Map<String, Serializable> describeAnnotation() {
    if ( annotation != null ) {
      return annotation.describe();
    }

    return null;
  }

  public void populateAnnotation( final Map<String, Serializable> properties ) {
    if ( annotation != null ) {
      annotation.populate( properties );
    }
  }

  public void iterateProperties( final KeyValueClosure closure ) {
    if ( annotation != null ) {
      annotation.iterateProperties( closure );
    }
  }

  public static enum Type {
    CREATE_MEASURE( "Create Measure" ),
    CREATE_ATTRIBUTE( "Create Attribute" );

    private final String description;

    Type( String description ) {
      this.description = description;
    }

    public String description() {
      return description;
    }

    public static String[] names() {
      Type[] types = values();
      String[] names = new String[types.length];
      for ( int i = 0; i < types.length; i++ ) {
        names[i] = types[i].name();
      }
      return names;
    }
  }

  public static enum TimeType {
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
    Null;

    public static String[] names() {
      TimeType[] types = values();
      String[] names = new String[types.length];
      for ( int i = 0; i < types.length; i++ ) {
        names[i] = types[i].name();
      }
      return names;
    }
  }

  public static enum GeoType {
    Lat_Long,
    Country,
    City,
    State,
    County,
    Postal_Code,
    Continent,
    Territory;

    public static String[] names() {
      GeoType[] types = values();
      String[] names = new String[types.length];
      for ( int i = 0; i < types.length; i++ ) {
        names[i] = types[i].name();
      }
      return names;
    }
  }

}
