/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.IModelerSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ModelerSourceFactory {

  private static Map<String, Class<? extends IModelerSource>> outputSources =
      new HashMap<String, Class<? extends IModelerSource>>();
  static {
    outputSources.put( TableModelerSource.SOURCE_TYPE, TableModelerSource.class );
  }

  private static Logger logger = LoggerFactory.getLogger( ModelerSourceFactory.class );

  public static void registerSourceType( String id, Class<? extends IModelerSource> clazz ) {
    outputSources.put( id, clazz );
  }

  public static IModelerSource generateSource( String type ) {
    Class<? extends IModelerSource> clz = outputSources.get( type );
    if ( clz == null ) {
      throw new IllegalArgumentException( "Cannot find IModelerSoruce for type: " + type );
    }
    IModelerSource instance = null;
    try {
      instance = clz.newInstance();
    } catch ( InstantiationException e ) {
      logger.error( "Error generating modeler source", e );
    } catch ( IllegalAccessException e ) {
      logger.error( "Error generating modeler source", e );
    }
    return instance;
  }
}
