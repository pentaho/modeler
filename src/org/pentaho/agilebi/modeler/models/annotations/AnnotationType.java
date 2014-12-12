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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerWorkspace;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rowell Belen
 */
public abstract class AnnotationType implements Serializable {

  private static final long serialVersionUID = 3952409344571242884L;

  @ModelProperty( name = "Display Name" )
  private String name;

  @ModelProperty( name = "Localized Name" )
  private String localizedName;

  @ModelProperty( name = "Description" )
  private String description;

  @ModelProperty( name = "Unique Members" )
  private boolean uniqueMembers;

  @ModelProperty( name = "Hidden" )
  private boolean hidden;

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

  public boolean isUniqueMembers() {
    return uniqueMembers;
  }

  public void setUniqueMembers( boolean uniqueMembers ) {
    this.uniqueMembers = uniqueMembers;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  protected List<Field> findAllFields( List<Field> fields, Class<?> type ) {

    fields.addAll( Arrays.asList( type.getDeclaredFields() ) );

    if ( type.getSuperclass() != null ) {
      fields = findAllFields( fields, type.getSuperclass() );
    }

    return fields;
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

  public void setModelPropertyName( String modelPropertyName, Object value ) throws Exception {

    List<Field> fields = findAllFields( new ArrayList<Field>(), this.getClass() );
    for ( Field f : fields ) {
      if ( f.isAnnotationPresent( ModelProperty.class ) ) {
        ModelProperty mp = f.getAnnotation( ModelProperty.class );
        if ( StringUtils.equals( mp.name(), modelPropertyName ) ) {
          if ( ClassUtils.isAssignable( f.getType(), value.getClass(), true ) ) {
            BeanUtils.setProperty( this, f.getName(), value );
          }
        }
      }
    }
  }

  public abstract void apply( final ModelerWorkspace workspace, final String column );
}
