/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.agilebi.modeler.models.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rowell Belen
 */
@Target( { ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface ModelProperty {
  String id() default "";

  String name() default "";

  int order() default Integer.MAX_VALUE;

  boolean hideUI() default false;

  AppliesTo[] appliesTo() default { AppliesTo.String, AppliesTo.Time, AppliesTo.Numeric };

  enum AppliesTo {
    String,
    Numeric,
    Time;
  }
}
