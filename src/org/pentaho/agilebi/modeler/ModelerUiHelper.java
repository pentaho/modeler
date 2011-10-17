/*
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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.propforms.*;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.BindingFactory;

/**
 * Created: 4/25/11
 *
 * @author rfellows
 */
public class ModelerUiHelper {

  public static void configureControllers(XulDomContainer container, ModelerWorkspace workspace, BindingFactory bf, ModelerController controller, ColResolverController colController) {
    bf.setDocument(container.getDocumentRoot());
    container.addEventHandler(controller);
    controller.setBindingFactory(bf);
    controller.setWorkspaceHelper(workspace.getWorkspaceHelper());

    AbstractModelerNodeForm propController = new MeasuresPropertiesForm(LocalizedString.DEFAULT_LOCALE);
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new DimensionPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new LevelsPropertiesForm(LocalizedString.DEFAULT_LOCALE );
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new MemberPropertyPropertiesForm(LocalizedString.DEFAULT_LOCALE );
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new HierarchyPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new MainModelerNodePropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();


    propController = new GenericPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new CategoryPropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new FieldsPropertiesForm(LocalizedString.DEFAULT_LOCALE);
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    propController = new RelationalModelNodePropertiesForm();
    container.addEventHandler(propController);
    controller.addPropertyForm(propController);
    propController.setBindingFactory(bf);
    propController.init();

    colController.setBindingFactory(bf);
    container.addEventHandler(colController);
    controller.setColResolver(colController);
    colController.init();
  }

}
