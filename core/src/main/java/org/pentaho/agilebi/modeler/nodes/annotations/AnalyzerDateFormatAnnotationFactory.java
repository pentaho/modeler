/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.metadata.model.olap.OlapAnnotation;

public class AnalyzerDateFormatAnnotationFactory implements IAnnotationFactory {

  public static AnalyzerDateFormatAnnotationFactory instance = new AnalyzerDateFormatAnnotationFactory();

  public static void register() {
    MemberAnnotationFactory.registerFactory( IAnalyzerDateFormatAnnotation.NAME,
        AnalyzerDateFormatAnnotationFactory.instance );
  }

  private AnalyzerDateFormatAnnotationFactory() {

  }

  @Override
  public IMemberAnnotation create( OlapAnnotation anno ) {
    return new AnalyzerDateFormatAnnotation( anno.getValue() );
  }

  public IMemberAnnotation create( LevelMetaData levelMetaData ) {
    return new AnalyzerDateFormatAnnotation( levelMetaData );
  }
}
