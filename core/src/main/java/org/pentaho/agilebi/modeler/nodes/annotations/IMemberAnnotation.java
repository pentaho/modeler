/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;

import java.util.List;

/**
 * Member Annotations are name/value contructs stored in the OLAP model. They're responsible for serializing themselves
 * in the model and returning validation state and messages.
 * 
 * User: nbaker Date: 10/17/11
 */
public interface IMemberAnnotation {
  String getName();

  void saveAnnotations( OlapHierarchyLevel obj );

  boolean isValid( AbstractMetaDataModelNode node );

  List<String> getValidationMessages( AbstractMetaDataModelNode node );

  void onAttach( AbstractMetaDataModelNode node );

  void onDetach( AbstractMetaDataModelNode node );

}
