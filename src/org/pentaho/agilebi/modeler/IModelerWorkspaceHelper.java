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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.strategy.AutoModelStrategy;

import java.util.List;

/**
 * User: nbaker Date: Jul 14, 2010
 */
public interface IModelerWorkspaceHelper {
  void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException;

  void autoModelFlatInBackground( ModelerWorkspace workspace ) throws ModelerException;

  void autoModelRelationalFlat( ModelerWorkspace workspace ) throws ModelerException;

  void autoModelRelationalFlatInBackground( ModelerWorkspace workspace ) throws ModelerException;

  void sortFields( List<AvailableField> availableFields );

  void populateDomain( ModelerWorkspace model ) throws ModelerException;

  String getLocale();

  public AutoModelStrategy getAutoModelStrategy();

  public void setAutoModelStrategy( AutoModelStrategy autoModelStrategy );

}
