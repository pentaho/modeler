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


package org.pentaho.agilebi.modeler.strategy;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;

/**
 * Created: 4/19/11
 * 
 * @author rfellows
 */
public class MultiTableAutoModelStrategy extends SimpleAutoModelStrategy {
  public MultiTableAutoModelStrategy( String locale ) {
    super( locale );
  }

  @Override
  public void autoModelOlap( ModelerWorkspace workspace, MainModelNode mainModel ) throws ModelerException {
    throw new UnsupportedOperationException( "This strategy does not support OLAP models" );
  }

}
