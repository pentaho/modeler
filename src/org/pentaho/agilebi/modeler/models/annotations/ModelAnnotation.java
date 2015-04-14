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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rowell Belen
 */
@MetaStoreElementType( name = "ModelAnnotation", description = "ModelAnnotation" )
public class ModelAnnotation<T extends AnnotationType> implements Serializable {

  private static final long serialVersionUID = 5742135911581602697L;
  private static final String OLAP_CUBES_PROPERTY = "olap_cubes";
  private static final String MEASURES_DIMENSION = "Measures";
  private static final String MEASURE_ELEMENT_NAME = "Measure";
  private static final String NAME_ATTRIBUTE = "name";
  private static final String COLUMN_ATTRIBUTE = "column";
  private static final String CALCULATED_MEMBER_ELEMENT_NAME = "CalculatedMember";

  private static final String CREATE_MEASURE_ENUM_VALUE = "Create Measure";
  private static final String CREATE_ATTRIBUTE_ENUM_VALUE = "Create Attribute";
  private static final String CREATE_DIMENSION_ENUM_VALUE = "Create Dimension Key";
  private static final String CREATE_CALCULATED_MEMBER_ENUM_VALUE = "Create Calculated Member";
  private static final String REMOVE_MEASURE_ENUM_VALUE = "Remove Measure";

  private SourceType sourceType = SourceType.StreamField;

  @MetaStoreAttribute
  private String name = UUID.randomUUID().toString(); // default random identifier

  @MetaStoreAttribute
  private String field;

  @MetaStoreAttribute
  private String cube;

  @MetaStoreAttribute
  private T annotation;

  public ModelAnnotation() {
  }

  // Required by the MetaStore
  public String getName() {
    return name;
  }

  // Required by MetaStore
  public void setName( String name ) {
    this.name = name;
  }

  public ModelAnnotation( final String field, final T annotation ) {
    setField( field );
    setAnnotation( annotation );
    this.sourceType = SourceType.StreamField;
  }

  public ModelAnnotation( final SourceType sourceType, final String cube, final String field, final T annotation ) {
    setCube( cube );
    setField( field );
    this.sourceType = sourceType;
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

  public T getAnnotation() {
    return annotation;
  }

  public void setAnnotation( T annotation ) {
    this.annotation = annotation;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType( SourceType sourceType ) {
    this.sourceType = sourceType;
  }

  public boolean apply( final ModelerWorkspace modelerWorkspace ) throws ModelerException {
    return annotation.apply( modelerWorkspace, resolveField( modelerWorkspace ) );
  }

  /**
   * Returns the physical column that this annotation should operate on.  For sources based on HierarchyLevel and
   * Measure, we need to consult the existing model to find the underlying physical source.
   *
   * @param modelerWorkspace
   * @return
   * @throws ModelerException
   */
  private String resolveField( final ModelerWorkspace modelerWorkspace ) throws ModelerException {

    switch( sourceType ) {
      case StreamField: {
        return field;
      }
      case Measure:
      case HierarchyLevel: {
        String locale = Locale.getDefault().toString();
        LogicalModel businessModel = modelerWorkspace.getLogicalModel( ModelerPerspective.ANALYSIS );
        List<OlapCube> olapCubes = (List<OlapCube>) businessModel.getProperty( OLAP_CUBES_PROPERTY );
        OlapCube olapCube = null;
        for ( int c = 0; c < olapCubes.size(); c++ ) {
          if ( ( (OlapCube) olapCubes.get( c ) ).getName().equals( cube ) ) {
            olapCube = (OlapCube) olapCubes.get( c );
            break;
          }
        }
        if ( olapCube == null ) {
          throw new ModelerException( 
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_CUBE", cube ) 
          );
        }
        if ( sourceType == SourceType.Measure ) {
          List measures = olapCube.getOlapMeasures();
          for ( int m = 0; m < measures.size(); m++ ) {
            OlapMeasure measure = (OlapMeasure) measures.get( m );
            if ( field.equals( "[" + MEASURES_DIMENSION + "].[" + measure.getLogicalColumn().getName( locale ) + "]" ) ) {
              return (String) measure.getLogicalColumn().getName( locale );
            }
          }
          throw new ModelerException( 
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", field ) 
          );
        } else {

          List usages = olapCube.getOlapDimensionUsages();
          for ( int u = 0; u < usages.size(); u++ ) {
            OlapDimensionUsage usage = (OlapDimensionUsage) usages.get( u );
            OlapDimension olapDimension = usage.getOlapDimension();
            List olapHierarchies = olapDimension.getHierarchies();
            for ( int h = 0; h < olapHierarchies.size(); h++ ) {
              StringBuffer buffer = new StringBuffer();
              buffer.append( "[" );
              buffer.append( usage.getName() );
              OlapHierarchy olapHierarchy = (OlapHierarchy) olapHierarchies.get( h );
              if ( StringUtils.isNotEmpty( olapHierarchy.getName() )
                && !StringUtils.equals( olapHierarchy.getName(), usage.getName() ) ) {
                buffer.append( "." ).append( olapHierarchy.getName() );
              }
              buffer.append( "].[" );
              List hierarchyLevels = olapHierarchy.getHierarchyLevels();
              for ( int hl = 0; hl < hierarchyLevels.size(); hl++ ) {
                OlapHierarchyLevel olapHierarchyLevel = (OlapHierarchyLevel) hierarchyLevels.get( hl );
                if ( field.equals( buffer.toString() + olapHierarchyLevel.getName() + "]" ) ) {
                  return (String) olapHierarchyLevel.getReferenceColumn().getName( locale );
                }
              }
            }
          }
          throw new ModelerException( 
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_LEVEL", field ) 
          );
        }
      }
      default: {
        throw new IllegalStateException();
      }
    }
  }

  /**
   * Returns the physical column that this annotation should operate on.  For sources based on HierarchyLevel and
   * Measure, we need to consult the existing model to find the underlying physical source.
   *
   * @param schema
   * @return
   * @throws ModelerException
   */
  private String resolveField( final Document schema ) throws ModelerException {

    switch( sourceType ) {
      case StreamField: {
        return field;
      }
      case Measure: {
        if ( schema == null ) {
          throw new ModelerException( 
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", field ) 
          );
        }

        // find Measure nodes
        NodeList measures = schema.getElementsByTagName( MEASURE_ELEMENT_NAME );
        if ( ( measures == null ) || ( measures.getLength() <= 0 ) ) {
          throw new ModelerException(
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", field )
          );
        }

        for ( int x = 0; x <= measures.getLength(); x++ ) {
          Node measureNode = measures.item( x );
          if ( measureNode != null ) {

            // get measure name
            Node nameNode = measureNode.getAttributes().getNamedItem( NAME_ATTRIBUTE );

            if ( nameNode != null ) {
              // match measure name to field
              if ( nameNode.getNodeValue().equals(
                field.substring( field.lastIndexOf( "[" ) + 1 ).replace( "]", "" )
              ) ) {
                // get the column
                Node columnNode = measureNode.getAttributes().getNamedItem( COLUMN_ATTRIBUTE );

                if ( columnNode != null ) {
                  return (String) columnNode.getNodeValue();
                }
              }
            }
          }
        }

        // not found
        throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", field )
        );

      }
      case HierarchyLevel: {
        // TODO
        throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_LEVEL", field )
        );
      }

      case CalculatedMember: {
        if ( schema == null ) {
          throw new ModelerException(
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_CALCULATEDMEMBER", field )
          );
        }

        // find Measure nodes
        NodeList calculatedMembers = schema.getElementsByTagName( CALCULATED_MEMBER_ELEMENT_NAME );
        if ( ( calculatedMembers == null ) || ( calculatedMembers.getLength() <= 0 ) ) {
          throw new ModelerException(
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_CALCULATEDMEMBER", field )
          );
        }

        for ( int x = 0; x <= calculatedMembers.getLength(); x++ ) {
          Node calculatedMember = calculatedMembers.item( x );
          if ( calculatedMember != null ) {

            // get measure name
            Node nameNode = calculatedMember.getAttributes().getNamedItem( NAME_ATTRIBUTE );

            if ( nameNode != null ) {
              // match measure name to field
              if ( nameNode.getNodeValue().equals(
                field.substring( field.lastIndexOf( "[" ) + 1 ).replace( "]", "" )
              ) ) {
                return nameNode.getNodeValue();
              }
            }
          }
        }

        // not found
        throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_CALCULATEDMEMBER", field )
        );
      }

      default: {
        throw new IllegalStateException();
      }
    }
  }

  public boolean apply( final Document schema ) throws ModelerException {
    return annotation.apply( schema, resolveField( schema ) );
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
    CREATE_MEASURE( CREATE_MEASURE_ENUM_VALUE ),
    CREATE_ATTRIBUTE( CREATE_ATTRIBUTE_ENUM_VALUE ),
    CREATE_DIMENSION_KEY( CREATE_DIMENSION_ENUM_VALUE ),
    CREATE_CALCULATED_MEMBER( CREATE_CALCULATED_MEMBER_ENUM_VALUE ),
    REMOVE_MEASURE( REMOVE_MEASURE_ENUM_VALUE );

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

  /**
   * Represents the source of the modeling action... i.e. are we creating a measure off of a field, level or another
   * measure?
   *
   * @author Benny
   */
  public static enum SourceType {
    StreamField,
    HierarchyLevel,
    Measure,
    CalculatedMember
  }

}
