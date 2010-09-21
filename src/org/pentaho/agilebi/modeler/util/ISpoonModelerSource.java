package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.di.core.database.DatabaseMeta;

/**
 * User: nbaker
 * Date: Sep 21, 2010
 */
public interface ISpoonModelerSource extends IModelerSource {
  DatabaseMeta getDatabaseMeta();
}
