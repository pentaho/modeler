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

import org.pentaho.metadata.model.olap.OlapAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * This factory is used to rehydrate annotations from saved state. IAnotation factories are registered for annotation
 * keys (Geo.Role, Data.Role, etc) and are called on to recreate the Modeler annotation objects from Metadata versions.
 * 
 * User: nbaker Date: 10/20/11
 */
public class MemberAnnotationFactory {
  private static Map<String, IAnnotationFactory> factories = new HashMap<String, IAnnotationFactory>();

  public static void registerFactory( String type, IAnnotationFactory factory ) {
    factories.put( type, factory );
  }

  public static IMemberAnnotation create( OlapAnnotation anno ) {
    if ( anno == null ) {
      throw new IllegalArgumentException( "Annotation is null" );
    }
    IAnnotationFactory fact = factories.get( anno.getName() );
    if ( fact != null ) {
      return fact.create( anno );
    }
    return null;
  }

}
