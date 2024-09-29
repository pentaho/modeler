/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.agilebi.modeler.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker Date: 4/16/11
 */
public class SchemaModel implements Serializable {
  private static final long serialVersionUID = -3897309328565658403L;
  private JoinTableModel factTable;
  private List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

  public SchemaModel() {

  }

  public void setFactTable( JoinTableModel factTable ) {
    this.factTable = factTable;
  }

  public JoinTableModel getFactTable() {
    return this.factTable;
  }

  public List<JoinRelationshipModel> getJoins() {
    return joins;
  }

  public void setJoins( List<JoinRelationshipModel> joins ) {
    this.joins = joins;
  }
}
