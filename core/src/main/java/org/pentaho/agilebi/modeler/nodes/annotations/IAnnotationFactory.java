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

import org.pentaho.metadata.model.olap.OlapAnnotation;

/**
 * Implementations can retore Modeler Annotations from the saved Metadata model objects
 * 
 * User: nbaker Date: 10/20/11
 */
public interface IAnnotationFactory {
  IMemberAnnotation create( OlapAnnotation anno );
}
