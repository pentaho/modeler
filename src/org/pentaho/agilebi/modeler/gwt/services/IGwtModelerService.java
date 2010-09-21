package org.pentaho.agilebi.modeler.gwt.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

/**
 * User: nbaker
 * Date: Jun 18, 2010
 */
public interface IGwtModelerService extends RemoteService, IModelerService {

}
