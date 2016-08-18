/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
