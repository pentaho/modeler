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

import java.util.List;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;

public class MultiTableModelerSource implements ISpoonModelerSource {

	public static final String SOURCE_TYPE = MultiTableModelerSource.class.getSimpleName();
	private DatabaseMeta databaseMeta;
	private List<LogicalRelationship> joinTemplates;

	public MultiTableModelerSource(DatabaseMeta databaseMeta, List<LogicalRelationship> joinTemplates) {
		this.databaseMeta = databaseMeta;
		this.joinTemplates = joinTemplates;
	}

	@Override
	public Domain generateDomain() throws ModelerException {
		MultiTableModelerSourceUtil util = new MultiTableModelerSourceUtil();
		Domain domain = util.generateDomain(this.databaseMeta, this.joinTemplates);
		return domain;
	}

	@Override
	public String getDatabaseName() {
		String name = null;
		if (this.databaseMeta != null) {
			name = this.databaseMeta.getDatabaseName();
		}
		return name;
	}

	@Override
	public void initialize(Domain domain) throws ModelerException {
	}

	@Override
	public void serializeIntoDomain(Domain d) {
		LogicalModel lm = d.getLogicalModels().get(0);
		lm.setProperty("source_type", SOURCE_TYPE);
	}

	@Override
	public String getSchemaName() {
		return null;
	}

	@Override
	public String getTableName() {
		return null;
	}

	@Override
	public DatabaseMeta getDatabaseMeta() {
		return this.databaseMeta;
	}
}
