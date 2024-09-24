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

package org.pentaho.agilebi.modeler.strategy;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;

/**
 * Created: 4/19/11
 * 
 * @author rfellows
 */
public interface AutoModelStrategy {

  void autoModelOlap( ModelerWorkspace workspace, MainModelNode mainModel ) throws ModelerException;

  void autoModelRelational( ModelerWorkspace workspace, RelationalModelNode relationalModelNode )
    throws ModelerException;

}
