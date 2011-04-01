/*
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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import static junit.framework.Assert.*;

/**
 * Created: 4/1/11
 *
 * @author rfellows
 */
public class ModelerWorkspaceTest {
  private static final String LOCALE = "en-US";
  RelationalModelNode relationalModelNode;
  static ModelerWorkspace workspace;

  @BeforeClass
  public static void init() throws Exception {
    System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    workspace = new ModelerWorkspace(new ModelerWorkspaceHelper(LOCALE));
    KettleEnvironment.init();
    Props.init(Props.TYPE_PROPERTIES_EMPTY);
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testUpConvertLegacyModel() throws Exception {
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi(new FileInputStream("test-res/products.xmi"));
    LogicalModel model = d.getLogicalModels().get(0);
    workspace.setDomain(d, false);

    assertEquals(0, workspace.getAvailableOlapFields().size());
    assertEquals(1, model.getLogicalTables().size());

    workspace.upConvertLegacyModel();

    assertEquals(2, model.getLogicalTables().size());

  }

  @Test
  public void testSetDomain_NeedsUpConverted() throws Exception {
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi(new FileInputStream("test-res/products.xmi"));
    LogicalModel model = d.getLogicalModels().get(0);
    workspace.setDomain(d);

    assertEquals(workspace.getAvailableFields().size(), workspace.getAvailableOlapFields().size());
    assertEquals(2, model.getLogicalTables().size());

    // verify the OLAP measures & dimensions get their logical columns set to the new OLAP table's columns
    for (DimensionMetaData dim : workspace.getModel().getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (LevelMetaData level : hier) {
          assertTrue(isLogicalColumnReferencedInAvailableOlapFields(level.getLogicalColumn()));
          assertFalse(isLogicalColumnReferencedInAvailableFields(level.getLogicalColumn()));
        }
      }
    }

    for (MeasureMetaData measure : workspace.getModel().getMeasures()) {
      assertTrue(isLogicalColumnReferencedInAvailableOlapFields(measure.getLogicalColumn()));
      assertFalse(isLogicalColumnReferencedInAvailableFields(measure.getLogicalColumn()));
    }

  }

  private boolean isLogicalColumnReferencedInAvailableOlapFields(LogicalColumn lc) {
    for (AvailableField field : workspace.getAvailableOlapFields()) {
      if (field.getLogicalColumn().getId().equals(lc.getId())) {
        return true;
      }
    }
    return false;
  }
  private boolean isLogicalColumnReferencedInAvailableFields(LogicalColumn lc) {
    for (AvailableField field : workspace.getAvailableFields()) {
      if (field.getLogicalColumn().getId().equals(lc.getId())) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testMondrianExportAfterUpConvertOfModel() throws Exception{
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi(new FileInputStream("test-res/products.xmi"));
    LogicalModel model = d.getLogicalModels().get(0);
    workspace.setDomain(d);

    MondrianModelExporter exporter = new MondrianModelExporter(model, Locale.getDefault().toString());
    String mondrianSchema = exporter.createMondrianModelXML();

    String mondrianXmlBeforeUpConvert = readFileAsString("test-res/products.mondrian.xml");

    // just ignore any differences in line separators
    mondrianSchema = mondrianSchema.replaceAll("\r", "");
    mondrianXmlBeforeUpConvert = mondrianXmlBeforeUpConvert.replaceAll("\r", "");

    assertEquals(mondrianXmlBeforeUpConvert, mondrianSchema);

  }

  private static String readFileAsString(String filePath) throws java.io.IOException{
    byte[] buffer = new byte[(int) new File(filePath).length()];
    BufferedInputStream f = null;
    try {
        f = new BufferedInputStream(new FileInputStream(filePath));
        f.read(buffer);
    } finally {
        if (f != null) try { f.close(); } catch (IOException ignored) { }
    }
    return new String(buffer);
}

}




