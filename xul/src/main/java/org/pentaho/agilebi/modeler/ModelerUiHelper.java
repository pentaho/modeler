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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
