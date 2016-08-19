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

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.agilebi.modeler.models.annotations.util.KeyValueClosure;
import org.pentaho.agilebi.modeler.models.annotations.util.XMLUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.metadata.model.concept.types.DataType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Rowell Belen
 */
public class ModelAnnotationGroupXmlWriter {

  private ModelAnnotationGroup modelAnnotations;

  public ModelAnnotationGroup getModelAnnotations() {
    return modelAnnotations;
  }

  public void setModelAnnotations(
      ModelAnnotationGroup modelAnnotations ) {
    this.modelAnnotations = modelAnnotations;
  }

  public ModelAnnotationGroupXmlWriter( ModelAnnotationGroup modelAnnotationGroup ) {
    this.modelAnnotations = modelAnnotationGroup;
  }

  public String getXML() {
    try {
      return XMLUtil.prettyPrint( getModelAnnotationsXml() );
    } catch ( Exception e ) {
      return getModelAnnotationsXml();
    }
  }

  private String getModelAnnotationsXml() {

    final StringBuffer xml = new StringBuffer();

    xml.append( "    <annotations>" );
    if ( getModelAnnotations() != null ) {
      for ( ModelAnnotation<?> modelAnnotation : getModelAnnotations() ) {

        // Add default name
        if ( StringUtils.isBlank( modelAnnotation.getName() ) ) {
          modelAnnotation.setName( UUID.randomUUID().toString() ); // backwards compatibility
        }

        xml.append( "      <annotation>" );
        xml.append( "        " ).append( XMLHandler.addTagValue( "name", modelAnnotation.getName() ) );
        xml.append( "        " )
            .append( XMLHandler.addTagValue( "field", modelAnnotation.getAnnotation().getField() ) );
        if ( modelAnnotation.getType() != null ) {
          xml.append( "        " ).append( XMLHandler.addTagValue( "type", modelAnnotation.getType().toString() ) );
          xml.append( "         <properties>" );
          modelAnnotation.iterateProperties( new KeyValueClosure() {
            @Override
            public void execute( String key, Serializable serializable ) {
              if ( !"field".equals( key ) ) {
                xml.append( "            " ).append( XMLHandler.openTag( "property" ) );
                xml.append( "               " ).append( XMLHandler.addTagValue( "name", key ) );
                xml.append( "               " ).append( XMLHandler.openTag( "value" ) );
                xml.append( XMLHandler.buildCDATA( serializable.toString() ) );
                xml.append( XMLHandler.closeTag( "value" ) );
                xml.append( "            " ).append( XMLHandler.closeTag( "property" ) );
              }
            }
          } );
          xml.append( "         </properties>" );
        }
        xml.append( "      </annotation>" );
      }
      xml.append( "    " )
          .append( XMLHandler.addTagValue( "sharedDimension", getModelAnnotations().isSharedDimension() ) );
      xml.append( "    " )
          .append( XMLHandler.addTagValue( "description", getModelAnnotations().getDescription() ) );
      xml.append( getDataProvidersXml( getModelAnnotations().getDataProviders() ) );
    }
    xml.append( "    </annotations>" );

    return xml.toString();
  }

  private String getDataProvidersXml( final List<DataProvider> dataProviders ) {
    final StringBuffer xml = new StringBuffer();

    if ( dataProviders != null && !dataProviders.isEmpty() ) {
      xml.append( "    <data-providers>" );
      for ( DataProvider provider : dataProviders ) {
        xml.append( "    <data-provider>" );
        xml.append( "        " ).append( XMLHandler.addTagValue( "name", provider.getName() ) );
        xml.append( "        " ).append( XMLHandler.addTagValue( "schemaName", provider.getSchemaName() ) );
        xml.append( "        " ).append( XMLHandler.addTagValue( "tableName", provider.getTableName() ) );
        xml.append( "        " )
            .append( XMLHandler.addTagValue( "databaseMetaRef", provider.getDatabaseMetaNameRef() ) );
        xml.append( getColumnMappingsXml( provider.getColumnMappings() ) );
        xml.append( "    </data-provider>" );
      }
      xml.append( "    </data-providers>" );
    }

    return xml.toString();
  }

  private String getColumnMappingsXml( final List<ColumnMapping> columnMappings ) {

    final StringBuffer xml = new StringBuffer();

    if ( columnMappings != null && !columnMappings.isEmpty() ) {
      xml.append( "    <column-mappings>" );
      for ( ColumnMapping columnMapping : columnMappings ) {
        xml.append( "    <column-mapping>" );
        xml.append( "        " ).append( XMLHandler.addTagValue( "name", columnMapping.getName() ) );
        xml.append( "        " ).append( XMLHandler.addTagValue( "columnName", columnMapping.getColumnName() ) );

        DataType dataType = columnMapping.getColumnDataType();
        if ( dataType != null ) {
          xml.append( "        " )
              .append( XMLHandler.addTagValue( "dataType", columnMapping.getColumnDataType().name() ) );
        }

        xml.append( "    </column-mapping>" );
      }
      xml.append( "    </column-mappings>" );
    }

    return xml.toString();
  }
}
