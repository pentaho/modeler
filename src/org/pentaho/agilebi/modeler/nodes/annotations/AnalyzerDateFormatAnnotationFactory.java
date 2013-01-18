package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;

public class AnalyzerDateFormatAnnotationFactory implements IAnnotationFactory {

  public static AnalyzerDateFormatAnnotationFactory instance = new AnalyzerDateFormatAnnotationFactory();
  public static void register(){
    MemberAnnotationFactory.registerFactory(IAnalyzerDateFormatAnnotation.NAME, AnalyzerDateFormatAnnotationFactory.instance);
  }
  private AnalyzerDateFormatAnnotationFactory() {
    
  }
  @Override
  public IMemberAnnotation create(OlapAnnotation anno) {
    return new AnalyzerDateFormatAnnotation(anno.getValue());
  }

  public IMemberAnnotation create(LevelMetaData levelMetaData) {
    return new AnalyzerDateFormatAnnotation(levelMetaData);
  }
}
