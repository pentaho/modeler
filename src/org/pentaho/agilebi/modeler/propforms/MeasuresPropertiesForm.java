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

package org.pentaho.agilebi.modeler.propforms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;


import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.BaseAggregationMetaDataNode;
import org.pentaho.agilebi.modeler.format.DataFormatHolder;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MeasuresPropertiesForm extends AbstractModelerNodeForm<BaseAggregationMetaDataNode> {
  private static final long serialVersionUID = -8703255300288774342L;
  protected Vector aggTypes;
  private String colName;
  private String locale;
  protected AggregationType defaultAggregation;
  protected String format;
  protected List<String> formatstring;
  private XulButton messageBtn;

  public MeasuresPropertiesForm( String panelId, String locale ) {
    super( panelId );
    this.locale = locale;
  }

  private PropertyChangeListener propListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( evt.getPropertyName().equals( "logicalColumn" ) ) {
        setColumnName( getNode().getLogicalColumn() );
      } else if ( evt.getPropertyName().equals( "possibleAggregations" ) ) {
        setPossibleAggregations( new Vector( getNode().getPossibleAggregations() ) );
      } else if ( evt.getPropertyName().equals( "defaultAggregation" ) ) {
        setDefaultAggregation( getNode().getDefaultAggregation() );
      } else if ( evt.getPropertyName().equals( "format" ) ) {
        setFormat( getNode().getFormat() );
      } else if ( evt.getPropertyName().equals( "formatList" ) ) {
        setFormatstring( getNode().getFormatstring() );
      }
    }
  };

  private PropertyChangeListener validListener = new PropertyChangeListener() {

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( !evt.getPropertyName().equals( "valid" ) ) {
        return;
      }
      showValidations();
    }
  };

  public MeasuresPropertiesForm( String locale ) {
    this( "measuresprops", locale );
  }

  @Bindable
  public void init( ModelerWorkspace workspace ) {
    super.init( workspace );
    XulMenuList formatXulMenuList = (XulMenuList) document.getElementById( "formatstring" );

    bf.createBinding( this, "notValid", "messages2", "visible" );
    bf.createBinding( this, "validMessages", "messages2label", "value", validMsgTruncatedBinding );
    bf.createBinding( this, "displayName", "displayname", "value" );
    bf.createBinding( this, "possibleAggregations", "defaultAggregation", "elements" );
    bf.createBinding( this, "defaultAggregation", "defaultAggregation", "selectedItem" );
    bf.createBinding( this, "format", formatXulMenuList, "value", new FormatStringConverter() );
    bf.createBinding( this, "formatstring", formatXulMenuList, "elements" );
    bf.createBinding( this, "backingColumnAvailable", "fixMeasuresColumnsBtn", "!visible" );
    bf.createBinding( this, "columnName", "measure_column_name", "value" );
    messageBtn = (XulButton) document.getElementById( "measure_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );
  }

  private void showValidations() {
    setNotValid( !getNode().isValid() );
    setBackingColumnAvailable( getNode().getLogicalColumn() != null );
    setValidMessages( getNode().getValidationMessagesString() );
  }

  public void setObject( BaseAggregationMetaDataNode t ) {
    if ( getNode() != null ) {
      getNode().removePropertyChangeListener( validListener );
      getNode().removePropertyChangeListener( propListener );
      setNode( null );
    }
    if ( t == null ) {
      return;
    }

    t.addPropertyChangeListener( validListener );
    t.addPropertyChangeListener( propListener );

    switch ( t.getLogicalColumn().getDataType() ) {
      case DATE:
        setFormatstring( DataFormatHolder.DATE_FORMATS );
        break;
      case NUMERIC:
        setFormatstring( DataFormatHolder.NUMBER_FORMATS );
        break;
      case STRING:
        setFormatstring( DataFormatHolder.CONVERSION_FORMATS );
        break;
      case BOOLEAN:
      case URL:
      case BINARY:
      case UNKNOWN:
      case IMAGE:
      default:
        break;
    }

    setDisplayName( t.getName() );
    setFormat( ( t.getFormat() != null &&  t.getFormat().length() > 0 ) ? t.getFormat() : "#" );

    AggregationType aggType = t.getDefaultAggregation();
    if ( t.getPossibleAggregations() != null ) {
      setPossibleAggregations( new Vector( t.getPossibleAggregations() ) );
    }
    setValidMessages( t.getValidationMessagesString() );
    setColumnName( t.getLogicalColumn() );
    setNode( t );
    showValidations();
    setDefaultAggregation( aggType );
  }

  @Bindable
  public void setColumnName( LogicalColumn col ) {
    String prevName = this.colName;
    // TODO: GWT LanguageChoice.getInstance().getDefaultLocale().toString()
    this.colName = ( col != null && col.getPhysicalColumn() != null ) ? col.getPhysicalColumn().getName( locale ) : ""; //$NON-NLS-1$
    this.firePropertyChange( "columnName", prevName, this.colName ); //$NON-NLS-1$
  }

  @Bindable
  public String getColumnName() {
    return colName;
  }

  @Bindable
  public boolean isNotValid() {
    if ( getNode() != null ) {
      return !getNode().isValid();
    } else {
      return false;
    }
  }

  @Bindable
  public void setNotValid( boolean notValid ) {
    this.firePropertyChange( "notValid", null, notValid );
  }

  @Bindable
  public boolean isBackingColumnAvailable() {
    if ( getNode() != null ) {
      return getNode().getLogicalColumn() != null;
    } else {
      return false;
    }
  }

  @Bindable
  public void setBackingColumnAvailable( boolean available ) {
    this.firePropertyChange( "backingColumnAvailable", null, available );
  }

  @Override
  public String getName() {
    return "propertiesForm";
  }

  @Bindable
  public String getDisplayName() {
    if ( getNode() == null ) {
      return null;
    }
    return getNode().getName();
  }

  @Bindable
  public void setDisplayName( String displayName ) {
    if ( getNode() != null ) {
      getNode().setName( displayName );
    }
    this.firePropertyChange( "displayName", null, displayName );

  }

  @Bindable
  public String getFormat() {
    return this.format;
  }

  @Bindable
  public void setFormat( String format ) {
    String previousFormat = this.format;
    this.format = format;
    if ( getNode() != null ) {
      getNode().setFormat( format );
    }
    this.firePropertyChange( "format", previousFormat, format );
  }

  @Bindable
  public Vector getPossibleAggregations() {
    return aggTypes;
  }

  @Bindable
  public void setPossibleAggregations( Vector aggTypes ) {
    Vector previous = this.aggTypes;
    this.aggTypes = aggTypes;
    this.firePropertyChange( "possibleAggregations", previous, aggTypes );
  }

  @Bindable
  public AggregationType getDefaultAggregation() {
    return defaultAggregation;
  }

  @Bindable
  public void setDefaultAggregation( AggregationType defaultAggregation ) {
    AggregationType previousAggregation = this.defaultAggregation;
    if ( previousAggregation == null && defaultAggregation == null ) {
      return;
    }
    if ( previousAggregation != null && defaultAggregation != null
        && previousAggregation.equals( defaultAggregation ) ) {
      return;
    }
    this.defaultAggregation = defaultAggregation;
    this.firePropertyChange( "defaultAggregation", previousAggregation, defaultAggregation );
    if ( getNode() != null ) {
      getNode().setDefaultAggregation( defaultAggregation );
    }
  }

  @Bindable
  public List<String> getFormatstring() {
    return formatstring;
  }

  @Bindable
  public void setFormatstring( List<String> formatList ) {
    List<String> previousXulMenuList = this.formatstring;
    this.formatstring = formatList;
    this.firePropertyChange( "formatstring", previousXulMenuList, this.formatstring );
    if ( getNode() != null ) {
      getNode().setFormatstring( formatList );
    }
  }

  /**
   * @author wseyler
   */
  private static class FormatStringConverter extends BindingConvertor<String, String> {

    @Override
    public String sourceToTarget( String value ) {
      if ( value == null ) {
        return "NONE";
      } else {
        return value;
      }
    }

    @Override
    public String targetToSource( String value ) {
      if ( value.equalsIgnoreCase( "NONE" ) ) {
        return null;
      } else {
        return value;
      }
    }

  }

  @Override
  public String getValidMessages() {
    if ( getNode() != null ) {
      return getNode().getValidationMessagesString();
    } else {
      return null;
    }
  }
}
