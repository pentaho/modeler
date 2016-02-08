/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2016 Pentaho Corporation (Pentaho). All rights reserved.
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

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.models.annotations.util.XMLUtil;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationGroupXmlWriterTest {

  @Test
  public void testGetXmlNoAnnotations() throws Exception {

    ModelAnnotationGroup group = new ModelAnnotationGroup();

    ModelAnnotationGroupXmlWriter xmlWriter = new ModelAnnotationGroupXmlWriter( group );

    String xml = xmlWriter.getXML();
    Assert.assertEquals( "<annotations><sharedDimension>N</sharedDimension><description/></annotations>",
      XMLUtil.compactPrint( xml ) );
  }

  @Test
  public void testGetXmlWithAnnotations() throws Exception {
    // mock data
    ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    ModelAnnotation<CreateMeasure>
        measure = new ModelAnnotation<CreateMeasure>();
    measure.setName( "myName" );
    CreateMeasure m = new CreateMeasure();
    m.setFormatString( "xxxx" );
    m.setAggregateType( AggregationType.SUM );
    m.setDescription( "some description" );
    m.setField( "col1" );
    measure.setAnnotation( m );
    modelAnnotationGroup.add( measure );
    modelAnnotationGroup.add( measure );

    LinkDimension linkDimension = new LinkDimension();
    linkDimension.setName( "ldName" );
    linkDimension.setSharedDimension( "sharedDimension" );
    linkDimension.setField( "ld" );
    ModelAnnotation ldAnnotation = new ModelAnnotation( linkDimension );
    ldAnnotation.setName( "ld" );
    modelAnnotationGroup.add( ldAnnotation );

    modelAnnotationGroup.setDescription( "Test Description" );

    ModelAnnotationGroupXmlWriter xmlWriter = new ModelAnnotationGroupXmlWriter( modelAnnotationGroup );
    String xml = xmlWriter.getXML();

    Assert.assertEquals( "<annotations>"
        + "<annotation>"
        + "<name>myName</name>"
        + "<field>col1</field>"
        + "<type>CREATE_MEASURE</type>"
        + "<properties>"
        + "<property><name>formatString</name><value><![CDATA[xxxx]]></value></property>"
        + "<property><name>hidden</name><value><![CDATA[false]]></value></property>"
        + "<property><name>description</name><value><![CDATA[some description]]></value></property>"
        + "<property><name>aggregateType</name><value><![CDATA[SUM]]></value></property></properties>"
        + "</annotation>"
        + "<annotation>"
        + "<name>myName</name>"
        + "<field>col1</field><type>CREATE_MEASURE</type>"
        + "<properties>"
        + "<property><name>formatString</name><value><![CDATA[xxxx]]></value></property>"
        + "<property><name>hidden</name><value><![CDATA[false]]></value></property>"
        + "<property><name>description</name><value><![CDATA[some description]]></value></property>"
        + "<property><name>aggregateType</name><value><![CDATA[SUM]]></value></property>"
        + "</properties>"
        + "</annotation>"
        + "<annotation>"
        + "<name>ld</name>"
        + "<field>ld</field><type>LINK_DIMENSION</type>"
        + "<properties><property><name>sharedDimension</name><value><![CDATA[sharedDimension]]></value></property>"
        + "<property><name>name</name><value><![CDATA[ldName]]></value></property></properties></annotation>"
        + "<sharedDimension>N</sharedDimension>"
        + "<description>Test Description</description>"
        + "</annotations>", XMLUtil.compactPrint( xml ) );
  }

  @Test
  public void testGetXmlWithDataProviders() throws Exception {
    // mock data
    ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    modelAnnotationGroup.setSharedDimension( true );

    DataProvider dp1 = new DataProvider();
    dp1.setName( "dp1Name" );

    DataProvider dp2 = new DataProvider();
    dp2.setName( "dp2Name" );

    ColumnMapping cm1 = new ColumnMapping();
    cm1.setName( "cm1name" );
    cm1.setColumnDataType( DataType.BOOLEAN );
    ColumnMapping cm2 = new ColumnMapping();
    cm2.setName( "cm2name" );
    cm2.setColumnDataType( DataType.DATE );
    dp2.setColumnMappings( Arrays.asList( new ColumnMapping[] { cm1, cm2 } ) );

    List<DataProvider> dataProviders = new ArrayList<DataProvider>();
    dataProviders.add( dp1 );
    dataProviders.add( dp2 );

    modelAnnotationGroup.setDataProviders( dataProviders );

    ModelAnnotationGroupXmlWriter xmlWriter = new ModelAnnotationGroupXmlWriter( modelAnnotationGroup );
    String xml = xmlWriter.getXML();

    Assert.assertEquals( XMLUtil.prettyPrint( ""
        + "  <annotations>"
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
        + "  </annotations>" ), XMLUtil.prettyPrint( xml ) );
  }
}
