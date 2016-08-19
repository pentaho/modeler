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

package org.pentaho.agilebi.modeler.nodes.annotations;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoRole;
import org.pentaho.agilebi.modeler.geo.LocationRole;
import org.pentaho.metadata.model.olap.OlapAnnotation;

/**
 * This factory is used to rehydrate annotations for the Modeler model form a saved state. It takes in an OlapAnnotation
 * from the saved model and returns the higher-level corresponding Object
 * 
 * User: nbaker Date: 10/20/11
 */
public class GeoAnnotationFactory implements IAnnotationFactory {
  private GeoContext context;

  public GeoAnnotationFactory( GeoContext context ) {
    this.context = context;
  }

  @Override
  public IMemberAnnotation create( OlapAnnotation anno ) {
    if ( anno.getName().equals( GeoContext.ANNOTATION_GEO_ROLE ) ) {
      return context.getGeoRoleByName( anno.getValue() );
    } else if ( anno.getName().equals( GeoContext.ANNOTATION_DATA_ROLE ) ) {
      // return markers for data types
      if ( anno.getValue().equals( "LOCATION_ROLE" ) ) {
        return new LocationRole();
      } else {
        return new GeoRole();
      }
    }
    return null;
  }
}
