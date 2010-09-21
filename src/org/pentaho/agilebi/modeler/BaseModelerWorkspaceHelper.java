package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public abstract class BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper{

  private static final List<AggregationType> DEFAULT_AGGREGATION_LIST = new ArrayList<AggregationType>();
  private static final List<AggregationType> DEFAULT_NON_NUMERIC_AGGREGATION_LIST = new ArrayList<AggregationType>();
  static {
    DEFAULT_AGGREGATION_LIST.add(AggregationType.NONE);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.SUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.AVERAGE);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.MINIMUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.MAXIMUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.COUNT);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.COUNT_DISTINCT);

    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add(AggregationType.NONE);
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add(AggregationType.COUNT);
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add(AggregationType.COUNT_DISTINCT);

  }
  
  public void populateDomain(ModelerWorkspace model) throws ModelerException {

    Domain domain = model.getDomain();
    domain.setId( model.getModelName() );

    List<Category> cats = domain.getLogicalModels().get(0).getCategories();
    LogicalTable logicalTable = domain.getLogicalModels().get(0).getLogicalTables().get(0);

    if (model.getModelSource() != null) {
      model.getModelSource().serializeIntoDomain(domain);
    }

    LogicalModel logicalModel = domain.getLogicalModels().get(0);
    logicalModel.setId("MODEL_1");
    logicalModel.setName( new LocalizedString( LocaleHolder.getLocaleAsString(), model.getModelName() ) );

    Category cat;
    // Find existing category or create new one

    if (cats.size() > 0) {
      cat = cats.get(0);
    } else {
      cat = new Category();
      logicalModel.addCategory(cat);
    }
    cat.setId(model.getModelName());
    cat.getLogicalColumns().clear();

    // Add all measures
    for (MeasureMetaData f : model.getModel().getMeasures()) {
      if (f.getLogicalColumn() == null) {
        continue;
      }
      LogicalColumn lCol = logicalModel.findLogicalColumn(f.getLogicalColumn().getId());
      if (cat.getLogicalColumns().contains(lCol)) {
        // clone the logical column
        // all measures must have a unique logical column
        // because of different names and aggregates
        lCol = (LogicalColumn)lCol.clone();
        lCol.setId(uniquify(lCol.getId(), logicalModel.getLogicalTables().get(0).getLogicalColumns()));
        logicalModel.getLogicalTables().get(0).addLogicalColumn(lCol);
        f.setLogicalColumn(lCol);
      }

      lCol.setName(new LocalizedString(LocaleHolder.getLocaleAsString(), f.getName()));
      AggregationType type = AggregationType.valueOf(f.getAggTypeDesc());
      if (type != AggregationType.NONE) {
        lCol.setAggregationType(type);
      }

      // set the format mask

      String formatMask = f.getFormat();
      if( MeasureMetaData.FORMAT_NONE.equals(formatMask) || (formatMask == null || formatMask.equals(""))) {
        formatMask = null;
      }
      if (formatMask != null) {
        lCol.setProperty("mask", formatMask); //$NON-NLS-1$
      } else if(lCol.getDataType() == DataType.NUMERIC){
        lCol.setProperty("mask", "#");
      } else {
        // remove old mask that might have been set
        if (lCol.getChildProperty("mask") != null) { //$NON-NLS-1$
          lCol.removeChildProperty("mask"); //$NON-NLS-1$
        }
      }

      // All Measures get a list of aggs to choose from within metadata
      // eventually this will be customizable

      if (lCol.getDataType() != DataType.NUMERIC) {
        lCol.setAggregationList(DEFAULT_NON_NUMERIC_AGGREGATION_LIST);
      } else {
        lCol.setAggregationList(DEFAULT_AGGREGATION_LIST);
      }
      cat.addLogicalColumn(lCol);
    }

    // Add levels
    for (DimensionMetaData dim : model.getModel().getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (int j = 0; j < hier.size(); j++) {
          LevelMetaData level = hier.get(j);
          if (level.getLogicalColumn() == null) {
            continue;
          }
          LogicalColumn lCol = logicalModel.findLogicalColumn(level.getLogicalColumn().getId());
          if(cat.getLogicalColumns().contains(lCol)){
            continue;
          }
          lCol.setName(new LocalizedString(LocaleHolder.getLocaleAsString(), level.getName()));
          if (cat.findLogicalColumn(lCol.getId()) == null) {
            cat.addLogicalColumn(lCol);
          }
        }
      }
    }

    // =========================== OLAP ===================================== //


      List<OlapDimensionUsage> usages = new ArrayList<OlapDimensionUsage>();
      List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
      List<OlapMeasure> measures = new ArrayList<OlapMeasure>();

      for (DimensionMetaData dim : model.getModel().getDimensions()) {

        OlapDimension dimension = new OlapDimension();
        String dimTitle = dim.getName();

        dimension.setName(dimTitle);
        dimension.setTimeDimension(dim.isTime());

        List<OlapHierarchy> hierarchies = new ArrayList<OlapHierarchy>();

        for (HierarchyMetaData hier : dim) {
          OlapHierarchy hierarchy = new OlapHierarchy(dimension);
          hierarchy.setName(hier.getName());
          hierarchy.setLogicalTable(logicalTable);
          List<OlapHierarchyLevel> levels = new ArrayList<OlapHierarchyLevel>();

          for (LevelMetaData lvl : hier) {
            OlapHierarchyLevel level = new OlapHierarchyLevel(hierarchy);
            level.setName(lvl.getName());
            if (lvl.getLogicalColumn() != null) {
              LogicalColumn lvlColumn = logicalModel.findLogicalColumn(lvl.getLogicalColumn().getId());
              level.setReferenceColumn(lvlColumn);
            }
            level.setHavingUniqueMembers(lvl.isUniqueMembers());
            levels.add(level);
          }

          hierarchy.setHierarchyLevels(levels);
          hierarchies.add(hierarchy);
        }

        if(hierarchies.isEmpty()) {
          // create a default hierarchy
          OlapHierarchy defaultHierarchy = new OlapHierarchy(dimension);
          defaultHierarchy.setLogicalTable(logicalTable);
          hierarchies.add(defaultHierarchy);
        }

        dimension.setHierarchies(hierarchies);

        olapDimensions.add(dimension);
        OlapDimensionUsage usage = new OlapDimensionUsage(dimension.getName(), dimension);
        usages.add(usage);

      }

      OlapCube cube = new OlapCube();
      cube.setLogicalTable(logicalTable);
      // TODO find a better way to generate default names
      //cube.setName( BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.CubeName", model.getModelName() ) ); //$NON-NLS-1$
      cube.setName( model.getModelName() ); //$NON-NLS-1$
      cube.setOlapDimensionUsages(usages);

      for (MeasureMetaData f : model.getModel().getMeasures()) {

        OlapMeasure measure = new OlapMeasure();
        if (f.getAggTypeDesc() != null) {
          f.getLogicalColumn().setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
        }
        measure.setLogicalColumn(f.getLogicalColumn());
        measures.add(measure);
      }

      cube.setOlapMeasures(measures);

      LogicalModel lModel = domain.getLogicalModels().get(0);

      if (olapDimensions.size() > 0) { // Metadata OLAP generator doesn't like empty lists.
        lModel.setProperty("olap_dimensions", olapDimensions); //$NON-NLS-1$
      }
      List<OlapCube> cubes = new ArrayList<OlapCube>();
      cubes.add(cube);
      lModel.setProperty("olap_cubes", cubes); //$NON-NLS-1$
  }

  public static final String uniquify(final String id, final List<? extends IConcept> concepts) {
    boolean gotNew = false;
    boolean found = false;
    int conceptNr = 1;
    String newId = id;
    while (!gotNew) {
      for (IConcept concept : concepts) {
        if (concept.getId().equalsIgnoreCase(newId)) {
          found = true;
          break;
        }
      }
      if (found) {
        conceptNr++;
        newId = id + "_" + conceptNr; //$NON-NLS-1$
        found = false;
      }else{
        gotNew = true;
      }
    }
    return newId;
  }
}
