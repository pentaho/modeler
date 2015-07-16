/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.w3c.dom.Document;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationGroupXmlReaderTest {

  @Test
  public void testGetXmlNoAnnotations() throws Exception {

    String xml = "<annotations><sharedDimension>N</sharedDimension><description>descr</description></annotations>";
    Document doc = XMLHandler.loadXMLString( xml );
    ModelAnnotationGroupXmlReader mar = new ModelAnnotationGroupXmlReader();
    ModelAnnotationGroup group = mar.readModelAnnotationGroup( doc );

    assertEquals( group.getDescription(), "descr" );
  }

  @Test
  public void testGetXmlWithAnnotations() throws Exception {

    String xml = "<annotations>"
        + "<annotation>"
        + "<name>myName</name>"
        + "<field>col1</field>"
        + "<type>CREATE_MEASURE</type>"
        + "<properties>"
        + "<property><name>formatString</name><value>xxxx</value></property>"
        + "<property><name>description</name><value>some description</value></property>"
        + "<property><name>aggregateType</name><value>SUM</value></property></properties>"
        + "</annotation>"
        + "<annotation>"
        + "<name>myName</name>"
        + "<field>col1</field><type>CREATE_MEASURE</type>"
        + "<properties>"
        + "<property><name>formatString</name><value>xxxx</value></property>"
        + "<property><name>description</name><value>some description</value></property>"
        + "<property><name>aggregateType</name><value>SUM</value></property>"
        + "</properties>"
        + "</annotation>"
        + "<annotation>"
        + "<name>ld</name>"
        + "<field>ld</field><type>LINK_DIMENSION</type>"
        + "<properties><property><name>sharedDimension</name><value>sharedDimension</value></property>"
        + "<property><name>name</name><value>ldName</value></property></properties></annotation>"
        + "<sharedDimension>N</sharedDimension>"
        + "<description>Test Description</description>"
        + "</annotations>";
    Document doc = XMLHandler.loadXMLString( xml );
    ModelAnnotationGroupXmlReader mar = new ModelAnnotationGroupXmlReader();
    ModelAnnotationGroup group = mar.readModelAnnotationGroup( doc );

    List<ModelAnnotation> lma = group.getModelAnnotations();
    assertEquals( 3, lma.size() );
    assertEquals( lma.get( 0 ).getName(), "myName" );
    CreateMeasure cm = (CreateMeasure) lma.get( 0 ).getAnnotation();
    assertEquals( cm.getFormatString(), "xxxx" );
    assertEquals( cm.getAggregateType(), AggregationType.SUM );
    assertEquals( cm.getDescription(), "some description" );
    assertEquals( cm.getField(), "col1" );

    assertEquals( group.get( 2 ).getName(), "ld" );
    LinkDimension linkDimension = (LinkDimension) group.get( 2 ).getAnnotation();
    assertEquals( linkDimension.getName(), "ldName" );
    assertEquals( linkDimension.getSharedDimension(), "sharedDimension" );
    assertEquals( linkDimension.getField(), "ld" );
  }

  @Test
  public void testGetXmlWithDataProviders() throws Exception {
    // mock data

    String xml = "  <annotations>"
        + "    <sharedDimension>Y</sharedDimension>"
        + "    <description/>"
        + "    <data-providers>"
        + "      <data-provider>"
        + "        <name>dp1Name</name>"
        + "        <schemaName />"
        + "        <tableName />"
        + "        <databaseMetaRef />"
        + "      </data-provider>"
        + "      <data-provider>"
        + "        <name>dp2Name</name>"
        + "        <schemaName />"
        + "        <tableName />"
        + "        <databaseMetaRef />"
        + "        <column-mappings>"
        + "          <column-mapping>"
        + "            <name>cm1name</name>"
        + "            <columnName />"
        + "            <dataType>BOOLEAN</dataType>"
        + "          </column-mapping>"
        + "          <column-mapping>"
        + "            <name>cm2name</name>"
        + "            <columnName />"
        + "            <dataType>DATE</dataType>"
        + "          </column-mapping>"
        + "        </column-mappings>"
        + "      </data-provider>"
        + "    </data-providers>"
        + "  </annotations>";

    Document doc = XMLHandler.loadXMLString( xml );
    ModelAnnotationGroupXmlReader mar = new ModelAnnotationGroupXmlReader();
    ModelAnnotationGroup modelAnnotationGroup = mar.readModelAnnotationGroup( doc );
    List<DataProvider> ldp = modelAnnotationGroup.getDataProviders();

    assertEquals( 2, ldp.size() );
    assertEquals( ldp.get( 0 ).getName(), "dp1Name" );
    assertEquals( ldp.get( 1 ).getName(), "dp2Name" );

    List<ColumnMapping> lcm = ldp.get( 1 ).getColumnMappings();
    assertEquals( 2, lcm.size() );
    assertEquals( lcm.get( 0 ).getName(), "cm1name" );
    assertEquals( lcm.get( 0 ).getColumnDataType(), DataType.BOOLEAN );

    assertEquals( lcm.get( 1 ).getName(), "cm2name" );
    assertEquals( lcm.get( 1 ).getColumnDataType(), DataType.DATE );
  }

  @Test
  public void testWriteReadsUpdateMeasureAnnotations() throws Exception {
    UpdateMeasure avgSales = new UpdateMeasure();
    avgSales.setMeasure( "[Measures].[Sales]" );
    avgSales.setCube( "Sales2" );
    avgSales.setAggregationType( AggregationType.AVERAGE );
    avgSales.setName( "Avg Sales" );
    ModelAnnotation<UpdateMeasure> salesAnnotation = new ModelAnnotation<>( avgSales );

    UpdateMeasure cntOrders = new UpdateMeasure();
    cntOrders.setMeasure( "[Measures].[Orders]" );
    cntOrders.setCube( "Sales2" );
    cntOrders.setAggregationType( AggregationType.COUNT );
    cntOrders.setName( "Order Count" );
    ModelAnnotation<UpdateMeasure> ordersAnnotation = new ModelAnnotation<>( cntOrders );

    ModelAnnotationGroup originalAnnotations = new ModelAnnotationGroup( salesAnnotation, ordersAnnotation );

    ModelAnnotationGroupXmlWriter writer = new ModelAnnotationGroupXmlWriter( originalAnnotations );
    ModelAnnotationGroupXmlReader reader = new ModelAnnotationGroupXmlReader();
    ModelAnnotationGroup readAnnotations = reader.readModelAnnotationGroup(
        XMLHandler.loadXMLString( writer.getXML() ) );
    assertEquals( 2, readAnnotations.size() );
    assertEquals( salesAnnotation, readAnnotations.get( 0 ) );
    assertEquals( ordersAnnotation, readAnnotations.get( 1 ) );
  }
}
