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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.propforms.AbstractModelerNodeForm;
import org.pentaho.agilebi.modeler.propforms.CategoryPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.DimensionPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.FieldsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.HierarchyPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.LevelsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.MainModelerNodePropertiesForm;
import org.pentaho.agilebi.modeler.propforms.MeasuresPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.MemberPropertyPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.RelationalModelNodePropertiesForm;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.BindingFactory;

/**
 * Created: 4/25/11
 * 
 * @author rfellows
 */
public class ModelerUiHelper {

  public static void configureControllers( XulDomContainer container, ModelerWorkspace workspace, BindingFactory bf,
      ModelerController controller, ColResolverController colController ) {
    bf.setDocument( container.getDocumentRoot() );
    container.addEventHandler( controller );
    controller.setBindingFactory( bf );
    controller.setWorkspaceHelper( workspace.getWorkspaceHelper() );

    // Arrays.asList isn't working properly in GWT, manually adding them one at at time
    List<AbstractModelerNodeForm<? extends AbstractMetaDataModelNode>> forms =
        new ArrayList<AbstractModelerNodeForm<? extends AbstractMetaDataModelNode>>();
    forms.add( new MeasuresPropertiesForm( LocalizedString.DEFAULT_LOCALE ) );
    forms.add( new DimensionPropertiesForm() );
    forms.add( new LevelsPropertiesForm( LocalizedString.DEFAULT_LOCALE ) );
    forms.add( new MemberPropertyPropertiesForm( LocalizedString.DEFAULT_LOCALE ) );
    forms.add( new HierarchyPropertiesForm() );
    forms.add( new MainModelerNodePropertiesForm() );
    forms.add( new GenericPropertiesForm() );
    forms.add( new CategoryPropertiesForm() );
    forms.add( new FieldsPropertiesForm( LocalizedString.DEFAULT_LOCALE ) );
    forms.add( new RelationalModelNodePropertiesForm() );

    for ( AbstractModelerNodeForm<? extends AbstractMetaDataModelNode> propController : forms ) {
      container.addEventHandler( propController );
      controller.addPropertyForm( propController );
      propController.setBindingFactory( bf );
      propController.init( workspace );
    }

    colController.setBindingFactory( bf );
    container.addEventHandler( colController );
    controller.setColResolver( colController );
    colController.init();
  }

}
