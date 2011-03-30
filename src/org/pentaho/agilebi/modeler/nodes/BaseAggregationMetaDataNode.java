package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created: 3/24/11
 *
 * @author rfellows
 */
public class BaseAggregationMetaDataNode extends BaseColumnBackedMetaData {

  public static final String FORMAT_NONE = "NONE"; //$NON-NLS-1$

  protected String format = FORMAT_NONE;
  protected String fieldTypeDesc = "---";
  protected String levelTypeDesc = "---";
  protected String aggTypeDesc = null;
  protected String displayName;

  protected String locale;

  protected List<String> numericAggTypes = new ArrayList<String>();

  {
    setNumericAggTypes();
  }

  protected void setNumericAggTypes() {
    numericAggTypes.add("SUM");
    numericAggTypes.add("AVERAGE");
    numericAggTypes.add("MINIMUM");
    numericAggTypes.add("MAXIMUM");
    numericAggTypes.add("COUNT");
    numericAggTypes.add("COUNT_DISTINCT");
  }

  protected List<String> textAggTypes = new ArrayList<String>();

  {
    setTextAggTypes();
  }

  protected void setTextAggTypes() {
    textAggTypes.add("COUNT");
    textAggTypes.add("COUNT_DISTINCT");
  }

  public BaseAggregationMetaDataNode(String locale) {
    this.locale = locale;
  }
  public BaseAggregationMetaDataNode( String fieldName, String format, String displayName, String locale ) {
    super(fieldName);
    this.format = format;
    this.displayName = displayName;
    this.locale = locale;
  }

  @Bindable
  public void setName( String name ) {
    if (!(name == null)) {
      super.setName(name);
      if (logicalColumn != null) {
        logicalColumn.setName(new LocalizedString(locale, name));
      }
    }
  }

  @Bindable
  public String getFormat() {
    if (format == null || "".equals(format) || "#".equals(format)) {
      return FORMAT_NONE;
    }
    return format;
  }

  @Bindable
  public void setFormat( String format ) {
    this.format = format;
  }

  @Bindable
  public String getFieldTypeDesc() {
    return fieldTypeDesc;
  }

  @Bindable
  public void setFieldTypeDesc( String fieldTypeDesc ) {
    this.fieldTypeDesc = fieldTypeDesc;
  }

  @Bindable
  public String getLevelTypeDesc() {
    return levelTypeDesc;
  }

  @Bindable
  public void setLevelTypeDesc( String levelTypeDesc ) {
    this.levelTypeDesc = levelTypeDesc;
  }

  @Bindable
  public String getAggTypeDesc() {
    if (logicalColumn == null) {
      return null;
    }
    if (aggTypeDesc == null || "".equals(aggTypeDesc)) {
      switch (logicalColumn.getDataType()) {
        case NUMERIC:
          aggTypeDesc = "SUM";
          break;
        default:
          aggTypeDesc = "COUNT";
      }
    }
    return aggTypeDesc;
  }

  @Bindable
  public void setAggTypeDesc( String aggTypeDesc ) {
    this.aggTypeDesc = aggTypeDesc;
  }

  // TODO: generate this based on field type

  @Bindable
  public Vector getAggTypeDescValues() {
    if (logicalColumn == null) {
      return null;
    }
    if (logicalColumn.getDataType() == DataType.NUMERIC) {
      return new Vector<String>(numericAggTypes);
    } else {
      return new Vector<String>(textAggTypes);
    }
  }

  public boolean equals( BaseAggregationMetaDataNode o ) {
    if (o == null || o instanceof BaseAggregationMetaDataNode == false) {
      return false;
    }
    BaseAggregationMetaDataNode f = (BaseAggregationMetaDataNode) o;

    if (o == this) {
      return true;
    }

    if (f.getLogicalColumn() == null || this.getLogicalColumn() == null) {
      return false;
    }

    if (f.getLogicalColumn().getId().equals(this.getLogicalColumn().getId())) {
      return true;
    }
    return false;
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if (name == null || "".equals(name)) {
      validationMessages.add(
          "Measure Name Missing");//BaseMessages.getString(ModelerWorkspace.class, "measure_name_missing"));
//      validationMessages.add(BaseMessages.getString(ModelerWorkspace.class, "measure_name_missing"));
      valid = false;
    }
    if (logicalColumn == null) {
      validationMessages.add("The column mapped to the measure ("+getName()+") is missing or no longer available.");
//      validationMessages.add(BaseMessages.getString(ModelerWorkspace.class, "measure_column_missing", getName()));
      valid = false;
    }
  }

  public String toString() {
    return name;
  }

}
