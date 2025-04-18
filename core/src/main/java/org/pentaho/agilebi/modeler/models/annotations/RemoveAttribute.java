/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.w3c.dom.Document;

import java.util.logging.Logger;

/**
 * @author Brandon Groves
 */
@MetaStoreElementType( name = "RemoveAttribute", description = "RemoveAttribute Annotation" )
public class RemoveAttribute extends AnnotationType {

  private static final long serialVersionUID = 6421805119523455990L;
  private static transient Logger logger = Logger.getLogger( AnnotationType.class.getName() );

  private static final String NAME_ID = "name";
  private static final String NAME_NAME = "Name";
  private static final int NAME_ORDER = 0;

  private static final String LEVEL_ID = "level";
  private static final String LEVEL_NAME = "Level";
  private static final int LEVEL_ORDER = 1;

  @MetaStoreAttribute
  @ModelProperty( id = NAME_ID, name = NAME_NAME, order = NAME_ORDER )
  private String name;

  @MetaStoreAttribute
  @ModelProperty( id = LEVEL_ID, name = LEVEL_NAME, order = LEVEL_ORDER )
  private String level;

  @Override
  public boolean apply(
    final ModelerWorkspace workspace, final IMetaStore metaStore ) throws ModelerException {
    LevelMetaData existingLevel = locateLevelFromFormula( workspace, level );
    boolean isApplied = false;

    if ( existingLevel != null && workspace != null ) {
      removeLevel( workspace, existingLevel );
      workspace.getWorkspaceHelper().populateDomain( workspace );
      isApplied = true;
    }

    return isApplied;
  }

  private LevelMetaData locateLevelFromFormula( final ModelerWorkspace workspace, final String formula ) {
    if ( formula == null || workspace == null ) {
      return null;
    }

    for ( DimensionMetaData dimensionMetaData : workspace.getModel().getDimensions() ) {
      for ( HierarchyMetaData hierarchyMetaData : dimensionMetaData ) {
        for ( LevelMetaData levelMetaData : hierarchyMetaData ) {
          StringBuffer formulaBuffer = new StringBuffer();
          formulaBuffer.append( "[" );
          formulaBuffer.append( dimensionMetaData.getName() );

          if ( StringUtils.isNotEmpty( hierarchyMetaData.getName() ) ) {
            formulaBuffer.append( "." );
            formulaBuffer.append( hierarchyMetaData.getName() );
          }

          formulaBuffer.append( "].[" );
          formulaBuffer.append( levelMetaData.getName(  ) );
          formulaBuffer.append( "]" );

          if ( formula.equals( formulaBuffer.toString() ) ) {
            return levelMetaData;
          }
        }
      }
    }

    return null;
  }

  /**
   * Removes level from Hierarchy
   *
   * @param levelMetaData level to be removed
   */
  private void removeLevel( final ModelerWorkspace workspace, final LevelMetaData levelMetaData ) {
    if ( levelMetaData == null ) {
      return;
    }

    HierarchyMetaData hierarchyMetaData = levelMetaData.getHierarchyMetaData();
    if ( hierarchyMetaData.getLevels().size() == 1 ) {
      // Remove the hierarchy and dimension if this is the only level created (ex. auto modeling)
      removeAutoLevel( workspace, levelMetaData, true );
    } else if ( hierarchyMetaData.contains( levelMetaData ) ) {
      hierarchyMetaData.remove( levelMetaData );
    }
  }

  @Override
  public boolean apply( final Document schema ) throws ModelerException {
    throw new UnsupportedOperationException();
  }

  @Override public void validate() throws ModelerException {
    if ( StringUtils.isBlank( getLevel() ) ) {
      throw new ModelerException( BaseMessages.getString( MSG_CLASS,
        "ModelAnnotation.RemoveAttribute.validation.FIELD_NAME_REQUIRED" ) );
    }
  }

  @Override
  public ModelAnnotation.Type getType() {
    return ModelAnnotation.Type.REMOVE_ATTRIBUTE;
  }

  private String summaryMsgKey() {
    return "Modeler.RemoveAttribute.Summary";
  }

  @Override
  public String getSummary() {
    return BaseMessages
      .getString( MSG_CLASS, summaryMsgKey(), getName() );
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel( String level ) {
    this.level = level;
  }

  @Override
  public String getField() {
    return null;
  }
}

