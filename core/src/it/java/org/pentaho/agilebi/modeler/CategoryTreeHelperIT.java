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


package org.pentaho.agilebi.modeler;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.util.SQLModelGenerator;

/**
 * Created: 4/8/11
 *
 * @author rfellows
 */
public class CategoryTreeHelperIT {

  CategoryTreeHelper helper;

  private static final String LOCALE = "en-US";
  RelationalModelNode relationalModelNode;
  static ModelerWorkspace workspace;
  RelationalModelNode rootNode;

  @BeforeClass
  public static void init() throws Exception {
    System.setProperty( "org.osjava.sj.root", "src/it/resources/solution1/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( ModelerMessagesHolder.getMessages() == null ) {
      ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    }
    workspace = new ModelerWorkspace( new ModelerWorkspaceHelper( LOCALE ) );
    try {
      KettleEnvironment.init();
      Props.init( Props.TYPE_PROPERTIES_EMPTY );
    } catch ( Exception e ) {
      // may already be initiaized
    }
    SQLModelGenerator gen = new SQLModelGenerator();
    Domain d = ModelerSourceUtil.generateDomain( getDatabaseMeta(), "", "CUSTOMERS" );
    workspace.setDomain( d );

  }

  public static DatabaseMeta getDatabaseMeta() {
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseType( "Hypersonic" ); //$NON-NLS-1$
    database.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    database.setDBName( "SampleData" ); //$NON-NLS-1$
    database.setName( "SampleData" ); //$NON-NLS-1$
    return database;
  }

  @Before
  public void setup() {
    helper = new CategoryTreeHelper();
    helper.setWorkspace( workspace );
    rootNode = new RelationalModelNode( workspace );
  }

  @Test
  public void testAddField() throws ModelerException {

    CategoryMetaData cat = new CategoryMetaData( "Category" );
    CategoryMetaDataCollection catCollection = new CategoryMetaDataCollection();
    rootNode.add( catCollection );
    catCollection.add( cat );

    helper.setSelectedTreeItem( cat );
    helper.clearTreeModel();
    assertEquals( 0, cat.size() );
    IPhysicalColumn physicalColumn =
        workspace.getDomain().getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ).getPhysicalColumns().get( 0 );
    List<AvailableField> fieldsToAdd = new ArrayList<AvailableField>();
    AvailableField field = new AvailableField();
    field.setPhysicalColumn( physicalColumn );
    fieldsToAdd.add( field );

    helper.addField( fieldsToAdd.toArray() );

    assertEquals( 1, cat.size() );

  }

  @Test
  public void testAddSameFieldMultipleTimes() throws ModelerException {
    CategoryMetaData cat = new CategoryMetaData( "Category" );
    CategoryMetaDataCollection catCollection = new CategoryMetaDataCollection();
    rootNode.add( catCollection );
    catCollection.add( cat );

    helper.setSelectedTreeItem( cat );
    helper.clearTreeModel();
    assertEquals( 0, cat.size() );
    IPhysicalColumn physicalColumn =
        workspace.getDomain().getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ).getPhysicalColumns().get( 0 );
    List<AvailableField> fieldsToAdd = new ArrayList<AvailableField>();

    AvailableField field = new AvailableField();
    field.setPhysicalColumn( physicalColumn );
    field.setName( physicalColumn.getName( "en-US" ) );
    fieldsToAdd.add( field );

    field = new AvailableField();
    field.setPhysicalColumn( physicalColumn );
    field.setName( physicalColumn.getName( "en-US" ) );
    fieldsToAdd.add( field );

    helper.addField( fieldsToAdd.toArray() );

    assertEquals( 2, cat.size() );
    FieldMetaData one = cat.get( 0 );
    FieldMetaData two = cat.get( 1 );

    assertTrue( one != two );

    // make sure they got unique column id's
    String expectedColId =
        "LC_" + ModelerWorkspace.toId( physicalColumn.getPhysicalTable().getName( "en-US" ) ) + "_"
        + ModelerWorkspace.toId( physicalColumn.getName( "en-US" ) );
    assertEquals( expectedColId, one.getLogicalColumn().getId() );
    assertEquals( expectedColId + "_2", two.getLogicalColumn().getId() );

    // make sure they have the same name
    assertEquals( physicalColumn.getName( "en-US" ), one.getName() );
    assertEquals( physicalColumn.getName( "en-US" ), two.getName() );

    // make sure we can independently manage names
    one.setName( "newName" );
    assertEquals( "newName", one.getName() );
    assertFalse( "newName".equals( two.getName() ) );

    // make sure we can independently manage agg types
    one.setDefaultAggregation( AggregationType.MAXIMUM );
    assertEquals( AggregationType.MAXIMUM, one.getDefaultAggregation() );
    assertEquals( AggregationType.NONE, two.getDefaultAggregation() );

  }

}
