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


package org.pentaho.agilebi.modeler;

import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;

public interface ColumnBackedNode {
  public static final String COLUMN_TYPE_SOURCE = "source";
  public static final String COLUMN_TYPE_ORDINAL = "ordinal";
  public static final String COLUMN_TYPE_CAPTION = "caption";

  public void setLogicalColumn( LogicalColumn col );

  public LogicalColumn getLogicalColumn();

  public void setLogicalOrdinalColumn( LogicalColumn col );

  public LogicalColumn getLogicalOrdinalColumn();

  public void setLogicalCaptionColumn( LogicalColumn col );

  public LogicalColumn getLogicalCaptionColumn();

  public String getName();

  public IPhysicalTable getTableRestriction();
}
