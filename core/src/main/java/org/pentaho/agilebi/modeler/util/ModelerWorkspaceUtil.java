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

package org.pentaho.agilebi.modeler.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.IncompatibleModelerException;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for generating ModelerModels for the User Interface.
 * 
 * @author nbaker
 * 
 */
public class ModelerWorkspaceUtil {

  private static Logger logger = LoggerFactory.getLogger( ModelerWorkspaceUtil.class );

  public static ModelerWorkspace populateModelFromSource( ModelerWorkspace model, IModelerSource source )
    throws ModelerException {
    Domain d = source.generateDomain();

    model.setModelSource( source );
    model.setModelName( source.getTableName() );
    model.setDomain( d );

    return model;
  }

  protected static void save( String content, String fileName ) throws IOException {
    File file = new File( fileName );
    OutputStream out = new FileOutputStream( file );
    out.write( content.getBytes( "UTF-8" ) );
    out.flush();
    out.close();
  }

  public static void saveWorkspace( ModelerWorkspace aModel, String fileName ) throws ModelerException {
    try {

      String xmi = getMetadataXML( aModel );

      // write the XMI to a tmp file
      // models was created earlier.
      try {
        save( xmi, fileName );
      } catch ( IOException e ) {
        logger.info( BaseMessages.getString( ModelerWorkspace.class,
            "ModelerWorkspaceUtil.Populate.BadGenerateMetadata" ), e ); //$NON-NLS-1$
        throw new ModelerException( BaseMessages.getString( ModelerWorkspace.class,
            "ModelerWorkspaceUtil.Populate.BadGenerateMetadata" ), e ); //$NON-NLS-1$
      }

    } catch ( Exception e ) {
      logger.error( "error", e );
      throw new ModelerException( e );
    }
  }

  public static void saveWorkspaceAsMondrianSchema( ModelerWorkspace aModel, String fileName, String locale )
    throws ModelerException {
    try {

      String xml = getMondrianSchemaXml( aModel, locale );
      if ( xml == null ) {
        return;
      }
      // write the XMI to a tmp file
      // models was created earlier.
      try {
        save( xml, fileName );
      } catch ( IOException e ) {
        logger.info( BaseMessages.getString( ModelerWorkspace.class,
            "ModelerWorkspaceUtil.Populate.BadGenerateMetadata" ), e ); //$NON-NLS-1$
        throw new ModelerException( BaseMessages.getString( ModelerWorkspace.class,
            "ModelerWorkspaceUtil.Populate.BadGenerateMetadata" ), e ); //$NON-NLS-1$
      }

    } catch ( Exception e ) {
      logger.error( "error", e );
      throw new ModelerException( e );
    }
  }

  public static String getMetadataXML( ModelerWorkspace aModel ) throws ModelerException {
    aModel.getWorkspaceHelper().populateDomain( aModel );
    XmiParser parser = new XmiParser();
    return parser.generateXmi( aModel.getDomain() );
  }

  public static String getMondrianSchemaXml( ModelerWorkspace modelerWorkspace, String locale ) throws Exception {
    modelerWorkspace.getWorkspaceHelper().populateDomain( modelerWorkspace );
    LogicalModel logicalModel = modelerWorkspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( logicalModel == null ) {
      return null;
    }
    MondrianModelExporter exporter = new MondrianModelExporter( logicalModel, locale );
    return exporter.createMondrianModelXML();
  }

  public static void loadWorkspace( String fileName, String aXml, ModelerWorkspace aModel ) throws ModelerException {

    try {
      XmiParser parser = new XmiParser();
      Domain domain = parser.parseXmi( new ByteArrayInputStream( aXml.getBytes( "UTF-8" ) ) );

      LogicalModel logical = domain.getLogicalModels().get( 0 );

      Object agileBiProp = logical.getProperty( "AGILE_BI_GENERATED_SCHEMA" );
      if ( agileBiProp == null || "FALSE".equals( agileBiProp ) ) {
        throw new IncompatibleModelerException();
      }

      // re-hydrate the source
      Object property = logical.getProperty( "source_type" ); //$NON-NLS-1$
      if ( property != null ) {
        IModelerSource theSource = ModelerSourceFactory.generateSource( property.toString() );
        theSource.initialize( domain );
        aModel.setModelSource( theSource );
      }

      aModel.setDomain( domain );
      aModel.setFileName( fileName );
      aModel.resolveConnectionFromDomain();
      // aModel.refresh(ModelerMode.ANALYSIS_AND_REPORTING);
      aModel.getWorkspaceHelper().populateDomain( aModel );
      aModel.setDirty( false );
    } catch ( Exception e ) {
      logger.error( "error", e );
      if ( e instanceof ModelerException ) {
        throw (ModelerException) e;
      }
      throw new ModelerException( BaseMessages.getString( ModelerWorkspace.class,
          "ModelerWorkspaceUtil.LoadWorkspace.Failed" ), e ); //$NON-NLS-1$
    }
  }
}
