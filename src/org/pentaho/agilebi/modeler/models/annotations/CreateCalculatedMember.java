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

import mondrian.olap.MondrianDef;
import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.models.annotations.util.AnnotationConstants;
import org.pentaho.agilebi.modeler.models.annotations.util.MondrianSchemaHandler;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.olap.OlapCalculatedMember;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Document;

import java.util.List;
import java.util.logging.Logger;

/**
 * Annotation to create a calculated member
 * <p/>
 * Created by pminutillo on 2/2/15.
 */
@MetaStoreElementType( name = "CreatedCalculatedMember", description = "CreatedCalculatedMember Annotation" )
public class CreateCalculatedMember extends AnnotationType {
  private static final long serialVersionUID = 5169827225345800226L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );

  private static final String NAME_ID = "name";
  private static final String NAME_NAME = "Name";
  private static final int NAME_ORDER = 0;

  private static final String CAPTION_ID = "caption";
  private static final String CAPTION_NAME = "Caption";
  private static final int CAPTION_ORDER = 0;

  private static final String DESCRIPTION_ID = "description";
  private static final String DESCRIPTION_NAME = "Description";
  private static final int DESCRIPTION_ORDER = 0;

  private static final String FORMULA_ID = "formula";
  private static final String FORMULA_NAME = "Formula";
  private static final int FORMULA_ORDER = 0;

  private static final String DIMENSION_ID = "dimension";
  private static final String DIMENSION_NAME = "Dimension";
  private static final int DIMENSION_ORDER = 0;

  private static final String VISIBLE_ID = "visible";
  private static final String VISIBLE_NAME = "Visible";
  private static final int VISIBLE_ORDER = 0;

  private static final String INLINE_ID = "inline";
  private static final String INLINE_NAME = "Inline";
  private static final int INLINE_ORDER = 0;

  public static final String FORMAT_STRING_ID = "formatString";
  public static final String FORMAT_STRING_NAME = "Format String";
  public static final int FORMAT_STRING_ORDER = 1;

  public static final String FORMAT_CATEGORY_ID = "formatCategory";
  public static final String FORMAT_CATEGORY_NAME = "Format Category";
  public static final int FORMAT_CATEGORY_ORDER = 2;

  public static final String DECIMAL_PLACES_ID = "decimalPlaces";
  public static final String DECIMAL_PLACES_NAME = "Decimal Places";
  public static final int DECIMAL_PLACES_ORDER = 3;

  public static final String MEASURE_CONTENT_ID = "measureContent";
  public static final String MEASURE_CONTENT_NAME = "Measure Content";
  public static final int MEASURE_CONTENT_ORDER = 4;

  public static final String CALCULATE_SUBTOTALS_ID = "calculateSubtotals";
  public static final String CALCULATE_SUBTOTALS_NAME = "Calculate Subtotals";
  public static final int CALCULATE_SUBTOTALS_ORDER = 5;

  public static final String CATALOG_NAME_ID = "catalogName";
  public static final String CATALOG_NAME_NAME = "Catalog Name";
  public static final int CATALOG_NAME_ORDER = 6;

  public static final String HIDDEN_ID = "hidden";
  public static final String HIDDEN_NAME = "Hidden";
  public static final int HIDDEN_ORDER = 7;

  public static final String CALCULATED_MEMBER_NODE_NAME = "CalculatedMember";
  public static final String CALCULATED_MEMBER_NAME_ATTRIB = "name";
  public static final String CALCULATED_MEMBER_COLUMN_ATTRIB = "column";
  public static final String CALCULATED_MEMBER_AGGREGATOR_ATTRIB = "aggregator";
  public static final String CUBE_XPATH_EXPR = "/Schema/Cube";

  public static final String MDI_GROUP = "CALC_MEASURE";

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  @Injection( name = MDI_GROUP + "_NAME", group = MDI_GROUP )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = CAPTION_ID, name = CAPTION_NAME, order = CAPTION_ORDER )
  private String caption;

  @MetaStoreAttribute
  @ModelProperty( id = DESCRIPTION_ID, name = DESCRIPTION_NAME, order = DESCRIPTION_ORDER )
  @Injection( name = MDI_GROUP + "_DESCRIPTION", group = MDI_GROUP )
  private String description;

  @MetaStoreAttribute
  @ModelProperty( id = FORMULA_ID, name = FORMULA_NAME, order = FORMULA_ORDER )
  @Injection( name = MDI_GROUP + "_FORMULA", group = MDI_GROUP )
  private String formula;

  // we don't currently support non-measure calc members so we aren't allowing metadata injection of the dimension yet
  @MetaStoreAttribute
  @ModelProperty( id = DIMENSION_ID, name = DIMENSION_NAME, order = DIMENSION_ORDER )
  private String dimension;

  /**
   * @deprecated Use {@link #hidden} instead.
   */
  @Deprecated
  @MetaStoreAttribute
  @ModelProperty( id = VISIBLE_ID, name = VISIBLE_NAME, order = VISIBLE_ORDER, hideUI = true )
  private boolean visible = true; // default

  @MetaStoreAttribute
  @ModelProperty( id = INLINE_ID, name = INLINE_NAME, order = INLINE_ORDER )
  private boolean inline;

  @MetaStoreAttribute
  @ModelProperty( id = FORMAT_STRING_ID, name = FORMAT_STRING_NAME, order = FORMAT_STRING_ORDER )
  @Injection( name = MDI_GROUP + "_FORMAT_STRING", group = MDI_GROUP )
  private String formatString;

  @MetaStoreAttribute
  @ModelProperty( id = FORMAT_CATEGORY_ID, name = FORMAT_CATEGORY_NAME, order = FORMAT_CATEGORY_ORDER )
  private String formatCategory;

  @MetaStoreAttribute
  @ModelProperty( id = DECIMAL_PLACES_ID, name = DECIMAL_PLACES_NAME, order = DECIMAL_PLACES_ORDER )
  private int decimalPlaces;

  @MetaStoreAttribute
  @ModelProperty( id = MEASURE_CONTENT_ID, name = MEASURE_CONTENT_NAME, order = MEASURE_CONTENT_ORDER )
  private String measureContent;

  @MetaStoreAttribute
  @ModelProperty( id = CALCULATE_SUBTOTALS_ID, name = CALCULATE_SUBTOTALS_NAME, order = CALCULATE_SUBTOTALS_ORDER )
  @Injection( name = MDI_GROUP + "_CALCULATE_SUBTOTALS", group = MDI_GROUP )
  private boolean calculateSubtotals;

  @MetaStoreAttribute
  @ModelProperty( id = HIDDEN_ID, name = HIDDEN_NAME, order = HIDDEN_ORDER )
  @Injection( name = MDI_GROUP + "_IS_HIDDEN", group = MDI_GROUP )
  private boolean hidden;

  private transient boolean pdiContext;

  /**
   * CalculatedMembers are not yet supported in the Metadata model
   *
   * @param workspace
   * @return
   * @throws org.pentaho.agilebi.modeler.ModelerException
   */
  @SuppressWarnings( "unchecked" ) @Override
  public boolean apply( ModelerWorkspace workspace, IMetaStore metaStore ) throws ModelerException {
    workspace.getWorkspaceHelper().populateDomain( workspace );
    List<OlapCube> cubes = (List<OlapCube>) workspace.getLogicalModel( ModelerPerspective.ANALYSIS ).getProperty(
        "olap_cubes" );
    OlapCube olapCube = cubes.get( 0 );
    OlapCalculatedMember calcMember =
        new OlapCalculatedMember( getName(), getDimension(), getFormula(), getFormatString(),
            isCalculateSubtotals(), isHidden() );
    olapCube.getOlapCalculatedMembers().add( calcMember );
    return true;
  }

  /**
   * @param schema
   * @return
   * @throws org.pentaho.agilebi.modeler.ModelerException
   */
  @Override public boolean apply( Document schema ) throws ModelerException {
    if ( schema == null ) {
      return false;
    }

    MondrianSchemaHandler mondrianSchemaHandler = new MondrianSchemaHandler( schema );

    MondrianDef.CalculatedMember calculatedMember = new MondrianDef.CalculatedMember();
    calculatedMember.name = this.getName();
    calculatedMember.caption = this.getCaption();
    calculatedMember.description = this.getDescription();
    calculatedMember.dimension = this.getDimension();
    calculatedMember.formula = this.getFormula();
    calculatedMember.visible = !this.isHidden();
    MondrianDef.CalculatedMemberProperty solveOrder = new MondrianDef.CalculatedMemberProperty();
    solveOrder.name = "SOLVE_ORDER";
    solveOrder.value = isCalculateSubtotals() ? "200" : "0";
    calculatedMember.memberProperties = new MondrianDef.CalculatedMemberProperty[] { solveOrder };

    // annotation to indicate this was created via inline modeling, should always be true
    MondrianDef.Annotation inline = new MondrianDef.Annotation();
    inline.name = AnnotationConstants.INLINE_ANNOTATION_CREATED_INLINE;
    inline.cdata = isInline() ? "true" : "false";

    // annotation to store decimal places (scale)
    MondrianDef.Annotation formatScaleAnnotation = new MondrianDef.Annotation();
    formatScaleAnnotation.name = AnnotationConstants.INLINE_ANNOTATION_FORMAT_SCALE;
    formatScaleAnnotation.cdata = String.valueOf( this.getDecimalPlaces() );

    // annotation to store format category
    MondrianDef.Annotation formatCategoryAnnotation = new MondrianDef.Annotation();
    formatCategoryAnnotation.name = AnnotationConstants.INLINE_ANNOTATION_FORMAT_CATEGORY;
    formatCategoryAnnotation.cdata = this.getFormatCategory();

    // annotation to store original formula
    MondrianDef.Annotation formulaExpressionAnnotation = new MondrianDef.Annotation();
    formulaExpressionAnnotation.name = AnnotationConstants.INLINE_ANNOTATION_FORMULA_EXPRESSION;
    formulaExpressionAnnotation.cdata = this.getFormula();

    // annotation to store calc subtotals
    MondrianDef.Annotation calcSubtotalsAnnotation = new MondrianDef.Annotation();
    calcSubtotalsAnnotation.name = AnnotationConstants.INLINE_ANNOTATION_CALCULATE_SUBTOTALS;
    calcSubtotalsAnnotation.cdata = isCalculateSubtotals() ? "true" : "false";

    // add annotations to collection
    MondrianDef.Annotations annot = new MondrianDef.Annotations();
    annot.array = new MondrianDef.Annotation[] {
      inline,
      formatScaleAnnotation,
      formatCategoryAnnotation,
      formulaExpressionAnnotation,
      calcSubtotalsAnnotation
    };

    calculatedMember.annotations = annot;

    calculatedMember.formatString = this.getFormatString();

    mondrianSchemaHandler.addCalculatedMember( null, calculatedMember );

    return true;
  }

  @Override
  public void validate() throws ModelerException {
    if ( StringUtils.isBlank( getName() ) ) {
      if ( this.pdiContext ) {
        throw new ModelerException(
            BaseMessages.getString(
                MSG_CLASS,
                "ModelAnnotation.CreateCalculatedMember.validation.MEASURE_NAME_REQUIRED"
            )
        );
      } else {
        throw new ModelerException(
            BaseMessages.getString(
                MSG_CLASS,
                "ModelAnnotation.CreateCalculatedMember.validation.CATALOG_NAME_REQUIRED"
            )
        );
      }
    }

    if ( StringUtils.isBlank( getFormula() ) ) {
      throw new ModelerException(
          BaseMessages.getString(
              MSG_CLASS,
              "ModelAnnotation.CreateCalculatedMember.validation.FORMULA_REQUIRED" )
      );
    }
  }

  @Override public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.CREATE_CALCULATED_MEMBER;
  }

  @Override public String getSummary() {
    return BaseMessages.getString( MSG_CLASS, "Modeler.CreateCalculatedMember.Summary", getName(), getFormula() );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption( String caption ) {
    this.caption = caption;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getFormula() {
    return formula;
  }

  public void setFormula( String formula ) {
    this.formula = formula;
  }

  public String getDimension() {
    return dimension;
  }

  public void setDimension( String dimension ) {
    this.dimension = dimension;
  }

  /**
   * @deprecated Use {@link #isHidden()} instead.
   */
  @Deprecated
  public boolean isVisible() {
    return visible;
  }

  /**
   * @deprecated Use {@link #setHidden(boolean)} instead.
   */
  @Deprecated
  public void setVisible( boolean visible ) {
    this.visible = visible;
  }

  public boolean isInline() {
    return inline;
  }

  public void setInline( boolean inline ) {
    this.inline = inline;
  }

  public String getFormatString() {
    return formatString;
  }

  public void setFormatString( String formatString ) {
    this.formatString = formatString;
  }

  public int getDecimalPlaces() {
    return decimalPlaces;
  }

  public void setDecimalPlaces( int decimalPlaces ) {
    this.decimalPlaces = decimalPlaces;
  }

  public void setMeasureContent( String measureContent ) {
    this.measureContent = measureContent;
  }

  public String getMeasureContent() {
    return measureContent;
  }

  public boolean isCalculateSubtotals() {
    return calculateSubtotals;
  }

  public void setCalculateSubtotals( boolean calculateSubtotals ) {
    this.calculateSubtotals = calculateSubtotals;
  }

  public String getField() {
    return null;
  }

  public boolean isPdiContext() {
    return pdiContext;
  }

  public void setPdiContext( boolean pdiContext ) {
    this.pdiContext = pdiContext;
  }

  public String getFormatCategory() {
    return formatCategory;
  }

  public void setFormatCategory( String formatCategory ) {
    this.formatCategory = formatCategory;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
    this.setVisible( !hidden ); // Override deprecated value
  }
}
