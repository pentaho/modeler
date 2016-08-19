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
