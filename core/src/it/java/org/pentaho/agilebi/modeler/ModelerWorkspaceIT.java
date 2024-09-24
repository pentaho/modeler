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

import org.junit.Test;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.LogicalColumn;

import static junit.framework.Assert.*;

/**
 * Created: 4/1/11
 *
 * @author rfellows
 */
public class ModelerWorkspaceIT extends AbstractModelerTest {

  @Test
  public void testFindLogicalColumn() throws Exception {
    generateTestDomain();
    ModelerWorkspaceHelper workspaceHelper = new ModelerWorkspaceHelper( "en-US" );
    workspaceHelper.autoModelFlat( workspace );
    workspaceHelper.autoModelRelationalFlat( workspace );

    LogicalColumn logicalColumn = workspace.getModel().getMeasures().get( 0 ).getLogicalColumn();
    IPhysicalColumn physicalColumn = logicalColumn.getPhysicalColumn();

    LogicalColumn lCol = workspace.findLogicalColumn( physicalColumn, ModelerPerspective.ANALYSIS );
    assertSame( logicalColumn, lCol );

    lCol = workspace.findLogicalColumn( physicalColumn, ModelerPerspective.REPORTING );
    assertNotNull( lCol );
    assertEquals( physicalColumn, lCol.getPhysicalColumn() );
    assertNotSame( logicalColumn, lCol );
  }

}
