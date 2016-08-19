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
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static javax.xml.xpath.XPathConstants.NODE;

/**
 * @author Brandon Groves
 */
public final class AnnotationUtil {

  public static final String MEASURE_ELEMENT_NAME = "Measure";
  public static final String LEVEL_ELEMENT_NAME = "Level";
  public static final String CALCULATED_MEMBER_ELEMENT_NAME = "CalculatedMember";
  public static final String CALCULATED_MEMBER_PROPERTY_ELEMENT_NAME = "CalculatedMemberProperty";

  public static final String NAME_ATTRIB = "name";
  public static final String CAPTION_ATTRIB = "caption";
  public static final String AGGREGATOR_ATTRIB = "aggregator";
  public static final String VISIBLE_ATTRIB = "visible";
  public static final String FORMATSTRING_ATTRIB = "formatString";
  private static final String ANNOTATION_TAG = "Annotation";

  private AnnotationUtil() {
  }

  /**
   * Get a named calc member element from a schema
   *
   * @param document
   * @param cubeName
   * @param measureName
   * @return
   * @throws ModelerException
   */
  public static Element getCalculatedMemberNode( final Document document, final String cubeName, final String measureName ) throws
    ModelerException {
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      String inlineWithHierarchy =
        format( "Schema/Cube[@name=\"%s\"]/CalculatedMember[@name=\"%s\"]",
          cubeName, measureName );
      return (Element) xPath.compile( inlineWithHierarchy ).evaluate( document, NODE );
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }

  /**
   * Get {@link OlapHierarchyLevel} based on the name
   *
   * @param levelName Name of the dimension to find
   * @param hierarchyLevels list of dimensions
   * @return Found dimension otherwise null
   */
  public static OlapHierarchyLevel getOlapHierarchyLevel( final String levelName,
                                                          final List<OlapHierarchyLevel> hierarchyLevels ) {
    if ( StringUtils.isBlank( levelName ) || hierarchyLevels == null || hierarchyLevels.isEmpty() ) {
      return null;
    }

    OlapHierarchyLevel hierarchyLevel = null;

    for ( OlapHierarchyLevel level : hierarchyLevels ) {
      if ( levelName.equals( level.getName() ) ) {
        hierarchyLevel = level;
        break;
      }
    }

    return hierarchyLevel;
  }

  /**
   * Get {@link OlapDimensionUsage} based on the name
   *
   * @param dimensionName Name of the dimension to find
   * @param dimensionUsages list of dimensions
   * @return Found dimension otherwise null
   */
  public static OlapDimensionUsage getOlapDimensionUsage( final String dimensionName,
                                                          final List<OlapDimensionUsage> dimensionUsages ) {
    if ( StringUtils.isBlank( dimensionName ) || dimensionUsages == null || dimensionUsages.isEmpty() ) {
      return null;
    }

    OlapDimensionUsage foundDimensionUsage = null;

    for ( OlapDimensionUsage dimensionUsage : dimensionUsages ) {
      if ( dimensionName.equals( dimensionUsage.getName() ) ) {
        foundDimensionUsage = dimensionUsage;
        break;
      }
    }

    return foundDimensionUsage;
  }

  /**
   * Retrieves {@link OlapMeasure} based on the name
   *
   * @param measureName Name of the measure
   * @param olapMeasures list of measures
   * @return Found measure otherwise null
   */
  public static OlapMeasure getOlapMeasure( final String measureName, List<OlapMeasure> olapMeasures ) {
    if ( StringUtils.isBlank( measureName ) || olapMeasures == null || olapMeasures.isEmpty() ) {
      return null;
    }

    OlapMeasure olapMeasure = null;

    for ( OlapMeasure measure : olapMeasures ) {
      if ( measureName.equals( measure.getName() ) ) {
        olapMeasure = measure;
        break;
      }
    }

    return olapMeasure;
  }

  /**
   * Retrieves {@link MeasureMetaData} based on the name.
   *
   * @param measureName Name of the measure
   * @param measures Collection of measures
   * @return FOund measure otherwise null
   */
  public static MeasureMetaData getMeasureMetaData( final String measureName,
                                                    MeasuresCollection measures ) {
    if ( StringUtils.isBlank( measureName ) || measures == null || measures.isEmpty() ) {
      return null;
    }

    MeasureMetaData measureMetaData = null;

    for ( MeasureMetaData measure : measures ) {
      if ( measureName.equals( measure.getName() ) ) {
        measureMetaData = measure;
      }
    }

    return measureMetaData;
  }

  /**
   * @param docToTest Document to find node
   * @param tagName Name of the tag to look up
   * @param nodeName Name of the node to look up
   * @param attributeName Name of the attribute to test
   * @param testValue Test value to compare against the attributeName value
   * @return True if nodeName value equals otherwise false
   */
  public static boolean validateNodeAttribute( Document docToTest, String tagName, String nodeName,
                                               String attributeName, String testValue ) {
    boolean validated = false;

    NodeList nodeList = docToTest.getElementsByTagName( tagName );
    if ( nodeList.getLength() > 0 ) {
      for ( int i = 0; i < nodeList.getLength(); i++ ) {
        Element element = (Element) nodeList.item( i );
        if ( nodeName.equals( element.getAttribute( NAME_ATTRIB ) ) ) {
          // Found element now test attribute
          validated = testValue.equals( element.getAttribute( attributeName ) );
          if ( validated ) {
            break;
          }
        }
      }
    }

    return validated;
  }

  /**
   *
   * @param docToTest
   * @param tagName
   * @param nodeName
   * @param annotationName
   * @param testValue
     * @return
     */
  public static boolean validateMondrianAnnotationValue( Document docToTest, String tagName, String nodeName,
                                                        String annotationName, String testValue ) {
    boolean validated = false;

    NodeList nodeList = docToTest.getElementsByTagName( tagName );
    if ( nodeList.getLength() > 0 ) {
      for ( int i = 0; i < nodeList.getLength(); i++ ) {
        Element element = (Element) nodeList.item( i );
        if ( nodeName.equals( element.getAttribute( NAME_ATTRIB ) ) ) {
          // Found element now test attribute
          NodeList mondrianAnnotationNodes = element.getElementsByTagName( ANNOTATION_TAG );
          for ( int x = 0; x < mondrianAnnotationNodes.getLength(); x++ ) {
            Element annotationElement = (Element) mondrianAnnotationNodes.item( x );
            if ( annotationName.equals( annotationElement.getAttribute( NAME_ATTRIB ) ) ) {
              // annotation found, test value
              validated = testValue.equals( annotationElement.getTextContent() );
            }

          }
        }
      }
    }

    return validated;
  }

  public static Document getMondrianDoc( final String mondrianTestFilePath )
    throws SAXException, IOException, ParserConfigurationException {
    File mondrianSchemaXmlFile = new File( mondrianTestFilePath );
    return DocumentBuilderFactory
      .newInstance()
      .newDocumentBuilder()
      .parse( mondrianSchemaXmlFile );
  }
}
