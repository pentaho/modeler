package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.beanutils.PropertyUtils;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */

@MetaStoreElementType( name = "SharedDimensionGroup", description = "Shared Dimension Group" )
public class SharedDimensionGroup extends ModelAnnotationGroup {

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

  public SharedDimensionGroup() {
  }

  public SharedDimensionGroup( ModelAnnotationGroup modelAnnotationGroup ) {
    super();
    try {
      PropertyUtils.copyProperties( this, modelAnnotationGroup );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId( String id ) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription( String description ) {
    this.description = description;
  }

  @Override
  public boolean isSharedDimension() {
    return sharedDimension;
  }

  @Override
  public void setSharedDimension( boolean sharedDimension ) {
    this.sharedDimension = sharedDimension;
  }

  @Override
  public List<DataProvider> getDataProviders() {
    return dataProviders;
  }

  @Override
  public void setDataProviders( List<DataProvider> dataProviders ) {
    this.dataProviders = dataProviders;
  }

  @Override
  public List<ModelAnnotation> getModelAnnotations() {
    return this;
  }

  @Override
  public void setModelAnnotations( List<ModelAnnotation> modelAnnotations ) {
    removeRange( 0, this.size() ); // remove all
    if ( modelAnnotations != null ) {
      addAll( modelAnnotations );
    }
  }
}
