package org.pentaho.agilebi.modeler.gwt;


import org.pentaho.agilebi.modeler.multitable.JoinDTO;
import org.pentaho.agilebi.modeler.multitable.JoinFieldDTO;
import org.pentaho.agilebi.modeler.multitable.JoinTableDTO;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.security.RowLevelSecurity;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.*;
import org.pentaho.metadata.model.olap.*;

import java.io.Serializable;
/*
 * This class is a workaround for GWT. GWT is not able to compile these classes are they have been used in a map
 * http://code.google.com/p/google-web-toolkit/issues/detail?id=3521
 */
public class BogoPojo implements Serializable {

  private static final long serialVersionUID = 7542132543385685472L;
  TargetTableType targetTableType;
  LocalizedString localizedString;
  DataType dataType;
  AggregationType aggType;
  TargetColumnType targetColumnType;
  LocaleType localeType;
  RowLevelSecurity rowLevelSecurity;
  SecurityOwner securityOwner;
  Security security;
  FieldType fieldType;
  Font font;
  TableType tableType;
  RelationshipType relationshipType;
  JoinType joinType;
  Alignment alignment;
  Color color;
  ColumnWidth columnWidth;
  Boolean booleanValue;
  OlapDimension olapDim;
  OlapCube olapcube;
  OlapMeasure olapMeasure;
  OlapHierarchy olapHier;
  OlapHierarchyLevel level;
  OlapDimensionUsage dimUsage;
  LogicalTable lTable;
  LogicalModel lModel;
  LogicalColumn lColumn;
  LogicalRelationship lRelationship;
  Domain domain;
  JoinDTO joinDTO;
  JoinFieldDTO joinFieldDTO;
  JoinTableDTO joinTableDTO;  
  
  private AggregationType[] aggTypes;

  public Domain getDomain() {
    return domain;
  }

  public void setDomain(Domain domain) {
    this.domain = domain;
  }

  public LogicalColumn getlColumn() {
    return lColumn;
  }

  public void setlColumn(LogicalColumn lColumn) {
    this.lColumn = lColumn;
  }

  public LogicalModel getlModel() {
    return lModel;
  }

  public void setlModel(LogicalModel lModel) {
    this.lModel = lModel;
  }

  public LogicalTable getlTable() {
    return lTable;
  }

  public void setlTable(LogicalTable lTable) {
    this.lTable = lTable;
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }
  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }
  public Alignment getAlignment() {
    return alignment;
  }
  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }
  public Color getColor() {
    return color;
  }
  public void setColor(Color color) {
    this.color = color;
  }
  public ColumnWidth getColumnWidth() {
    return columnWidth;
  }
  public void setColumnWidth(ColumnWidth columnWidth) {
    this.columnWidth = columnWidth;
  }
  public JoinType getJoinType() {
    return joinType;
  }
  public void setJoinType(JoinType joinType) {
    this.joinType = joinType;
  }
  public RelationshipType getRelationshipType() {
    return relationshipType;
  }
  public void setRelationshipType(RelationshipType relationshipType) {
    this.relationshipType = relationshipType;
  }
  public TableType getTableType() {
    return tableType;
  }
  public void setTableType(TableType tableType) {
    this.tableType = tableType;
  }
  public Font getFont() {
    return font;
  }
  public void setFont(Font font) {
    this.font = font;
  }
  public TargetTableType getTargetTableType() {
    return targetTableType;
  }
  public void setTargetTableType(TargetTableType targetTableType) {
    this.targetTableType = targetTableType;
  }
  public LocalizedString getLocalizedString() {
    return localizedString;
  }
  public void setLocalizedString(LocalizedString localizedString) {
    this.localizedString = localizedString;
  }
  public DataType getDataType() {
    return dataType;
  }
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  public AggregationType getAggregationType() {
    return aggType;
  }
  public void setAggregationType(AggregationType aggType) {
    this.aggType = aggType;
  }

  public AggregationType[] getAggregationTypes() {
    return aggTypes;
  }
  public void setAggregationTypes(AggregationType[] aggTypes) {
    this.aggTypes = aggTypes;
  }

  public TargetColumnType getTargetColumnType() {
    return targetColumnType;
  }
  public void setTargetColumnType(TargetColumnType targetColumnType) {
    this.targetColumnType = targetColumnType;
  }
  public void setLocaleType(LocaleType localeType) {
    this.localeType = localeType;
  }
  public LocaleType getLocaleType() {
    return localeType;
  }
  public void setRowLevelSecurity(RowLevelSecurity rowLevelSecurity) {
    this.rowLevelSecurity = rowLevelSecurity;
  }
  public RowLevelSecurity getRowLevelSecurity() {
    return rowLevelSecurity;
  }
  public void setSecurityOwner(SecurityOwner securityOwner) {
    this.securityOwner = securityOwner;
  }
  public SecurityOwner getSecurityOwner() {
    return securityOwner;
  }
  public void setSecurity(Security security) {
    this.security = security;
  }
  public Security getSecurity() {
    return security;
  }
  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }
  public FieldType getFieldType() {
    return fieldType;
  }

  public OlapDimension getOlapDim() {
    return olapDim;
  }

  public void setOlapDim( OlapDimension olapDim ) {
    this.olapDim = olapDim;
  }

  public OlapCube getOlapcube() {
    return olapcube;
  }

  public void setOlapcube( OlapCube olapcube ) {
    this.olapcube = olapcube;
  }

  public OlapMeasure getOlapMeasure() {
    return olapMeasure;
  }

  public void setOlapMeasure( OlapMeasure olapMeasure ) {
    this.olapMeasure = olapMeasure;
  }

  public OlapHierarchy getOlapHier() {
    return olapHier;
  }

  public void setOlapHier( OlapHierarchy olapHier ) {
    this.olapHier = olapHier;
  }

  public OlapHierarchyLevel getLevel() {
    return level;
  }

  public void setLevel( OlapHierarchyLevel level ) {
    this.level = level;
  }

  public OlapDimensionUsage getDimUsage() {
    return dimUsage;
  }

  public void setDimUsage( OlapDimensionUsage dimUsage ) {
    this.dimUsage = dimUsage;
  }

  public LogicalRelationship getlRelationship() {
	return lRelationship;
  }

  public void setlRelationship(LogicalRelationship lRelationship) {
	this.lRelationship = lRelationship;
  }

  public JoinDTO getJoinDTO() {
	return joinDTO;
  }

  public void setJoinDTO(JoinDTO joinDTO) {
	this.joinDTO = joinDTO;
  }

  public JoinFieldDTO getJoinFieldDTO() {
	return joinFieldDTO;
  }

  public void setJoinFieldDTO(JoinFieldDTO joinFieldDTO) {
	this.joinFieldDTO = joinFieldDTO;
  }

  public JoinTableDTO getJoinTableDTO() {
	return joinTableDTO;
  }

  public void setJoinTableDTO(JoinTableDTO joinTableDTO) {
	this.joinTableDTO = joinTableDTO;
  }
}
