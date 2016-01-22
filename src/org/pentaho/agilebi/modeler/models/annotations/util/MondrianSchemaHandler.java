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

package org.pentaho.agilebi.modeler.models.annotations.util;

import mondrian.olap.MondrianDef;
import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static java.lang.String.format;
import static javax.xml.xpath.XPathConstants.NODE;

/**
 * This class will attempt to encapsulate and abstract from the user
 * the nuances of manipulating an existing Mondrian schema
 *
 * Created by pminutillo on 3/7/15.
 */
public class MondrianSchemaHandler {
  protected static final Class<?> MSG_CLASS = MondrianSchemaHandler.class;

  public static final String XPATH_SEPARATOR = "/";
  public static final String SCHEMA_XPATH_EXPR = XPATH_SEPARATOR + "Schema";
  public static final String CUBE_XPATH_EXPR = SCHEMA_XPATH_EXPR + XPATH_SEPARATOR + "Cube";

  public static final String MEASURE_ELEMENT_NAME = "Measure";
  public static final String MEASURE_NAME_ATTRIBUTE = "name";
  public static final String MEASURE_COLUMN_ATTRIBUTE = "column";
  public static final String MEASURE_AGGREGATOR_ATTRIBUTE = "aggregator";

  public static final String MEASURE_DIMENSION = "Measure";
  public static final String DIMENSION = "Dimension";
  public static final String MEASURE_FORMAT_STRING_ATTRIBUTE = "formatString";
  public static final String CALCULATED_MEMBER_FORMAT_STRING_ATTRIBUTE = "formatString";

  private Document schema;

  public MondrianSchemaHandler() {

  }

  public MondrianSchemaHandler( Document schema ) {
    this.schema = schema;
  }

  /**
   *
   * @param cubeName
   * @param measure
   * @throws ModelerException
   */
  public void addMeasure( String cubeName, MondrianDef.Measure measure ) throws ModelerException {
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      StringBuffer xPathExpr = new StringBuffer();

      String cubeXPathPart = CUBE_XPATH_EXPR;

      if ( cubeName != null ) {
        cubeXPathPart += "[@name=\"" + cubeName + "\"]";
      }

      xPathExpr.append( cubeXPathPart );
      XPathExpression xPathExpression = xPath.compile( xPathExpr.toString() );
      Node cube = (Node) xPathExpression.evaluate( this.schema, NODE );
      Element measureElement;
      measureElement = this.schema.createElement( MEASURE_ELEMENT_NAME );

      // check if cube contains calculated members
      NodeList calculatedMemberNodeList = this.schema.getElementsByTagName( AnnotationConstants.CALCULATED_MEMBER_NODE_NAME );
      if ( ( calculatedMemberNodeList != null ) && ( calculatedMemberNodeList.getLength() > 0 ) ) {
        // get the first calculated member
        Node firstCalculatedMemberNode = calculatedMemberNodeList.item( 0 );
        // insert measure before the first calculated member
        cube.insertBefore( measureElement, firstCalculatedMemberNode );
      } else {
        cube.appendChild( measureElement );
      }

      measureElement.setAttribute( MEASURE_NAME_ATTRIBUTE, measure.name );
      measureElement.setAttribute( MEASURE_COLUMN_ATTRIBUTE, measure.column );
      measureElement.setAttribute( MEASURE_AGGREGATOR_ATTRIBUTE,  measure.aggregator );

      if ( measure.formatString != null ) {
        measureElement.setAttribute( MEASURE_FORMAT_STRING_ATTRIBUTE, measure.formatString );
      }

    } catch ( XPathExpressionException e ) {
      throw new ModelerException( e );
    }

  }

  /**
   *
   * @param cubeName
   * @param calculatedMember
   * @throws ModelerException
   */
  public void addCalculatedMember( String cubeName, MondrianDef.CalculatedMember calculatedMember ) throws ModelerException {
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      StringBuilder xPathExpr = new StringBuilder();

      String cubeXPathPart = CUBE_XPATH_EXPR;

      if ( cubeName != null ) {
        cubeXPathPart += "[@name=\"" + cubeName + "\"]";
      }

      xPathExpr.append( cubeXPathPart );
      XPathExpression xPathExpression = xPath.compile( xPathExpr.toString() );
      Node cube = (Node) xPathExpression.evaluate( this.schema, NODE );
      Element measureElement;
      measureElement = this.schema.createElement( AnnotationConstants.CALCULATED_MEMBER_NODE_NAME );
      cube.appendChild( measureElement );
      measureElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_NAME_ATTRIBUTE, calculatedMember.name );
      measureElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_CAPTION_ATTRIBUTE, calculatedMember.caption );
      measureElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_DESCRIPTION_ATTRIBUTE, calculatedMember.description );
      measureElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_DIMENSION_ATTRIBUTE, calculatedMember.dimension );
      measureElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_FORMULA_ATTRIBUTE, calculatedMember.formula );
      measureElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_VISIBLE_ATTRIBUTE, calculatedMember.visible.toString() );
      measureElement.setAttribute( CALCULATED_MEMBER_FORMAT_STRING_ATTRIBUTE, calculatedMember.formatString );

      if ( calculatedMember.annotations != null ) {
        Element annotationsElement = this.schema.createElement( AnnotationConstants.CALCULATED_MEMBER_ANNOTATIONS_ELEMENT_NAME );
        for ( MondrianDef.Annotation annot : calculatedMember.annotations.array ) {
          Element annotationElement = this.schema.createElement( AnnotationConstants.CALCULATED_MEMBER_ANNOTATION_ELEMENT_NAME );
          annotationElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_PROPERTY_NAME_ATTRIBUTE, annot.name );
          annotationElement.setTextContent( annot.cdata );
          annotationsElement.appendChild( annotationElement );
        }
        measureElement.appendChild( annotationsElement );
      }

      if ( calculatedMember.memberProperties != null ) {
        for ( MondrianDef.CalculatedMemberProperty property : calculatedMember.memberProperties ) {
          Element propertyElement = this.schema.createElement( AnnotationConstants.CALCULATED_MEMBER_PROPERTY_ELEMENT_NAME );
          propertyElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_PROPERTY_NAME_ATTRIBUTE, property.name );
          propertyElement.setAttribute( AnnotationConstants.CALCULATED_MEMBER_PROPERTY_VALUE_ATTRIBUTE, property.value );
          measureElement.appendChild( propertyElement );
        }
      }

    } catch ( XPathExpressionException e ) {
      throw new ModelerException( e );
    }
  }

  /**
   * Gets a measure node from a schema
   * @param cubeName
   * @param measureName
   * @return
   * @throws ModelerException
     */
  private Node getMeasureNode( String cubeName, String measureName ) throws ModelerException {
    if ( StringUtils.isBlank( measureName ) ) {
      return null;
    }

    String cubeXPathPart = CUBE_XPATH_EXPR;

    if ( !StringUtils.isBlank( cubeName ) ) {
      cubeXPathPart += "[@name=\"" + cubeName + "\"]";
    }

    // try to resolve name attribute if formatted with dimension
    if ( measureName.contains( "[" ) ) {
      // assuming measure is immediate child of dimension
      //  e.g. [Measures].[Quantity]
      measureName = measureName.substring(
        measureName.lastIndexOf( "[" ) + 1,
        measureName.lastIndexOf( "]" )
      );
    }

    // use XPath to get measure node and remove it from it's parent
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      StringBuffer xPathExpr = new StringBuffer();
      xPathExpr.append( cubeXPathPart + "//" + MEASURE_DIMENSION + "[@name=\"" + measureName + "\"]" );
      XPathExpression xPathExpression = xPath.compile( xPathExpr.toString() );
      return (Node) xPathExpression.evaluate( this.schema, NODE );
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }

  /**
   * remove a measure
   * @param cubeName
   * @param measureName
   * @throws ModelerException
   */
  public void removeMeasure( String cubeName, String measureName ) throws ModelerException {
    try {
      Node measure = getMeasureNode( cubeName, measureName );
      measure.getParentNode().removeChild( measure );
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }


  /**
   * Update measure with name and/or aggregation type.
   *
   * @param cubeName Cube to search for measure
   * @param measureName Name of measure to search for
   * @param measure The updated measure
   * @throws ModelerException
   */
  public boolean updateMeasure( String cubeName, String measureName, MondrianDef.Measure measure ) throws ModelerException {
    if ( StringUtils.isBlank( measureName ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "MondrianSchemaHelper.updateMeasure.UNABLE_TO_FIND_MEASURE" )
      );
    }

    // try to resolve name attribute if formatted with dimension
    if ( measureName.contains( "[" ) ) {
      // assuming measure is immediate child of dimension
      //  e.g. [Measures].[Quantity]
      measureName = measureName.substring(
        measureName.lastIndexOf( "[" ) + 1,
        measureName.lastIndexOf( "]" )
      );
    }

    try {
      // Check to make sure there isn't a measure that already exists with the new name
      Node duplicateMeasure = getMeasureNode( cubeName, measure.name );
      if ( !measureName.equals( measure.name ) && duplicateMeasure != null ) {
        return false;
      }

      Node measureNode = getMeasureNode( cubeName, measureName );
      if ( measureNode == null ) {
        return false;
      }

      NamedNodeMap measureAttrs = measureNode.getAttributes();

      // Change aggregation
      if ( !StringUtils.isBlank( measure.aggregator ) ) {
        Node aggNode = measureAttrs.getNamedItem( "aggregator" );
        aggNode.setNodeValue( measure.aggregator );
      }

      // Change format
      Node formatNode = measureAttrs.getNamedItem( "formatString" );
      formatNode.setNodeValue( measure.formatString );

      // Name Change
      if ( !StringUtils.isBlank( measure.name ) ) {
        Node nameNode = measureAttrs.getNamedItem( "name" );
        nameNode.setNodeValue( measure.name );
      }
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
    return true;
  }

  /**
   *
   * @param cubeName
   * @param calculatedMemberName
   * @param updatedCalculatedMember
   * @return
   * @throws ModelerException
   */
  public boolean updateCalculatedMember( String cubeName, String calculatedMemberName,
                                         MondrianDef.CalculatedMember updatedCalculatedMember ) throws ModelerException {
    if ( StringUtils.isBlank( calculatedMemberName ) ) {
      throw new ModelerException(
        BaseMessages.getString( MSG_CLASS, "MondrianSchemaHelper.updateMeasure.UNABLE_TO_FIND_MEASURE" )
      );
    }

    try {
      Element existingCalculatedMemberNode = getCalculatedMeasureNode( cubeName, calculatedMemberName );
      if ( existingCalculatedMemberNode == null ) {
        return false;
      }

      NamedNodeMap measureAttrs = existingCalculatedMemberNode.getAttributes();

      // Change format
      if ( !StringUtils.isBlank( updatedCalculatedMember.formatString ) ) {
        Node formatNode = measureAttrs.getNamedItem( "formatString" );
        formatNode.setNodeValue( updatedCalculatedMember.formatString );
      }

      // Name Change
      if ( !StringUtils.isBlank( updatedCalculatedMember.name ) ) {
        Node nameNode = measureAttrs.getNamedItem( "name" );
        nameNode.setNodeValue( updatedCalculatedMember.name );
      }

      // Caption
      if ( !StringUtils.isBlank( updatedCalculatedMember.caption ) ) {
        Node captionNode = measureAttrs.getNamedItem( "caption" );
        captionNode.setNodeValue( updatedCalculatedMember.caption );
      }

      // Description
      if ( !StringUtils.isBlank( updatedCalculatedMember.description ) ) {
        Node captionNode = measureAttrs.getNamedItem( "description" );
        captionNode.setNodeValue( updatedCalculatedMember.description );
      }

      // Formula
      if ( !StringUtils.isBlank( updatedCalculatedMember.formula ) ) {
        Node captionNode = measureAttrs.getNamedItem( "formula" );
        captionNode.setNodeValue( updatedCalculatedMember.formula );
      }

      // Dimension
      if ( !StringUtils.isBlank( updatedCalculatedMember.dimension ) ) {
        Node captionNode = measureAttrs.getNamedItem( "dimension" );
        captionNode.setNodeValue( updatedCalculatedMember.dimension );
      }

      // Hierarchy
      if ( !StringUtils.isBlank( updatedCalculatedMember.hierarchy ) ) {
        Node captionNode = measureAttrs.getNamedItem( "hierarchy" );
        captionNode.setNodeValue( updatedCalculatedMember.hierarchy );
      }

      // Parent
      if ( !StringUtils.isBlank( updatedCalculatedMember.parent ) ) {
        Node captionNode = measureAttrs.getNamedItem( "parent" );
        captionNode.setNodeValue( updatedCalculatedMember.parent );
      }

      // Visible
      if ( !StringUtils.isBlank( updatedCalculatedMember.visible.toString() ) ) {
        Node captionNode = measureAttrs.getNamedItem( "visible" );
        captionNode.setNodeValue( updatedCalculatedMember.visible.toString() );
      }

      // get mondrian annotation values from updated calculated member passed in
      String updatedFormatCategory = "";
      String updatedFormatScale = "";
      String updatedFormulaExpression = "";
      String updatedInline = "";
      String updatedCalcSubtotals = "";

      for ( int x = 0; x <= updatedCalculatedMember.annotations.array.length - 1; x++ ) {
        MondrianDef.Annotation annotation = updatedCalculatedMember.annotations.array[ x ];
        switch ( annotation.name ) {
          case AnnotationConstants.CALCULATED_MEMBER_FORMAT_CATEGORY :
            updatedFormatCategory = annotation.cdata;
            break;
          case AnnotationConstants.CALCULATED_MEMBER_FORMAT_SCALE :
            updatedFormatScale = annotation.cdata;
            break;
          case AnnotationConstants.CALCULATED_MEMBER_FORMULA_EXPRESSION :
            updatedFormulaExpression = annotation.cdata;
            break;
          case AnnotationConstants.CALCULATED_MEMBER_INLINE :
            updatedInline = annotation.cdata;
            break;
          case AnnotationConstants.CALCULATED_MEMBER_CALC_SUBTOTALS :
            updatedCalcSubtotals = annotation.cdata;
            break;
        }
      }

      // Update Mondrian annotation values in existing
      NodeList annotationsNodes = existingCalculatedMemberNode.getElementsByTagName( AnnotationConstants.ANNOTATIONS_NODE_NAME );

      // if no annotations are found, move on
      if ( annotationsNodes.getLength() <= 0 ) {
        return true;
      }

      // Assume the first is the only Annotations node, as per the spec
      Element annotationsNode = (Element) annotationsNodes.item( 0 );

      NodeList annotationNodes  = annotationsNode.getElementsByTagName( AnnotationConstants.ANNOTATION_NODE_NAME );

      if ( annotationNodes.getLength() <= 0 ) {
        return true;
      }

      for ( int x = 0; x <= annotationNodes.getLength() - 1; x++ ) {

        Node annotationNode = annotationNodes.item( x );

        if ( annotationNode.getNodeType() == Node.ELEMENT_NODE ) {
          if ( annotationNode.getAttributes().getNamedItem( "name" ).getTextContent()
            .equals( AnnotationConstants.CALCULATED_MEMBER_FORMAT_CATEGORY ) ) {
            annotationNode.setTextContent( updatedFormatCategory );
          } else if ( annotationNode.getAttributes().getNamedItem( "name" ).getTextContent()
            .equals( AnnotationConstants.CALCULATED_MEMBER_FORMAT_SCALE ) ) {
            annotationNode.setTextContent( updatedFormatScale );
          } else if ( annotationNode.getAttributes().getNamedItem( "name" ).getTextContent()
            .equals( AnnotationConstants.CALCULATED_MEMBER_FORMULA_EXPRESSION ) ) {
            annotationNode.setTextContent( updatedFormulaExpression );
          } else if ( annotationNode.getAttributes().getNamedItem( "name" ).getTextContent()
            .equals( AnnotationConstants.CALCULATED_MEMBER_INLINE ) ) {
            annotationNode.setTextContent( updatedInline );
          } else if ( annotationNode.getAttributes().getNamedItem( "name" ).getTextContent()
            .equals( AnnotationConstants.CALCULATED_MEMBER_CALC_SUBTOTALS ) ) {
            annotationNode.setTextContent( updatedCalcSubtotals );
          }
        }
      }
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }

    return true;
  }

  private Element getLevelNode( String cubeName, String dimensionName, String hierarchyName, String levelName )
    throws ModelerException {
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      String inlineWithHierarchy =
        format( "Schema/Cube[@name=\"%s\"]/Dimension[@name=\"%s\"]/Hierarchy[@name=\"%s\"]/Level[@name=\"%s\"]",
          cubeName, dimensionName, hierarchyName, levelName );
      Element levelElement = (Element) xPath.compile( inlineWithHierarchy ).evaluate( this.schema, NODE );
      if ( levelElement == null ) {
        if ( dimensionName.equals( hierarchyName ) ) {
          String inlineDefaultHierarchy =
            format( "Schema/Cube[@name=\"%s\"]/Dimension[@name=\"%s\"]/Hierarchy[not(@name) or @name=\"\"]/Level[@name=\"%s\"]",
              cubeName, dimensionName, levelName );
          levelElement = (Element) xPath.compile( inlineDefaultHierarchy ).evaluate( this.schema, NODE );
        }
        if ( levelElement == null ) {
          String dimensionUageXPath =
            format( "Schema/Cube[@name=\"%s\"]/DimensionUsage[@name=\"%s\"]", cubeName, dimensionName );
          Element usageElement = (Element) xPath.compile( dimensionUageXPath ).evaluate( this.schema, NODE );
          if ( usageElement != null ) {
            String sharedCompleteXPath =
              format( "Schema/Dimension[@name=\"%s\"]/Hierarchy[@name=\"%s\"]/Level[@name=\"%s\"]",
                usageElement.getAttribute( "source" ), hierarchyName, levelName );
            levelElement = (Element) xPath.compile( sharedCompleteXPath ).evaluate( this.schema, NODE );
            if ( levelElement == null && dimensionName.equals( hierarchyName ) ) {
              String sharedDefaultHierarchyXPath =
                format( "Schema/Dimension[@name=\"%s\"]/Hierarchy[not(@name)]/Level[@name=\"%s\"]",
                  usageElement.getAttribute( "source" ), levelName );
              levelElement = (Element) xPath.compile( sharedDefaultHierarchyXPath ).evaluate( this.schema, NODE );
            }
          }
        }
      }
      return levelElement;
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }

  private Element getCalculatedMeasureNode( final String cubeName, final String measureName ) throws ModelerException {
    try {
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      String inlineWithHierarchy =
        format( "Schema/Cube[@name=\"%s\"]/CalculatedMember[@name=\"%s\" and @dimension=\"Measures\"]",
          cubeName, measureName );
      return (Element) xPath.compile( inlineWithHierarchy ).evaluate( this.schema, NODE );
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }

  }

  /**
   * set visible=false on the given measure
   *
   * @param cubeName Cube to search for measure
   * @param measureName Name of measure to search for
   * @param visible
   * @throws ModelerException
   */
  public boolean showHideMeasure( final String cubeName, final String measureName, final boolean visible ) throws ModelerException {
    Element measureNode = (Element) getMeasureNode( cubeName, measureName );
    if ( measureNode == null ) {
      measureNode = getCalculatedMeasureNode( cubeName, measureName );
    }
    if ( measureNode !=  null ) {
      showHideElement( measureNode, visible );
      return true;
    } else {
      return false;
    }
  }

  /**
   * set visible=false on the given attribute
   *
   * @param cubeName Cube to search for level
   * @param dimensionName Dimension to search for level
   * @param hierarchyName Hierarchy to search for level
   * @param levelName Name of level to search for
   * @param visible
   * @throws ModelerException
   */
  public boolean showHideAttribute(
    final String cubeName, final String dimensionName, final String hierarchyName, final String levelName,
    final boolean visible )
    throws ModelerException {
    Element levelNode = getLevelNode( cubeName, dimensionName, hierarchyName, levelName );
    if ( levelNode != null ) {
      showHideElement( levelNode, visible );
      return true;
    }
    return false;
  }

  private void showHideElement( final Element levelNode, final boolean visible ) {
    levelNode.setAttribute( "visible", Boolean.toString( visible ) );
  }

  public Document getSchema() {
    return schema;
  }

  public void setSchema( Document schema ) {
    this.schema = schema;
  }
}
