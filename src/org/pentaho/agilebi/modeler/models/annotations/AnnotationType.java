/*! ******************************************************************************
 *
 * Pentaho Community Edition Project: pentaho-modeler
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ********************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.automodel.PhysicalTableImporter;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Rowell Belen
 */
public abstract class AnnotationType implements Serializable {

  protected static final String OLAP_CUBES_PROPERTY = "olap_cubes";
  protected static final String MEASURES_DIMENSION = "Measures";
  protected static final String MEASURE_ELEMENT_NAME = "Measure";
  protected static final String NAME_ATTRIBUTE = "name";
  protected static final String COLUMN_ATTRIBUTE = "column";
  protected static final String CALCULATED_MEMBER_ELEMENT_NAME = "CalculatedMember";

  protected static final Class<?> MSG_CLASS = BaseModelerWorkspaceHelper.class;
  private static final long serialVersionUID = 3952409344571242884L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );

  protected Logger getLogger() {
    return logger;
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

    for ( ModelProperty p : getModelProperties() ) {
      propertyNames.add( p.name() );
    }

    return propertyNames;
  }

  public List<ModelProperty> getModelProperties() {
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
    return properties;
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

    if ( field == null ) {
      return; // exit early
    }

    if ( value == null ) {
      try {
        PropertyUtils.setProperty( this, field.getName(), value );
      } catch ( Exception e ) {
        // ignore
      }
      return; // exit early
    }

    if ( value != null && ClassUtils.isAssignable( value.getClass(), field.getType(), true ) ) {
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
        if ( value == null || StringUtils.isBlank( value.toString() ) ) {
          return; // do not log
        }
        // ignore error but log
        getLogger().warning( "Unable to convert " + value.toString() + " in to " + field.getType() );
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
        getLogger().warning( "Unable to set value for id: " + id );
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
    String locale = workspace.getWorkspaceHelper().getLocale();
    workspace.getModel().getDimensions();
    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
        for ( LevelMetaData levelMetaData : hierarchyMetaData ) {
          if ( levelMetaData.getLogicalColumn().getName( locale ).equalsIgnoreCase( column )
              || levelMetaData.getLogicalColumn().getName( locale ).equals( beautify( column ) ) ) {
            return levelMetaData;
          }
        }
      }
    }
    return null;
  }

  protected LogicalColumn locateLogicalColumn( final ModelerWorkspace workspace, final String columnName ) {
    String locale = workspace.getWorkspaceHelper().getLocale();
    LogicalModel logicalModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    logicalModel.getLogicalTables();
    for ( LogicalTable logicalTable : logicalModel.getLogicalTables() ) {
      for ( LogicalColumn logicalColumn : logicalTable.getLogicalColumns() ) {
        if ( logicalColumn.getName( locale ).equalsIgnoreCase( columnName )
            || logicalColumn.getName( locale ).equalsIgnoreCase( beautify( columnName ) ) ) {
          return logicalColumn;
        }
      }
    }
    return null;
  }

  protected void removeAutoMeasure( final ModelerWorkspace workspace, final String column ) {
    MeasureMetaData measure = locateMeasure( workspace, column );
    if ( measure != null ) {
      workspace.getModel().getMeasures().remove( measure );
    }
  }

  private MeasureMetaData locateMeasure( final ModelerWorkspace workspace, final String column ) {
    MeasuresCollection measures = workspace.getModel().getMeasures();
    for ( MeasureMetaData measure : measures ) {
      if ( measure.getLogicalColumn().getName( workspace.getWorkspaceHelper().getLocale() ).equals( column )
          || measure.getLogicalColumn().getName( workspace.getWorkspaceHelper().getLocale() ).equals(
          beautify( column ) ) ) {
        return measure;
      }
    }
    return null;
  }

  protected String beautify( final String column ) {
    return column == null ? null : PhysicalTableImporter.beautifyName( column );
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
          getLogger().warning( "Unable to set value for id: " + id );
        }
      }
    }
  }

  /**
   * Retrieves the olap cube from the workspace based on the cube name
   *
   * @param modelerWorkspace workspace to search for the cube
   * @param cubeName         cube name
   * @return OlapCube otherwise null
   */
  private OlapCube getOlapCube( final ModelerWorkspace modelerWorkspace, final String cubeName ) {
    LogicalModel businessModel = modelerWorkspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    List<OlapCube> olapCubes = (List<OlapCube>) businessModel.getProperty( OLAP_CUBES_PROPERTY );
    OlapCube olapCube = null;
    for ( int c = 0; c < olapCubes.size(); c++ ) {
      if ( ( (OlapCube) olapCubes.get( c ) ).getName().equals( cubeName ) ) {
        olapCube = (OlapCube) olapCubes.get( c );
        break;
      }
    }

    return olapCube;
  }

  /**
   * Returns the physical column name that this annotation should operate on. For sources
   * based on HierarchyLevels we need to consult the existing model to find the underlying
   * source field.
   *
   * @param modelerWorkspace Workspace to look for the level
   * @param levelName        Level formula to find the level
   * @param cubeName         Cube name to find the level
   * @return field
   * @throws ModelerException
   */
  protected String resolveFieldFromLevel( final ModelerWorkspace modelerWorkspace,
      final String levelName,
      final String cubeName ) throws ModelerException {
    if ( StringUtils.isBlank( levelName ) || StringUtils.isBlank( cubeName ) || modelerWorkspace == null ) {
      throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_LEVEL", levelName )
      );
    }

    String locale = Locale.getDefault().toString();
    OlapCube olapCube = getOlapCube( modelerWorkspace, cubeName );
    if ( olapCube == null ) {
      throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_CUBE", cubeName )
      );
    }

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
          if ( levelName.equals( buffer.toString() + olapHierarchyLevel.getName() + "]" ) ) {
            return olapHierarchyLevel.getReferenceColumn().getName( locale );
          }
        }
      }
    }
    throw new ModelerException(
        BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_LEVEL", levelName )
    );
  }

  /**
   * Returns the physical column name that this annotation should operate on. For sources
   * based on Measures we need to consult the existing model to find the underlying
   * source field.
   *
   * @param modelerWorkspace Workspace to look for the level
   * @param measureName      Measure formula to find the level
   * @param cubeName         Cube name to find the level
   * @return field
   * @throws ModelerException
   */
  protected String resolveFieldFromMeasure( final ModelerWorkspace modelerWorkspace,
      final String measureName,
      final String cubeName ) throws ModelerException {
    if ( StringUtils.isBlank( measureName ) || StringUtils.isBlank( cubeName ) || modelerWorkspace == null ) {
      throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", measureName )
      );
    }

    String locale = Locale.getDefault().toString();
    OlapCube olapCube = getOlapCube( modelerWorkspace, cubeName );
    if ( olapCube == null ) {
      throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_CUBE", cubeName )
      );
    }
    List measures = olapCube.getOlapMeasures();
    for ( int m = 0; m < measures.size(); m++ ) {
      OlapMeasure measure = (OlapMeasure) measures.get( m );
      if ( measureName
          .equals( "[" + MEASURES_DIMENSION + "].[" + measure.getLogicalColumn().getName( locale ) + "]" ) ) {
        return (String) measure.getLogicalColumn().getName( locale );
      }
    }
    throw new ModelerException(
        BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", measureName )
    );
  }

  /**
   * Returns the physical column name that this annotation should operate on. For sources
   * based on CalculatedMember we need to consult the existing model to find the underlying
   * source field.
   *
   * @param schema  OLAP schema to search
   * @param measure String name of the measure
   * @return field name
   * @throws ModelerException
   */
  protected String resolveFieldFromMeasure( final Document schema, String measure ) throws ModelerException {
    if ( schema != null && !StringUtils.isBlank( measure ) ) {
      NodeList measures = schema.getElementsByTagName( MEASURE_ELEMENT_NAME );
      if ( ( measures == null ) || ( measures.getLength() <= 0 ) ) {
        throw new ModelerException(
            BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", measure )
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
                measure.substring( measure.lastIndexOf( "[" ) + 1 ).replace( "]", "" )
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
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", measure )
      );
    } else {
      throw new ModelerException(
          BaseMessages.getString( "ModelAnnotation.resolveField.UNABLE_TO_FIND_MEASURE", measure )
      );
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

  @Override
  public boolean equals( Object obj ) {
    return EqualsBuilder.reflectionEquals( this, obj );
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode( this );
  }

  /**
   * Applies modeling changes on a Metadata model
   *
   * @param workspace
   * @param metaStore
   * @throws ModelerException
   */
  public abstract boolean apply(
      final ModelerWorkspace workspace, final IMetaStore metaStore ) throws ModelerException;

  /**
   * Applies modeling change on a Mondrian schema
   *
   * @param schema
   * @throws ModelerException
   */
  public abstract boolean apply( final Document schema ) throws ModelerException;

  public abstract void validate() throws ModelerException;

  public abstract ModelAnnotation.Type getType();

  public abstract String getSummary();

  public abstract String getName();

  public abstract String getField();

  public boolean equalsLogically( AnnotationType obj ) {

    if ( obj == null || obj.getClass() != getClass() ) {
      return false;
    }

    EqualsBuilder eq = new EqualsBuilder();

    // by default just see if the name is the same
    String thatName = obj.getName() == null ? null : obj.getName().toLowerCase();
    String myName = getName() == null ? null : getName().toLowerCase();
    eq.append( myName, thatName );

    return eq.isEquals();

  }

}
