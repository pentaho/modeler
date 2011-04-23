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

import org.pentaho.agilebi.modeler.strategy.StarSchemaAutoModelStrategy;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.strategy.MultiTableAutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.*;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;


/**
 * UI model behind the XUL-based interface. This class contains a reference from the context in
 * which the modeling was initiated through an IModelerSource which also provides model generation.
 *
 * @author nbaker
 */
@SuppressWarnings("unchecked")
public class ModelerWorkspace extends XulEventSourceAdapter implements Serializable{

  private static final long serialVersionUID = 2058731810283858276L;
  private AvailableItemCollection availableTables = new AvailableItemCollection();

  private MainModelNode model;
  private RelationalModelNode relationalModel;

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
  private AbstractMetaDataModelNode selectedRelationalNode;

  private transient ModelerMode currentModellingMode = ModelerMode.ANALYSIS_AND_REPORTING;
  private transient ModelerPerspective currentModelerPerspective = ModelerPerspective.ANALYSIS;

  private transient SimpleAutoModelStrategy simpleAutoModelStrategy;
  private transient MultiTableAutoModelStrategy multiTableAutoModelStrategy;
  private transient StarSchemaAutoModelStrategy starSchemaAutoModelStrategy;

  public ModelerWorkspace(IModelerWorkspaceHelper helper) {

    this.isTemporary = true;
    this.workspaceHelper = helper;

    setModel(new MainModelNode(this));
    setRelationalModel(new RelationalModelNode(this));

    simpleAutoModelStrategy = new SimpleAutoModelStrategy(workspaceHelper.getLocale());
    multiTableAutoModelStrategy = new MultiTableAutoModelStrategy(workspaceHelper.getLocale());
    starSchemaAutoModelStrategy = new StarSchemaAutoModelStrategy(workspaceHelper.getLocale());
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
    model.addPropertyChangeListener("valid", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        isValid();
      }
    });
  }

  @Bindable
  public RelationalModelNode getRelationalModel() {
    return relationalModel;
  }

  @Bindable
  public void setRelationalModel( RelationalModelNode model ) {
    this.relationalModel = model;
    relationalModel.addPropertyChangeListener("children", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (!modelIsChanging) {
          fireRelationalModelChanged();
        }
      }
    });
    relationalModel.addPropertyChangeListener("valid", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        isValid();
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
  public String getRelationalModelName() {
    return relationalModel.getName();
  }
  @Bindable
  public void setRelationalModelName(String modelName) {
    String prevVal = model.getName();
    relationalModel.setName(modelName);
    setDirty(true);
    this.firePropertyChange("relationalModelName", prevVal, modelName); //$NON-NLS-1$
  }

  @Bindable
  public boolean isDirty() {
    return dirty;
  }

  @Bindable
  public boolean isValid() {
    boolean valid = false;
    switch (getModellingMode()) {
      case ANALYSIS_AND_REPORTING:
        valid = this.model.isValid() && relationalModel.isValid();
        break;
      case REPORTING_ONLY:
        valid = relationalModel.isValid();
    }
    firePropertyChange("valid", null, valid);
    return valid;
  }

  @Bindable
  public List<String> getValidationMessages() {
    List<String> modelMsg = model.getValidationMessages();
    List<String> relModelMsg = relationalModel.getValidationMessages();
    modelMsg.addAll(relModelMsg);
    return modelMsg;
  }

  @Bindable
  public void setDirty( boolean dirty ) {
    boolean prevVal = this.dirty;
    this.dirty = dirty;
    this.firePropertyChange("dirty", prevVal, this.dirty); //$NON-NLS-1$
  }

  @Bindable
  public AvailableItemCollection getAvailableTables() {
    return availableTables;
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


  public DimensionMetaData createDimensionFromAvailableTable(AvailableTable tbl) {
    DimensionMetaData dimension = new DimensionMetaData(tbl.getName());
    dimension.setExpanded(true);
    HierarchyMetaData hierarchy = new HierarchyMetaData(tbl.getName());

    hierarchy.setExpanded(true);
    for(AvailableField field : tbl.getChildren()){
      ColumnBackedNode node = this.createColumnBackedNode(field, ModelerPerspective.ANALYSIS);
      LevelMetaData level = createLevelForParentWithNode(hierarchy, node);
      hierarchy.add(level);
    }
    
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

  public void addCategory( CategoryMetaData cat ) {
    boolean prevChangeState = this.modelIsChanging;
    this.setRelationalModelIsChanging(true);
    this.relationalModel.getCategories().add(cat);
    this.setRelationalModelIsChanging(prevChangeState);
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

  public FieldMetaData createFieldForParentWithNode( CategoryMetaData parent, AvailableField selectedField ) {
    FieldMetaData field = new FieldMetaData(parent, selectedField.getName(), "",
        selectedField.getDisplayName(), workspaceHelper.getLocale()); //$NON-NLS-1$
    ColumnBackedNode node = createColumnBackedNode(selectedField, ModelerPerspective.REPORTING);
    field.setLogicalColumn(node.getLogicalColumn());
    field.setFieldTypeDesc(node.getLogicalColumn().getDataType().getName());
    return field;
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

  private void fireTablesChanged() {
    // set the automodel strategy based on the number of available tables
    if (availableTables.size() > 1) {
      if (availableTables.findFactTable() != null) {
        workspaceHelper.setAutoModelStrategy(starSchemaAutoModelStrategy);
      } else {
        workspaceHelper.setAutoModelStrategy(multiTableAutoModelStrategy);
      }
    } else {
      workspaceHelper.setAutoModelStrategy(simpleAutoModelStrategy);
    }
    firePropertyChange("availableTables", null, this.availableTables); //$NON-NLS-1$
  }

  private void fireModelChanged() {
    firePropertyChange("model", null, model); //$NON-NLS-1$
    setDirty(true);
  }

  private void fireRelationalModelChanged() {
    firePropertyChange("relationalModel", null, relationalModel); //$NON-NLS-1$
    setDirty(true);
  }

  public MeasureMetaData createMeasureForNode( AvailableField selectedField ) {

    MeasureMetaData meta = new MeasureMetaData(selectedField.getName(), "",
        selectedField.getDisplayName(), workspaceHelper.getLocale()); //$NON-NLS-1$
    ColumnBackedNode node = createColumnBackedNode(selectedField, ModelerPerspective.ANALYSIS);
    meta.setLogicalColumn(node.getLogicalColumn());
    return meta;
  }

  public void addMeasure( MeasureMetaData measure) {

    boolean prevChangeState = isModelChanging();
    this.setModelIsChanging(true);
    this.model.getMeasures().add(measure);
    this.setModelIsChanging(prevChangeState);
  }

  public LogicalColumn findLogicalColumn( String id ) {
    LogicalColumn col = null;
    for (LogicalColumn c : domain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()) {
      if (c.getName(workspaceHelper.getLocale()).equals(id)) {
        col = c;
        break;
      }
    }
    return col;
  }

  public LogicalTable findLogicalTable(IPhysicalTable table) {
    return findLogicalTable(table, currentModelerPerspective);
  }
  public LogicalTable findLogicalTable(IPhysicalTable table, ModelerPerspective perspective) {
    for (LogicalTable logicalTable : domain.getLogicalModels().get(0).getLogicalTables()) {
      if (logicalTable.getPhysicalTable().equals(table) || logicalTable.getId().equals(table.getId())) {
        boolean isOlapTable = logicalTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX);
        if (perspective == ModelerPerspective.ANALYSIS && isOlapTable) {
          return logicalTable;
        } else if (perspective == ModelerPerspective.REPORTING && !isOlapTable) {
          return logicalTable;
        }
      }
    }
    return null;
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

  public void refresh(ModelerMode mode) throws ModelerException {
    if (source == null) {
      return;
    }

    Domain newDomain = source.generateDomain(mode == ModelerMode.ANALYSIS_AND_REPORTING);
    refresh(newDomain);
  }
  public void refresh(Domain newDomain) throws ModelerException {

    setModelIsChanging(true);
    setRelationalModelIsChanging(true);

    Comparator<AvailableField> fieldComparator = new Comparator<AvailableField>() {
          public int compare( AvailableField arg0, AvailableField arg1 ) {
            return arg0.getPhysicalColumn().getId().compareTo(arg1.getPhysicalColumn().getId());
          }
    };

    LogicalModel logicalModel = newDomain.getLogicalModels().get(0);

    // Add in new physical tables/columns
    for (IPhysicalTable table : newDomain.getPhysicalModels().get(0).getPhysicalTables()) {
      // see if the table is in the availableTables list
      AvailableTable aTable = availableTables.findAvailableTable(table.getName(LocalizedString.DEFAULT_LOCALE));
      if (aTable == null) {
        // new table, make sure we add it
        availableTables.add(new AvailableTable(table));
      } else {
        // table already exists here, make sure all the fields are accounted for
        for(IPhysicalColumn column : table.getPhysicalColumns()) {
          boolean exists = false;
          inner:
          for(AvailableField field : aTable.getAvailableFields()) {
            if (field.isSameUnderlyingPhysicalColumn(column)) {
              exists = true;
              break inner;
            }
          }
          if (!exists) {
            AvailableField field = new AvailableField(column);
            aTable.getAvailableFields().add(field);
            Collections.sort(aTable.getAvailableFields(), fieldComparator);
          }
        }
      }
    }

    // Remove available tables/fields that no longer exist correspond to an available physical column
    List<AvailableTable> tablesToRemove = new ArrayList<AvailableTable>();
    List<AvailableTable> tablesList = availableTables.getAsAvailableTablesList();

    for (AvailableTable aTable : tablesList) {
        List<AvailableField> toRemove = new ArrayList<AvailableField>();
        boolean tableExists = false;
        for (AvailableField field : aTable.getAvailableFields()) {
          boolean exists = false;
          inner:
          for (IPhysicalTable table : newDomain.getPhysicalModels().get(0).getPhysicalTables()) {
            if (aTable.isSameUnderlyingPhysicalTable(table)) {
              tableExists = true;
              for(IPhysicalColumn column : table.getPhysicalColumns()) {
                if (field.isSameUnderlyingPhysicalColumn(column)) {
                    exists = true;
                    break inner;
                }
              }
            }
          }
          if (!exists) {
            toRemove.add(field);
          }
        }
        aTable.getAvailableFields().removeAll(toRemove);
        workspaceHelper.sortFields(aTable.getAvailableFields());

        if (!tableExists) {
          tablesToRemove.add(aTable);
        }
              }
    availableTables.removeAll(tablesToRemove);
    // TODO, sort the tables???


    fireTablesChanged();

    for (MeasureMetaData measure : model.getMeasures()) {
      boolean found = false;
      if (measure.getLogicalColumn() != null) {
        inner:
        for (AvailableTable table : tablesList) {
            if (table.containsUnderlyingPhysicalColumn(measure.getLogicalColumn().getPhysicalColumn())) {
              // the physical column backing this measure is still available, it is ok
              found = true;
              break inner;
            }
            }
          }
      if (!found) {
        // the physical column that backed this measure no longer exists in the model.
        // therefore, we must invalidate it's logical column
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
              for (AvailableTable table : tablesList) {
                  if (table.containsUnderlyingPhysicalColumn(lm.getLogicalColumn().getPhysicalColumn())) {
                    // the physical column backing this level is still available, it is ok
                    found = true;
                    break inner;
                  }
                  }
                }
            if (!found) {
              // the physical column that backed this level no longer exists in the model.
              // therefore, we must invalidate it's logical column
              lm.setLogicalColumn(null);
            }
          }
        }
      }
    } catch(Exception e){
      e.printStackTrace();
    }

    for (CategoryMetaData category : relationalModel.getCategories()) {
      for (FieldMetaData field : category) {
        boolean found = false;
        if (field.getLogicalColumn() != null) {
          inner:
          for (AvailableTable table : tablesList) {
              if (table.containsUnderlyingPhysicalColumn(field.getLogicalColumn().getPhysicalColumn())) {
                // the physical column backing this field is still available, it is ok
                found = true;
                break inner;
              }
              }
            }
        if (!found) {
          // the physical column that backed this field no longer exists in the model.
          // therefore, we must invalidate it's logical column
          field.setLogicalColumn(null);
        }
      }
    }

    // If the new model was previously "auto-modeled" we need to clean that now
    LogicalModel newLModel = newDomain.getLogicalModels().get(0);
    if (newLModel != null) {
      List<OlapDimension> theDimensions = (List) newLModel.getProperty("olap_dimensions"); //$NON-NLS-1$
      if (theDimensions != null) {
        theDimensions.clear();
      }
      List<OlapCube> theCubes = (List) newLModel.getProperty("olap_cubes"); //$NON-NLS-1$
      if (theCubes != null) {
        theCubes.clear();
      }
    }
    
    // replace the domain with the new domain, which
    // makes sure the physical and logical columns are accurate
    domain = newDomain;

    setModelIsChanging(false);
    setRelationalModelIsChanging(false);

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

  public void setAvailableTables(AvailableItemCollection tables){
    this.availableTables = tables;
    fireTablesChanged();
  }


  public void setDomain( Domain d ) {
    setDomain(d, true);
  }

  // this method signature is intended to provide a simpler path for unit testing the upConvert method on it's own
  protected void setDomain(Domain d, boolean upConvertDesired) {
    this.domain = d;
    this.setModelIsChanging(true);
    this.setRelationalModelIsChanging(true);
    this.model.getDimensions().clear();
    this.model.getMeasures().clear();
    this.relationalModel.getCategories().clear();
    this.availableTables.clear();

    boolean needsUpConverted = false;
    if (upConvertDesired) needsUpConverted = upConvertLegacyModel();
    List<IAvailableItem> items = new ArrayList<IAvailableItem>();
    for (IPhysicalTable table : domain.getPhysicalModels().get(0).getPhysicalTables()) {
      boolean isFact = table.getProperty("FACT_TABLE") != null ? (Boolean) table.getProperty("FACT_TABLE") : false;
      items.add(new AvailableTable(table, isFact));
    }

    availableTables.setChildren(items);
    
    fireTablesChanged();

    LogicalModel lModel = domain.getLogicalModels().get(0);

    setModelName(lModel.getName(workspaceHelper.getLocale()));
    setRelationalModelName(lModel.getName(workspaceHelper.getLocale()));

    // Set the type of modeling session. This will propigate to the UI
    if(domain.getLogicalModels().get(0).getProperty("MondrianCatalogRef") != null){
      this.setModellingMode(ModelerMode.ANALYSIS_AND_REPORTING);
    } else {
      this.setModellingMode(ModelerMode.REPORTING_ONLY);
    }

    List<OlapDimension> theDimensions = (List) lModel.getProperty(LogicalModel.PROPERTY_OLAP_DIMS); //$NON-NLS-1$
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

    List<OlapCube> theCubes = (List) lModel.getProperty(LogicalModel.PROPERTY_OLAP_CUBES); //$NON-NLS-1$
    if (theCubes != null) {
      Iterator<OlapCube> theCubeItr = theCubes.iterator();
      while (theCubeItr.hasNext()) {
        OlapCube theCube = theCubeItr.next();

        List<OlapMeasure> theMeasures = theCube.getOlapMeasures();
        Iterator<OlapMeasure> theMeasuresItr = theMeasures.iterator();
        while (theMeasuresItr.hasNext()) {
          OlapMeasure theMeasure = theMeasuresItr.next();

          MeasureMetaData theMeasureMD = new MeasureMetaData(workspaceHelper.getLocale());

          if (theMeasure.getName() == null || theMeasure.getName().length() == 0) {
            theMeasureMD.setName(theMeasure.getLogicalColumn().getName(workspaceHelper.getLocale()));
          } else {
            theMeasureMD.setName(theMeasure.getName());
          }
          theMeasureMD.setFormat((String) theMeasure.getLogicalColumn().getProperty("mask")); //$NON-NLS-1$
          theMeasureMD.setDefaultAggregation(theMeasure.getLogicalColumn().getAggregationType());

          theMeasureMD.setLogicalColumn(theMeasure.getLogicalColumn());
          this.model.getMeasures().add(theMeasureMD);
        }
      }
    }

    int i = 1;
    for (Category cat : this.getDomain().getLogicalModels().get(0).getCategories()) {
      String catName = BaseModelerWorkspaceHelper.getCleanCategoryName(cat.getName(workspaceHelper.getLocale()), this, i++);
      CategoryMetaData catMeta = new CategoryMetaData(catName);
      for (LogicalColumn col : cat.getLogicalColumns()) {
        LogicalTable table = col.getLogicalTable();

        if (!table.getLogicalColumns().contains(col)) {
          table.addLogicalColumn(col);
        }
        
        Object formatMask = col.getProperty("mask");
        String colName = col.getName(workspaceHelper.getLocale());
        AggregationType aggType = col.getAggregationType();

        FieldMetaData field = new FieldMetaData(catMeta,
            colName,
            formatMask == null ? null : formatMask.toString(),
            colName,
            workspaceHelper.getLocale());
        if (aggType != null) {
          field.setDefaultAggregation(aggType);
        } else {
          field.setDefaultAggregation(AggregationType.NONE);
        }
        field.setLogicalColumn(col);

        catMeta.add(field);
      }
      this.getRelationalModel().getCategories().add(catMeta);
    }

    if (needsUpConverted) upConvertMeasuresAndDimensions();

    this.setModelIsChanging(false, true);
    this.setRelationalModelIsChanging(false, true);

  }

  private void upConvertMeasuresAndDimensions() {
    LogicalModel model = domain.getLogicalModels().get(0);

    // set the dimension logical column references to the new olap columns
    for (DimensionMetaData dim : getModel().getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (LevelMetaData level : hier) {
          // create new logical columns
          AvailableField field = new AvailableField(level.getLogicalColumn().getPhysicalColumn());
          ColumnBackedNode node = createColumnBackedNode(field, ModelerPerspective.ANALYSIS);
          level.setLogicalColumn(node.getLogicalColumn());
        }
      }
    }

    // set the measure logical column references to the new olap columns
    for (MeasureMetaData measure : getModel().getMeasures()) {
      AvailableField field = new AvailableField(measure.getLogicalColumn().getPhysicalColumn());
      ColumnBackedNode node = createColumnBackedNode(field, ModelerPerspective.ANALYSIS);
      measure.setLogicalColumn(node.getLogicalColumn());
    }

    return;
  }

  protected boolean upConvertLegacyModel() {
    // first, determine if we need to up-convert models created before
    // the separation of OLAP and Reporting models to the new style
    int olapTableCount=0, reportingTableCount=0;
    LogicalModel model = domain.getLogicalModels().get(0);
    for (LogicalTable table : model.getLogicalTables()) {
      if (table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
        olapTableCount++;
      } else {
        reportingTableCount++;
      }
    }
    if (olapTableCount == 0) {
      // need to forward port this model
      BaseModelerWorkspaceHelper.duplicateLogicalTablesForDualModelingMode(domain.getLogicalModels().get(0));
      return true;
    }

    return false;
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
      fireTablesChanged();
      model.validateTree();
      isValid();
      fireModelChanged();
    }
  }

  public void setRelationalModelIsChanging( boolean changing ) {
    setRelationalModelIsChanging(changing, true);
  }

  public void setRelationalModelIsChanging( boolean changing, boolean fireChanged ) {
    this.modelIsChanging = changing;
    if (!changing && fireChanged) {
      fireTablesChanged();
      relationalModel.validateTree();
      isValid();
      fireRelationalModelChanged();
    }
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

  @Bindable
  public AbstractMetaDataModelNode getSelectedRelationalNode() {
    return selectedRelationalNode;
  }

  @Bindable
  public void setSelectedRelationalNode( AbstractMetaDataModelNode node) {
    AbstractMetaDataModelNode prevVal = this.selectedRelationalNode;
    this.selectedRelationalNode = node;
    firePropertyChange("selectedRelationalNode", prevVal, node); //$NON-NLS-1$
  }

  public IModelerWorkspaceHelper getWorkspaceHelper() {
    return workspaceHelper;
  }

  public void setWorkspaceHelper( IModelerWorkspaceHelper workspaceHelper ) {
    this.workspaceHelper = workspaceHelper;
  }

  @Bindable
  public ModelerMode getModellingMode() {
    return currentModellingMode;
  }

  @Bindable
  public void setModellingMode(ModelerMode currentModellingMode) {
    ModelerMode prevVal = this.currentModellingMode;
    this.currentModellingMode = currentModellingMode;
    firePropertyChange("modellingMode", prevVal, this.currentModellingMode);
    isValid();
  }

  public ModelerPerspective getCurrentModelerPerspective() {
    return currentModelerPerspective;
  }

  public void setCurrentModelerPerspective(ModelerPerspective currentModelerPerspective) {
    this.currentModelerPerspective = currentModelerPerspective;
  }

  public ColumnBackedNode createColumnBackedNode(AvailableField field, ModelerPerspective perspective) {
    String locale = workspaceHelper.getLocale();
    ColumnBackedNode node = new BaseColumnBackedMetaData(field.getName());
    LogicalTable lTab = findLogicalTable(field.getPhysicalColumn().getPhysicalTable(), perspective);
    LogicalColumn lCol = null;

    if (perspective == ModelerPerspective.ANALYSIS) {
      // try to find the existing OLAP logical column, since we keep them around
      lCol = findLogicalColumn(field.getPhysicalColumn(), perspective);
    }

    if (lCol == null) {
      lCol = new LogicalColumn();
      lCol.setLogicalTable(lTab);
      lCol.setParentConcept(lTab);
      lCol.setPhysicalColumn(field.getPhysicalColumn());
      lCol.setDataType(field.getPhysicalColumn().getDataType());
      lCol.setAggregationList(field.getPhysicalColumn().getAggregationList());
      lCol.setAggregationType(field.getPhysicalColumn().getAggregationType());
      lCol.setName(new LocalizedString(locale, field.getPhysicalColumn().getName(locale)));

      String colId = "LC_" + lTab.getName(locale) + "_" + field.getName();

      if (perspective == ModelerPerspective.ANALYSIS) {
        colId += BaseModelerWorkspaceHelper.OLAP_SUFFIX;
      }

      colId = BaseModelerWorkspaceHelper.uniquify(colId, lTab.getLogicalColumns());
      lCol.setId(colId);

      lTab.addLogicalColumn(lCol);
    }

    node.setLogicalColumn(lCol);
    return node;
  }

  protected LogicalColumn findLogicalColumn(IPhysicalColumn column, ModelerPerspective perspective) {
    LogicalColumn col = null;
    IPhysicalTable physicalTable = column.getPhysicalTable();
    for (LogicalTable table : getDomain().getLogicalModels().get(0).getLogicalTables()) {
      if (table.getPhysicalTable().getId().equals(physicalTable.getId())) {
        if ((perspective == ModelerPerspective.ANALYSIS && table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX))
            || perspective == ModelerPerspective.REPORTING) {

          for (LogicalColumn lCol : table.getLogicalColumns()) {
            if (lCol.getPhysicalColumn().getId().equals(column.getId())) {
              return lCol;
            }
          }
          
        }
      }
    }

    return col;
  }

}
