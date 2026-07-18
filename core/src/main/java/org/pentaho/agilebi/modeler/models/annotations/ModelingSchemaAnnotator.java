/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


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
