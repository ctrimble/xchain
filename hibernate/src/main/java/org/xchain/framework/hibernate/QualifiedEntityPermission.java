/**
 *    Copyright 2011 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.xchain.framework.hibernate;

import org.xchain.framework.security.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason Rose
 * @author Josh Kennedy
 */
public abstract class QualifiedEntityPermission
  implements Permission
{
  private static final Logger log = LoggerFactory.getLogger(QualifiedEntityPermission.class);
  protected Class<?> entityClass = null;
  protected EntityOperation operation = null;

  public QualifiedEntityPermission(EntityOperation operation, Class<?> entityClass)
  {
    this.operation = operation;
    this.entityClass = entityClass;
  }

  public Class<?> getEntityClass()
  {
    return this.entityClass;
  }

  public boolean implies( Permission p )
  {
    if( !(p instanceof EntityPermission) ) {
      return false;
    }

    EntityPermission<?> ep = (EntityPermission<?>)p;

    // if the class for this test is not assignable to the entity class in question, then return false.
    if( !entityClass.isAssignableFrom(ep.getEntityClass()) ) {
      return false;
    }

    // if the operation for this class does not imply the operation for the specified class, then return false.
    if( !operation.implies( ep.getOperation() ) ) {
      return false;
    }

    // we may imply this permission, execute the query and test the result.
    boolean result = false;
    try {
      Object queryResult = qualifyPermission(p);
      result = processResult(queryResult);
    }
    catch( Exception e ) {
      log.error("Unhandled Exception", e);
    }

    return result;
  }
  
  /**
   * Performs the database-level context filtering for the permission.
   * @param p The permission we're checking
   * @return The result from the database
   */
  public abstract Object qualifyPermission(Permission p);

  protected boolean processResult( Object queryResult )
  {
    boolean result = false;

    if( queryResult == null ) {
      result = false;
    } else if( queryResult instanceof Object[] ) {
      throw new IllegalArgumentException("The supplied query cannot have more than one column in the result.");
    } else if( queryResult instanceof Boolean ) {
      result = (Boolean) queryResult;
    } else if( queryResult instanceof Integer ) {
      result = ((Integer) queryResult) > 0;
    } else if( queryResult instanceof Long ) {
      result = ((Long) queryResult) > 0;
    } else {
      result = false;
    }

    return result;
  }
}
