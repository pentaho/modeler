/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2017 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */
package org.pentaho.agilebi.modeler.models.annotations;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.ModelITHelper;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

import static org.junit.Assert.*;

public class CreateMeasureIT {
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }
  @Test
  public void createMeasureWorksOnDimensionlessModel() throws Exception {
    ModelerWorkspace workspace = createWorkspace();

    CreateMeasure valueMeasure2 = new CreateMeasure();
    valueMeasure2.setField( "value" );
    valueMeasure2.setName( "value" );
    valueMeasure2.setAggregateType( AggregationType.SUM );
    valueMeasure2.apply( workspace, new MemoryMetaStore() );

    CreateMeasure valueMeasure = new CreateMeasure();
    valueMeasure.setField( "value" );
    valueMeasure.setName( "The Value" );
    valueMeasure.setAggregateType( AggregationType.SUM );
    valueMeasure.apply( workspace, new MemoryMetaStore() );

    CreateMeasure idMeasure = new CreateMeasure();
    idMeasure.setField( "id" );
    idMeasure.setName( "Id Count" );
    idMeasure.setAggregateType( AggregationType.COUNT );
    idMeasure.apply( workspace, new MemoryMetaStore() );
    OlapCube olapCube = getCubes( workspace ).get( 0 );
    assertEquals( 3, olapCube.getOlapMeasures().size() );
    assertEquals( 3, workspace.getModel().getMeasures().size() );
    assertEquals( 0, olapCube.getOlapDimensionUsages().size() );
    assertEquals( "The Value", olapCube.getOlapMeasures().get( 1 ).getName() );
    assertEquals( "Id Count", olapCube.getOlapMeasures().get( 2 ).getName() );
  }

  @SuppressWarnings( "unchecked" )
  private List<OlapCube> getCubes( ModelerWorkspace wspace ) {
    return (List<OlapCube>) wspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty(
      LogicalModel.PROPERTY_OLAP_CUBES );
  }

  private ModelerWorkspace createWorkspace() throws Exception {
    String sql = "CREATE TABLE testTable\n"
      + "(\n"
      + "  value bigint\n"
      + ", id varchar(25)\n"
      + ");\n";
    return ModelITHelper.modelTable( "CreateMeasureIT-H2-DB", "testTable", sql );
  }
}