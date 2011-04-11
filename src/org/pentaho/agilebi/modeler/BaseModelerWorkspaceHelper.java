package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.metadata.model.*;
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

import java.util.*;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public abstract class BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper{

  private static final List<AggregationType> DEFAULT_AGGREGATION_LIST = new ArrayList<AggregationType>();
  private static final List<AggregationType> DEFAULT_NON_NUMERIC_AGGREGATION_LIST = new ArrayList<AggregationType>();
  private static String locale;
  public static final String OLAP_SUFFIX = "_OLAP";

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

  public BaseModelerWorkspaceHelper(String locale){
    BaseModelerWorkspaceHelper.locale = locale;
  }
  
  public void populateDomain(ModelerWorkspace model) throws ModelerException {

    Domain domain = model.getDomain();
    domain.setId( model.getModelName() );

    LogicalTable logicalTable = domain.getLogicalModels().get(0).getLogicalTables().get(0);

    if (model.getModelSource() != null) {
      model.getModelSource().serializeIntoDomain(domain);
    }

    LogicalModel logicalModel = domain.getLogicalModels().get(0);
    logicalModel.setId("MODEL_1");
    logicalModel.setName( new LocalizedString(locale, model.getModelName() ) );

    populateCategories(model);



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
            LogicalColumn lCol = lvl.getLogicalColumn();
            if (lCol != null) {
              LogicalTable lTable = lCol.getLogicalTable();

              if (!lTable.getLogicalColumns().contains(lCol)) {
                lTable.addLogicalColumn(lCol);
              }
              level.setReferenceColumn(lCol);
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
        LogicalColumn lCol = f.getLogicalColumn();
        LogicalTable lTable = lCol.getLogicalTable();

        if (!lTable.getLogicalColumns().contains(lCol)) {
          lTable.addLogicalColumn(lCol);
        }

        OlapMeasure measure = new OlapMeasure();
        if (f.getAggTypeDesc() != null) {
          lCol.setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
        }
        measure.setLogicalColumn(lCol);
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

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    BaseModelerWorkspaceHelper.locale = locale;
  }

  protected void populateCategories(ModelerWorkspace workspace) {
    RelationalModelNode model = workspace.getRelationalModel();
    LogicalModel logicalModel = workspace.getDomain().getLogicalModels().get(0);
    logicalModel.getCategories().clear();

    for (CategoryMetaData catMeta : model.getCategories()) {
      Category cat = new Category();
      cat.setName(new LocalizedString(this.getLocale(), catMeta.getName()));
      cat.setId(catMeta.getName());

      for (FieldMetaData fieldMeta : catMeta) {
        LogicalColumn lCol = fieldMeta.getLogicalColumn();
        LogicalTable lTable = lCol.getLogicalTable();

        if (!lTable.getLogicalColumns().contains(lCol)) {
          lTable.addLogicalColumn(lCol);
        }

        lCol.setName(new LocalizedString(locale, fieldMeta.getName()));
        AggregationType type = AggregationType.valueOf(fieldMeta.getAggTypeDesc());
        lCol.setAggregationType(type);

        String formatMask = fieldMeta.getFormat();
        if( BaseAggregationMetaDataNode.FORMAT_NONE.equals(formatMask) || (formatMask == null || formatMask.equals(""))) {
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

        if (lCol.getDataType() != DataType.NUMERIC) {
          lCol.setAggregationList(DEFAULT_NON_NUMERIC_AGGREGATION_LIST);
        } else {
          lCol.setAggregationList(DEFAULT_AGGREGATION_LIST);
        }
        cat.addLogicalColumn(lCol);

      }
      logicalModel.addCategory(cat);
    }
  }


  public static String getCorrespondingOlapColumnId(LogicalColumn lc) {
    String tableId = lc.getLogicalTable().getId().replaceAll("BT", "LC");
    String olapColId = tableId + OLAP_SUFFIX + "_" + lc.getPhysicalColumn().getId();
    return olapColId;
  }

  public static void duplicateLogicalTablesForDualModelingMode(LogicalModel model) {
    String locale = "en-US";
    int tableCount = model.getLogicalTables().size();
    for (int i = 0; i < tableCount; i++) {
      LogicalTable table = model.getLogicalTables().get(i);
      LogicalTable copiedTable = (LogicalTable) table.clone();
      copiedTable.setId(copiedTable.getId() + BaseModelerWorkspaceHelper.OLAP_SUFFIX);
      model.addLogicalTable(copiedTable);
    }
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {

    MainModelNode mainModel = getMainModelNode(workspace);
    mainModel.setName(workspace.getModelName());
    workspace.setModel(mainModel);

    final boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);

    // remove all logical columns from existing logical tables
    for (LogicalTable table : workspace.getDomain().getLogicalModels().get(0).getLogicalTables()) {
      if (table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
        table.getLogicalColumns().clear();
      }
    }

    List<AvailableField> fields = workspace.getAvailableFields();
    for( AvailableField field : fields ) {
      LogicalTable parentTable = workspace.findLogicalTable(field.getPhysicalColumn().getPhysicalTable(), ModelerPerspective.ANALYSIS);
      DataType dataType = field.getPhysicalColumn().getDataType();
      if( dataType == DataType.NUMERIC) {
        // create a measure
        MeasureMetaData measure = workspace.createMeasureForNode(field);
        workspace.getModel().getMeasures().add(measure);
      }
      // create a dimension
      workspace.addDimensionFromNode(workspace.createColumnBackedNode(field, ModelerPerspective.ANALYSIS));

    }
    workspace.setModelIsChanging(prevChangeState);
    workspace.setSelectedNode(workspace.getModel());
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlatInBackground( final ModelerWorkspace workspace ) throws ModelerException {
//    throw new UnsupportedOperationException("Not available outside of Spoon");
    autoModelFlat(workspace);
  }
  public void sortFields( List<AvailableField> availableFields) {
    Collections.sort(availableFields, new Comparator<AvailableField>() {
      public int compare(AvailableField o1, AvailableField o2) {
        if (o1 == null && o2 == null) {
          return 0;
        } else if (o1 == null) {
          return -1;
        } else if (o2 == null) {
          return 1;
        }
        String name1 = ((AvailableField) o1).getDisplayName();
        String name2 = ((AvailableField) o2).getDisplayName();
        if (name1 == null && name2 == null) {
          return 0;
        } else if (name1 == null) {
          return -1;
        } else if (name2 == null) {
          return 1;
        }
        return name1.compareToIgnoreCase(name2);
      }
    });
  }

  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlat(ModelerWorkspace workspace) throws ModelerException {
    RelationalModelNode relationalModelNode = getRelationalModelNode(workspace);

    relationalModelNode.setName(workspace.getRelationalModelName());

    workspace.setRelationalModel(relationalModelNode);
    final boolean prevChangeState = workspace.isModelChanging();

    workspace.setRelationalModelIsChanging(true);

    // remove all logical columns from existing logical tables
    for (LogicalTable table : workspace.getDomain().getLogicalModels().get(0).getLogicalTables()) {
      if (!table.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
        table.getLogicalColumns().clear();
      }
    }

    List<? extends IPhysicalTable> tables = workspace.getDomain().getPhysicalModels().get(0).getPhysicalTables();
    Set<String> tableIds = new HashSet<String>();
    for (IPhysicalTable table : tables) {
      if (!tableIds.contains(table.getId())) {
        tableIds.add(table.getId());
        String catName = getCleanCategoryName(table.getName(getLocale()), workspace, tableIds.size());
        CategoryMetaData category = new CategoryMetaData(catName);

        List<AvailableField> fields = workspace.getAvailableFields();
        for( AvailableField field : fields ) {
          if (field.getPhysicalColumn().getPhysicalTable().getId().equals(table.getId())) {
            category.add(workspace.createFieldForParentWithNode(category, field));
          }
        }
        relationalModelNode.getCategories().add(category);
      }
    }

    workspace.setRelationalModelIsChanging(prevChangeState);
    workspace.setSelectedNode(workspace.getRelationalModel());

  }

  /**
   * Builds a Relational Model from the existing collection of logical tables
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelMultiTableRelational(ModelerWorkspace workspace) throws ModelerException {
    autoModelRelationalFlat(workspace);
  }


  private FieldMetaData createFieldForCategoryWithColumn( CategoryMetaData parent, LogicalColumn column ) {
    FieldMetaData field = new FieldMetaData(parent, column.getName(getLocale()), "",
        column.getName(getLocale()), getLocale()); //$NON-NLS-1$
    field.setLogicalColumn(column);
    field.setFieldTypeDesc(column.getDataType().getName());
    parent.add(field);
    return field;
  }



  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlatInBackground(ModelerWorkspace workspace) throws ModelerException {
    autoModelRelationalFlat(workspace);
  }

  public static String getCleanCategoryName(String name, ModelerWorkspace workspace, int index) {
    if (name == null) {
      return "Category " + index;
    } else if (name.equals("LOGICAL_TABLE_1") || name.equals("INLINE_SQL_1")) {
      if (workspace.getModel().getName() != null) {
        return workspace.getModel().getName();
      } else {
        return "Category " + index;
      }
    } else {
      return name;
    }
  }

  protected abstract MainModelNode getMainModelNode(ModelerWorkspace workspace);
  protected abstract RelationalModelNode getRelationalModelNode(ModelerWorkspace workspace);

}
