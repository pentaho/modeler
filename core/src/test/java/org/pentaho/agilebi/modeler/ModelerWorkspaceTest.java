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

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.format.DataFormatHolder;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;

public class ModelerWorkspaceTest {

  protected ModelerWorkspace workspace;
  private static final String LOCALE = "en-US";

  @Before
  public void setUp() throws Exception {

    if ( ModelerMessagesHolder.getMessages() == null ) {
      ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    }

    Reader propsReader = new FileReader( new File( "src/test/resources/geoRoles.properties" ) );
    Properties props = new Properties();
    props.load( propsReader );
    GeoContextConfigProvider config = new GeoContextPropertiesProvider( props );
    GeoContext geo = GeoContextFactory.create( config );

    workspace = new ModelerWorkspace( new ModelerWorkspaceHelper( LOCALE ), geo );
  }

  @Test
  public void testCreateFieldForParentWithNode_Date() {
    testCreateFieldForParentWithNode( DataType.DATE, DataFormatHolder.DATE_FORMATS );
  }

  @Test
  public void testCreateFieldForParentWithNode_Numeric() {
    testCreateFieldForParentWithNode( DataType.NUMERIC, DataFormatHolder.NUMBER_FORMATS );
  }

  public void testCreateFieldForParentWithNode( DataType dataType, List<String> formatingStrings ) {
    LogicalColumn logicalColumn = mock( LogicalColumn.class );
    when( logicalColumn.getDataType() ).thenReturn( dataType );

    ColumnBackedNode backedNode = mock( ColumnBackedNode.class );
    when( backedNode.getLogicalColumn() ).thenReturn( logicalColumn );

    ModelerWorkspace modelerWorkspace = spy( workspace );
    doReturn( backedNode ).when( modelerWorkspace )
      .createColumnBackedNode( any( AvailableField.class ), any( ModelerPerspective.class ) );

    CategoryMetaData parent = mock( CategoryMetaData.class );
    AvailableField selectedField = mock( AvailableField.class );
    FieldMetaData fieldMetaData = modelerWorkspace.createFieldForParentWithNode( parent, selectedField );
    assertEquals( formatingStrings, fieldMetaData.getFormatstring() );
  }

  @Test
  public void testUpConvertLegacyModel() throws Exception {
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi( Files.newInputStream( Paths.get( "src/test/resources/products.xmi" ) ) );
    LogicalModel model = d.getLogicalModels().get( 0 );

    assertEquals( 1, model.getLogicalTables().size() );
    // Up-Convert happens in the setDomain now
    workspace.setDomain( d, false );

    assertEquals( 1, model.getLogicalTables().size() );

  }

  @Test
  public void testUpConvert_v2() throws Exception {
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi( Files.newInputStream( Paths.get( "src/test/resources/multi-table-model-2.0.xmi" ) ) );
    LogicalModel model = d.getLogicalModels().get( 0 );

    assertEquals( 6, model.getLogicalTables().size() );
    // Up-Convert happens in the setDomain now
    workspace.setDomain( d, true );

    assertEquals( 3, model.getLogicalTables().size() );

    LogicalModel olapModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    assertEquals( 3, olapModel.getLogicalTables().size() );
  }

  @Test
  public void testSetDomain_NeedsUpConverted() throws Exception {
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi( Files.newInputStream( Paths.get( "src/test/resources/products.xmi" ) ) );
    LogicalModel model = d.getLogicalModels().get( 0 );
    workspace.setDomain( d );

    assertEquals( 1, model.getLogicalTables().size() );
    assertEquals( 2, d.getLogicalModels().size() );

    model = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );

    // verify the OLAP measures & dimensions get their logical columns set to the new OLAP table's columns
    for ( DimensionMetaData dim : workspace.getModel().getDimensions() ) {
      for ( HierarchyMetaData hier : dim ) {
        for ( LevelMetaData level : hier ) {
          assertTrue( isColumnReferencedInAvailableFields( level.getLogicalColumn() ) );
          assertTrue( isReferencedTableOlapVersion( level.getLogicalColumn() ) );
          assertFalse( isReferencedTableReportingVersion( level.getLogicalColumn() ) );
        }
      }
    }

    for ( MeasureMetaData measure : workspace.getModel().getMeasures() ) {
      assertTrue( isColumnReferencedInAvailableFields( measure.getLogicalColumn() ) );
      assertTrue( isReferencedTableOlapVersion( measure.getLogicalColumn() ) );
      assertFalse( isReferencedTableReportingVersion( measure.getLogicalColumn() ) );
    }

    model = workspace.getLogicalModel( ModelerPerspective.REPORTING );
    // verify the reporting model is correct still
    for ( CategoryMetaData cat : workspace.getRelationalModel().getCategories() ) {
      for ( FieldMetaData field : cat ) {
        assertTrue( isColumnReferencedInAvailableFields( field.getLogicalColumn() ) );
        assertTrue( isReferencedTableReportingVersion( field.getLogicalColumn() ) );
      }
    }
  }

  private boolean isReferencedTableOlapVersion( LogicalColumn logicalColumn ) {
    for ( LogicalTable table : workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getLogicalTables() ) {
      if ( table.getId().equals( logicalColumn.getLogicalTable().getId() ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean isReferencedTableReportingVersion( LogicalColumn logicalColumn ) {
    for ( LogicalTable table : workspace.getLogicalModel( ModelerPerspective.REPORTING ).getLogicalTables() ) {
      if ( table.getId().equals( logicalColumn.getLogicalTable().getId() ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean isColumnReferencedInAvailableFields( LogicalColumn lc ) {
    for ( AvailableTable table : workspace.getAvailableTables().getAsAvailableTablesList() ) {
      if ( table.containsUnderlyingPhysicalColumn( lc.getPhysicalColumn() ) ) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testMondrianExportAfterUpConvertOfModel() throws Exception {
    XmiParser parser = new XmiParser();
    Domain d = parser.parseXmi( Files.newInputStream( Paths.get( "src/test/resources/products.xmi" ) ) );
    workspace.setDomain( d );
    LogicalModel model = d.getLogicalModels().get( 1 );

    MondrianModelExporter exporter = new MondrianModelExporter( model, Locale.getDefault().toString() );
    String mondrianSchema = exporter.createMondrianModelXML();
    String mondrianXmlBeforeUpConvert = readFileAsString( "src/test/resources/products.mondrian.xml" );

    // just ignore any differences in line separators
    mondrianSchema = mondrianSchema.replaceAll( "\r", "" );
    mondrianXmlBeforeUpConvert = mondrianXmlBeforeUpConvert.replaceAll( "\r", "" );

    String expected = StringUtils.deleteWhitespace( mondrianSchema );
    String actual = StringUtils.deleteWhitespace( mondrianXmlBeforeUpConvert );

    // assertTrue(StringUtils.deleteWhitespace(mondrianXmlBeforeUpConvert).equals(StringUtils.deleteWhitespace
    // (mondrianSchema)));
    assertEquals( actual, expected );
    System.out.println( "after assert" );

  }

  private static String readFileAsString( String filePath ) throws java.io.IOException {
    byte[] buffer = new byte[ (int) new File( filePath ).length() ];
    try ( BufferedInputStream f = new BufferedInputStream( Files.newInputStream( Paths.get( filePath ) ) ) ) {
      f.read( buffer );
    }
    // ignore
    return new String( buffer );
  }
}
