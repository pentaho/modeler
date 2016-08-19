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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.BaseAggregationMetaDataNode;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/24/11
 * 
 * @author rfellows
 */
public class FieldsPropertiesForm extends MeasuresPropertiesForm {
  private static final long serialVersionUID = 1554704803752425723L;
  private static final String ID = "fieldprops";
  private Collection<AggregationType> selectedAggregations;
  private XulButton messageBtn;

  public FieldsPropertiesForm( String locale ) {
    super( ID, locale );
  }

  public FieldsPropertiesForm( String panelId, String locale ) {
    super( panelId, locale );
  }

  @Override
  @Bindable
  public void init( ModelerWorkspace workspace ) {
    this.workspace = workspace;
    deck = (XulDeck) document.getElementById( "propertiesdeck" );
    panel = (XulVbox) document.getElementById( ID );
    XulMenuList formatFieldList = (XulMenuList) document.getElementById( "fieldformatstring" );

    bf.createBinding( this, "notValid", "fieldmessages", "visible" );
    bf.createBinding( this, "validMessages", "fieldmessageslabel", "value", validMsgTruncatedBinding );
    bf.createBinding( this, "displayName", "fielddisplayname", "value" );
    bf.createBinding( this, "possibleAggregations", "field_optionalAggregationTypes", "elements" );
    bf.createBinding( this, "selectedAggregations", "field_optionalAggregationTypes", "selectedItems", BindingConvertor.collection2ObjectArray() );
    bf.createBinding( this, "possibleAggregations", "field_defaultAggregation", "elements" );
    bf.createBinding( this, "defaultAggregation", "field_defaultAggregation", "selectedItem" );

    bf.createBinding( this, "format", formatFieldList, "value", new FormatStringConverter() );
    bf.createBinding( this, "formatstring", formatFieldList, "elements" );
    bf.createBinding( this, "backingColumnAvailable", "fixFieldColumnsBtn", "!visible" );
    bf.createBinding( this, "columnName", "field_column_name", "value" );
    messageBtn = (XulButton) document.getElementById( "field_message_btn" );
    bf.createBinding( this, "validMessages", messageBtn, "visible", showMsgBinding );

  }

  @Override
  public void setObject( BaseAggregationMetaDataNode t ) {
    selectedAggregations = null;
    super.setObject( t );
    if ( t == null ) {
      return;
    }

    // curent aggregation is blown away when setting the potential list. Cache it and reset it on the other-side
    AggregationType aggType = t.getDefaultAggregation();
    setSelectedAggregations( t.getSelectedAggregations() );
    setDefaultAggregation( aggType ); // reset default
  }

  @Bindable
  @Override
  public void setPossibleAggregations( Vector aggTypes ) {
    this.aggTypes = aggTypes;
    this.firePropertyChange( "possibleAggregations", null, aggTypes );
    if ( getNode() != null ) {
      setSelectedAggregations( aggTypes );
    }
  }

  @Bindable
  public void setSelectedAggregations( Collection<AggregationType> selectedAggs ) {
    Collection<AggregationType> prevVal = selectedAggregations;
    selectedAggregations = selectedAggs;
    if ( getNode() != null ) {
      getNode().setSelectedAggregations( new ArrayList<AggregationType>( selectedAggs ) );
    }
    if ( prevVal == null || prevVal.equals( selectedAggregations ) == false ) {
      this.firePropertyChange( "selectedAggregations", null, selectedAggregations );
    }
    // this will clear the selection of the default aggregate. Reselect it here

  }

  @Bindable
  public List<AggregationType> getSelectedAggregations() {
    if ( getNode() != null ) {
      return getNode().getSelectedAggregations();
    }
    return Collections.emptyList();
  }

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
}
