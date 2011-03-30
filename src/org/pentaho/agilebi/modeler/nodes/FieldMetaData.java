package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.propforms.FieldsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/18/11
 *
 * @author rfellows
 */
public class FieldMetaData extends BaseAggregationMetaDataNode {

  private static final String IMAGE = "images/fields.png";
  private static final long serialVersionUID = -7091129923372909756L;
  private CategoryMetaData parent;

  public FieldMetaData( String locale ) {
    super(locale);
  }

  public FieldMetaData(CategoryMetaData parent, String fieldName, String format, String displayName, String locale ) {
    super(fieldName, format, displayName, locale);
    this.parent = parent;
  }

  @Override
  protected void setNumericAggTypes() {
    numericAggTypes.add("NONE");
    numericAggTypes.add("SUM");
    numericAggTypes.add("AVERAGE");
    numericAggTypes.add("MINIMUM");
    numericAggTypes.add("MAXIMUM");
    numericAggTypes.add("COUNT");
    numericAggTypes.add("COUNT_DISTINCT");
  }


  @Override
  protected void setTextAggTypes() {
    textAggTypes.add("NONE");
    textAggTypes.add("COUNT");
    textAggTypes.add("COUNT_DISTINCT");
  }

  @Bindable
  public String getAggTypeDesc() {
    if (logicalColumn == null) {
      return null;
    }
    if (aggTypeDesc == null || "".equals(aggTypeDesc)) {
      switch (logicalColumn.getDataType()) {
        case NUMERIC:
          aggTypeDesc = "NONE";
          break;
        default:
          aggTypeDesc = "NONE";
      }
    }
    return aggTypeDesc;
  }

  public CategoryMetaData getParent() {
    return parent;
  }

  public void setParent( CategoryMetaData md ) {
    this.parent = md;
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm<BaseAggregationMetaDataNode>> getPropertiesForm() {
    return FieldsPropertiesForm.class;
  }

}
