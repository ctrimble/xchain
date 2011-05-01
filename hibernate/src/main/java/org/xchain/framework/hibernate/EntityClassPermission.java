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

/**
 * @author Christian Trimble
 * @author Jason Rose
 */
public class EntityClassPermission
  implements Permission
{
  EntityOperation operation = null;
  Class<?> entityClass;
  
  public EntityClassPermission(EntityOperation operation, Class entityClass)
  {
    this.operation = operation;
    this.entityClass = entityClass;
  }

  public EntityOperation getOperation()
  {
    return this.operation;
  }

  public Class getEntityClass()
  {
    return this.entityClass;
  }

  /**
   * This method returns false, since EntityPermission subclasses should never be tied to an Identity.
   */
  public boolean implies( Permission permission )
  {
    if( !(permission instanceof EntityClassPermission) ) {
      return false;
    }

    EntityClassPermission ecp = (EntityClassPermission)permission;

    // if the class for this permission is not the same as or a super class or interface of the entity being checked,
    // then we do not imply the permission to edit the class.
    if( !entityClass.isAssignableFrom(ecp.getEntityClass()) ) {
      return false;
    }

    if( !operation.implies(ecp.getOperation()) ) {
      return false;
    }

    return true;
  }
  
  @Override
  public String toString()
  {
    final String format = "%s[%s, %s]";
    return String.format(format, getClass().getName(), getOperation(), getEntityClass().getName());
  }
}
