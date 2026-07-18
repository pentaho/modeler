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



package org.pentaho.agilebi.modeler.geo;

import org.pentaho.agilebi.modeler.ModelerException;

/**
 * Created by IntelliJ IDEA. User: rfellows Date: 9/27/11 Time: 10:14 AM To change this template use File | Settings |
 * File Templates.
 */
public interface GeoContextConfigProvider {

  public String getDimensionName() throws ModelerException;

  public String getRoles() throws ModelerException;

  public String getRoleAliases( String roleName ) throws ModelerException;

  public String getRoleRequirements( String roleName ) throws ModelerException;

}
