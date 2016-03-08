/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler.gwt;

import java.io.Serializable;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.geo.LocationRole;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.format.DataFormatHolder;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.security.RowLevelSecurity;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.Alignment;
import org.pentaho.metadata.model.concept.types.Color;
import org.pentaho.metadata.model.concept.types.ColumnWidth;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.pentaho.metadata.model.concept.types.Font;
import org.pentaho.metadata.model.concept.types.JoinType;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.RelationshipType;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.metadata.model.concept.types.TargetColumnType;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;

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
  OlapAnnotation olapAnnotation;
  LogicalTable lTable;
  LogicalModel lModel;
  LogicalColumn lColumn;
  LogicalRelationship lRelationship;
  Domain domain;
  JoinRelationshipModel joinDTO;
  JoinFieldModel joinFieldDTO;
  JoinTableModel joinTableDTO;
  GeoRole geoRole;
  LocationRole locationRole;
  GeoContext geoContext;
  DataFormatHolder dataFormatHolder;

  private AggregationType[] aggTypes;

  public Domain getDomain() {
    return domain;
  }

  public void setDomain( Domain domain ) {
    this.domain = domain;
  }

  public LogicalColumn getlColumn() {
    return lColumn;
  }

  public void setlColumn( LogicalColumn lColumn ) {
    this.lColumn = lColumn;
  }

  public LogicalModel getlModel() {
    return lModel;
  }

  public void setlModel( LogicalModel lModel ) {
    this.lModel = lModel;
  }

  public LogicalTable getlTable() {
    return lTable;
  }

  public void setlTable( LogicalTable lTable ) {
    this.lTable = lTable;
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue( Boolean booleanValue ) {
    this.booleanValue = booleanValue;
  }

  public Alignment getAlignment() {
    return alignment;
  }

  public void setAlignment( Alignment alignment ) {
    this.alignment = alignment;
  }

  public Color getColor() {
    return color;
  }

  public void setColor( Color color ) {
    this.color = color;
  }

  public ColumnWidth getColumnWidth() {
    return columnWidth;
  }

  public void setColumnWidth( ColumnWidth columnWidth ) {
    this.columnWidth = columnWidth;
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public void setJoinType( JoinType joinType ) {
    this.joinType = joinType;
  }

  public RelationshipType getRelationshipType() {
    return relationshipType;
  }

  public void setRelationshipType( RelationshipType relationshipType ) {
    this.relationshipType = relationshipType;
  }

  public TableType getTableType() {
    return tableType;
  }

  public void setTableType( TableType tableType ) {
    this.tableType = tableType;
  }

  public Font getFont() {
    return font;
  }

  public void setFont( Font font ) {
    this.font = font;
  }

  public TargetTableType getTargetTableType() {
    return targetTableType;
  }

  public void setTargetTableType( TargetTableType targetTableType ) {
    this.targetTableType = targetTableType;
  }

  public LocalizedString getLocalizedString() {
    return localizedString;
  }

  public void setLocalizedString( LocalizedString localizedString ) {
    this.localizedString = localizedString;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType( DataType dataType ) {
    this.dataType = dataType;
  }

  public AggregationType getAggregationType() {
    return aggType;
  }

  public void setAggregationType( AggregationType aggType ) {
    this.aggType = aggType;
  }

  public AggregationType[] getAggregationTypes() {
    return aggTypes;
  }

  public void setAggregationTypes( AggregationType[] aggTypes ) {
    this.aggTypes = aggTypes;
  }

  public TargetColumnType getTargetColumnType() {
    return targetColumnType;
  }

  public void setTargetColumnType( TargetColumnType targetColumnType ) {
    this.targetColumnType = targetColumnType;
  }

  public void setLocaleType( LocaleType localeType ) {
    this.localeType = localeType;
  }

  public LocaleType getLocaleType() {
    return localeType;
  }

  public void setRowLevelSecurity( RowLevelSecurity rowLevelSecurity ) {
    this.rowLevelSecurity = rowLevelSecurity;
  }

  public RowLevelSecurity getRowLevelSecurity() {
    return rowLevelSecurity;
  }

  public void setSecurityOwner( SecurityOwner securityOwner ) {
    this.securityOwner = securityOwner;
  }

  public SecurityOwner getSecurityOwner() {
    return securityOwner;
  }

  public void setSecurity( Security security ) {
    this.security = security;
  }

  public Security getSecurity() {
    return security;
  }

  public void setFieldType( FieldType fieldType ) {
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

  public JoinRelationshipModel getJoinDTO() {
    return joinDTO;
  }

  public void setJoinDTO( JoinRelationshipModel joinDTO ) {
    this.joinDTO = joinDTO;
  }

  public JoinFieldModel getJoinFieldDTO() {
    return joinFieldDTO;
  }

  public void setJoinFieldDTO( JoinFieldModel joinFieldDTO ) {
    this.joinFieldDTO = joinFieldDTO;
  }

  public JoinTableModel getJoinTableDTO() {
    return joinTableDTO;
  }

  public void setJoinTableDTO( JoinTableModel joinTableDTO ) {
    this.joinTableDTO = joinTableDTO;
  }

  public OlapAnnotation getOlapAnnotation() {
    return olapAnnotation;
  }

  public void setOlapAnnotation( OlapAnnotation olapAnnotation ) {
    this.olapAnnotation = olapAnnotation;
  }

  public GeoRole getGeoRole() {
    return geoRole;
  }

  public void setGeoRole( GeoRole geoRole ) {
    this.geoRole = geoRole;
  }

  public LocationRole getLocationRole() {
    return locationRole;
  }

  public void setLocationRole( LocationRole locationRole ) {
    this.locationRole = locationRole;
  }

  public GeoContext getGeoContext() {
    return geoContext;
  }

  public void setGeoContext( GeoContext geoContext ) {
    this.geoContext = geoContext;
  }

  public DataFormatHolder getDataFormatHolder() {
    return dataFormatHolder;
  }

  public void setDataFormatHolder( DataFormatHolder dataFormatHolder ) {
    this.dataFormatHolder = dataFormatHolder;
  }
}
