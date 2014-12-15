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

import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import static org.pentaho.metadata.model.concept.types.AggregationType.AVERAGE;
import org.pentaho.metadata.util.XmiParser;

import java.io.FileInputStream;

public class MeasureTest {
  @Test
  public void testCreatesNewMeasureWithAggregation() throws Exception {
    Measure measure = new Measure();
    measure.setAggregateType( AVERAGE );
    measure.setName( "Avg Weight" );
    measure.setFormatString( "##.##" );

    ModelerWorkspace model =
        new ModelerWorkspace( new ModelerWorkspaceHelper( "" ) );
    model.setDomain( new XmiParser().parseXmi( new FileInputStream( "test-res/products.xmi" ) ) );

    measure.apply( model, "QUANTITYINSTOCK" );
    MeasuresCollection measures = model.getModel().getMeasures();
    assertEquals( 4, measures.size() );
    MeasureMetaData measureMetaData = measures.get( 3 );
    assertEquals( "QUANTITYINSTOCK", measureMetaData.getColumnName() );
    assertEquals( "Avg Weight", measureMetaData.getName() );
    assertEquals( "##.##", measureMetaData.getFormat() );
    assertEquals( AVERAGE, measureMetaData.getDefaultAggregation() );
  }
}
