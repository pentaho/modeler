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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * UI model behind the XUL-based interface. This class contains a reference from the context in
 * which the modeling was initiated through an IModelerSource which also provides model generation.
 *
 * @author nbaker
 */
@SuppressWarnings("unchecked")
public class ModelerWorkspace extends XulEventSourceAdapter implements Serializable{

  private AvailableFieldCollection availableFields = new AvailableFieldCollection();

  private MainModelNode model;

  private String sourceName;

  private transient IModelerSource source;

  private String selectedServer;

  private String selectedVisualization;

  private String schemaName;

  private Domain domain;

  private boolean dirty = true;

  // full path to file
  private String fileName;

  private boolean modelIsChanging;

  private boolean isTemporary;

  private AbstractMetaDataModelNode selectedNode;
  private IModelerWorkspaceHelper workspaceHelper;

  public ModelerWorkspace(IModelerWorkspaceHelper helper) {

    this.isTemporary = true;
    this.workspaceHelper = helper;

    setModel(new MainModelNode());

  }

  @Bindable
  public MainModelNode getModel() {
    return model;
  }

  @Bindable
  public void setModel( MainModelNode model ) {
    this.model = model;
    model.addPropertyChangeListener("children", new PropertyChangeListener() { //$NON-NLS-1$

      public void propertyChange( PropertyChangeEvent arg0 ) {
        if (!modelIsChanging) {
          fireModelChanged();
        }
      }
    });
  }

  @Bindable
  public void setFileName( String fileName ) {
    String prevVal = this.fileName;
    String prevFriendly = getShortFileName();

    this.fileName = fileName;
    firePropertyChange("fileName", prevVal, fileName); //$NON-NLS-1$
    firePropertyChange("shortFileName", prevFriendly, getShortFileName()); //$NON-NLS-1$
  }

  @Bindable
  public String getShortFileName() {

    if (fileName == null) {
      return null;
    }
    int extensionPos = fileName.lastIndexOf('.');
    if (extensionPos == -1) {
      extensionPos = fileName.length();
    }
    int sepPos = fileName.replace('\\', '/').lastIndexOf('/');
    if (sepPos == -1) {
      sepPos = 0;
    } else {
      sepPos++;
    }
    return fileName.substring(sepPos, extensionPos);
  }

  @Bindable
  public String getFileName() {
    return fileName;
  }

  //transMeta.getFilename()

  @Bindable
  public String getSourceName() {
    return sourceName;
  }

  @Bindable
  public void setSourceName( String sourceName ) {
    this.sourceName = sourceName;
  }

  @Bindable
  public String getModelName() {
    return model.getName();
  }

  @Bindable
  public void setModelName( String modelName ) {
    String prevVal = model.getName();
    model.setName(modelName);
    setDirty(true);
    this.firePropertyChange("modelName", prevVal, modelName); //$NON-NLS-1$
  }

  @Bindable
  public boolean isDirty() {
    return dirty;
  }

  @Bindable
  public boolean isValid() {
    model.validateTree();
    return this.model.isValid();
  }

  @Bindable
  public List<String> getValidationMessages() {
    return model.getValidationMessages();
  }

  @Bindable
  public void setDirty( boolean dirty ) {
    boolean prevVal = this.dirty;
    this.dirty = dirty;
    this.firePropertyChange("dirty", prevVal, this.dirty); //$NON-NLS-1$
  }

  @Bindable
  public AvailableFieldCollection getAvailableFields() {
    return availableFields;
  }


  @Bindable
  public void setSelectedVisualization( String aVisualization ) {
    this.selectedVisualization = aVisualization;
  }

  @Bindable
  public String getSelectedVisualization() {
    return this.selectedVisualization;
  }

  public DimensionMetaData createDimensionFromNode( ColumnBackedNode obj ) {
    DimensionMetaData dimension = new DimensionMetaData(obj.getName());
    dimension.setExpanded(true);
    HierarchyMetaData hierarchy = createHierarchyForParentWithNode(dimension, obj);
    hierarchy.setParent(dimension);
    hierarchy.setExpanded(true);
    dimension.add(hierarchy);
    return dimension;
  }


  public DimensionMetaData createDimensionWithName( String dimName ) {
    DimensionMetaData dimension = new DimensionMetaData(dimName);
    dimension.setExpanded(true);
    HierarchyMetaData hierarchy = createHierarchyForParentWithNode(dimension, null);
    hierarchy.setParent(dimension);
    hierarchy.setExpanded(true);
    dimension.add(hierarchy);
    return dimension;
  }


  public void addDimensionFromNode( ColumnBackedNode obj ) {
    addDimension(createDimensionFromNode(obj));
  }

  public void addDimension( DimensionMetaData dim ) {
    boolean prevChangeState = this.modelIsChanging;
    this.setModelIsChanging(true);
    this.model.getDimensions().add(dim);
    this.setModelIsChanging(prevChangeState);
  }

  public LevelMetaData createLevelForParentWithNode( HierarchyMetaData parent, ColumnBackedNode obj ) {
    LevelMetaData level = new LevelMetaData(parent, obj.getName());
    level.setParent(parent);
    level.setLogicalColumn(obj.getLogicalColumn());
    return level;
  }

  public LevelMetaData createLevelForParentWithNode( HierarchyMetaData parent, String name ) {
    LevelMetaData level = new LevelMetaData(parent, name);
    level.setParent(parent);
    level.setLogicalColumn(findLogicalColumn(name));
    return level;
  }

  public HierarchyMetaData createHierarchyForParentWithNode( DimensionMetaData parent, ColumnBackedNode obj ) {
    HierarchyMetaData hier = new HierarchyMetaData(obj.getName());
    hier.setParent(parent);
    hier.setExpanded(true);
    if (obj != null) {
      LevelMetaData level = createLevelForParentWithNode(hier, obj);
      hier.add(level);
    }
    return hier;
  }

  private void fireFieldsChanged() {
    firePropertyChange("availableFields", null, this.availableFields); //$NON-NLS-1$
  }

  private void fireModelChanged() {
    firePropertyChange("model", null, model); //$NON-NLS-1$
    setDirty(true);
  }

  public MeasureMetaData createMeasureForNode( AvailableField selectedField ) {

    MeasureMetaData meta = new MeasureMetaData(selectedField.getName(), "",
        selectedField.getDisplayName()); //$NON-NLS-1$
    meta.setLogicalColumn(selectedField.getLogicalColumn());

    return meta;
  }

  public void addMeasure( MeasureMetaData measure ) {

    boolean prevChangeState = isModelChanging();
    this.setModelIsChanging(true);
    this.model.getMeasures().add(measure);
    this.setModelIsChanging(prevChangeState);
  }

  public LogicalColumn findLogicalColumn( String id ) {
    LogicalColumn col = null;
    for (LogicalColumn c : domain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()) {
      if (c.getName("en_US").equals(id)) {
        col = c;
        break;
      }
    }
    return col;
  }

  public void setModelSource( IModelerSource source ) {
    this.source = source;
  }

  public IModelerSource getModelSource() {
    return source;
  }

  public void setFields( List<MeasureMetaData> fields ) {
    this.model.getMeasures().clear();
    this.model.getMeasures().addAll(fields);
  }

  public void refresh() throws ModelerException {
    if (source == null) {
      return;
    }
    Domain newDomain = source.generateDomain();
    refresh(newDomain);
  }

  public void refresh(Domain newDomain) throws ModelerException {

    // Add in new logicalColumns
    for (LogicalColumn lc : newDomain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()) {
      boolean exists = false;
      inner:
      for (AvailableField fmd : this.availableFields) {
        if (fmd.getLogicalColumn().getId().equals(lc.getId())) {
          fmd.setLogicalColumn(lc);
          exists = true;
          break inner;
        }
      }
      if (!exists) {
        AvailableField fm = new AvailableField();
        fm.setLogicalColumn(lc);
        fm.setName(lc.getName("en_US"));//TODO GWT i18n: Locale.getDefault().toString()));
        fm.setDisplayName(lc.getName("en_US"));//TODO GWT i18n: Locale.getDefault().toString()));
        availableFields.add(fm);
        Collections.sort(availableFields, new Comparator<AvailableField>() {
          public int compare( AvailableField arg0, AvailableField arg1 ) {
            return arg0.getLogicalColumn().getId().compareTo(arg1.getLogicalColumn().getId());
          }
        });
      }
    }



    // Remove logicalColumns that no longer exist.
    List<AvailableField> toRemove = new ArrayList<AvailableField>();
    for (AvailableField fm : availableFields) {
      boolean exists = false;
      LogicalColumn fmlc = fm.getLogicalColumn();
      inner:
      for (LogicalColumn lc : newDomain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()) {
        if (lc.getId().equals(fmlc.getId())) {
          exists = true;
          break inner;
        }
      }
      if (!exists) {
        toRemove.add(fm);
      }
    }
    availableFields.removeAll(toRemove);
    workspaceHelper.sortFields(availableFields);

    fireFieldsChanged();


    for (MeasureMetaData measure : model.getMeasures()) {
      boolean found = false;
      if (measure.getLogicalColumn() != null) {
        for (AvailableField fm : availableFields) {
          if (fm.getLogicalColumn().getId().equals(measure.getLogicalColumn().getId())) {
            found = true;
          } else {
            if (fm.getLogicalColumn().getProperty(SqlPhysicalColumn.TARGET_COLUMN).equals(
                measure.getLogicalColumn().getProperty(SqlPhysicalColumn.TARGET_COLUMN))) {
              // clone the logical column into the new model
              // this is necessary because a model may contain
              // multiple measures, each with their own
              // default aggregation and name
              LogicalColumn lCol = (LogicalColumn) fm.getLogicalColumn().clone();
              lCol.setId(measure.getLogicalColumn().getId());
              newDomain.getLogicalModels().get(0).getLogicalTables().get(0).addLogicalColumn(lCol);
              found = true;
            }
          }
        }
      }
      if (!found) {
        measure.setLogicalColumn(null);
      }
    }

    try{
      for (DimensionMetaData dm : model.getDimensions()) {
        for (HierarchyMetaData hm : dm) {
          for (LevelMetaData lm : hm) {
            boolean found = false;
            if (lm.getLogicalColumn() != null) {
              inner:
              for (AvailableField fm : availableFields) {
                if (fm.getLogicalColumn().getId().equals(lm.getLogicalColumn().getId())) {
                  found = true;
                  break inner;
                }
              }
            }
            if (!found) {
              lm.setLogicalColumn(null);
            }
          }
        }
      }
    } catch(Exception e){
      e.printStackTrace();
    }

    // replace the domain with the new domain, which
    // makes sure the physical and logical columns are accurate
    domain = newDomain;

    model.validateTree();
  }


  public String getDatabaseName() {
    return source.getDatabaseName();
  }

  @Bindable
  public String getSchemaName() {
    return schemaName;
  }

  @Bindable
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public void setAvailableFields(AvailableFieldCollection fields){
    this.availableFields = fields;
    firePropertyChange("availableFields", null, getAvailableFields()); //$NON-NLS-1$

  }

  public void setDomain( Domain d ) {
    this.domain = d;
    this.model.getDimensions().clear();
    this.model.getMeasures().clear();
    this.availableFields.clear();

    LogicalTable table = domain.getLogicalModels().get(0).getLogicalTables().get(0);
    for (LogicalColumn c : table.getLogicalColumns()) {
      AvailableField fm = new AvailableField();
      fm.setLogicalColumn(c);
      fm.setName(c.getPhysicalColumn().getName("en_US"));//TODO GWT i18n: Locale.getDefault().toString()));
      fm.setDisplayName(c.getName("en_US"));//TODO GWT i18n: Locale.getDefault().toString()));
      fm.setAggTypeDesc(c.getAggregationType().toString());
      availableFields.add(fm);
    }

    workspaceHelper.sortFields(availableFields);

    firePropertyChange("availableFields", null, getAvailableFields()); //$NON-NLS-1$

    LogicalModel lModel = domain.getLogicalModels().get(0);

    if (lModel.getCategories().size() > 0) {
      setModelName(lModel.getCategories().get(0).getId());
    }

    List<OlapDimension> theDimensions = (List) lModel.getProperty("olap_dimensions"); //$NON-NLS-1$
    if (theDimensions != null) {
      Iterator<OlapDimension> theDimensionItr = theDimensions.iterator();
      while (theDimensionItr.hasNext()) {
        OlapDimension theDimension = theDimensionItr.next();

        DimensionMetaData theDimensionMD = new DimensionMetaData(theDimension.getName());

        List<OlapHierarchy> theHierarchies = (List) theDimension.getHierarchies();
        Iterator<OlapHierarchy> theHierarchiesItr = theHierarchies.iterator();
        while (theHierarchiesItr.hasNext()) {
          OlapHierarchy theHierarchy = theHierarchiesItr.next();
          HierarchyMetaData theHierarchyMD = new HierarchyMetaData(theHierarchy.getName());

          List<OlapHierarchyLevel> theLevels = theHierarchy.getHierarchyLevels();
          Iterator<OlapHierarchyLevel> theLevelsItr = theLevels.iterator();
          while (theLevelsItr.hasNext()) {
            OlapHierarchyLevel theLevel = theLevelsItr.next();
            LevelMetaData theLevelMD = new LevelMetaData(theHierarchyMD, theLevel.getName());

            theLevelMD.setParent(theHierarchyMD);
            theLevelMD.setLogicalColumn(theLevel.getReferenceColumn());
            theHierarchyMD.add(theLevelMD);
          }

          theHierarchyMD.setParent(theDimensionMD);
          theDimensionMD.add(theHierarchyMD);
        }
        this.model.getDimensions().add(theDimensionMD);
      }
    }

    List<OlapCube> theCubes = (List) lModel.getProperty("olap_cubes"); //$NON-NLS-1$
    if (theCubes != null) {
      Iterator<OlapCube> theCubeItr = theCubes.iterator();
      while (theCubeItr.hasNext()) {
        OlapCube theCube = theCubeItr.next();

        List<OlapMeasure> theMeasures = theCube.getOlapMeasures();
        Iterator<OlapMeasure> theMeasuresItr = theMeasures.iterator();
        while (theMeasuresItr.hasNext()) {
          OlapMeasure theMeasure = theMeasuresItr.next();

          MeasureMetaData theMeasureMD = new MeasureMetaData();
          theMeasureMD.setName(
              theMeasure.getLogicalColumn().getName("en_US"));//TODO GWT i18n: Locale.getDefault().toString()));
          theMeasureMD.setFormat((String) theMeasure.getLogicalColumn().getProperty("mask")); //$NON-NLS-1$
          theMeasureMD.setAggTypeDesc(theMeasure.getLogicalColumn().getAggregationType().toString());

          theMeasureMD.setLogicalColumn(theMeasure.getLogicalColumn());
          this.model.getMeasures().add(theMeasureMD);
        }
      }
    }
    model.validateTree();

  }

  public void resolveConnectionFromDomain() {
    // set up the datasource
    if (domain != null && source != null) {
      SqlPhysicalModel physicalModel = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
      //TODO: resolve GWT DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(physicalModel.getId(), physicalModel.getDatasource());
      //TODO: resolve GWT source.setDatabaseMeta(databaseMeta);
    }

  }

  public Domain getDomain() {
    return updateDomain();
  }

  private Domain updateDomain() {
    // TODO: update domain with changes
    return domain;
  }

  public void setModelIsChanging( boolean changing ) {
    setModelIsChanging(changing, true);
  }

  public void setModelIsChanging( boolean changing, boolean fireChanged ) {
    this.modelIsChanging = changing;
    if (!changing && fireChanged) {
      fireFieldsChanged();
      model.validateTree();
      fireModelChanged();
    }
    model.setSupressEvents(changing);
  }

  @Bindable
  public boolean isModelChanging() {
    return modelIsChanging;
  }

  @Bindable
  public void setTemporary( boolean isTempoarary ) {
    this.isTemporary = isTempoarary;
  }

  @Bindable
  public boolean isTemporary() {
    return this.isTemporary;
  }

  @Bindable
  public AbstractMetaDataModelNode getSelectedNode() {
    return selectedNode;
  }

  @Bindable
  public void setSelectedNode( AbstractMetaDataModelNode node ) {
    AbstractMetaDataModelNode prevVal = this.selectedNode;
    this.selectedNode = node;
    firePropertyChange("selectedNode", prevVal, node); //$NON-NLS-1$
  }

  public IModelerWorkspaceHelper getWorkspaceHelper() {
    return workspaceHelper;
  }

  public void setWorkspaceHelper( IModelerWorkspaceHelper workspaceHelper ) {
    this.workspaceHelper = workspaceHelper;
  }
}