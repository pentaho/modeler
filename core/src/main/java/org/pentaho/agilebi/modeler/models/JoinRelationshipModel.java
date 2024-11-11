/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.models;

import java.io.Serializable;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class JoinRelationshipModel extends XulEventSourceAdapter implements Serializable {
  private static final long serialVersionUID = -1365363591444333452L;
  private JoinFieldModel leftKeyFieldModel;
  private JoinFieldModel rightKeyFieldModel;

  public JoinRelationshipModel() {

  }

  @Bindable
  public JoinFieldModel getLeftKeyFieldModel() {
    return this.leftKeyFieldModel;
  }

  @Bindable
  public void setLeftKeyFieldModel( JoinFieldModel leftKeyFieldModel ) {
    this.leftKeyFieldModel = leftKeyFieldModel;
  }

  @Bindable
  public JoinFieldModel getRightKeyFieldModel() {
    return this.rightKeyFieldModel;
  }

  @Bindable
  public void setRightKeyFieldModel( JoinFieldModel rightKeyFieldModel ) {
    this.rightKeyFieldModel = rightKeyFieldModel;
  }

  @Bindable
  public String getName() {
    String innerJoinLabel = ModelerMessagesHolder.getMessages().getString( "multitable.INNER_JOIN" );
    String leftTable = this.leftKeyFieldModel.getParentTable().getName();
    String rightTable = this.rightKeyFieldModel.getParentTable().getName();
    StringBuffer joinName = new StringBuffer();
    joinName.append( leftTable );
    joinName.append( "." );
    joinName.append( this.leftKeyFieldModel.getName() );
    joinName.append( " - " );
    joinName.append( innerJoinLabel );
    joinName.append( " - " );
    joinName.append( rightTable );
    joinName.append( "." );
    joinName.append( this.rightKeyFieldModel.getName() );
    return joinName.toString();
  }

  public boolean equals( JoinRelationshipModel join ) {

    String leftTable1 = join.getLeftKeyFieldModel().getParentTable().getName();
    String rightTable1 = join.getRightKeyFieldModel().getParentTable().getName();
    String leftTable2 = this.leftKeyFieldModel.getParentTable().getName();
    String rightTable2 = this.rightKeyFieldModel.getParentTable().getName();

    // eval1
    // join1:table1 = table2
    // join2:table2 = table1
    boolean eval1 = leftTable1.equals( rightTable2 ) && leftTable2.equals( rightTable1 );
    // eval2
    // join1:table1 = table2
    // join2:table1 = table2
    boolean eval2 = leftTable1.equals( leftTable2 ) && rightTable1.equals( rightTable2 );
    return eval1 || eval2;
  }
}
