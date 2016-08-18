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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import static org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup.ApplyStatus.*;

@MetaStoreElementType( name = "ModelAnnotationGroup", description = "ModelAnnotationGroup" )
public class ModelAnnotationGroup extends ArrayList<ModelAnnotation> {

  @MetaStoreAttribute
  private String id;

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @MetaStoreAttribute
  private boolean sharedDimension;

  @MetaStoreAttribute
  private List<DataProvider> dataProviders = new ArrayList<DataProvider>();

  @MetaStoreAttribute
  private List<ModelAnnotation> modelAnnotations; // indicate to metastore to persist items (calls the getter/setter)

  public ModelAnnotationGroup() {
    super();
  }

  public ModelAnnotationGroup( ModelAnnotation... modelAnnotations ) {
    super( Arrays.asList( modelAnnotations ) );
  }

  public String getId() {
    return id;
  }

  public void setId( final String id ) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<ModelAnnotation> getModelAnnotations() {
    return this;
  }

  public void setModelAnnotations( List<ModelAnnotation> modelAnnotations ) {
    removeRange( 0, this.size() ); // remove all
    if ( modelAnnotations != null ) {
      addAll( modelAnnotations );
    }
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public boolean isSharedDimension() {
    return sharedDimension;
  }

  public void setSharedDimension( boolean sharedDimension ) {
    this.sharedDimension = sharedDimension;
  }

  public List<DataProvider> getDataProviders() {
    return dataProviders;
  }

  public void setDataProviders( List<DataProvider> dataProviders ) {
    this.dataProviders = dataProviders;
  }

  @Override
  public boolean equals( Object obj ) {

    try {
      if ( !EqualsBuilder.reflectionEquals( this, obj ) ) {
        return false;
      }

      // manually check annotations
      ModelAnnotationGroup objGroup = (ModelAnnotationGroup) obj;
      if ( this.size() != objGroup.size() ) {
        return false;
      }

      for ( int i = 0; i < this.size(); i++ ) {
        if ( !this.get( i ).equals( objGroup.get( i ) ) ) {
          return false;
        }
      }

      return true;
    } catch ( Exception e ) {
      return false;
    }
  }

  public enum ApplyStatus {
    SUCCESS,
    FAILED,
    NULL_ANNOTATION
  }

  private interface AnnotateStrategy {
    boolean apply( ModelAnnotation modelAnnotation ) throws ModelerException;

    Map<ApplyStatus, List<ModelAnnotation>> applyAll( ModelAnnotationGroup modelAnnotations ) throws ModelerException;

    boolean isEmptyModel();
  }

  public Map<ApplyStatus, List<ModelAnnotation>> applyAnnotations( final Document mondrianSchema )
    throws ModelerException {
    return applyAnnotations( mondrianSchema, this );
  }

  private Map<ApplyStatus, List<ModelAnnotation>> applyAnnotations(
      final Document mondrianSchema, final ModelAnnotationGroup toApply ) throws ModelerException {
    AnnotateStrategy strategy = new AnnotateStrategy() {

      @Override public boolean apply( final ModelAnnotation modelAnnotation ) throws ModelerException {
        return modelAnnotation.apply( mondrianSchema );
      }

      @Override public Map<ApplyStatus, List<ModelAnnotation>> applyAll(
        final ModelAnnotationGroup modelAnnotations ) throws ModelerException {
        return applyAnnotations( mondrianSchema, modelAnnotations );
      }

      @Override public boolean isEmptyModel() {
        return !mondrianSchema.hasChildNodes();
      }
    };
    return applyAnnotations( strategy, toApply );
  }

  public Map<ApplyStatus, List<ModelAnnotation>> applyAnnotations(
      final ModelerWorkspace model, final IMetaStore metaStore )
      throws ModelerException {
    return applyAnnotations( model, metaStore, this );
  }

  private Map<ApplyStatus, List<ModelAnnotation>> applyAnnotations(
      final ModelerWorkspace model, final IMetaStore metaStore, final ModelAnnotationGroup toApply )
      throws ModelerException {
    AnnotateStrategy strategy = new AnnotateStrategy() {

      @Override public boolean apply( final ModelAnnotation modelAnnotation ) throws ModelerException {
        return modelAnnotation.apply( model, metaStore );
      }

      @Override public Map<ApplyStatus, List<ModelAnnotation>> applyAll(
        final ModelAnnotationGroup modelAnnotations ) throws ModelerException {
        return applyAnnotations( model, metaStore, modelAnnotations );
      }

      @Override public boolean isEmptyModel() {
        return model.getModel().getDimensions().size() == 0 && model.getModel().getMeasures().size() == 0;
      }
    };
    return applyAnnotations( strategy, toApply );
  }

  private Map<ApplyStatus, List<ModelAnnotation>> applyAnnotations( AnnotateStrategy strategy,
                                                                    final ModelAnnotationGroup toApply )
    throws ModelerException {
    if ( strategy.isEmptyModel() ) {
      //the model is empty so there is no use trying to apply annotations.
      //this usually happens when there is no data.
      return Collections.emptyMap();
    }
    Map<ApplyStatus, List<ModelAnnotation>> statusMap = initStatusMap();
    ModelAnnotationGroup failedAnnotations = new ModelAnnotationGroup();
    for ( ModelAnnotation modelAnnotation : toApply ) {
      if ( modelAnnotation.getAnnotation() == null ) {
        statusMap.get( ApplyStatus.NULL_ANNOTATION ).add( modelAnnotation );
        continue;
      }
      boolean applied = strategy.apply( modelAnnotation );
      if ( applied ) {
        statusMap.get( ApplyStatus.SUCCESS ).add( modelAnnotation );
      } else {
        failedAnnotations.add( modelAnnotation );
      }
    }
    if ( failedAnnotations.size() < toApply.size() ) {
      Map<ApplyStatus, List<ModelAnnotation>> recurStatusMap = strategy.applyAll( failedAnnotations );
      for ( ApplyStatus applyStatus : ApplyStatus.values() ) {
        statusMap.get( applyStatus ).addAll( recurStatusMap.get( applyStatus ) );
      }
    } else if ( failedAnnotations.size() > 0 ) {
      for ( ModelAnnotation failedAnnotation : failedAnnotations ) {
        statusMap.get( ApplyStatus.FAILED ).add( failedAnnotation );
      }
    }
    return statusMap;
  }

  private Map<ApplyStatus, List<ModelAnnotation>> initStatusMap() {
    HashMap<ApplyStatus, List<ModelAnnotation>> statusMap = new HashMap<ApplyStatus, List<ModelAnnotation>>();
    for ( ApplyStatus applyStatus : ApplyStatus.values() ) {
      statusMap.put( applyStatus, new ArrayList<ModelAnnotation>() );
    }
    return statusMap;
  }

  public void addInjectedAnnotations( List<? extends AnnotationType> annotations ) {

    for ( AnnotationType annotationType : annotations ) {
      ModelAnnotation existingAnnotation = findExistingAnnotation( annotationType );
      ModelAnnotation ma = existingAnnotation == null ? new ModelAnnotation() : existingAnnotation;

      ma.setName( annotationType.getName() );

      if ( existingAnnotation == null ) {
        ma.setAnnotation( annotationType );

        add( ma );
      } else {
        // set each of the specific values that are injected onto the existing annotation
        List<ModelProperty> modelProperties = annotationType.getModelProperties();
        modelProperties.stream().forEach( modelProperty -> {
          try {
            Object value = annotationType.getModelPropertyValueById( modelProperty.id() );
            if ( value != null ) {
              ma.getAnnotation().setModelPropertyValueById( modelProperty.id(), value );
            }
          } catch ( Exception e ) {
            // this shouldn't happen since we are iterating over the properties
          }
        } );

      }
    }
  }

  protected ModelAnnotation findExistingAnnotation( AnnotationType annotation ) {
    List<ModelAnnotation> matches = stream()
      .filter( ma -> ma.getAnnotation().equalsLogically( annotation ) )
      .collect( Collectors.toList() );

    return CollectionUtils.isNotEmpty( matches ) ? matches.get( 0 ) : null;
  }
}
