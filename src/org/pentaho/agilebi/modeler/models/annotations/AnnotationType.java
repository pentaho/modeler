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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.w3c.dom.Document;

/**
 * @author Rowell Belen
 */
public abstract class AnnotationType implements Serializable {

  protected static final Class<?> MSG_CLASS = BaseModelerWorkspaceHelper.class;
  private static final long serialVersionUID = 3952409344571242884L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );

  protected List<Field> findAllFields( List<Field> fields, Class<?> type ) {

    fields.addAll( Arrays.asList( type.getDeclaredFields() ) );

    if ( type.getSuperclass() != null ) {
      fields = findAllFields( fields, type.getSuperclass() );
    }

    return fields;
  }

  public List<String> getModelPropertyIds() {

    List<String> ids = new ArrayList<String>();

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        ids.add( mp.id() );
      }
    }

    return ids;
  }

  public void setModelPropertyValueById( String id, Object value ) throws Exception {

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        if ( StringUtils.equals( mp.id(), id ) ) {
          attemptAutoConvertAndAssign( f, value );
        }
      }
    }
  }

  public Object getModelPropertyValueById( String id ) throws Exception {

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        if ( StringUtils.equals( mp.id(), id ) ) {
          return PropertyUtils.getProperty( this, f.getName() );
        }
      }
    }

    return null;
  }

  public Object getModelPropertyValueByName( String name ) throws Exception {

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        if ( StringUtils.equals( mp.name(), name ) ) {
          return PropertyUtils.getProperty( this, f.getName() );
        }
      }
    }

    return null;
  }

  public Class getModelPropertyNameClassType( String name ) {
    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        if ( StringUtils.equals( mp.name(), name ) ) {
          return f.getType();
        }
      }
    }

    return null;
  }

  public List<String> getModelPropertyNames() {

    final List<String> propertyNames = new ArrayList<String>();
    final List<ModelProperty> properties = new ArrayList<ModelProperty>();

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        properties.add( mp );
      }
    }

    // Sort ModelProperty based on order
    Collections.sort( properties, new Comparator<ModelProperty>() {
      @Override
      public int compare( ModelProperty m1, ModelProperty m2 ) {
        if ( m1.order() <= m2.order() ) {
          return -1;
        }
        return 1;
      }
    } );

    for ( ModelProperty p : properties ) {
      propertyNames.add( p.name() );
    }

    return propertyNames;
  }

  public void setModelPropertyByName( String modelPropertyName, Object value ) throws Exception {

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        if ( StringUtils.equals( mp.name(), modelPropertyName ) ) {
          attemptAutoConvertAndAssign( f, value );
        }
      }
    }
  }

  protected void attemptAutoConvertAndAssign( final Field field, final Object value ) throws Exception {

    if ( ClassUtils.isAssignable( value.getClass(), field.getType(), true ) ) {
      PropertyUtils.setProperty( this, field.getName(), value );
    } else {

      try {
        if ( ClassUtils.isAssignable( field.getType(), Boolean.class, true ) ) {
          PropertyUtils.setProperty( this, field.getName(), BooleanUtils.toBoolean( value.toString() ) );
          return;
        }

        if ( ClassUtils.isAssignable( field.getType(), AggregationType.class, true ) ) {
          AggregationType type = AggregationType.valueOf( value.toString() );
          if ( type != null ) {
            PropertyUtils.setProperty( this, field.getName(), type );
          }
          return;
        }

        if ( ClassUtils.isAssignable( field.getType(), ModelAnnotation.TimeType.class, true ) ) {
          ModelAnnotation.TimeType type = ModelAnnotation.TimeType.valueOf( value.toString() );
          if ( type != null ) {
            PropertyUtils.setProperty( this, field.getName(), type );
          }
          return;
        }

        if ( ClassUtils.isAssignable( field.getType(), ModelAnnotation.GeoType.class, true ) ) {
          ModelAnnotation.GeoType type = ModelAnnotation.GeoType.valueOf( value.toString() );
          if ( type != null ) {
            PropertyUtils.setProperty( this, field.getName(), type );
          }
          return;
        }

        if ( NumberUtils.isNumber( value.toString() ) ) {
          Number number = NumberUtils.createNumber( value.toString() );
          PropertyUtils.setProperty( this, field.getName(), number );
        }
      } catch ( Exception e ) {
        // ignore
        logger.warning( "Unable to convert " + value.toString() + " in to " + field.getType() );
      }
    }
  }

  public Map<String, Serializable> describe() {
    Map<String, Serializable> map = new HashMap<String, Serializable>();

    List<String> ids = getModelPropertyIds();
    for ( String id : ids ) {
      try {
        Object value = getModelPropertyValueById( id );
        if ( value != null && isSerializable( value.getClass() ) ) {
          map.put( id, (Serializable) value );
        }
      } catch ( Exception e ) {
        // ignore
        logger.warning( "Unable to set value for id: " + id );
      }
    }
    return map;
  }

  protected void removeAutoLevel( final ModelerWorkspace workspace, final LevelMetaData levelMetaData ) {
    if ( levelMetaData == null ) {
      return;
    }
    HierarchyMetaData hierarchy = levelMetaData.getHierarchyMetaData();
    DimensionMetaData dimension = hierarchy.getDimensionMetaData();
    if ( hierarchy.getLevels().size() > 1 ) {
      return;
    }
    if ( dimension.contains( hierarchy ) ) {
      dimension.remove( hierarchy );
    }
    if ( dimension.size() > 0 ) {
      return;
    }
    DimensionMetaDataCollection dimensions = workspace.getModel().getDimensions();
    if ( dimensions.contains( dimension ) ) {
      dimensions.remove( dimension );
    }
  }

  protected LevelMetaData locateLevel( final ModelerWorkspace workspace, final String column ) throws ModelerException {
    workspace.getModel().getDimensions();
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
        for ( LevelMetaData levelMetaData : hierarchyMetaData ) {
          if ( levelMetaData.getLogicalColumn().getName(workspace.getWorkspaceHelper().getLocale() ).equals( column ) ) {
            return levelMetaData;
          }
        }
      }
    }
    return null;
  }

  protected LogicalColumn locateLogicalColumn( final ModelerWorkspace workspace, final String columnName ) {
    LogicalModel logicalModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    logicalModel.getLogicalTables();
    for ( LogicalTable logicalTable : logicalModel.getLogicalTables() ) {
      for ( LogicalColumn logicalColumn : logicalTable.getLogicalColumns() ) {
        if ( logicalColumn.getName( workspace.getWorkspaceHelper().getLocale() ).equalsIgnoreCase( columnName ) ) {
          return logicalColumn;
        }
      }
    }
    return null;
  }

  public void populate( final Map<String, Serializable> propertiesMap ) {

    if ( propertiesMap != null && propertiesMap.keySet() != null ) {

      Iterator<String> itr = propertiesMap.keySet().iterator();
      while ( itr.hasNext() ) {

        String id = itr.next();
        try {
          setModelPropertyValueById( id, propertiesMap.get( id ) );
        } catch ( Exception e ) {
          // do nothing
          logger.warning( "Unable to set value for id: " + id );
        }
      }
    }
  }

  public void iterateProperties( KeyValueClosure closure ) {

    Map<String, Serializable> properties = describe();
    if ( closure == null || properties == null || properties.keySet() == null ) {
      return;
    }

    Iterator<String> itr = properties.keySet().iterator();
    while ( itr.hasNext() ) {
      String key = itr.next();
      if ( StringUtils.isNotBlank( key ) ) {
        Serializable value = properties.get( key );
        if ( value != null ) {
          closure.execute( key, value );
        }
      }
    }
  }

  private boolean isSerializable( Class<?> classToCheck ) {
    return Serializable.class.isAssignableFrom( classToCheck );
  }

  /**
   * Applies modeling changes on a Metadata model using a field as the source.
   * 
   * @param workspace
   * @param field
   * @throws ModelerException
   */
  public abstract boolean apply( final ModelerWorkspace workspace, final String field ) throws ModelerException;

  /**
   * Applies modeling change on a Mondrian schema using a field as the source..
   * 
   * @param schema
   * @param field
   * @throws ModelerException
   */
  public abstract boolean apply( final Document schema, final String field ) throws ModelerException;

  public abstract void validate() throws ModelerException;

  public abstract ModelAnnotation.Type getType();

  public abstract String getSummary();

  public abstract String getName();
}
