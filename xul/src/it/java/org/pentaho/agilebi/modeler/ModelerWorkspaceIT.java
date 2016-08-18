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
