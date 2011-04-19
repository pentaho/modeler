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
 * 
 * @author Ezequiel Cuellar
 */

package org.pentaho.agilebi.modeler.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pentaho.agilebi.modeler.AbstractModelerTest;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.olap.OlapCube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTableModelerSourceTest extends AbstractModelerTest {

	private static Logger logger = LoggerFactory.getLogger(MultiTableModelerSourceTest.class);

	static {
		try {
			KettleEnvironment.init();
			Props.init(Props.TYPE_PROPERTIES_EMPTY);
		} catch (Exception e) {
			// May be already initialized by another test
		}

		if (ModelerMessagesHolder.getMessages() == null) {
			ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
		}
	}

	@Test
	public void testStarSchema() {

		Domain domain = null;
		try {
			MultiTableModelerSource multiTable = new MultiTableModelerSource(this.getDatabase(), getSchemaModel1(), this.getDatabase().getName(), Arrays.asList("CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE"));
			domain = multiTable.generateDomain(true);
			assertNotNull(domain);

			List<OlapCube> cubes = (List) domain.getLogicalModels().get(0).getProperty("olap_cubes");
			OlapCube cube = cubes.get(0);

			// Ensure cube has a fact table.
			assertNotNull(cube.getLogicalTable());
			// Ensure we have logical relationships (joins).
			assertEquals(true, domain.getLogicalModels().get(0).getLogicalRelationships().size() > 0);
			// Ensure all joins use Olap tables.
			for (LogicalRelationship logicalRelationship : domain.getLogicalModels().get(0).getLogicalRelationships()) {
				assertEquals(true, logicalRelationship.getToTable().getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX));
				assertEquals(true, logicalRelationship.getFromTable().getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX));
			}

			multiTable = new MultiTableModelerSource(this.getDatabase(), getSchemaModel2(), this.getDatabase().getName(), Arrays.asList("CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE"));
			domain = multiTable.generateDomain(true);
			// Used wrong columns on purpose. Ensure there are no logical
			// relationships. IllegalStateException should have been thrown.
			assertEquals(true, domain.getLogicalModels().get(0).getLogicalRelationships().size() == 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testReportingSchema() {

		Domain domain = null;
		try {
			MultiTableModelerSource multiTable = new MultiTableModelerSource(this.getDatabase(), getSchemaModel1(), this.getDatabase().getName(), Arrays.asList("CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE"));
			domain = multiTable.generateDomain(false);
			// Ensure domain was created.
			assertNotNull(domain);
			// Ensure we have logical relationships (joins).
			assertEquals(true, domain.getLogicalModels().get(0).getLogicalRelationships().size() > 0);
			// Ensure all joins DO NOT use Olap tables.
			for (LogicalRelationship logicalRelationship : domain.getLogicalModels().get(0).getLogicalRelationships()) {
				assertEquals(true, !logicalRelationship.getToTable().getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX));
				assertEquals(true, !logicalRelationship.getFromTable().getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX));
			}

			multiTable = new MultiTableModelerSource(this.getDatabase(), getSchemaModel2(), this.getDatabase().getName(), Arrays.asList("CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE"));
			domain = multiTable.generateDomain(false);
			// Used wrong columns on purpose. Ensure there are no logical
			// relationships. IllegalStateException should have been thrown.
			assertEquals(true, domain.getLogicalModels().get(0).getLogicalRelationships().size() == 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SchemaModel getSchemaModel1() {
		List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

		JoinTableModel joinTable1 = new JoinTableModel();
		joinTable1.setName("CUSTOMERS");

		JoinTableModel joinTable2 = new JoinTableModel();
		joinTable2.setName("PRODUCTS");

		JoinRelationshipModel join1 = new JoinRelationshipModel();
		JoinFieldModel lField1 = new JoinFieldModel();
		lField1.setName("CUSTOMERNAME");
		lField1.setParentTable(joinTable1);
		join1.setLeftKeyFieldModel(lField1);

		JoinFieldModel rField1 = new JoinFieldModel();
		rField1.setName("PRODUCTCODE");
		rField1.setParentTable(joinTable2);
		join1.setRightKeyFieldModel(rField1);

		joins.add(join1);
		SchemaModel model = new SchemaModel();
		model.setJoins(joins);
		return model;
	}

	private SchemaModel getSchemaModel2() {
		List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

		JoinTableModel joinTable1 = new JoinTableModel();
		joinTable1.setName("CUSTOMERS");

		JoinTableModel joinTable2 = new JoinTableModel();
		joinTable2.setName("PRODUCTS");

		JoinRelationshipModel join1 = new JoinRelationshipModel();
		JoinFieldModel lField1 = new JoinFieldModel();
		lField1.setName("BAD_COLUMN");
		lField1.setParentTable(joinTable1);
		join1.setLeftKeyFieldModel(lField1);

		JoinFieldModel rField1 = new JoinFieldModel();
		rField1.setName("BAD_COLUMN");
		rField1.setParentTable(joinTable2);
		join1.setRightKeyFieldModel(rField1);

		joins.add(join1);
		SchemaModel model = new SchemaModel();
		model.setJoins(joins);
		return model;
	}

	private DatabaseMeta getDatabase() {
		DatabaseMeta database = new DatabaseMeta();
		database.setDatabaseType("Hypersonic");
		database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
		database.setDBName("SampleData");
		database.setName("SampleData");
		return database;
	}
}
