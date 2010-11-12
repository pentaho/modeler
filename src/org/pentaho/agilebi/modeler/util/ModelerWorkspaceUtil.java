package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.IncompatibleModelerException;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.XmiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Utility class for generating ModelerModels for the User Interface.
 *
 * @author nbaker
 *
 */
public class ModelerWorkspaceUtil {

  private static Logger logger = LoggerFactory.getLogger(ModelerWorkspaceUtil.class);


  public static ModelerWorkspace populateModelFromSource( ModelerWorkspace model, IModelerSource source) throws
      ModelerException {
    Domain d = source.generateDomain();

    model.setModelSource(source);
    model.setModelName(source.getTableName());
    model.setDomain(d);

    return model;
  }

  private static String MODELER_NAME = "OutputStepModeler"; //$NON-NLS-1$

  public static void saveWorkspace(ModelerWorkspace aModel, String fileName) throws ModelerException {
  	try {

	    String xmi = getMetadataXML(aModel);

	    // write the XMI to a tmp file
	    // models was created earlier.
	    try{

	      File file = new File(fileName);
	      OutputStream out = new FileOutputStream(file);
	      out.write(xmi.getBytes("UTF-8"));
	      out.flush();
	      out.close();

	    } catch(IOException e){
	      logger.info(BaseMessages.getString(ModelerWorkspace.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	      throw new ModelerException(BaseMessages.getString(ModelerWorkspace.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	    }

  	} catch (Exception e) {
  		logger.error("error", e);
  		throw new ModelerException(e);
  	}
  }

  public static String getMetadataXML(ModelerWorkspace aModel) throws ModelerException {
    aModel.getWorkspaceHelper().populateDomain(aModel);
    XmiParser parser = new XmiParser();
    return parser.generateXmi(aModel.getDomain());

  }

  public static void loadWorkspace(String fileName, String aXml, ModelerWorkspace aModel) throws ModelerException {

    try{
      XmiParser parser = new XmiParser();
      Domain domain = parser.parseXmi(new ByteArrayInputStream(aXml.getBytes("UTF-8")));

      LogicalModel logical = domain.getLogicalModels().get(0);

      Object agileBiProp = logical.getProperty("AGILE_BI_GENERATED_SCHEMA");
      if(agileBiProp == null || "FALSE".equals(agileBiProp)){
        throw new IncompatibleModelerException();
      }

      // re-hydrate the source
      Object property = logical.getProperty("source_type"); //$NON-NLS-1$
      if( property != null ) {
        IModelerSource theSource = ModelerSourceFactory.generateSource(property.toString());
        theSource.initialize(domain);
        aModel.setModelSource(theSource);
      }

    	aModel.setDomain(domain);
    	aModel.setFileName(fileName);
    	aModel.resolveConnectionFromDomain();
    	aModel.refresh();
        aModel.getWorkspaceHelper().populateDomain(aModel);
    	aModel.setDirty(false);
    } catch (Exception e){
      logger.error("error", e);
      if(e instanceof ModelerException){
        throw (ModelerException) e;
      }
      throw new ModelerException(BaseMessages.getString(ModelerWorkspace.class, "ModelerWorkspaceUtil.LoadWorkspace.Failed"),e); //$NON-NLS-1$
    }
  }
}
