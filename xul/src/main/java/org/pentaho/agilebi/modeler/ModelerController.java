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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableItemCollection;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.IAvailableItem;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.propforms.AbstractModelerNodeForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.components.XulTabpanel;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * XUL Event Handler for the modeling interface. This class interacts with a ModelerModel to store state.
 * 
 * @author nbaker
 */
public class ModelerController extends AbstractXulEventHandler {

  protected static final String FIELD_LIST_ID = "fieldList"; //$NON-NLS-1$

  protected static final String SOURCE_NAME_LABEL_ID = "source_name"; //$NON-NLS-1$
  protected static final String RELATIONAL_NAME_LABEL_ID = "relational_source_name";

  // private static Logger logger = LoggerFactory.getLogger(ModelerController.class);

  protected ModelerWorkspace workspace;

  private IUriHandler uriHandler;
  private XulTree dimensionTree;
  private XulTree categoriesTree;
  private XulDeck propDeck;
  private IAvailableItem[] selectedFields = new IAvailableItem[] {};

  private XulDeck modelDeck;
  private XulVbox reportingPanel;
  private XulTabpanel reportingTabPanel;
  private XulHbox modelPanel;

  protected BindingFactory bf;

  private Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms =
      new HashMap<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm>();

  private ColResolverController colController;

  private transient DimensionTreeHelper dimTreeHelper;
  private transient CategoryTreeHelper catTreeHelper;

  // private IModelerWorkspaceHelper workspaceHelper;
  private XulTabbox modelTabbox;

  // private transient ModelerMode currentModellingMode = ModelerMode.ANALYSIS_AND_REPORTING;
  // private transient ModelerPerspective currentModelerPerspective = ModelerPerspective.ANALYSIS;

  // protected IModelerMessages messages;
  //
  // // TODO: Nodes are referencing the main bundle. Static location seems natural, evaluate better place.
  // public static IModelerMessages MESSAGES;

  public ModelerController( ModelerWorkspace workspace ) {
    this.workspace = workspace;
  }

  public void setUriHandler( IUriHandler uriHandler ) {
    this.uriHandler = uriHandler;
  }

  public String getName() {
    return "modeler"; //$NON-NLS-1$
  }

  public void onFieldListDrag( DropEvent event ) {
    // nothing to do here
  }

  public void onModelTreeDrag( DropEvent event ) {
    // todo, disable dragging of Root elements once we've updated the tree UI
  }

  @Bindable
  public void checkDropLocation( DropEvent event ) {
    List<Object> data = event.getDataTransfer().getData();
    for ( Object obj : data ) {
      if ( obj instanceof AbstractMetaDataModelNode && event.getDropParent() != null ) {
        event.setAccepted( ( (AbstractMetaDataModelNode) event.getDropParent() ).acceptsDrop( obj ) );
      }
    }
  }

  @Bindable
  public void onModelTreeDrop( DropEvent event ) {
    try {
      if ( getModelerPerspective() == ModelerPerspective.ANALYSIS ) {
        dimTreeHelper.onModelDrop( event );
      } else {
        catTreeHelper.onModelDrop( event );
      }
    } catch ( Exception e ) {
      event.setAccepted( false );

      try {
        XulMessageBox msg = null; //$NON-NLS-1$
        msg = (XulMessageBox) document.createElement( "messagebox" );
        msg.setTitle( ModelerMessagesHolder.getMessages().getString( "invalid_model" ) ); //$NON-NLS-1$
        msg.setMessage( e.getCause().getMessage() );
        msg.open();
      } catch ( Exception e1 ) {
        e1.printStackTrace();
      }
    }
  }

  @Bindable
  public void addField() {
    try {
      if ( getModelerPerspective() == ModelerPerspective.ANALYSIS ) {
        dimTreeHelper.addField( getSelectedFields() );
      } else {
        catTreeHelper.addField( getSelectedFields() );
      }
    } catch ( Exception e ) {
      try {
        XulMessageBox msg = null; //$NON-NLS-1$
        msg = (XulMessageBox) document.createElement( "messagebox" );
        msg.setTitle( ModelerMessagesHolder.getMessages().getString( "invalid_model" ) ); //$NON-NLS-1$
        msg.setMessage( e.getCause().getMessage() );
        msg.open();
      } catch ( Exception e1 ) {
        e1.printStackTrace();
      }
    }
  }

  @Bindable
  public void setSelectedFieldsChanged( Object selected ) {
    if ( selected != null && selected instanceof IAvailableItem ) {
      selectedFields = new IAvailableItem[] { (IAvailableItem) selected };
    }
  }

  @Bindable
  public void init() throws ModelerException {

    bf.setDocument( document );
    propDeck = (XulDeck) document.getElementById( "propertiesdeck" ); //$NON-NLS-1$

    dimTreeHelper = new DimensionTreeHelper( propertiesForms, propDeck, workspace, document );
    catTreeHelper = new CategoryTreeHelper( propertiesForms, propDeck, workspace, document );

    dimensionTree = (XulTree) document.getElementById( "dimensionTree" ); //$NON-NLS-1$
    categoriesTree = (XulTree) document.getElementById( "categoriesTree" ); //$NON-NLS-1$
    modelDeck = (XulDeck) document.getElementById( "modelDeck" ); //$NON-NLS-1$
    reportingPanel = (XulVbox) document.getElementById( "reportingModelPanel" );
    reportingTabPanel = (XulTabpanel) document.getElementById( "reportingTabPanel" );
    modelPanel = (XulHbox) document.getElementById( "modelPanel" );
    modelTabbox = (XulTabbox) document.getElementById( "modelTabbox" );

    XulTree fieldListTree = (XulTree) document.getElementById( FIELD_LIST_ID );

    bf.setBindingType( Type.ONE_WAY );
    fieldListBinding = bf.createBinding( workspace.getAvailableTables(), "children", FIELD_LIST_ID, "elements" ); //$NON-NLS-1$ //$NON-NLS-2$
    selectedFieldsBinding = bf.createBinding( FIELD_LIST_ID, "selectedItem", this, "selectedFieldsChanged" ); //$NON-NLS-1$//$NON-NLS-2$

    bf.createBinding( FIELD_LIST_ID, "selectedItem", workspace, "selectedAvailableItem" );

    bf.createBinding( workspace, "currentModelerPerspective", workspace, "currentModelerTreeHelper",
        new BindingConvertor<ModelerPerspective, ModelerTreeHelper>() {

          @Override
          public ModelerTreeHelper sourceToTarget( ModelerPerspective modelerPerspective ) {
            switch ( modelerPerspective ) {
              case ANALYSIS:
                return dimTreeHelper;
              case REPORTING:
                return catTreeHelper;
              default:
                return dimTreeHelper;
            }
          }

          @Override
          public ModelerPerspective targetToSource( ModelerTreeHelper modelerTreeHelper ) {
            return null; // To change body of implemented methods use File | Settings | File Templates.
          }
        } );

    // for the firing of the modelerperspective
    workspace.setCurrentModelerPerspective( workspace.getCurrentModelerPerspective() );

    modelTreeBinding = bf.createBinding( workspace, "model", dimensionTree, "elements" ); //$NON-NLS-1$//$NON-NLS-2$
    relModelTreeBinding = bf.createBinding( workspace, "relationalModel", categoriesTree, "elements" ); //$NON-NLS-1$//$NON-NLS-2$

    modelerModeBinding = bf.createBinding( workspace, "modellingMode", this, "modellingMode" );

    bf.createBinding( dimensionTree, "selectedItem", dimTreeHelper, "treeSelectionChanged" ); //$NON-NLS-1$//$NON-NLS-2$
    bf.createBinding( categoriesTree, "selectedItem", catTreeHelper, "treeSelectionChanged" ); //$NON-NLS-1$//$NON-NLS-2$

    bf.setBindingType( Type.BI_DIRECTIONAL );
    bf.createBinding( workspace.getModel(), "name", workspace.getRelationalModel(), "name" );
    bf.createBinding( workspace, "selectedNode", dimensionTree, "selectedItems",
        new BindingConvertor<AbstractMetaDataModelNode, Collection>() { //$NON-NLS-1$//$NON-NLS-2$

          @Override
          public Collection sourceToTarget( AbstractMetaDataModelNode arg0 ) {
            return Collections.singletonList( arg0 );
          }

          @Override
          public AbstractMetaDataModelNode targetToSource( Collection arg0 ) {
            return (AbstractMetaDataModelNode) ( ( arg0 == null || arg0.isEmpty() ) ? null : arg0.iterator().next() );
          }

        } );

    bf.createBinding( workspace, "selectedRelationalNode", categoriesTree, "selectedItems",
        new BindingConvertor<AbstractMetaDataModelNode, Collection>() { //$NON-NLS-1$//$NON-NLS-2$

          @Override
          public Collection sourceToTarget( AbstractMetaDataModelNode arg0 ) {
            return Collections.singletonList( arg0 );
          }

          @Override
          public AbstractMetaDataModelNode targetToSource( Collection arg0 ) {
            return (AbstractMetaDataModelNode) ( ( arg0 == null || arg0.isEmpty() ) ? null : arg0.iterator().next() );
          }

        } );

    bf.setBindingType( Type.ONE_WAY );

    bf.createBinding( dimensionTree, "selectedItem", "dimensionBtn", "disabled", new ButtonConvertor(
        DimensionMetaDataCollection.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    bf.createBinding( dimensionTree, "selectedItem", "hierarchyBtn", "disabled", new ButtonConvertor(
        DimensionMetaData.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add measure button state based on selection in the model tree
    bf.createBinding( dimensionTree, "selectedItem", "measureBtn", "disabled", new ButtonConvertor(
        MeasuresCollection.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add level button state based on selection in the model tree
    bf.createBinding( dimensionTree, "selectedItem", "levelBtn", "disabled", new ButtonConvertor(
        HierarchyMetaData.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add member prop button state based on selection in the model tree
    bf.createBinding( dimensionTree, "selectedItem", "memberPropBtn", "disabled", new ButtonConvertor(
        LevelMetaData.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add field button state based on the selection in the model tree
    bf.createBinding( dimensionTree, "selectedItem", "addField", "disabled", new AcceptsDropConvertor( workspace,
        MeasuresCollection.class, HierarchyMetaData.class, LevelMetaData.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add field button state based on the selection in field list
    bf.createBinding( "fieldList", "selectedItem", "addField", "disabled", new AcceptsAvailableItemDropConvertor(
        workspace ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // // control the add member prop button state based on the selection in field list
    // bf.createBinding("fieldList", "selectedItem", "measureBtn", "disabled",
    //        new AcceptsAvailableFieldDropConvertor(dimTreeHelper, MeasuresCollection.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    //
    // // control the add member prop button state based on the selection in field list
    // bf.createBinding("fieldList", "selectedItem", "levelBtn", "disabled",
    //        new AcceptsAvailableFieldDropConvertor(dimTreeHelper, HierarchyMetaData.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    //
    // // control the add member prop button state based on the selection in field list
    // bf.createBinding("fieldList", "selectedItem", "memberPropBtn", "disabled",
    //        new AcceptsAvailableFieldDropConvertor(dimTreeHelper, LevelMetaData.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add field button state based on the selection in field list
    // bf.createBinding("fieldList", "selectedItem", "addField", "disabled",
    //        new AcceptsAvailableItemDropConvertor(catTreeHelper)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    bf.createBinding( categoriesTree, "selectedItem", "addField", "disabled", new AcceptsDropConvertor( workspace,
        CategoryMetaData.class, CategoryMetaDataCollection.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    bf.createBinding( categoriesTree, "selectedItem", "fieldBtn", "disabled", new ButtonConvertor(
        CategoryMetaData.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    bf.createBinding( categoriesTree, "selectedItem", "categoryBtn", "disabled", new ButtonConvertor(
        CategoryMetaDataCollection.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    // control the add field prop button state based on the selection in field list
    bf.createBinding( "fieldList", "selectedItem", "fieldBtn", "disabled", new AcceptsAvailableFieldDropConvertor(
        catTreeHelper, CategoryMetaData.class ) ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    bf.setBindingType( Type.BI_DIRECTIONAL );
    fireBindings();

    dimensionTree.setSelectedItems( Collections.singletonList( workspace.getModel() ) );
    if ( workspace.getRelationalModel().size() > 0 ) {
      categoriesTree.setSelectedItems( Collections.singletonList( workspace.getRelationalModel() ) );
    }

  }

  @Bindable
  public void showAutopopulatePrompt() {
    try {

      if ( getModelerPerspective() == ModelerPerspective.ANALYSIS ) {
        MainModelNode model = workspace.getModel();
        if ( model.getDimensions().isEmpty() && model.getMeasures().isEmpty() ) {
          autoPopulate();
        } else {
          showAutoModelConfirmDialog();
        }
      } else {
        RelationalModelNode model = workspace.getRelationalModel();
        if ( model.getCategories().isEmpty() ) {
          autoPopulate();
        } else {
          showAutoModelConfirmDialog();
        }
      }
    } catch ( XulException e ) {
      e.printStackTrace();
    }
  }

  private String getAutoPopulateMsg() {
    if ( getModelerPerspective() == ModelerPerspective.ANALYSIS ) {
      return ModelerMessagesHolder.getMessages().getString( "auto_populate_msg" ); //$NON-NLS-1$;
    } else {
      return ModelerMessagesHolder.getMessages().getString( "auto_populate_relational_msg" ); //$NON-NLS-1$;
    }
  }

  private void showAutoModelConfirmDialog() throws XulException {
    XulConfirmBox confirm = (XulConfirmBox) document.createElement( "confirmbox" ); //$NON-NLS-1$
    confirm.setTitle( ModelerMessagesHolder.getMessages().getString( "auto_populate_title" ) ); //$NON-NLS-1$
    confirm.setMessage( getAutoPopulateMsg() );
    confirm.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "yes" ) ); //$NON-NLS-1$
    confirm.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "no" ) ); //$NON-NLS-1$

    confirm.addDialogCallback( new XulDialogCallback() {
      public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
        if ( returnCode == Status.ACCEPT ) {
          autoPopulate();
        }
      }

      public void onError( XulComponent sender, Throwable t ) {
      }
    } );
    confirm.open();
  }

  protected void fireBindings() throws ModelerException {
    try {
      modelerModeBinding.fireSourceChanged();
      fieldListBinding.fireSourceChanged();
      selectedFieldsBinding.fireSourceChanged();
      modelTreeBinding.fireSourceChanged();
      relModelTreeBinding.fireSourceChanged();
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new ModelerException( e );
    }
  }

  /**
   * Goes back to the source of the metadata and see if anything has changed. Updates the UI accordingly
   */
  @Bindable
  public void refreshFields() throws ModelerException {
    workspace.refresh( workspace.getModellingMode() );
  }

  public void setFileName( String fileName ) {
    workspace.setFileName( fileName );
  }

  @Bindable
  public void showNewMeasureDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewMeasureTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewMeasureText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );
      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            MeasuresCollection theMesaures = (MeasuresCollection) dimTreeHelper.getSelectedTreeItem();
            MeasureMetaData theMeasure =
                new MeasureMetaData( "" + retVal, "", "" + retVal, workspace.getWorkspaceHelper().getLocale() ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

            theMeasure.setParent( theMesaures );
            theMeasure.validate();

            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging( true );
            theMesaures.add( theMeasure );
            workspace.setModelIsChanging( prevChangeState );

          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }

      } );
      prompt.open();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public void showNewHierarchyDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewHierarchyTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewHierarchyText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );
      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            DimensionMetaData theDimension = (DimensionMetaData) dimTreeHelper.getSelectedTreeItem();
            HierarchyMetaData theHieararchy = new HierarchyMetaData( "" + retVal );
            theHieararchy.setParent( theDimension );
            theHieararchy.setExpanded( true );

            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging( true );
            theDimension.add( theHieararchy );
            workspace.setModelIsChanging( prevChangeState );
          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }
      } );
      prompt.open();
    } catch ( XulException e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public void showNewLevelDialog() {

    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewLevelTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewLevelText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );
      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            HierarchyMetaData theHierarchy = (HierarchyMetaData) dimTreeHelper.getSelectedTreeItem();
            LevelMetaData theLevel = new LevelMetaData( theHierarchy, "" + retVal );

            theLevel.validate();
            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging( true );
            theHierarchy.add( theLevel );
            workspace.setModelIsChanging( prevChangeState );

          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }
      } );
      prompt.open();

    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }

  @Bindable
  public void showNewMemberPropDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewMemberPropertyTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewMemberPropertyText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );
      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            LevelMetaData theLevel = (LevelMetaData) dimTreeHelper.getSelectedTreeItem();
            MemberPropertyMetaData theMemberProp = new MemberPropertyMetaData( theLevel, "" + retVal );

            theMemberProp.validate();
            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging( true );
            theLevel.add( theMemberProp );
            workspace.setModelIsChanging( prevChangeState );

          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }
      } );
      prompt.open();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public void showNewDimensionDialog() {

    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewDimensionTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewDimensionText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );

      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {

            DimensionMetaData dimension = new DimensionMetaData( "" + retVal ); //$NON-NLS-1$
            dimension.setExpanded( true );
            HierarchyMetaData hierarchy = new HierarchyMetaData( "" + retVal ); //$NON-NLS-1$
            hierarchy.setExpanded( true );
            hierarchy.validate();
            hierarchy.setParent( dimension );
            dimension.add( hierarchy );
            workspace.addDimension( dimension );
          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }

      } );
      prompt.open();
    } catch ( XulException e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public void showNewCategoryDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewCategoryTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewCategoryText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );
      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            CategoryMetaDataCollection theNode = workspace.getRelationalModel().getCategories();
            CategoryMetaData theCategory = new CategoryMetaData( "" + retVal );
            boolean prevChangeState = workspace.isModelChanging();
            theCategory.validate();
            workspace.setRelationalModelIsChanging( true );
            theNode.add( theCategory );
            theCategory.setExpanded( true );
            workspace.setRelationalModelIsChanging( prevChangeState );
          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }
      } );
      prompt.open();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public void showNewFieldDialog() {

    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
      prompt.setTitle( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewFieldTitle" ) ); //$NON-NLS-1$
      prompt.setMessage( ModelerMessagesHolder.getMessages().getString( "ModelerController.NewFieldText" ) ); //$NON-NLS-1$
      prompt.setAcceptLabel( ModelerMessagesHolder.getMessages().getString( "dialog.OK" ) );
      prompt.setCancelLabel( ModelerMessagesHolder.getMessages().getString( "dialog.CANCEL" ) );
      prompt.addDialogCallback( new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if ( returnCode == Status.ACCEPT ) {
            CategoryMetaData theCategory = (CategoryMetaData) catTreeHelper.getSelectedTreeItem();
            FieldMetaData theField =
                new FieldMetaData( theCategory, "" + retVal, "", "" + retVal, workspace.getWorkspaceHelper()
                    .getLocale() );

            theField.validate();
            boolean prevChangeState = workspace.isModelChanging();
            workspace.setRelationalModelIsChanging( true );
            theCategory.add( theField );
            theCategory.setExpanded( true );
            workspace.setRelationalModelIsChanging( prevChangeState );

          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();
        }
      } );
      prompt.open();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private Binding fieldListBinding;
  private Binding selectedFieldsBinding;

  private Binding modelTreeBinding;
  private Binding relModelTreeBinding;
  private Binding modelerModeBinding;

  @Bindable
  public void removeField() {
    dimTreeHelper.removeField();
  }

  @Bindable
  public void removeRelationalNode() {
    catTreeHelper.removeField();
  }

  @Bindable
  public void moveFieldUp() {
    dimTreeHelper.moveFieldUp();
  }

  @Bindable
  public void moveFieldDown() {
    dimTreeHelper.moveFieldDown();
  }

  @Bindable
  public void moveRelationalFieldUp() {
    catTreeHelper.moveFieldUp();
  }

  @Bindable
  public void moveRelationalFieldDown() {
    catTreeHelper.moveFieldDown();
  }

  public ModelerWorkspace getModel() {
    return workspace;
  }

  public void setModel( ModelerWorkspace model ) throws ModelerException {
    this.workspace = model;
    fireBindings();
  }

  @Bindable
  public void showValidationMessagesDialog() {
    AbstractMetaDataModelNode node;
    if ( workspace.getCurrentModelerPerspective() == ModelerPerspective.ANALYSIS ) {
      node = workspace.getSelectedNode();
      if ( node == null ) {
        node = workspace.getModel();
      }
    } else {
      node = workspace.getSelectedRelationalNode();
      if ( node == null ) {
        node = workspace.getRelationalModel();
      }
    }
    showValidationMessages( new ArrayList<String>( node.getValidationMessages() ) );
  }

  protected void showValidationMessages() {
    showValidationMessages( workspace.getValidationMessages() );
  }

  protected void showValidationMessages( List<String> messages ) {

    StringBuffer validationErrors =
        new StringBuffer( ModelerMessagesHolder.getMessages().getString( "model_contains_errors" ) ); //$NON-NLS-1$
    for ( String msg : messages ) {
      validationErrors.append( msg );
      validationErrors.append( "\n" ); //$NON-NLS-1$
    }
    try {
      XulMessageBox msg = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
      msg.setTitle( ModelerMessagesHolder.getMessages().getString( "model_not_valid" ) ); //$NON-NLS-1$
      msg.setMessage( validationErrors.toString() );
      msg.open();
    } catch ( XulException e ) {
      e.printStackTrace();
    }
  }

  ModelerTreeHelper getCurrentModelerTreeHelper() {
    ModelerTreeHelper helper;
    switch ( workspace.getCurrentModelerPerspective() ) {
      case REPORTING:
        helper = catTreeHelper;
        break;
      case ANALYSIS:
        helper = dimTreeHelper;
        break;
      default:
        helper = null;
        break;
    }
    return helper;
  }

  Object getSelectedTreeItem() {
    ModelerTreeHelper helper = getCurrentModelerTreeHelper();
    if ( helper == null ) {
      return null;
    }
    return helper.getSelectedTreeItem();
  }

  @Bindable
  public void resolveMissingColumn() {
    Object selectedTreeItem = getSelectedTreeItem();
    if ( selectedTreeItem == null ) {
      return;
    }
    if ( !( selectedTreeItem instanceof ColumnBackedNode ) ) {
      return;
    }
    AbstractMetaDataModelNode selectedNode = (AbstractMetaDataModelNode) selectedTreeItem;
    if ( selectedNode.isValid() ) {
      return;
    }
    changeColumn();
  }

  public void changeColumn( String columnType ) {
    Object selectedTreeItem = getSelectedTreeItem();
    if ( selectedTreeItem == null ) {
      return;
    }

    ColumnBackedNode selectedColumnBackedNode = (ColumnBackedNode) selectedTreeItem;

    // only restrict to a table if this node needs to & the node has siblings that might conflict when changing the
    // parent
    IPhysicalTable restrictedPhysicalTable = selectedColumnBackedNode.getTableRestriction();
    if ( restrictedPhysicalTable == null ) {
      colController.show( workspace, selectedColumnBackedNode, columnType );
      return;
    }

    AvailableTable restrictToTable = null;
    AvailableItemCollection availableTables = workspace.getAvailableTables();
    String name = restrictedPhysicalTable.getName( getWorkspaceHelper().getLocale() );
    restrictToTable = availableTables.findAvailableTable( name );
    colController.show( workspace, selectedColumnBackedNode, columnType, restrictToTable );
  }

  public void clearColumn( String columnType ) {
    Object selectedTreeItem = getSelectedTreeItem();
    if ( selectedTreeItem == null ) {
      return;
    }

    ColumnBackedNode selectedColumnBackedNode = (ColumnBackedNode) selectedTreeItem;
    if ( ColumnBackedNode.COLUMN_TYPE_ORDINAL.equals( columnType ) ) {
      selectedColumnBackedNode.setLogicalOrdinalColumn( null );
    } else if ( ColumnBackedNode.COLUMN_TYPE_CAPTION.equals( columnType ) ) {
      selectedColumnBackedNode.setLogicalCaptionColumn( null );
    }
  }

  @Bindable
  public void changeColumn() {
    changeColumn( ColumnBackedNode.COLUMN_TYPE_SOURCE );
  }

  @Bindable
  public void changeOrdinalColumn() {
    changeColumn( ColumnBackedNode.COLUMN_TYPE_ORDINAL );
  }

  @Bindable
  public void clearOrdinalColumn() {
    clearColumn( ColumnBackedNode.COLUMN_TYPE_ORDINAL );
  }

  @Bindable
  public void changeCaptionColumn() {
    changeColumn( ColumnBackedNode.COLUMN_TYPE_CAPTION );
  }

  @Bindable
  public void clearCaptionColumn() {
    clearColumn( ColumnBackedNode.COLUMN_TYPE_CAPTION );
  }

  @Bindable
  public void showHelp( String uri ) {
    if ( uriHandler == null ) {
      // TODO: log a warning that the uri handler is not defined.
      return;
    }
    uriHandler.openUri( uri );
  }

  public void addPropertyForm( AbstractModelerNodeForm form ) {
    propertiesForms.put( form.getClass(), form );
  }

  public void setColResolver( ColResolverController controller ) {
    this.colController = controller;
  }

  public void autoPopulate() {
    try {
      // TODO: GWT-ify
      switch ( workspace.getCurrentModelerPerspective() ) {
        case REPORTING:
          workspace.getWorkspaceHelper().autoModelRelationalFlatInBackground( this.workspace );
          this.categoriesTree.expandAll();
          break;
        case ANALYSIS:
          workspace.getWorkspaceHelper().autoModelFlatInBackground( this.workspace );
          this.dimensionTree.expandAll();
          break;
      }
    } catch ( ModelerException e ) {
      e.printStackTrace();
    }
  }

  public void togglePropertiesPanel() {
    setPropVisible( !isPropVisible() );
  }

  private boolean propVisible = true;

  public boolean isPropVisible() {
    return propVisible;
  }

  public void setPropVisible( boolean vis ) {
    boolean prevVal = propVisible;
    this.propVisible = vis;
    this.firePropertyChange( "propVisible", prevVal, vis ); //$NON-NLS-1$
  }

  private static class ButtonConvertor extends BindingConvertor<Object, Boolean> {

    private Class type;

    public ButtonConvertor( Class aClass ) {
      type = aClass;
    }

    public Boolean sourceToTarget( Object value ) {
      return value == null || !( value.getClass() == type );
    }

    public Object targetToSource( Boolean value ) {
      return null;
    }
  }

  private static class AcceptsAvailableFieldDropConvertor extends BindingConvertor<Object, Boolean> {
    private ModelerTreeHelper helper;
    protected Set<Class> types = new HashSet<Class>();
    private Class restrictType;

    public AcceptsAvailableFieldDropConvertor( ModelerTreeHelper helper ) {
      this.helper = helper;
      this.types.add( AvailableField.class );
    }

    public AcceptsAvailableFieldDropConvertor( ModelerTreeHelper helper, Class aClass ) {
      this.helper = helper;
      this.restrictType = aClass;
      this.types.add( AvailableField.class );
    }

    public ModelerTreeHelper getHelper() {
      return helper;
    }

    public void setHelper( ModelerTreeHelper helper ) {
      this.helper = helper;
    }

    public Boolean sourceToTarget( Object value ) {
      if ( getHelper() == null ) {
        return true;
      }
      Object obj = getHelper().getSelectedTreeItem();

      if ( value == null || !types.contains( value.getClass() ) ) {
        return true; // disable the button by setting the disabled state to true
      }
      if ( this.restrictType != null ) {
        if ( obj instanceof AbstractMetaDataModelNode ) {
          if ( obj.getClass() == restrictType ) {
            AbstractMetaDataModelNode n = (AbstractMetaDataModelNode) obj;
            return !n.acceptsDrop( value );
          }
        }
      } else {
        if ( obj instanceof AbstractMetaDataModelNode ) {
          AbstractMetaDataModelNode n = (AbstractMetaDataModelNode) obj;
          return !n.acceptsDrop( value );
        }
      }
      return true;
    }

    public Object targetToSource( Boolean value ) {
      return null;
    }
  }

  private static class AcceptsAvailableItemDropConvertor extends AcceptsAvailableFieldDropConvertor {
    private ModelerWorkspace workspace;

    public AcceptsAvailableItemDropConvertor( ModelerWorkspace workspace ) {
      super( workspace.getCurrentModelerTreeHelper() );
      types.add( AvailableTable.class );
      this.workspace = workspace;
    }

    public AcceptsAvailableItemDropConvertor( ModelerWorkspace workspace, Class aClass ) {
      super( workspace.getCurrentModelerTreeHelper(), aClass );
      types.add( AvailableTable.class );
      this.workspace = workspace;
    }

    @Override
    public ModelerTreeHelper getHelper() {
      return workspace.getCurrentModelerTreeHelper();
    }

  }

  private static class AcceptsDropConvertor extends BindingConvertor<Object, Boolean> {
    private Set<Class> types;
    private ModelerWorkspace workspace;

    public AcceptsDropConvertor( ModelerWorkspace workspace, Class... aClasses ) {
      this.types = new HashSet( Arrays.asList( aClasses ) );
      this.workspace = workspace;
    }

    public Boolean sourceToTarget( Object value ) {
      if ( value == null || !types.contains( value.getClass() ) ) {
        return true; // disable the button by setting the disabled state to true
      }
      if ( workspace != null ) {
        IAvailableItem selected = workspace.getSelectedAvailableItem();
        if ( selected instanceof AvailableField ) {
          AbstractMetaDataModelNode node = (AbstractMetaDataModelNode) value;
          AvailableField n = (AvailableField) selected;
          return !node.acceptsDrop( n );
        }
      }
      return true;
    }

    public Object targetToSource( Boolean value ) {
      return null;
    }
  }

  @Bindable
  public void setSelectedFields( Object[] aFields ) {
    IAvailableItem[] f = new IAvailableItem[aFields.length];
    for ( int i = 0; i < aFields.length; i++ ) {
      if ( aFields[i] instanceof AvailableField ) {
        f[i] = (AvailableField) aFields[i];
      } else if ( aFields[i] instanceof AvailableTable ) {
        f[i] = (AvailableTable) aFields[i];
      }
    }
    selectedFields = f;
  }

  @Bindable
  public Object[] getSelectedFields() {
    if ( selectedFields == null ) {
      selectedFields = new IAvailableItem[] {};
    }
    return selectedFields;
  }

  public void setBindingFactory( BindingFactory bf ) {
    this.bf = bf;
  }

  public IModelerWorkspaceHelper getWorkspaceHelper() {
    return workspace.getWorkspaceHelper();
  }

  public void setWorkspaceHelper( IModelerWorkspaceHelper workspaceHelper ) {
    workspace.setWorkspaceHelper( workspaceHelper );
  }

  public boolean saveWorkspace( String fileName ) throws ModelerException {
    workspace.getModel().validateTree();
    if ( workspace.isValid() == false ) {
      showValidationMessages();
      return false;
    }
    workspace.setFileName( fileName );
    workspace.setDirty( false );
    workspace.setTemporary( false );
    return true;
  }

  public void resetPropertyForm() {
    this.propDeck.setSelectedIndex( 0 );
    dimTreeHelper.setSelectedTreeItem( null );
    catTreeHelper.setSelectedTreeItem( null );
  }

  @Bindable
  public void showReportingOnlyMode() {
    setModellingMode( ModelerMode.REPORTING_ONLY );
  }

  @Bindable
  public void showAnalysisAndReportingMode() {
    setModellingMode( ModelerMode.ANALYSIS_AND_REPORTING );
  }

  @Bindable
  public ModelerMode getModellingMode() {
    return workspace.getModellingMode();
  }

  @Bindable
  public void setModellingMode( ModelerMode mode ) {
    if ( mode == ModelerMode.REPORTING_ONLY ) {
      // reparent the reporting panel outside of the tabset
      if ( reportingTabPanel.getChildNodes().size() > 0 && modelPanel.getChildNodes().size() == 0 ) {
        int height = modelPanel.getHeight();
        modelPanel.addComponent( reportingPanel );
        modelPanel.setHeight( height );
      }
      modelDeck.setSelectedIndex( 1 );
      workspace.setCurrentModelerPerspective( ModelerPerspective.REPORTING );
    } else {
      // put the reporting panel back in the tabset
      if ( modelPanel.getChildNodes().size() > 0 && reportingTabPanel.getChildNodes().size() == 0 ) {
        reportingTabPanel.addComponent( reportingPanel );
      }
      modelDeck.setSelectedIndex( 0 );
      if ( modelTabbox.getSelectedIndex() == 0 ) {
        workspace.setCurrentModelerPerspective( ModelerPerspective.ANALYSIS );
      } else {
        workspace.setCurrentModelerPerspective( ModelerPerspective.REPORTING );
      }
    }
    workspace.setModellingMode( mode );
  }

  @Bindable
  public void clearFields() {
    dimTreeHelper.clearFields();
  }

  @Bindable
  public void clearRelationalFields() {
    catTreeHelper.clearFields();
  }

  public ModelerPerspective getModelerPerspective() {
    return workspace.getCurrentModelerPerspective();
  }

  public void setModelerPerspective( ModelerPerspective perspective ) {
    ModelerPerspective prevVal = workspace.getCurrentModelerPerspective();
    workspace.setCurrentModelerPerspective( perspective );
    if ( prevVal != perspective ) {
      this.modelTabbox.setSelectedIndex( perspective == ModelerPerspective.ANALYSIS ? 0 : 1 );
    }
    // force refresh the property form panel
    if ( perspective == ModelerPerspective.ANALYSIS ) {
      dimTreeHelper.setTreeSelectionChanged( dimTreeHelper.getSelectedTreeItem() );
    } else {
      catTreeHelper.setTreeSelectionChanged( catTreeHelper.getSelectedTreeItem() );
    }
  }

  @Bindable
  public void setModelerPerspective( String perspective ) {
    setModelerPerspective( ModelerPerspective.valueOf( perspective ) );
  }

  public DimensionTreeHelper getDimTreeHelper() {
    if ( dimTreeHelper == null ) {
      dimTreeHelper = new DimensionTreeHelper( propertiesForms, propDeck, workspace, document );
    }
    return dimTreeHelper;
  }

  public CategoryTreeHelper getCatTreeHelper() {
    if ( catTreeHelper == null ) {
      catTreeHelper = new CategoryTreeHelper( propertiesForms, propDeck, workspace, document );
    }
    return catTreeHelper;
  }

  @Bindable
  public void collapseAll() {
    dimensionTree.collapseAll();
  }

  @Bindable
  public void expandAll() {
    dimensionTree.expandAll();
  }

  @Bindable
  public void collapseRelationalAll() {
    categoriesTree.collapseAll();
  }

  @Bindable
  public void expandRelationalAll() {
    categoriesTree.expandAll();
  }

}
