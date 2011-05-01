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

import java.io.Serializable;

import org.xchain.framework.security.Permission;

/**
 * @author Christian Trimble
 * @author Jason Rose
 */
public class EntityPermission<E>
  extends EntityClassPermission
{
  Serializable id = null;
  E entity = null;
  
  public EntityPermission(EntityOperation operation, Serializable id, E entity)
  {
    super(operation, entity.getClass());
    this.id = id;
    this.entity = entity;
  }

  public Serializable getId()
  {
    return this.id;
  }

  /**
   * Returns the entity that is being edited.
   */
  public E getEntity()
  {
    return this.entity;
  }

  /**
   * This method only returns true if the specified permission is the same instance as this instance.
   */
  public boolean implies( Permission permission )
  {
    return this == permission;
  }
  
  @Override
  public String toString()
  {
    final String format = "%s[%s, %s, %s]";
    return String.format(format, getClass().getName(), getOperation(), getEntityClass().getName(), id);
  }
}
