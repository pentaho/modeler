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

  import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
  import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
  import org.pentaho.agilebi.modeler.models.SchemaModel;
  import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.RelationshipType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

  public class MultiTableModelerSource implements ISpoonModelerSource {

    private ModelGenerator generator;
    private DatabaseMeta databaseMeta;
    private SchemaModel schemaModel;
    private List<String> selectedTables;
    private String datasourceName;
    public static final String SOURCE_TYPE = MultiTableModelerSource.class.getSimpleName();
    private static Logger logger = LoggerFactory.getLogger(MultiTableModelerSource.class);

    public MultiTableModelerSource(DatabaseMeta databaseMeta, SchemaModel schemaModel, String datasourceName, List<String> selectedTables) {
      this.datasourceName = datasourceName;
      this.databaseMeta = databaseMeta;
      this.schemaModel = schemaModel;
      this.selectedTables = selectedTables;
      this.generator = new ModelGenerator();
    }

    @Override
    public Domain generateDomain(boolean doOlap) throws ModelerException {
    	
    	 Domain domain = null;
         try {
           // Generate domain based on the table names.

           String locale = LocalizedString.DEFAULT_LOCALE;
           this.generator.setLocale(locale);
           this.generator.setDatabaseMeta(databaseMeta);
           this.generator.setModelName(datasourceName);

           Set<String> usedTables = new HashSet<String>();
           List<SchemaTable> schemas = new ArrayList<SchemaTable>();
           if(selectedTables.size() == 1){   // special single table story BISERVER-5806
             schemas.add(new SchemaTable("", selectedTables.get(0)));
           } else {
        	 for(JoinRelationshipModel joinModel : schemaModel.getJoins()) {
                 
        	   String fromTable = joinModel.getLeftKeyFieldModel().getParentTable().getName();	  
        	   String toTable = joinModel.getRightKeyFieldModel().getParentTable().getName();
        		 
               if(!usedTables.contains(fromTable)){
                 schemas.add(new SchemaTable("", fromTable));
                 usedTables.add(fromTable);
               }
               if(!usedTables.contains(toTable)){
                 schemas.add(new SchemaTable("", toTable));
                 usedTables.add(toTable);
               }
             }
           }
           SchemaTable tableNames[] = new SchemaTable[schemas.size()];
           tableNames = schemas.toArray(tableNames);
           this.generator.setTableNames(tableNames);
           domain = this.generator.generateDomain();
           domain.setId(datasourceName);
           
           ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper(locale);
           ModelerWorkspace workspace = new ModelerWorkspace(helper);
           workspace.setDomain(domain);
           
           LogicalModel logicalModel = domain.getLogicalModels().get(0);
           logicalModel.setName(new LocalizedString(locale, datasourceName));
           logicalModel.setDescription(new LocalizedString(locale, "This is the data model for "
                 + datasourceName));  // TODO do this with messages
           
           workspace.setModelName(datasourceName);
           helper.autoModelMultiTableRelational(workspace);
           if(doOlap) {
        	   helper.autoModelFlat(workspace);
           }           
           helper.populateDomain(workspace);

           for(LogicalTable businessTable : logicalModel.getLogicalTables()) {
             businessTable.setName(new LocalizedString(locale, businessTable.getPhysicalTable().getName(locale)));
           }

           //Create olap tables
           //BaseModelerWorkspaceHelper.duplicateLogicalTablesForDualModelingMode(logicalModel);
           
           // Create and add LogicalRelationships to the LogicalModel from the
           // domain.
           generateLogicalRelationships(logicalModel, false);
           if(doOlap) {
        	   generateLogicalRelationships(logicalModel, true);
        	   
        	   LogicalTable factTable = findFactTable(schemaModel.getFactTable(), logicalModel);
        	   if(factTable == null) {
        		   throw new IllegalStateException("Fact table not found");
        	   }
          	   List<OlapCube> cubes = (List) logicalModel.getProperty("olap_cubes");
          	   OlapCube cube = cubes.get(0);
          	   cube.setLogicalTable(factTable);
           }
           
         } catch (Exception e) {
           e.printStackTrace();
           logger.info(e.getLocalizedMessage());
           throw new ModelerException(e.getLocalizedMessage());
         }
         return domain;
    }
    
    private LogicalTable findFactTable(JoinTableModel table, LogicalModel logicalModel) {
       LogicalTable factTable = null;
   	   for (LogicalTable lTable : logicalModel.getLogicalTables()) {
   		   if(lTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) { 
	   		   if(lTable.getPhysicalTable().getProperty("target_table").equals(table.getName())) {
	   			  lTable.getPhysicalTable().setProperty("FACT_TABLE", true);
	   		      factTable = lTable;
	   		      break;
	   		   }
   		   }
   	   }
   	   return factTable;
    }
    
    private void generateLogicalRelationships(LogicalModel logicalModel, boolean doOlap) throws IllegalStateException {
    	for(JoinRelationshipModel joinModel : schemaModel.getJoins()) {
    	  	String lTable = joinModel.getLeftKeyFieldModel().getParentTable().getName();	  
        	String rTable = joinModel.getRightKeyFieldModel().getParentTable().getName();
    		
            LogicalTable fromTable = null;
            LogicalColumn fromColumn = null;
            LogicalTable toTable = null;
            LogicalColumn toColumn = null;

            for (LogicalTable logicalTable : logicalModel.getLogicalTables()) {
            	
              if(doOlap && !logicalTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
            	  continue;
              } else if(!doOlap && logicalTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX)) {
            	  continue;
              }

              if (logicalTable.getPhysicalTable().getProperty("target_table").equals(lTable)) {
                fromTable = logicalTable;

                for (LogicalColumn logicalColumn : fromTable.getLogicalColumns()) {
                  if (logicalColumn.getPhysicalColumn().getProperty("target_column").equals(joinModel.getLeftKeyFieldModel().getName())) {
                    fromColumn = logicalColumn;
                  }
                }
              }
              if (logicalTable.getPhysicalTable().getProperty("target_table").equals(rTable)) {
                toTable = logicalTable;

                for (LogicalColumn logicalColumn : toTable.getLogicalColumns()) {
                  if (logicalColumn.getPhysicalColumn().getProperty("target_column").equals(joinModel.getRightKeyFieldModel().getName())) {
                    toColumn = logicalColumn;
                  }
                }
              }
            }
            
            if(fromTable == null || fromColumn == null || toTable == null || toColumn == null) {
            	throw new IllegalStateException("Invalid Relationship");
            }

            LogicalRelationship logicalRelationship = new LogicalRelationship();
            logicalRelationship.setRelationshipType(RelationshipType._1_1);
            logicalRelationship.setFromTable(fromTable);
            logicalRelationship.setFromColumn(fromColumn);
            logicalRelationship.setToTable(toTable);
            logicalRelationship.setToColumn(toColumn);
            logicalModel.addLogicalRelationship(logicalRelationship);
          }
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

    public Domain generateDomain() throws ModelerException {
    	return generateDomain(false);
    }
  }
