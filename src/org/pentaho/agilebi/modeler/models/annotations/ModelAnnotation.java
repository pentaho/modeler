/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rowell Belen
 */
public class ModelAnnotation<T extends AnnotationType> implements Serializable {

  private static final long serialVersionUID = 5742135911581602697L;

  private static final String CREATE_MEASURE_ENUM_VALUE = "Create Measure";
  private static final String CREATE_ATTRIBUTE_ENUM_VALUE = "Create Attribute";
  private static final String CREATE_DIMENSION_ENUM_VALUE = "Create Dimension Key";
  private static final String CREATE_CALCULATED_MEMBER_ENUM_VALUE = "Create Calculated Measure";
  private static final String REMOVE_MEASURE_ENUM_VALUE = "Remove Measure";
  private static final String LINK_DIMENSION_ENUM_VALUE = "Link Dimension";
  private static final String REMOVE_ATTRIBUTE_ENUM_VALUE = "Remove Attribute";
  private static final String UPDATE_MEASURE_ENUM_VALUE = "Update Measure";
  private static final String UPDATE_CALCULATED_MEMBER_ENUM_VALUE = "Update Calculated Measure";
  private static final String SHOW_HIDE_ATTRIBUTE_ENUM_VALUE = "Show or Hide Level";
  private static final String SHOW_HIDE_MEASURE_ENUM_VALUE = "Show or Hide Measure";
  private static final String UPDATE_ATTRIBUTE_ENUM_VALUE = "Update Attribute";
  private static final String BLANK_ENUM_VALUE = "";

  @MetaStoreAttribute
  private String name = UUID.randomUUID().toString(); // default random identifier

  @MetaStoreAttribute
  @Deprecated // Legacy Support
  private String field;

  @MetaStoreAttribute
  @Deprecated // Legacy Support
  private String cube;

  @MetaStoreAttribute
  private T annotation;

  public ModelAnnotation() {
  }

  @Deprecated // Legacy Support
  public ModelAnnotation( final String field, final T annotation ) {
    setAnnotation( annotation );
    setField( field ); // backwards compatibility
  }

  // Required by the MetaStore
  public String getName() {
    return name;
  }

  // Required by MetaStore
  public void setName( String name ) {
    this.name = name;
  }

  public ModelAnnotation( final T annotation ) {
    setAnnotation( annotation );
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

  private static <S extends org.pentaho.agilebi.modeler.models.annotations.AnnotationType> List<ModelAnnotation<S>>
      filter(
      final List<ModelAnnotation<? extends org.pentaho.agilebi.modeler.models.annotations.AnnotationType>> annotations,
      Class<S> cls ) {

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

  @Deprecated // Legacy Support
  public String getField() {
    try {
      // Return the field property of the annotation type
      String f = PropertyUtils.getProperty( annotation, "field" ).toString();
      return f;
    } catch ( Exception e ) {
      // Return legacy field
      return field;
    }
  }

  @Deprecated // Legacy Support
  public void setField( String field ) {
    this.field = field;
    try {
      // try to apply to the field property of the annotation, if exists
      PropertyUtils.setProperty( annotation, "field", field );
    } catch ( Exception e ) {
      // ignore
    }
  }

  @Deprecated // Legacy Support
  public String getCube() {
    return cube;
  }

  @Deprecated // Legacy Support
  public void setCube( String cube ) {
    this.cube = cube;
  }

  public T getAnnotation() {
    return annotation;
  }

  public void setAnnotation( T annotation ) {
    this.annotation = annotation;
  }

  public boolean apply( final ModelerWorkspace modelerWorkspace, final IMetaStore metaStore ) throws ModelerException {

    // Backwards Compatibility
    if ( this.getField() != null ) {
      try {
        // try to apply to the field property of the annotation, if exists
        PropertyUtils.setProperty( annotation, "field", this.getField() );
      } catch ( Exception e ) {
        // ignore
      }
    }

    return annotation.apply( modelerWorkspace, metaStore );
  }

  public boolean apply( final Document schema ) throws ModelerException {
    return annotation.apply( schema );
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

  @Override
  public boolean equals( Object obj ) {
    return EqualsBuilder.reflectionEquals( this, obj );
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode( this );
  }

  public static enum Type {
    CREATE_MEASURE( CREATE_MEASURE_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return !modelAnnotations.isSharedDimension();
      }
    },
    CREATE_ATTRIBUTE( CREATE_ATTRIBUTE_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return true;
      }
    },
    CREATE_DIMENSION_KEY( CREATE_DIMENSION_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations, final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return modelAnnotations.isSharedDimension()
          && ( isDimensionKey( modelAnnotation ) || !hasDimensionKey( modelAnnotations ) );
      }

      private boolean isDimensionKey( final ModelAnnotation modelAnnotation ) {
        return CREATE_DIMENSION_KEY.equals( modelAnnotation.getType() );
      }

      private boolean hasDimensionKey( final ModelAnnotationGroup modelAnnotations ) {
        for ( ModelAnnotation modelAnnotation : modelAnnotations ) {
          if ( isDimensionKey( modelAnnotation ) ) {
            return true;
          }
        }
        return false;
      }
    },
    LINK_DIMENSION( LINK_DIMENSION_ENUM_VALUE ) {
      @Override
      public boolean isApplicable( final ModelAnnotationGroup modelAnnotations, final ModelAnnotation modelAnnotation,
                                   final ValueMetaInterface valueMeta ) {
        return !modelAnnotations.isSharedDimension();
      }
    },
    CREATE_CALCULATED_MEMBER( CREATE_CALCULATED_MEMBER_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    REMOVE_MEASURE( REMOVE_MEASURE_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    REMOVE_ATTRIBUTE( REMOVE_ATTRIBUTE_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    UPDATE_MEASURE( UPDATE_MEASURE_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    UPDATE_CALCULATED_MEMBER( UPDATE_CALCULATED_MEMBER_ENUM_VALUE ) {
      @Override public boolean isApplicable(
        final ModelAnnotationGroup modelAnnotations,
        final ModelAnnotation modelAnnotation,
        final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    BLANK( BLANK_ENUM_VALUE ) {
      @Override public boolean isApplicable(
          final ModelAnnotationGroup modelAnnotations,
          final ModelAnnotation modelAnnotation,
          final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    SHOW_HIDE_ATTRIBUTE( SHOW_HIDE_ATTRIBUTE_ENUM_VALUE ) {
      @Override
      public boolean isApplicable(
        final ModelAnnotationGroup modelAnnotations,
        final ModelAnnotation modelAnnotation,
        final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    SHOW_HIDE_MEASURE( SHOW_HIDE_MEASURE_ENUM_VALUE ) {
      @Override
      public boolean isApplicable(
        final ModelAnnotationGroup modelAnnotations,
        final ModelAnnotation modelAnnotation,
        final ValueMetaInterface valueMeta ) {
        return false;
      }
    },
    UPDATE_ATTRIBUTE( UPDATE_ATTRIBUTE_ENUM_VALUE ) {
      @Override
      public boolean isApplicable( final ModelAnnotationGroup modelAnnotations, final ModelAnnotation modelAnnotation,
                                   final ValueMetaInterface valueMeta ) {
        return false;
      }
    };

    private final String description;

    Type( String description ) {
      this.description = description;
    }

    public static String[] names() {
      Type[] types = values();
      String[] names = new String[ types.length ];
      for ( int i = 0; i < types.length; i++ ) {
        names[ i ] = types[ i ].name();
      }
      return names;
    }

    public String description() {
      return description;
    }

    public abstract boolean isApplicable(
        final ModelAnnotationGroup modelAnnotations,
        final ModelAnnotation modelAnnotation,
        final ValueMetaInterface valueMeta );
  }

  public static enum TimeType {
    TimeYears,
    TimeHalfYears,
    TimeQuarters,
    TimeMonths,
    TimeWeeks,
    TimeDays,
    TimeHours,
    TimeMinutes,
    TimeSeconds;

    public static String[] names() {
      TimeType[] types = values();
      String[] names = new String[ types.length ];
      for ( int i = 0; i < types.length; i++ ) {
        names[ i ] = types[ i ].name();
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
      String[] names = new String[ types.length ];
      for ( int i = 0; i < types.length; i++ ) {
        names[ i ] = types[ i ].name();
      }
      return names;
    }
  }
}
