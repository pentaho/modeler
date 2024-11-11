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
