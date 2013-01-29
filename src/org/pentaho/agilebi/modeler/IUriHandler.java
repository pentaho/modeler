package org.pentaho.agilebi.modeler;


/**
 * An interface for objects that can open a url. 
 * This is useful for popping up (infocenter) help pages.
 * @author rbouman
 * 
 */
public interface IUriHandler {

  public void openUri(String uri);
  
}
