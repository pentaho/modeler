package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.metadata.model.olap.OlapAnnotation;

/**
 * User: nbaker
 * Date: 10/20/11
 */
public interface IAnnotationFactory {
  IMemberAnnotation create(OlapAnnotation anno);
}
