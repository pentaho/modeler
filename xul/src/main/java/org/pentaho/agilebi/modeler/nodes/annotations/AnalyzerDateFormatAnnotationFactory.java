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

import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.metadata.model.olap.OlapAnnotation;

public class AnalyzerDateFormatAnnotationFactory implements IAnnotationFactory {

  public static AnalyzerDateFormatAnnotationFactory instance = new AnalyzerDateFormatAnnotationFactory();

  public static void register() {
    MemberAnnotationFactory.registerFactory( IAnalyzerDateFormatAnnotation.NAME,
        AnalyzerDateFormatAnnotationFactory.instance );
  }

  private AnalyzerDateFormatAnnotationFactory() {

  }

  @Override
  public IMemberAnnotation create( OlapAnnotation anno ) {
    return new AnalyzerDateFormatAnnotation( anno.getValue() );
  }

  public IMemberAnnotation create( LevelMetaData levelMetaData ) {
    return new AnalyzerDateFormatAnnotation( levelMetaData );
  }
}
