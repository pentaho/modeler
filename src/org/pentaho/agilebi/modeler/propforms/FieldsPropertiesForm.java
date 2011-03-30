package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/24/11
 *
 * @author rfellows
 */
public class FieldsPropertiesForm extends MeasuresPropertiesForm {

  private static final String ID = "fieldprops";

  public FieldsPropertiesForm(String locale) {
    super(ID, locale);
  }

  public FieldsPropertiesForm(String panelId, String locale) {
    super(panelId, locale);
  }

  @Override
  @Bindable
  public void init() {
    deck = (XulDeck) document.getElementById("propertiesdeck");
    panel = (XulVbox) document.getElementById(ID);

    bf.createBinding(this, "notValid", "fieldmessages", "visible");
    bf.createBinding(this, "validMessages", "fieldmessageslabel", "value");
    bf.createBinding(this, "displayName", "fielddisplayname", "value");
    bf.createBinding(this, "aggTypes", "fieldaggregationtype", "elements");
    bf.createBinding(this, "aggTypeDesc", "fieldaggregationtype", "selectedItem");
    bf.createBinding(this, "format", "fieldformatstring", "selectedItem", new FormatStringConverter());
    bf.createBinding(this, "notValid", "fixFieldColumnsBtn", "visible");
    bf.createBinding(this, "columnName", "field_column_name", "value");

  }

  private static class FormatStringConverter extends BindingConvertor<String, String> {

    @Override
    public String sourceToTarget( String value ) {
      if (value == null) {
        return "NONE";
      } else {
        return value;
      }
    }

    @Override
    public String targetToSource( String value ) {
      if (value.equalsIgnoreCase("NONE")) {
        return null;
      } else {
        return value;
      }
    }

  }
}
