package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;

/**
 * Created: 3/18/11
 *
 * @author rfellows
 */
public class CategoryPropertiesForm extends AbstractModelerNodeForm<CategoryMetaData> {

  private static final String ID = "categoryprops";

  public CategoryPropertiesForm() {
    super(ID);
  }

  public void setObject(CategoryMetaData categoryMetaData) {

  }
}
