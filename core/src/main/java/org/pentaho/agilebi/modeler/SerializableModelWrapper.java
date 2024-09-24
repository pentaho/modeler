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

package org.pentaho.agilebi.modeler;

import java.io.Serializable;

public class SerializableModelWrapper implements Serializable {
  private static final long serialVersionUID = -1350694219885660135L;
  private IModelerSource source;
  private String domain;

  public IModelerSource getSource() {
    return source;
  }

  public void setSource( IModelerSource source ) {
    this.source = source;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain( String domain ) {
    this.domain = domain;
  }
}
