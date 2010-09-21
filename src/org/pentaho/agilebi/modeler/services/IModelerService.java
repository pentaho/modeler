package org.pentaho.agilebi.modeler.services;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

import java.util.List;

/**
 * User: nbaker
 * Date: Jun 18, 2010
 */
public interface IModelerService {
  Domain generateDomain(String connectionName, String tableName, String dbType, String query, String datasourceName) throws Exception;
  BogoPojo gwtWorkaround ( BogoPojo pojo);
  String serializeModels(Domain domain, String name) throws Exception;
  Domain loadDomain(String id) throws Exception;
}
