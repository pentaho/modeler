package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;

import java.util.List;

/**
 * User: nbaker
 * Date: 10/17/11
 */
public interface IMemberAnnotation {
  String getName();
  void saveAnnotations(Object obj);
  boolean isValid(AbstractMetaDataModelNode node);
  List<String> getValidationMessages(AbstractMetaDataModelNode node);

}
