/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;
import org.w3c.dom.Document;

import java.io.InputStream;

public class ModelingSchemaAnnotator implements MondrianSchemaAnnotator {
  @Override public InputStream getInputStream(
      final InputStream schemaInputStream, final InputStream annotationsInputStream ) {
    ModelAnnotationGroupXmlReader reader = new ModelAnnotationGroupXmlReader();
    try {
      Document annotationsDoc = XMLHandler.loadXMLFile( annotationsInputStream );
      Document schemaDoc = XMLHandler.loadXMLFile( schemaInputStream );
      ModelAnnotationGroup modelAnnotations = reader.readModelAnnotationGroup( annotationsDoc );
      modelAnnotations.applyAnnotations( schemaDoc );
      return IOUtils.toInputStream( XMLHandler.formatNode( schemaDoc ) );
    } catch ( Exception e ) {
      throw new RepositoryException( e );
    }
  }
}
