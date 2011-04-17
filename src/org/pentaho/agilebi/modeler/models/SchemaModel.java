package org.pentaho.agilebi.modeler.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker
 * Date: 4/16/11
 */
public class SchemaModel implements Serializable {
  private List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

  public SchemaModel(){

  }

  public List<JoinRelationshipModel> getJoins() {
    return joins;
  }

  public void setJoins(List<JoinRelationshipModel> joins) {
    this.joins = joins;
  }
}
