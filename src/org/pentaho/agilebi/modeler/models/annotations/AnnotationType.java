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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Rowell Belen
 */
public abstract class AnnotationType implements Serializable {

  protected static final String NAME_ID = "name";
  protected static final String NAME_NAME = "Display Name";
  protected static final String CAPTION_ID = "caption";
  protected static final String CAPTION_NAME = "Caption";
  protected static final String DESCRIPTION_ID = "description";
  protected static final String DESCRIPTION_NAME = "Description";
  protected static final String HIDDEN_ID = "hidden";
  protected static final String HIDDEN_NAME = "Hidden";
  protected static final String BUSINESS_GROUP_ID = "businessGroup";
  protected static final String BUSINESS_GROUP_NAME = "Business Group";
  private static final long serialVersionUID = 3952409344571242884L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );
  @ModelProperty( id = NAME_ID, name = NAME_NAME )
  private String name;

  @ModelProperty( id = CAPTION_ID, name = CAPTION_NAME )
  private String localizedName;

  @ModelProperty( id = DESCRIPTION_ID, name = DESCRIPTION_NAME )
  private String description;

  @ModelProperty( id = HIDDEN_ID, name = HIDDEN_NAME )
  private boolean hidden;

  @ModelProperty( id = BUSINESS_GROUP_ID, name = BUSINESS_GROUP_NAME )
  private String businessGroup;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public void setLocalizedName( String localizedName ) {
    this.localizedName = localizedName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public String getBusinessGroup() {
    return businessGroup;
  }

  public void setBusinessGroup( String businessGroup ) {
    this.businessGroup = businessGroup;
  }

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

  public List<String> getModelPropertyNames() {

    List<String> propertyNames = new ArrayList<String>();

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        propertyNames.add( mp.name() );
      }
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
      if ( ClassUtils.isAssignable( field.getType(), Boolean.class, true ) ) {
        PropertyUtils.setProperty( this, field.getName(), BooleanUtils.toBoolean( value.toString() ) );
        return;
      }

      if ( NumberUtils.isNumber( value.toString() ) ) {
        Number number = NumberUtils.createNumber( value.toString() );
        PropertyUtils.setProperty( this, field.getName(), number );
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

  public abstract void apply( final ModelerWorkspace workspace, final String column ) throws ModelerException;

  public abstract ModelAnnotation.Type getType();

}
