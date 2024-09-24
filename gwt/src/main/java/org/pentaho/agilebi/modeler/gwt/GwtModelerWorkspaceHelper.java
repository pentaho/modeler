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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.gwt;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;

/**
 * User: nbaker Date: Jul 14, 2010
 */
public class GwtModelerWorkspaceHelper extends BaseModelerWorkspaceHelper {

  public static final String DEFAULT_LOCALE = "en_US";

  public GwtModelerWorkspaceHelper() {
    super( DEFAULT_LOCALE );
    BogoPojo bogo = new BogoPojo();
  }

  @Override
  protected MainModelNode getMainModelNode( ModelerWorkspace workspace ) {
    MainModelNode mainModel = null;
    if ( workspace.getModel() == null ) {
      mainModel = new MainModelNode();
      mainModel.setWorkspace( workspace );
    } else {
      workspace.getModel().getMeasures().clear();
      workspace.getModel().getDimensions().clear();
      mainModel = workspace.getModel();
    }
    return mainModel;
  }

  @Override
  protected RelationalModelNode getRelationalModelNode( ModelerWorkspace workspace ) {
    RelationalModelNode relationalModel = null;
    if ( workspace.getRelationalModel() == null ) {
      relationalModel = new RelationalModelNode();
      relationalModel.setWorkspace( workspace );
    } else {
      workspace.getRelationalModel().getCategories().clear();
      relationalModel = workspace.getRelationalModel();
    }
    return relationalModel;
  }

}
