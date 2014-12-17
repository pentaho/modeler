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
import java.util.Map;

/**
 * @author Rowell Belen
 */
public class Attribute extends AnnotationType {

  private static final long serialVersionUID = 5169827225345800226L;

  protected static final String UNIQUE_ID = "unique";
  protected static final String UNIQUE_NAME = "Is Unique";

  protected static final String TIME_FORMAT_ID = "timeFormat";
  protected static final String TIME_FORMAT_NAME = "Time Format";

  protected static final String TIME_TYPE_ID = "timeType";
  protected static final String TIME_TYPE_NAME = "Time Type";

  protected static final String GEO_TYPE_ID = "geoType";
  protected static final String GEO_TYPE_NAME = "Geo Type";

  protected static final String ORDINAL_FIELD_ID = "ordinalField";
  protected static final String ORDINAL_FIELD_NAME = "Ordinal Field";

  @ModelProperty( id = UNIQUE_ID, name = UNIQUE_NAME )
  private boolean unique;

  @ModelProperty( id = TIME_FORMAT_ID, name = TIME_FORMAT_NAME )
  private String timeFormat;

  @ModelProperty( id = TIME_TYPE_ID, name = TIME_TYPE_NAME )
  private ModelAnnotation.TimeType timeType;

  @ModelProperty( id = GEO_TYPE_ID, name = GEO_TYPE_NAME )
  private ModelAnnotation.GeoType geoType;

  @ModelProperty( id = ORDINAL_FIELD_ID, name = ORDINAL_FIELD_NAME )
  private String ordinalField;

  public boolean isUnique() {
    return unique;
  }

  public void setUnique( boolean unique ) {
    this.unique = unique;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  public void setTimeFormat( String timeFormat ) {
    this.timeFormat = timeFormat;
  }

  public ModelAnnotation.TimeType getTimeType() {
    return timeType;
  }

  public void setTimeType( ModelAnnotation.TimeType timeType ) {
    this.timeType = timeType;
  }

  public ModelAnnotation.GeoType getGeoType() {
    return geoType;
  }

  public void setGeoType( ModelAnnotation.GeoType geoType ) {
    this.geoType = geoType;
  }

  public String getOrdinalField() {
    return ordinalField;
  }

  public void setOrdinalField( String ordinalField ) {
    this.ordinalField = ordinalField;
  }

  @Override
  public void apply( final ModelerWorkspace workspace, final String column ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void populate( final Map<String, Serializable> propertiesMap ) {

    super.populate( propertiesMap ); // let base class handle primitives, etc.

    // correctly convert time type
    if ( propertiesMap.containsKey( TIME_TYPE_ID ) ) {
      Serializable value = propertiesMap.get( TIME_TYPE_ID );
      if ( value != null ) {
        setTimeType( ModelAnnotation.TimeType.valueOf( value.toString() ) );
      }
    }

    // correctly convert geo type
    if ( propertiesMap.containsKey( GEO_TYPE_ID ) ) {
      Serializable value = propertiesMap.get( GEO_TYPE_ID );
      if ( value != null ) {
        setGeoType( ModelAnnotation.GeoType.valueOf( value.toString() ) );
      }
    }
  }

  @Override
  public boolean isActionSupported( ModelAnnotation.Action action ) {
    if ( ModelAnnotation.Action.CREATE == action ) {
      return true; // only supported action for now
    }

    return false;
  }

  @Override
  public AnnotationSubType getType() {
    return AnnotationSubType.ATTRIBUTE;
  }
}
