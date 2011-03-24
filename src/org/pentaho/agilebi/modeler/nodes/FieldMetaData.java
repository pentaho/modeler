package org.pentaho.agilebi.modeler.nodes;

import org.pentaho.agilebi.modeler.propforms.LevelsPropertiesForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;

import java.io.Serializable;

/**
 * Created: 3/18/11
 *
 * @author rfellows
 */
public class FieldMetaData extends BaseColumnBackedMetaData<CategoryMetaData> implements Serializable {

  private static final String IMAGE = "images/fields.png";
  private static final long serialVersionUID = -7091129923372909756L;

  public FieldMetaData(){
    super();
  }

  public FieldMetaData( CategoryMetaData parent, String name ) {
    super(parent, name);
  }

  @Override
  public String getValidImage() {
    return IMAGE;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return LevelsPropertiesForm.class;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((columnName == null) ? 0 : columnName.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result
        + ((uniqueMembers == null) ? 0 : uniqueMembers.hashCode());
    return result;
  }

  public boolean equals( FieldMetaData obj ) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FieldMetaData other = (FieldMetaData) obj;
    if (columnName == null) {
      if (other.columnName != null) {
        return false;
      }
    } else if (!columnName.equals(other.columnName)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (parent == null) {
      if (other.parent != null) {
        return false;
      }
    } else if (!parent.equals(other.parent)) {
      return false;
    }
    return true;
  }


}
