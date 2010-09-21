package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.AvailableField;

import java.util.List;

/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public interface IModelerWorkspaceHelper {
  void autoModelFlat(ModelerWorkspace workspace) throws ModelerException;
  void autoModelFlatInBackground(ModelerWorkspace workspace) throws ModelerException;
  void sortFields( List<AvailableField> availableFields);
  void populateDomain(ModelerWorkspace model) throws ModelerException;
}
