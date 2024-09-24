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

package org.pentaho.agilebi.modeler;

import org.pentaho.metadata.model.Domain;

/**
 * Provides a ModelerModel with information needed to generate the UI and well as the final Mondrian and Metadata
 * models. All information from the context in which the modeling is initiated should be contained within implementors
 * of this interface.
 * 
 * @author nbaker
 */
public interface IModelerSource {

  public Domain generateDomain() throws ModelerException;

  public Domain generateDomain( boolean dualModelingMode ) throws ModelerException;

  public String getDatabaseName();

  public void initialize( Domain domain ) throws ModelerException;

  public void serializeIntoDomain( Domain d );

  public String getSchemaName();

  public String getTableName();

}
