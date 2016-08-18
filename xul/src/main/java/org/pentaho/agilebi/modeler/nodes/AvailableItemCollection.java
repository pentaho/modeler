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

package org.pentaho.agilebi.modeler.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

/**
 * Created: 4/11/11
 * 
 * @author rfellows
 */
public class AvailableItemCollection extends AbstractModelList<IAvailableItem> {
  public static final String IMAGE_FILE = "";
  private static final long serialVersionUID = -3640424842982505015L;

  private boolean expanded = true;
  private String name;
  private String image;

  public AvailableItemCollection() {
    setImage( IMAGE_FILE );
  }

  /**
   * This method overrides AbstractModelList.getChildren to support the ability to NOT show the table level node in the
   * tree if only 1 table is available. instead only the flat list oc fields should show.
   * 
   * @return
   */
  @Bindable
  @Override
  public List<IAvailableItem> getChildren() {
    int tableCount = getAvailableTableCount();
    if ( tableCount == 1 ) {
      return getAsFlatAvailableFieldsList();
    } else {
      Collections.sort( children, itemComparator );
      return children;
    }
  }

  @Bindable
  @Override
  public void setChildren( List<IAvailableItem> children ) {
    this.children = children;
    fireCollectionChanged();
  }

  public AvailableTable findAvailableTable( String tableName ) {
    for ( IAvailableItem item : children ) {
      if ( item instanceof AvailableTable ) {
        AvailableTable table = (AvailableTable) item;
        if ( table.getName().equals( tableName ) ) {
          return table;
        }
      }
    }
    return null;
  }

  public int getAvailableTableCount() {
    int count = 0;
    for ( IAvailableItem item : children ) {
      if ( item instanceof AvailableTable ) {
        count++;
      }
    }
    return count;
  }

  public List<AvailableTable> getAsAvailableTablesList() {
    List<AvailableTable> tables = new ArrayList<AvailableTable>();
    for ( IAvailableItem item : children ) {
      if ( item instanceof AvailableTable ) {
        tables.add( (AvailableTable) item );
      }
    }
    return tables;
  }

  protected List<IAvailableItem> getAsFlatAvailableFieldsList() {
    List<IAvailableItem> fields = new ArrayList<IAvailableItem>();
    for ( IAvailableItem item : children ) {
      if ( item instanceof AvailableTable ) {
        AvailableTable table = (AvailableTable) item;
        for ( AvailableField field : table.getAvailableFields() ) {
          fields.add( field );
        }
      }
    }
    return fields;
  }

  @Bindable
  public boolean isExpanded() {
    return expanded;
  }

  @Bindable
  public void setExpanded( boolean expanded ) {
    this.expanded = expanded;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  @Bindable
  public String getImage() {
    return image;
  }

  @Bindable
  public void setImage( String image ) {
    this.image = image;
  }

  private Comparator<IAvailableItem> itemComparator = new Comparator<IAvailableItem>() {
    public int compare( IAvailableItem arg0, IAvailableItem arg1 ) {
      return arg0.getName().compareTo( arg1.getName() );
    }
  };

  public AvailableTable findFactTable() {
    for ( IAvailableItem item : children ) {
      if ( item instanceof AvailableTable ) {
        AvailableTable table = (AvailableTable) item;
        if ( table.isFactTable() ) {
          return table;
        }
      }
    }
    return null;
  }

  public void setFactTable( IPhysicalTable table ) {
    for ( IAvailableItem item : children ) {
      if ( item instanceof AvailableTable ) {
        AvailableTable t = (AvailableTable) item;
        if ( !t.isSameUnderlyingPhysicalTable( table ) && t.isFactTable() ) {
          // clear the previous fact table setting
          t.setFactTable( false );
        } else if ( t.isSameUnderlyingPhysicalTable( table ) ) {
          t.setFactTable( true );
        }
      }
    }
  }

}
