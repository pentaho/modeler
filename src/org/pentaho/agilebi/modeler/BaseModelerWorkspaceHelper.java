package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.strategy.AutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.*;

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

  private AutoModelStrategy autoModelStrategy;

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
    autoModelStrategy = new SimpleAutoModelStrategy(locale);
  }

  public void populateDomain(ModelerWorkspace model) throws ModelerException {

    Domain domain = model.getDomain();
    domain.setId(model.getModelName());

    LogicalModel logicalModel = domain.getLogicalModels().get(0);

    if (model.getModelSource() != null) {
      model.getModelSource().serializeIntoDomain(domain);
    }

    logicalModel.setId("MODEL_1");
    logicalModel.setName( new LocalizedString(locale, model.getModelName() ) );
    logicalModel.setProperty("AGILE_BI_VERSION","2.0");

    populateCategories(model);

    
    // =========================== OLAP ===================================== //


    if(model.getModel() != null && model.getModel().getDimensions().size() > 0) {
	    LogicalTable factTable = null;
      //check to see if there's only one effective table
      if(logicalModel.getLogicalTables().size() <= 2){
        for(LogicalTable lTable : logicalModel.getLogicalTables()) {
            if(lTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
                factTable = lTable;
            }
          }
      } else { // otherwise we're in a multi-table situation, find the table flagged as the fact table
        for(LogicalTable lTable : logicalModel.getLogicalTables()) {
            if(lTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
              boolean isFact = lTable.getPhysicalTable().getProperty("FACT_TABLE") != null ? (Boolean) lTable.getPhysicalTable().getProperty("FACT_TABLE") : false;
              if(isFact) {
                  factTable = lTable;
                break;
              }
            }
          }
      }

      if(factTable == null) {
        throw new IllegalStateException("Fact table is missing.");
      }
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
          List<OlapHierarchyLevel> levels = new ArrayList<OlapHierarchyLevel>();

          for (LevelMetaData lvl : hier) {
            OlapHierarchyLevel level = new OlapHierarchyLevel(hierarchy);
            level.setName(lvl.getName());
            LogicalColumn lCol = lvl.getLogicalColumn();

            if (lCol != null) {

              // Due to a bug in LogicalTable's clone() logical columns will be a child of an OLAP while reporting a
              // different parent.
              LogicalTable supposedLTable = lCol.getLogicalTable();
              LogicalTable olapCloneLTable = findOlapCloneForTableInDomain(supposedLTable, domain);
              hierarchy.setLogicalTable(olapCloneLTable);
              if (!olapCloneLTable.getLogicalColumns().contains(lCol)) {
                olapCloneLTable.addLogicalColumn(lCol);
              }

              for(IMemberAnnotation anno : lvl.getMemberAnnotations().values()){
                if(anno != null){
                  anno.saveAnnotations(level);
                }
              }


              level.setReferenceColumn(lCol);
              hierarchy.setLogicalTable(olapCloneLTable);
              if(logicalModel.getLogicalTables().size() > 2){ //only do this for multi-table situations
                hierarchy.setPrimaryKey(findPrimaryKeyFor(logicalModel, factTable, olapCloneLTable));
              }
            }

            for (MemberPropertyMetaData memberProp : lvl) {
              LogicalColumn lc = memberProp.getLogicalColumn();
              if ( lc != null && !level.getLogicalColumns().contains(lc) ) {
                level.getLogicalColumns().add(lc);
              }
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

          defaultHierarchy.setLogicalTable(factTable);  // TODO: set this to what???

          hierarchies.add(defaultHierarchy);
        }

        dimension.setHierarchies(hierarchies);

        olapDimensions.add(dimension);
        OlapDimensionUsage usage = new OlapDimensionUsage(dimension.getName(), dimension);
        usages.add(usage);

      }
      
      OlapCube cube = new OlapCube();	  
      cube.setLogicalTable(factTable);
      // TODO find a better way to generate default names
      //cube.setName( BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.CubeName", model.getModelName() ) ); //$NON-NLS-1$
      cube.setName( model.getModelName() ); //$NON-NLS-1$
      cube.setOlapDimensionUsages(usages);

      Map<String, LogicalColumn> backingColumns = new HashMap<String, LogicalColumn>();
      for (MeasureMetaData f : model.getModel().getMeasures()) {
        LogicalColumn lCol = f.getLogicalColumn();
        LogicalTable lTable = lCol.getLogicalTable();
        OlapMeasure measure = new OlapMeasure();

        String colKey = lTable.getId() + "." + lCol.getId();
        // see if any measures already are using this LogicalColumn. if so, clone it.
        if (backingColumns.containsKey(colKey)) {
          // already used, duplicate it
          LogicalColumn clone = (LogicalColumn)lCol.clone();
          clone.setId(uniquify(clone.getId(), lTable.getLogicalColumns()));
          lCol = clone;
        } else {
          backingColumns.put(colKey, lCol);
        }

        if (!lTable.getLogicalColumns().contains(lCol)) {
          lTable.addLogicalColumn(lCol);
        }

        if (f.getDefaultAggregation() != null) {
          lCol.setAggregationType(f.getDefaultAggregation());
        }

        setLogicalColumnFormat(f.getFormat(), lCol);
        
        measure.setName(f.getName());

        measure.setLogicalColumn(lCol);
        measures.add(measure);
      }

      cube.setOlapMeasures(measures);

      if (olapDimensions.size() > 0) { // Metadata OLAP generator doesn't like empty lists.
    	  logicalModel.setProperty("olap_dimensions", olapDimensions); //$NON-NLS-1$
      }
      List<OlapCube> cubes = new ArrayList<OlapCube>();
      cubes.add(cube);
      logicalModel.setProperty("olap_cubes", cubes); //$NON-NLS-1$
    }

  }

  private String combineRequiredParents(GeoRole role) {
    if(role.getRequiredParentRoles().size() > 0) {
      StringBuffer sb = new StringBuffer();
      for(GeoRole r : role.getRequiredParentRoles()) {
        if(sb.length() > 0) {
          sb.append(",");
        }
        sb.append(r.getName());
      }
      return sb.toString();
    }
    return null;
  }

  private LogicalTable findOlapCloneForTableInDomain(LogicalTable supposedLTable, Domain domain) {
    if(supposedLTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)){
      return supposedLTable;
    }
    for(LogicalTable table : domain.getLogicalModels().get(0).getLogicalTables()){
      if(table.getId().equals(supposedLTable.getId() + BaseModelerWorkspaceHelper.OLAP_SUFFIX)){
        return table;
      }
    }
    throw new IllegalStateException("Unable to find a OLAP copy for table: "+supposedLTable.getId());
  }

  private LogicalColumn findPrimaryKeyFor(LogicalModel model, LogicalTable factTable, LogicalTable dimTable) {
    LogicalRelationship ship = model.findRelationshipUsing(dimTable, factTable);
    if(ship == null){
      throw new IllegalStateException("Unable to find a primary key for table: "+dimTable.getId());
    }
    
    if (ship.getFromTable().equals(dimTable)){
      return ship.getFromColumn();
    } else {
      return ship.getToColumn();
    }
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
        AggregationType type = fieldMeta.getDefaultAggregation();
        lCol.setAggregationType(type);

        setLogicalColumnFormat(fieldMeta.getFormat(), lCol);

        Set<AggregationType> possibleAggs = new HashSet<AggregationType>();
        possibleAggs.add(fieldMeta.getDefaultAggregation());
        possibleAggs.addAll(fieldMeta.getSelectedAggregations());
        lCol.setAggregationList(Arrays.<AggregationType>asList(possibleAggs.toArray(new AggregationType[possibleAggs.size()])));
        cat.addLogicalColumn(lCol);

      }
      logicalModel.addCategory(cat);
    }
  }


  private void setLogicalColumnFormat(String format, LogicalColumn lCol) {
    String formatMask = format;
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
    autoModelStrategy.autoModelOlap(workspace, getMainModelNode(workspace));
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlatInBackground( final ModelerWorkspace workspace ) throws ModelerException {
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
    autoModelStrategy.autoModelRelational(workspace, getRelationalModelNode(workspace));
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

  public AutoModelStrategy getAutoModelStrategy() {
    return autoModelStrategy;
  }

  public void setAutoModelStrategy(AutoModelStrategy autoModelStrategy) {
    this.autoModelStrategy = autoModelStrategy;
  }
}
