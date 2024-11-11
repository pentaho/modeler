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

package org.pentaho.agilebi.modeler.models.annotations;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.IMetaStoreObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This object factory doesn't really do anything special. It is required to get around some classloading issues in PDI
 *
 * @author Rowell Belen
 */
public class ModelAnnotationObjectFactory implements IMetaStoreObjectFactory {

  @Override
  public Object instantiateClass( String s, Map<String, String> map ) throws MetaStoreException {
    try {
      return Class.forName( s ).newInstance();
    } catch ( Exception e ) {
      throw new MetaStoreException( e );
    }
  }

  @Override
  public Map<String, String> getContext( Object object ) throws MetaStoreException {
    return new HashMap<String, String>();
  }

}
