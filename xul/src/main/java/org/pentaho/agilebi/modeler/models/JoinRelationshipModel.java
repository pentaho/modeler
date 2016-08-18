/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
