package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.metadata.model.olap.OlapAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * This factory is used to rehydrate annotations from saved state. IAnotation factories are registered for annotation
 * keys (Geo.Role, Data.Role, etc) and are called on to recreate the Modeler annotation objects from Metadata versions.
 *
 * User: nbaker
 * Date: 10/20/11
 */
public class MemberAnnotationFactory {
  private static Map<String, IAnnotationFactory> factories = new HashMap<String, IAnnotationFactory>();

  public static void registerFactory(String type, IAnnotationFactory factory){
    factories.put(type, factory);
  }

  public static IMemberAnnotation create(OlapAnnotation anno){
    if(anno == null){
      throw new IllegalArgumentException("Annotation is null");
    }
    IAnnotationFactory fact = factories.get(anno.getName());
    if(fact != null){
      return fact.create(anno);
    }
    return null;
  }

}
