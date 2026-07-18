/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.agilebi.modeler.propforms;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.impl.XulEventHandler;

import java.io.Serializable;

public interface ModelerNodePropertiesForm<T> extends XulEventHandler, Serializable {
  void init( ModelerWorkspace workspace );

  void setBindingFactory( BindingFactory bf );

  void activate( T selection );

  void setObject( T t );
}
