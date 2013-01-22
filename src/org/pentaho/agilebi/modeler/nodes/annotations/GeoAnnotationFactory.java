package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.geo.LocationRole;
import org.pentaho.metadata.model.olap.OlapAnnotation;

/**
 * This factory is used to rehydrate annotations for the Modeler model form a saved state.
 * It takes in an OlapAnnotation from the saved model and returns the higher-level corresponding
 * Object
 *
 * User: nbaker
 * Date: 10/20/11
 */
public class GeoAnnotationFactory implements IAnnotationFactory {
  private GeoContext context;

  public GeoAnnotationFactory(GeoContext context){
    this.context = context;
  }
  @Override
  public IMemberAnnotation create(OlapAnnotation anno) {
    if(anno.getName().equals(GeoContext.ANNOTATION_GEO_ROLE)){
      return context.getGeoRoleByName(anno.getValue());
    } else if(anno.getName().equals(GeoContext.ANNOTATION_DATA_ROLE)){
      //return markers for data types
      if(anno.getValue().equals("LOCATION_ROLE")){
        return new LocationRole();
      } else {
        return new GeoRole();
      }
    }
    return null;
  }
}
