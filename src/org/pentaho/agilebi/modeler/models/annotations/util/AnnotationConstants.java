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

/**
 * Created by pminutillo on 1/19/2016.
 */
public class AnnotationConstants {
  // inline annotation names
  public static final String INLINE_ANNOTATION_CREATED_INLINE = "InlineCreatedInline";
  public static final String INLINE_ANNOTATION_FORMAT_SCALE = "InlineFormatScale";
  public static final String INLINE_ANNOTATION_FORMAT_CATEGORY = "InlineFormatCategory";
  public static final String INLINE_ANNOTATION_FORMULA_EXPRESSION = "InlineFormulaExpression";
  public static final String INLINE_ANNOTATION_CALCULATE_SUBTOTALS = "InlineCalcSubtotals";
  public static final String INLINE_MEMBER_FORMAT_STRING = "InlineMemberFormatString";
  public static final String LEVEL_FORMATTER_ATTRIBUTE = "formatter";
  public static final String INLINE_MEMBER_FORMATTER_CLASS =
    "org.pentaho.platform.plugin.action.mondrian.formatter.InlineMemberFormatter";

  public static final String CALCULATED_MEMBER_FORMAT_CATEGORY = "formatCategory";
  public static final String CALCULATED_MEMBER_FORMAT_SCALE = "formatScale";
  public static final String CALCULATED_MEMBER_FORMULA_EXPRESSION = "formulaExpression";
  public static final String CALCULATED_MEMBER_INLINE = "inline";
  public static final String CALCULATED_MEMBER_CALC_SUBTOTALS = "calcSubtotals";

  public static final String ANNOTATIONS_NODE_NAME = "Annotations";
  public static final String ANNOTATION_NODE_NAME = "Annotation";
  public static final String ANNOTATION_NAME_ATTRIUBUTE = "name";
  public static final String CALCULATED_MEMBER_NODE_NAME = "CalculatedMember";
  public static final String MEASURE_NODE_NAME = "Measure";

  public static final String NO_ANNOTATIONS_FOUND_MESSAGE = "No annotations returned from updateCalculatedMember";

  public static final String CALCULATED_MEMBER_PROPERTY_ELEMENT_NAME = "CalculatedMemberProperty";
  public static final String CALCULATED_MEMBER_ANNOTATION_ELEMENT_NAME = "Annotation";
  public static final String CALCULATED_MEMBER_ANNOTATIONS_ELEMENT_NAME = "Annotations";
  public static final String CALCULATED_MEMBER_CAPTION_ATTRIBUTE = "caption";
  public static final String CALCULATED_MEMBER_DESCRIPTION_ATTRIBUTE = "description";
  public static final String CALCULATED_MEMBER_DIMENSION_ATTRIBUTE = "dimension";
  public static final String CALCULATED_MEMBER_FORMULA_ATTRIBUTE = "formula";
  public static final String CALCULATED_MEMBER_NAME_ATTRIBUTE = "name";
  public static final String CALCULATED_MEMBER_PROPERTY_NAME_ATTRIBUTE = "name";
  public static final String CALCULATED_MEMBER_PROPERTY_VALUE_ATTRIBUTE = "value";
  public static final String CALCULATED_MEMBER_VISIBLE_ATTRIBUTE = "visible";
  public static final String CALCULATED_MEMBER_FORMAT_STRING_ATTRIBUTE = "formatString";
  public static final String CALCULATED_MEMBER_HIERARCHY_ATTRIBUTE = "hierarchy";
  public static final String CALCULATED_MEMBER_PARENT_ATTRIBUTE = "parent";
}
