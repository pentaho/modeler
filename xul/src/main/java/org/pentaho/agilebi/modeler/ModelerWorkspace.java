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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableItemCollection;
import org.pentaho.agilebi.modeler.nodes.AvailableTable;
import org.pentaho.agilebi.modeler.nodes.BaseColumnBackedMetaData;
import org.pentaho.agilebi.modeler.nodes.CategoryMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.FieldMetaData;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.IAvailableItem;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.nodes.TimeRole;
import org.pentaho.agilebi.modeler.nodes.annotations.AnalyzerDateFormatAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.GeoAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.IAnnotationFactory;
import org.pentaho.agilebi.modeler.nodes.annotations.IMemberAnnotation;
import org.pentaho.agilebi.modeler.nodes.annotations.MemberAnnotationFactory;
import org.pentaho.agilebi.modeler.strategy.MultiTableAutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.StarSchemaAutoModelStrategy;
import org.pentaho.agilebi.modeler.format.DataFormatHolder;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapAnnotation;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * UI model behind the XUL-based interface. This class contains a reference from the context in which the modeling was
 * initiated through an IModelerSource which also provides model generation.
 * 
 * @author nbaker
 */
@SuppressWarnings( "unchecked" )
public class ModelerWorkspace extends XulEventSourceAdapter implements Serializable {

  private static final long serialVersionUID = 2058731810283858276L;
  private AvailableItemCollection availableTables = new AvailableItemCollection();

  private MainModelNode model;
  private RelationalModelNode relationalModel;

  private String sourceName;

  private transient IModelerSource source;

  private String selectedVisualization;

  private String schemaName;

  private Domain domain;

  private boolean dirty = true;

  // full path to file
  private String fileName;

  private boolean modelIsChanging;

  private boolean isTemporary;

  private AbstractMetaDataModelNode selectedNode;
  private IAvailableItem selectedAvailableItem;
  private IModelerWorkspaceHelper workspaceHelper;
  private AbstractMetaDataModelNode selectedRelationalNode;

  private transient ModelerMode currentModellingMode = ModelerMode.ANALYSIS_AND_REPORTING;
  private transient ModelerPerspective currentModelerPerspective = ModelerPerspective.ANALYSIS;

  private transient SimpleAutoModelStrategy simpleAutoModelStrategy;
  private transient MultiTableAutoModelStrategy multiTableAutoModelStrategy;
  private transient StarSchemaAutoModelStrategy starSchemaAutoModelStrategy;

  private GeoContext geoContext;

  private transient ModelerTreeHelper currentModelerTreeHelper;

  public ModelerWorkspace( IModelerWorkspaceHelper helper ) {
    this( helper, null );
  }

  public ModelerWorkspace( IModelerWorkspaceHelper helper, GeoContext geoContext ) {
    this.isTemporary = true;
    this.workspaceHelper = helper;

    setModel( new MainModelNode( this ) );
    setRelationalModel( new RelationalModelNode( this ) );

    this.geoContext = geoContext;
    simpleAutoModelStrategy = new SimpleAutoModelStrategy( workspaceHelper.getLocale(), geoContext );
    multiTableAutoModelStrategy = new MultiTableAutoModelStrategy( workspaceHelper.getLocale() );
    starSchemaAutoModelStrategy = new StarSchemaAutoModelStrategy( workspaceHelper.getLocale(), geoContext );
    AnalyzerDateFormatAnnotationFactory.register();
  }

  public GeoContext getGeoContext() {
    return geoContext;
  }

  @Bindable
  public MainModelNode getModel() {
    return model;
  }

  @Bindable
  public void setModel( MainModelNode model ) {
    this.model = model;
    model.addPropertyChangeListener( "children",
        new PropertyChangeListener() { //$NON-NLS-1$

          public void propertyChange( PropertyChangeEvent arg0 ) {
            if ( !modelIsChanging ) {
              fireModelChanged();
            }
          }
        } );
    model.addPropertyChangeListener( "valid", new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        isValid();
      }
    } );
  }

  @Bindable
  public RelationalModelNode getRelationalModel() {
    return relationalModel;
  }

  @Bindable
  public void setRelationalModel( RelationalModelNode model ) {
    this.relationalModel = model;
    relationalModel.addPropertyChangeListener( "children", new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        if ( !modelIsChanging ) {
          fireRelationalModelChanged();
        }
      }
    } );
    relationalModel.addPropertyChangeListener( "valid", new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        isValid();
      }
    } );
  }

  @Bindable
  public void setFileName( String fileName ) {
    String prevVal = this.fileName;
    String prevFriendly = getShortFileName();

    this.fileName = fileName;
    firePropertyChange( "fileName", prevVal, fileName ); //$NON-NLS-1$
    firePropertyChange( "shortFileName", prevFriendly, getShortFileName() ); //$NON-NLS-1$
  }

  @Bindable
  public String getShortFileName() {

    if ( fileName == null ) {
      return null;
    }
    int extensionPos = fileName.lastIndexOf( '.' );
    if ( extensionPos == -1 ) {
      extensionPos = fileName.length();
    }
    int sepPos = fileName.replace( '\\', '/' ).lastIndexOf( '/' );
    if ( sepPos == -1 ) {
      sepPos = 0;
    } else {
      sepPos++;
    }
    return fileName.substring( sepPos, extensionPos );
  }

  @Bindable
  public String getFileName() {
    return fileName;
  }

  // transMeta.getFilename()

  @Bindable
  public String getSourceName() {
    return sourceName;
  }

  @Bindable
  public void setSourceName( String sourceName ) {
    this.sourceName = sourceName;
    this.firePropertyChange( "sourceName", null, sourceName ); //$NON-NLS-1$
  }

  @Bindable
  public String getModelName() {
    return model.getName();
  }

  @Bindable
  public void setModelName( String modelName ) {
    String prevVal = model.getName();
    model.setName( modelName );
    setDirty( true );
    this.firePropertyChange( "modelName", prevVal, modelName ); //$NON-NLS-1$
  }

  @Bindable
  public String getRelationalModelName() {
    return relationalModel.getName();
  }

  @Bindable
  public void setRelationalModelName( String modelName ) {
    String prevVal = model.getName();
    relationalModel.setName( modelName );
    setDirty( true );
    this.firePropertyChange( "relationalModelName", prevVal, modelName ); //$NON-NLS-1$
  }

  @Bindable
  public boolean isDirty() {
    return dirty;
  }

  @Bindable
  public boolean isValid() {
    boolean valid = false;
    switch ( getModellingMode() ) {
      case ANALYSIS_AND_REPORTING:
        valid = this.model.isValid() && relationalModel.isValid();
        break;
      case REPORTING_ONLY:
        valid = relationalModel.isValid();
    }
    firePropertyChange( "valid", null, valid );
    return valid;
  }

  @Bindable
  public List<String> getValidationMessages() {
    Set<String> modelMsg = model.getValidationMessages();
    Set<String> relModelMsg = relationalModel.getValidationMessages();
    modelMsg.addAll( relModelMsg );
    return new ArrayList<String>( modelMsg );
  }

  @Bindable
  public void setDirty( boolean dirty ) {
    boolean prevVal = this.dirty;
    this.dirty = dirty;
    this.firePropertyChange( "dirty", prevVal, this.dirty ); //$NON-NLS-1$
  }

  @Bindable
  public AvailableItemCollection getAvailableTables() {
    return availableTables;
  }

  @Bindable
  public void setSelectedVisualization( String aVisualization ) {
    this.selectedVisualization = aVisualization;
  }

  @Bindable
  public String getSelectedVisualization() {
    return this.selectedVisualization;
  }

  public DimensionMetaData createDimensionFromNode( ColumnBackedNode obj ) {
    DimensionMetaData dimension = new DimensionMetaData( obj.getName() );
    dimension.setExpanded( true );
    HierarchyMetaData hierarchy = createHierarchyForParentWithNode( dimension, obj );
    hierarchy.setParent( dimension );
    hierarchy.setExpanded( true );
    dimension.add( hierarchy );
    return dimension;
  }

  public DimensionMetaData createDimensionFromAvailableTable( AvailableTable tbl ) {
    DimensionMetaData dimension = new DimensionMetaData( tbl.getName() );
    dimension.setExpanded( true );
    HierarchyMetaData hierarchy = new HierarchyMetaData( tbl.getName() );

    hierarchy.setExpanded( true );
    for ( AvailableField field : tbl.getChildren() ) {
      ColumnBackedNode node = this.createColumnBackedNode( field, ModelerPerspective.ANALYSIS );
      LevelMetaData level = createLevelForParentWithNode( hierarchy, node );
      hierarchy.add( level );
    }

    hierarchy.setParent( dimension );
    hierarchy.setExpanded( true );
    dimension.add( hierarchy );
    return dimension;
  }

  public DimensionMetaData createDimensionWithName( String dimName ) {
    DimensionMetaData dimension = new DimensionMetaData( dimName );
    dimension.setExpanded( true );
    HierarchyMetaData hierarchy = createHierarchyForParentWithNode( dimension, null );
    hierarchy.setParent( dimension );
    hierarchy.setExpanded( true );
    dimension.add( hierarchy );
    return dimension;
  }

  public void addDimensionFromNode( ColumnBackedNode obj ) {
    addDimension( createDimensionFromNode( obj ) );
  }

  public void addDimension( DimensionMetaData dim ) {
    boolean prevChangeState = this.modelIsChanging;
    this.setModelIsChanging( true );
    this.model.getDimensions().add( dim );
    this.setModelIsChanging( prevChangeState );
  }

  public void addCategory( CategoryMetaData cat ) {
    boolean prevChangeState = this.modelIsChanging;
    this.setRelationalModelIsChanging( true );
    this.relationalModel.getCategories().add( cat );
    this.setRelationalModelIsChanging( prevChangeState );
  }

  public LevelMetaData createLevelForParentWithNode( HierarchyMetaData parent, ColumnBackedNode obj ) {
    LevelMetaData level = new LevelMetaData( parent, obj.getName() );
    level.setParent( parent );
    level.setLogicalColumn( obj.getLogicalColumn() );
    return level;
  }

  public LevelMetaData createLevelForParentWithNode( HierarchyMetaData parent, String name ) {
    LevelMetaData level = new LevelMetaData( parent, name );
    level.setParent( parent );
    level.setLogicalColumn( findLogicalColumn( name ) );
    return level;
  }

  public MemberPropertyMetaData createMemberPropertyForParentWithNode( LevelMetaData parent, ColumnBackedNode obj ) {
    MemberPropertyMetaData memberProp = new MemberPropertyMetaData( parent, obj.getName() );
    memberProp.setParent( parent );
    memberProp.setLogicalColumn( obj.getLogicalColumn() );
    return memberProp;
  }

  public MemberPropertyMetaData createMemberPropertyForParentWithNode( LevelMetaData parent, String name ) {
    MemberPropertyMetaData memberProp = new MemberPropertyMetaData( parent, name );
    memberProp.setParent( parent );
    memberProp.setLogicalColumn( findLogicalColumn( name ) );
    return memberProp;
  }

  public FieldMetaData createFieldForParentWithNode( CategoryMetaData parent, AvailableField selectedField ) {
    FieldMetaData field =
        new FieldMetaData( parent, selectedField.getName(), "", selectedField.getDisplayName(), workspaceHelper
            .getLocale() ); //$NON-NLS-1$
    ColumnBackedNode node = createColumnBackedNode( selectedField, ModelerPerspective.REPORTING );
    field.setLogicalColumn( node.getLogicalColumn() );
    field.setFieldTypeDesc( node.getLogicalColumn().getDataType().getName() );
    switch ( node.getLogicalColumn().getDataType() ) {
      case DATE:
        field.setFormatstring( DataFormatHolder.DATE_FORMATS );
        break;
      case NUMERIC:
        field.setFormatstring( DataFormatHolder.NUMBER_FORMATS );
        break;
      case STRING:
        field.setFormatstring( DataFormatHolder.CONVERSION_FORMATS );
        break;
      case BOOLEAN:
      case URL:
      case BINARY:
      case UNKNOWN:
      case IMAGE:
      default:
        break;
    }
    field.setFormat( "" );
    return field;
  }

  public HierarchyMetaData createHierarchyForParentWithNode( DimensionMetaData parent, ColumnBackedNode obj ) {
    HierarchyMetaData hier = new HierarchyMetaData( obj.getName() );
    hier.setParent( parent );
    hier.setExpanded( true );
    if ( obj != null ) {
      LevelMetaData level = createLevelForParentWithNode( hier, obj );
      hier.add( level );
    }
    return hier;
  }

  private void fireTablesChanged() {
    // set the automodel strategy based on the number of available tables
    if ( availableTables.size() > 1 ) {
      if ( availableTables.findFactTable() != null ) {
        workspaceHelper.setAutoModelStrategy( starSchemaAutoModelStrategy );
      } else {
        workspaceHelper.setAutoModelStrategy( multiTableAutoModelStrategy );
      }
    } else {
      workspaceHelper.setAutoModelStrategy( simpleAutoModelStrategy );
    }
    firePropertyChange( "availableTables", null, this.availableTables ); //$NON-NLS-1$
  }

  private void fireModelChanged() {
    firePropertyChange( "model", null, model ); //$NON-NLS-1$
    setDirty( true );
  }

  private void fireRelationalModelChanged() {
    firePropertyChange( "relationalModel", null, relationalModel ); //$NON-NLS-1$
    setDirty( true );
  }

  public MeasureMetaData createMeasureForNode( AvailableField selectedField ) {

    MeasureMetaData meta =
        new MeasureMetaData( selectedField.getName(), "", selectedField.getDisplayName(), workspaceHelper.getLocale() ); //$NON-NLS-1$
    ColumnBackedNode node = createColumnBackedNode( selectedField, ModelerPerspective.ANALYSIS );
    meta.setLogicalColumn( node.getLogicalColumn() );
    return meta;
  }

  public void addMeasure( MeasureMetaData measure ) {

    boolean prevChangeState = isModelChanging();
    this.setModelIsChanging( true );
    this.model.getMeasures().add( measure );
    this.setModelIsChanging( prevChangeState );
  }

  public LogicalColumn findLogicalColumn( String id ) {
    LogicalColumn col = null;
    for ( LogicalColumn c : getLogicalModel( currentModelerPerspective ).getLogicalTables().get( 0 )
        .getLogicalColumns() ) {
      if ( c.getName( workspaceHelper.getLocale() ).equals( id ) ) {
        col = c;
        break;
      }
    }
    return col;
  }

  public LogicalTable findLogicalTable( IPhysicalTable table ) {
    return findLogicalTable( table, currentModelerPerspective );
  }

  public LogicalTable findLogicalTable( IPhysicalTable table, ModelerPerspective perspective ) {
    LogicalModel logicalModel = this.getLogicalModel( perspective );
    if ( logicalModel == null ) {
      return null;
    }
    for ( LogicalTable logicalTable : logicalModel.getLogicalTables() ) {
      if ( logicalTable.getPhysicalTable().equals( table )
          || logicalTable.getPhysicalTable().getId().equals( table.getId() )
          || logicalTable.getId().equals( table.getId() ) ) {

        return logicalTable;
        // boolean isOlapTable = logicalTable.getId().endsWith(BaseModelerWorkspaceHelper.OLAP_SUFFIX);
        // if (perspective == ModelerPerspective.ANALYSIS && isOlapTable) {
        // return logicalTable;
        // } else if (perspective == ModelerPerspective.REPORTING && !isOlapTable) {
        // return logicalTable;
        // }
      }
    }
    return null;
  }

  public void setModelSource( IModelerSource source ) {
    this.source = source;
  }

  public IModelerSource getModelSource() {
    return source;
  }

  public void setFields( List<MeasureMetaData> fields ) {
    this.model.getMeasures().clear();
    this.model.getMeasures().addAll( fields );
  }

  public void refresh( ModelerMode mode ) throws ModelerException {
    if ( source == null ) {
      return;
    }

    Domain newDomain = source.generateDomain( mode == ModelerMode.ANALYSIS_AND_REPORTING );
    refresh( newDomain );
  }

  public boolean supportsOlap( Domain d ) {
    if ( d.getLogicalModels().size() < 2 ) {
      return false;
    } else {
      LogicalModel lModel = d.getLogicalModels().get( 1 );
      return "true".equals( lModel.getProperty( "DUAL_MODELING_SCHEMA" ) )
          || lModel.getProperty( "MondrianCatalogRef" ) != null;
    }
  }

  public void refresh( Domain newDomain ) throws ModelerException {

    List<IAvailableItem> items = new ArrayList<IAvailableItem>();
    for ( IPhysicalTable table : newDomain.getPhysicalModels().get( 0 ).getPhysicalTables() ) {
      boolean isFact = table.getProperty( "FACT_TABLE" ) != null ? (Boolean) table.getProperty( "FACT_TABLE" ) : false;
      items.add( new AvailableTable( table, isFact ) );
    }

    availableTables.setChildren( items );

    setModelIsChanging( true );
    setRelationalModelIsChanging( true );

    // Set the type of modeling session. This will propigate to the UI
    if ( supportsOlap( newDomain ) ) {
      this.setModellingMode( ModelerMode.ANALYSIS_AND_REPORTING );
    } else {
      this.setModellingMode( ModelerMode.REPORTING_ONLY );
      // clear out OLAP side of the existing model
      model.getDimensions().clear();
    }
    List<AvailableTable> tablesList = availableTables.getAsAvailableTablesList();

    fireTablesChanged();

    // replace the domain with the new domain, which
    // makes sure the physical and logical columns are accurate
    domain = newDomain;

    for ( MeasureMetaData measure : model.getMeasures() ) {
      boolean found = false;
      if ( measure.getLogicalColumn() != null ) {
        inner:
        for ( AvailableTable table : tablesList ) {
          for ( AvailableField f : table.getAvailableFields() ) {
            if ( f.getPhysicalColumn().getId().equals( measure.getLogicalColumn().getPhysicalColumn().getId() )
                && f.getPhysicalColumn().getPhysicalTable().getId().equals(
                    measure.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId() ) ) {
              // the physical column backing this measure is still available, set it to the new one
              measure.setLogicalColumn( createColumnBackedNode( f, currentModelerPerspective ).getLogicalColumn() );
              found = true;
              break inner;
            }
          }
        }
        if ( !found ) {
          // the physical column that backed this measure no longer exists in the model.
          // therefore, we must invalidate it's logical column
          measure.setLogicalColumn( null );
        }
      }
    }

    try {
      for ( DimensionMetaData dm : model.getDimensions() ) {
        for ( HierarchyMetaData hm : dm ) {
          for ( LevelMetaData lm : hm ) {
            boolean found = false;
            if ( lm.getLogicalColumn() != null ) {
              inner:
              for ( AvailableTable table : tablesList ) {
                for ( AvailableField f : table.getAvailableFields() ) {
                  if ( f.getPhysicalColumn().getId().equals( lm.getLogicalColumn().getPhysicalColumn().getId() )
                      && f.getPhysicalColumn().getPhysicalTable().getId().equals(
                          lm.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId() ) ) {
                    // the physical column backing this level is still available, it is ok

                    lm.setLogicalColumn( createColumnBackedNode( f, currentModelerPerspective ).getLogicalColumn() );
                    found = true;
                    break inner;
                  }
                }
              }
            }
            if ( !found ) {
              // the physical column that backed this level no longer exists in the model.
              // therefore, we must invalidate it's logical column
              lm.setLogicalColumn( null );
            }
          }
        }
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    for ( CategoryMetaData category : relationalModel.getCategories() ) {
      for ( FieldMetaData field : category ) {
        boolean found = false;
        if ( field.getLogicalColumn() != null ) {
          inner:
          for ( AvailableTable table : tablesList ) {
            for ( AvailableField f : table.getAvailableFields() ) {
              if ( f.getPhysicalColumn().getId().equals( field.getLogicalColumn().getPhysicalColumn().getId() )
                  && f.getPhysicalColumn().getPhysicalTable().getId().equals(
                      field.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId() ) ) {

                // the physical column backing this field is still available, it is ok
                found = true;
                break inner;
              }
            }
          }
          if ( !found ) {
            // the physical column that backed this field no longer exists in the model.
            // therefore, we must invalidate it's logical column
            field.setLogicalColumn( null );
          }
        }
      }
    }

    // If the new model was previously "auto-modeled" we need to clean that now
    LogicalModel newLModel = getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( newLModel != null ) {
      List<OlapDimension> theDimensions = (List) newLModel.getProperty( "olap_dimensions" ); //$NON-NLS-1$
      if ( theDimensions != null ) {
        theDimensions.clear();
      }
      List<OlapCube> theCubes = (List) newLModel.getProperty( "olap_cubes" ); //$NON-NLS-1$
      if ( theCubes != null ) {
        theCubes.clear();
      }
    }

    setModelIsChanging( false );
    setRelationalModelIsChanging( false );

  }

  public String getDatabaseName() {
    return source.getDatabaseName();
  }

  @Bindable
  public String getSchemaName() {
    return schemaName;
  }

  @Bindable
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public void setAvailableTables( AvailableItemCollection tables ) {
    this.availableTables = tables;
    fireTablesChanged();
  }

  public void setDomain( Domain d ) {
    setDomain( d, true );
  }

  // this method signature is intended to provide a simpler path for unit testing the upConvert method on its own
  protected void setDomain( Domain d, boolean upConvertDesired ) {
    this.domain = d;
    this.setModelIsChanging( true );
    this.setRelationalModelIsChanging( true );
    this.model.getDimensions().clear();
    this.model.getMeasures().clear();
    this.relationalModel.getCategories().clear();
    this.availableTables.clear();

    if ( upConvertDesired ) {
      upConvertLegacyModel();
    }
    List<IAvailableItem> items = new ArrayList<IAvailableItem>();
    for ( IPhysicalTable table : domain.getPhysicalModels().get( 0 ).getPhysicalTables() ) {
      Boolean isFact = (Boolean) table.getProperty( "FACT_TABLE" );
      items.add( new AvailableTable( table, isFact == null ? false : isFact.booleanValue() ) );
    }

    availableTables.setChildren( items );

    fireTablesChanged();

    LogicalModel lModel = domain.getLogicalModels().get( 0 );

    setModelName( lModel.getName( workspaceHelper.getLocale() ) );
    setRelationalModelName( lModel.getName( workspaceHelper.getLocale() ) );

    // Set the type of modeling session. This will propagate to the UI
    if ( supportsOlap( domain ) ) {
      this.setModellingMode( ModelerMode.ANALYSIS_AND_REPORTING );
    } else {
      this.setModellingMode( ModelerMode.REPORTING_ONLY );
    }

    lModel = getLogicalModel( ModelerPerspective.ANALYSIS );
    List<OlapDimension> theDimensions = null;
    if ( lModel != null ) {
      theDimensions = (List) lModel.getProperty( LogicalModel.PROPERTY_OLAP_DIMS ); //$NON-NLS-1$
    }
    if ( theDimensions != null ) {
      Iterator<OlapDimension> theDimensionItr = theDimensions.iterator();
      while ( theDimensionItr.hasNext() ) {
        OlapDimension theDimension = theDimensionItr.next();

        DimensionMetaData theDimensionMD = new DimensionMetaData( theDimension.getName(), theDimension.getType() );
        theDimensionMD.setTimeDimension( theDimension.isTimeDimension() );
        List<OlapHierarchy> theHierarchies = (List) theDimension.getHierarchies();
        Iterator<OlapHierarchy> theHierarchiesItr = theHierarchies.iterator();
        while ( theHierarchiesItr.hasNext() ) {
          OlapHierarchy theHierarchy = theHierarchiesItr.next();
          HierarchyMetaData theHierarchyMD = new HierarchyMetaData( theHierarchy.getName() );

          List<OlapHierarchyLevel> theLevels = theHierarchy.getHierarchyLevels();
          Iterator<OlapHierarchyLevel> theLevelsItr = theLevels.iterator();
          while ( theLevelsItr.hasNext() ) {
            OlapHierarchyLevel theLevel = theLevelsItr.next();
            LevelMetaData theLevelMD = new LevelMetaData( theHierarchyMD, theLevel.getName() );

            theLevelMD.setParent( theHierarchyMD );

            theLevelMD.setUniqueMembers( theLevel.isHavingUniqueMembers() );
            if ( theDimensionMD.isTimeDimension() ) {
              TimeRole role = TimeRole.fromMondrianAttributeValue( theLevel.getLevelType() );
              if ( role != null ) {
                theLevelMD.setDataRole( role );
              }
            }

            // Make sure we're dealing with the OLAP copy. Note that duplicated columns will have an OLAP_[0-9]+ at the
            // end
            String refID;
            LogicalColumn olapCol;

            olapCol = theLevel.getReferenceColumn();
            if ( olapCol != null ) {
              refID = olapCol.getId();
              if ( !refID.endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX )
                  && !refID.contains( BaseModelerWorkspaceHelper.OLAP_SUFFIX + "_" ) ) {
                olapCol = ModelerConversionUtil.findCorrespondingOlapColumn( olapCol, lModel );
                theLevel.setReferenceColumn( olapCol );
              }
              theLevelMD.setLogicalColumn( olapCol );
            }

            olapCol = theLevel.getReferenceOrdinalColumn();
            if ( olapCol != null ) {
              refID = olapCol.getId();
              if ( !refID.endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX )
                  && !refID.contains( BaseModelerWorkspaceHelper.OLAP_SUFFIX + "_" ) ) {
                olapCol = ModelerConversionUtil.findCorrespondingOlapColumn( olapCol, lModel );
                theLevel.setReferenceOrdinalColumn( olapCol );
              }
              theLevelMD.setLogicalOrdinalColumn( olapCol );
            }

            olapCol = theLevel.getReferenceCaptionColumn();
            if ( olapCol != null ) {
              refID = olapCol.getId();
              if ( !refID.endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX )
                  && !refID.contains( BaseModelerWorkspaceHelper.OLAP_SUFFIX + "_" ) ) {
                olapCol = ModelerConversionUtil.findCorrespondingOlapColumn( olapCol, lModel );
                theLevel.setReferenceCaptionColumn( olapCol );
              }
              theLevelMD.setLogicalCaptionColumn( olapCol );
            }
            // get any logicalColumns and turn them into member properties
            if ( theLevel.getLogicalColumns() != null && theLevel.getLogicalColumns().size() > 0 ) {
              for ( LogicalColumn lc : theLevel.getLogicalColumns() ) {
                // BISERVER-11578 - Protect against null lc's in the collection. We still need to
                // investigate why this can happen in the model.
                if ( lc == null ) {
                  continue;
                }

                if ( !lc.getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX )
                    && !lc.getId().contains( BaseModelerWorkspaceHelper.OLAP_SUFFIX + "_" ) ) {
                  // not pointing to the olap col
                  lc = ModelerConversionUtil.findCorrespondingOlapColumn( lc, lModel );
                }
                MemberPropertyMetaData memberProp =
                    new MemberPropertyMetaData( theLevelMD, lc.getName( workspaceHelper.getLocale() ) );
                memberProp.setLogicalColumn( lc );
                memberProp.setDescription( lc.getDescription( workspaceHelper.getLocale() ) );
                theLevelMD.add( memberProp );
              }
            }
            List<OlapAnnotation> annotations = theLevel.getAnnotations();
            if ( annotations != null ) {
              for ( OlapAnnotation anno : annotations ) {
                IMemberAnnotation annoMeta = MemberAnnotationFactory.create( anno );
                theLevelMD.getMemberAnnotations().put( anno.getName(), annoMeta );
              }
            }
            theHierarchyMD.add( theLevelMD );
          }

          theHierarchyMD.setParent( theDimensionMD );
          theDimensionMD.add( theHierarchyMD );
        }
        this.model.getDimensions().add( theDimensionMD );
      }
    }
    List<OlapCube> theCubes = null;
    if ( lModel != null ) {
      theCubes = (List) lModel.getProperty( LogicalModel.PROPERTY_OLAP_CUBES ); //$NON-NLS-1$
    }
    if ( theCubes != null ) {
      Iterator<OlapCube> theCubeItr = theCubes.iterator();
      while ( theCubeItr.hasNext() ) {
        OlapCube theCube = theCubeItr.next();

        List<OlapMeasure> theMeasures = theCube.getOlapMeasures();
        Iterator<OlapMeasure> theMeasuresItr = theMeasures.iterator();
        while ( theMeasuresItr.hasNext() ) {
          OlapMeasure theMeasure = theMeasuresItr.next();

          MeasureMetaData theMeasureMD = new MeasureMetaData( workspaceHelper.getLocale() );

          if ( theMeasure.getName() == null || theMeasure.getName().length() == 0 ) {
            theMeasureMD.setName( theMeasure.getLogicalColumn().getName( workspaceHelper.getLocale() ) );
          } else {
            theMeasureMD.setName( theMeasure.getName() );
          }
          theMeasureMD.setFormat( (String) theMeasure.getLogicalColumn().getProperty( "mask" ) ); //$NON-NLS-1$
          theMeasureMD.setDefaultAggregation( theMeasure.getLogicalColumn().getAggregationType() );
          String possibleMeasureName = theMeasure.getLogicalColumn().getId();
          if ( !theMeasure.getLogicalColumn().getId().endsWith( BaseModelerWorkspaceHelper.OLAP_SUFFIX )
              && !theMeasure.getLogicalColumn().getId().contains( BaseModelerWorkspaceHelper.OLAP_SUFFIX + "_" ) ) {
            // change the backing column to the olap version
            LogicalColumn olapCol =
                ModelerConversionUtil.findCorrespondingOlapColumn( theMeasure.getLogicalColumn(), lModel );
            theMeasure.setLogicalColumn( olapCol );
          }

          // BISERVER-6077 - Mondrian exporter uses logical column names as measure names, make sure they get set
          // properly
          LogicalColumn lCol = theMeasure.getLogicalColumn();
          Set<String> locales = lCol.getName().getLocales();
          String[] stringLocals = locales.toArray( new String[] {} );
          if ( stringLocals != null && stringLocals.length > 0 ) {
            if ( theMeasure.getName() == null || theMeasure.getName().trim().length() == 0 ) {
              theMeasure.setName( possibleMeasureName );
            }
            lCol.setName( new LocalizedString( stringLocals[0], theMeasure.getName() ) );
          }

          theMeasureMD.setLogicalColumn( lCol );
          this.model.getMeasures().add( theMeasureMD );
        }
      }
    }

    lModel = this.getLogicalModel( ModelerPerspective.REPORTING );
    int i = 1;
    for ( Category cat : lModel.getCategories() ) {
      String catName =
          BaseModelerWorkspaceHelper.getCleanCategoryName( cat.getName( workspaceHelper.getLocale() ), this, i++ );
      CategoryMetaData catMeta = new CategoryMetaData( catName );
      for ( LogicalColumn col : cat.getLogicalColumns() ) {
        LogicalTable table = col.getLogicalTable();

        if ( !table.getLogicalColumns().contains( col ) ) {
          table.addLogicalColumn( col );
        }

        Object formatMask = col.getProperty( "mask" );
        String colName = col.getName( workspaceHelper.getLocale() );
        AggregationType aggType = col.getAggregationType();

        FieldMetaData field =
            new FieldMetaData( catMeta, colName, formatMask == null ? null : formatMask.toString(), colName,
                workspaceHelper.getLocale() );
        if ( aggType != null ) {
          field.setDefaultAggregation( aggType );
        } else {
          field.setDefaultAggregation( AggregationType.NONE );
        }
        field.setLogicalColumn( col );

        catMeta.add( field );
      }
      this.getRelationalModel().getCategories().add( catMeta );
    }

    this.setModelIsChanging( false, true );
    this.setRelationalModelIsChanging( false, true );

  }

  private void upConvertMeasuresAndDimensions() {
    if ( domain.getLogicalModels().size() == 1 ) {
      return;
    }
    LogicalModel model = domain.getLogicalModels().get( 1 );

    // clean out the tables, we'll recreate the logical columns
    for ( LogicalTable table : model.getLogicalTables() ) {
      table.getLogicalColumns().clear();
    }

    // set the dimension logical column references to the new olap columns
    for ( DimensionMetaData dim : getModel().getDimensions() ) {
      for ( HierarchyMetaData hier : dim ) {
        for ( LevelMetaData level : hier ) {
          // create new logical columns
          AvailableField field = new AvailableField( level.getLogicalColumn().getPhysicalColumn() );
          ColumnBackedNode node = createColumnBackedNode( field, ModelerPerspective.ANALYSIS );
          level.setLogicalColumn( node.getLogicalColumn() );
        }
      }
    }

    // set the measure logical column references to the new olap columns
    for ( MeasureMetaData measure : getModel().getMeasures() ) {
      AvailableField field = new AvailableField( measure.getLogicalColumn().getPhysicalColumn() );
      ColumnBackedNode node = createColumnBackedNode( field, ModelerPerspective.ANALYSIS );
      measure.setLogicalColumn( node.getLogicalColumn() );
    }

    // make sure the relationships are set too
    ModelerConversionUtil.duplicateRelationshipsForOlap( domain.getLogicalModels().get( 0 ), model );

    return;
  }

  protected boolean upConvertLegacyModel() {
    double version = ModelerConversionUtil.upConvertDomain( domain );
    if ( version < Double.parseDouble( BaseModelerWorkspaceHelper.AGILE_BI_VERSION ) ) {
      return true;
    }
    return false;
  }

  public void resolveConnectionFromDomain() {
    // set up the datasource
    if ( domain != null && source != null ) {
      domain.getPhysicalModels().get( 0 );
      // TODO: resolve GWT DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(physicalModel.getId(),
      // physicalModel.getDatasource());
      // TODO: resolve GWT source.setDatabaseMeta(databaseMeta);
    }

  }

  public Domain getDomain() {
    return updateDomain();
  }

  private Domain updateDomain() {
    // TODO: update domain with changes
    return domain;
  }

  public void setModelIsChanging( boolean changing ) {
    setModelIsChanging( changing, true );
  }

  public void setModelIsChanging( boolean changing, boolean fireChanged ) {
    this.modelIsChanging = changing;
    if ( !changing && fireChanged ) {
      fireTablesChanged();
      model.validateTree();
      isValid();
      fireModelChanged();
    }
  }

  public void setRelationalModelIsChanging( boolean changing ) {
    setRelationalModelIsChanging( changing, true );
  }

  public void setRelationalModelIsChanging( boolean changing, boolean fireChanged ) {
    this.modelIsChanging = changing;
    if ( !changing && fireChanged ) {
      fireTablesChanged();
      relationalModel.validateTree();
      isValid();
      fireRelationalModelChanged();
    }
  }

  @Bindable
  public boolean isModelChanging() {
    return modelIsChanging;
  }

  @Bindable
  public void setTemporary( boolean isTempoarary ) {
    this.isTemporary = isTempoarary;
  }

  @Bindable
  public boolean isTemporary() {
    return this.isTemporary;
  }

  @Bindable
  public AbstractMetaDataModelNode getSelectedNode() {
    return selectedNode;
  }

  @Bindable
  public void setSelectedNode( AbstractMetaDataModelNode node ) {
    AbstractMetaDataModelNode prevVal = this.selectedNode;
    this.selectedNode = node;
    firePropertyChange( "selectedNode", prevVal, node ); //$NON-NLS-1$
  }

  @Bindable
  public IAvailableItem getSelectedAvailableItem() {
    return selectedAvailableItem;
  }

  @Bindable
  public void setSelectedAvailableItem( IAvailableItem selectedAvailableItem ) {
    IAvailableItem prev = this.selectedAvailableItem;
    this.selectedAvailableItem = selectedAvailableItem;
    firePropertyChange( "selectedAvailableItem", prev, selectedAvailableItem );
  }

  @Bindable
  public AbstractMetaDataModelNode getSelectedRelationalNode() {
    return selectedRelationalNode;
  }

  @Bindable
  public void setSelectedRelationalNode( AbstractMetaDataModelNode node ) {
    AbstractMetaDataModelNode prevVal = this.selectedRelationalNode;
    this.selectedRelationalNode = node;
    firePropertyChange( "selectedRelationalNode", prevVal, node ); //$NON-NLS-1$
  }

  public IModelerWorkspaceHelper getWorkspaceHelper() {
    return workspaceHelper;
  }

  public void setWorkspaceHelper( IModelerWorkspaceHelper workspaceHelper ) {
    this.workspaceHelper = workspaceHelper;
  }

  @Bindable
  public ModelerMode getModellingMode() {
    return currentModellingMode;
  }

  @Bindable
  public void setModellingMode( ModelerMode currentModellingMode ) {
    ModelerMode prevVal = this.currentModellingMode;
    this.currentModellingMode = currentModellingMode;
    firePropertyChange( "modellingMode", prevVal, this.currentModellingMode );
    isValid();
  }

  public void setGeoContext( GeoContext geoContext ) {
    this.geoContext = geoContext;
    // reset the automodelstrategies
    this.simpleAutoModelStrategy.setGeoContext( geoContext );
    this.starSchemaAutoModelStrategy.setGeoContext( geoContext );

    IAnnotationFactory fact = new GeoAnnotationFactory( geoContext );
    MemberAnnotationFactory.registerFactory( "Geo.Role", fact );
    MemberAnnotationFactory.registerFactory( "Data.Role", fact );
    MemberAnnotationFactory.registerFactory( "Geo.RequiredParents", fact );
  }

  @Bindable
  public ModelerPerspective getCurrentModelerPerspective() {
    return currentModelerPerspective;
  }

  @Bindable
  public void setCurrentModelerPerspective( ModelerPerspective currentModelerPerspective ) {
    this.currentModelerPerspective = currentModelerPerspective;
    firePropertyChange( "currentModelerPerspective", null, currentModelerPerspective );
  }

  public ColumnBackedNode createColumnBackedNode( AvailableField field, ModelerPerspective perspective ) {
    String locale = workspaceHelper.getLocale();
    ColumnBackedNode node = new BaseColumnBackedMetaData( field.getName() );
    LogicalTable lTab = findLogicalTable( field.getPhysicalColumn().getPhysicalTable(), perspective );
    LogicalColumn lCol = null;

    if ( lCol == null ) {
      lCol = new LogicalColumn();
      lCol.setLogicalTable( lTab );
      // lCol.setParentConcept(lTab);
      lCol.setPhysicalColumn( field.getPhysicalColumn() );
      lCol.setDataType( field.getPhysicalColumn().getDataType() );
      if ( field.getPhysicalColumn().getAggregationList() != null ) {
        lCol.setAggregationList( field.getPhysicalColumn().getAggregationList() );
      }
      if ( field.getPhysicalColumn().getAggregationType() != null ) {
        lCol.setAggregationType( field.getPhysicalColumn().getAggregationType() );
      }
      lCol.setName( new LocalizedString( locale, field.getPhysicalColumn().getName( locale ) ) );
      String colId =
          "LC_" + toId( lTab.getPhysicalTable().getName( locale ) ) + "_" + toId( field.getPhysicalColumn().getId() );

      // lCol.setDescription(new LocalizedString(locale, field.getPhysicalColumn().getName(locale)));

      if ( perspective == ModelerPerspective.ANALYSIS ) {
        colId += BaseModelerWorkspaceHelper.OLAP_SUFFIX;
      }

      colId = BaseModelerWorkspaceHelper.uniquify( colId, lTab.getLogicalColumns() );
      lCol.setId( colId );

      lTab.addLogicalColumn( lCol );
    }

    node.setLogicalColumn( lCol );
    return node;
  }

  public LogicalColumn findLogicalColumn( IPhysicalColumn column, ModelerPerspective perspective ) {
    LogicalColumn col = null;
    IPhysicalTable physicalTable = column.getPhysicalTable();
    LogicalModel logicalModel = this.getLogicalModel( perspective );
    if ( logicalModel == null ) {
      return col;
    }
    for ( LogicalTable table : logicalModel.getLogicalTables() ) {
      if ( table.getPhysicalTable().getId().equals( physicalTable.getId() ) ) {
        if ( ( perspective == ModelerPerspective.ANALYSIS && table.getId().endsWith(
            BaseModelerWorkspaceHelper.OLAP_SUFFIX ) )
            || ( perspective == ModelerPerspective.REPORTING && !table.getId().endsWith(
                BaseModelerWorkspaceHelper.OLAP_SUFFIX ) ) ) {

          for ( LogicalColumn lCol : table.getLogicalColumns() ) {
            if ( lCol.getPhysicalColumn().getId().equals( column.getId() ) ) {
              return lCol;
            }
          }

        }
      }
    }

    return col;
  }

  @Bindable
  public ModelerTreeHelper getCurrentModelerTreeHelper() {
    return currentModelerTreeHelper;
  }

  @Bindable
  public void setCurrentModelerTreeHelper( ModelerTreeHelper currentModelerTreeHelper ) {
    this.currentModelerTreeHelper = currentModelerTreeHelper;
  }

  public LogicalModel getLogicalModel() {
    return getLogicalModel( ModelerPerspective.REPORTING );
  }

  public LogicalModel getLogicalModel( ModelerPerspective type ) {
    switch ( type ) {
      case ANALYSIS:
        if ( this.getDomain().getLogicalModels().size() == 1 ) {
          // we don't have an ANALYSIS model to return
          return null;
        } else {
          return this.getDomain().getLogicalModels().get( 1 );
        }
      default:
        return this.getDomain().getLogicalModels().get( 0 );
    }
  }

  public static final String toId( String name ) {
    if ( name == null ) {
      return name;
    }
    name = name.replaceAll( "[ .,:(){}\\[\\]]", "_" ); //$NON-NLS-1$ //$NON-NLS-2$
    name = name.replaceAll( "[\"`']", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    name = name.replaceAll( "_+", "_" ); //$NON-NLS-1$ //$NON-NLS-2$
    return name;
  }

  public static final String removeQuotes( String name ) {
    if ( name == null ) {
      return name;
    }
    name = name.replaceAll( "[\"`']", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    return name;
  }

}
