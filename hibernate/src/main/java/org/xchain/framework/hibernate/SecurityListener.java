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

import org.hibernate.cfg.Configuration;
import org.hibernate.event.Initializable;
import org.hibernate.event.PreDeleteEvent;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.xchain.framework.security.SecurityManager;

/**
 * @author Christian Trimble
 * @author Jason Rose
 */
public class SecurityListener
  implements PreDeleteEventListener, PreInsertEventListener, PreUpdateEventListener, PostLoadEventListener
{
  /**
   * Returns false if the user can delete the specified event, true otherwise.
   */
  public boolean onPreDelete( PreDeleteEvent event )
  {
    System.out.println("Checking pre delete permission.");

    // check if the current user can delete this entity.
//    SecurityManager.instance().checkPermission(new EntityPermission(EntityOperation.DELETE, event.getId(), event.getEntity(), event.getDeletedState(), event.getDeletedState(), event.getPersister()));
    
    // return false if the user can do the delete.
    return false;
  }

  public boolean onPreInsert( PreInsertEvent event )
  {
    System.out.println("Checking pre delete permission.");

    // check if the current user can insert this entity.
//    SecurityManager.instance().checkPermission(new EntityPermission(EntityOperation.INSERT, event.getId(), event.getEntity(), event.getState(), null, event.getPersister()));

    // return false if the user can do the insert.
    return false;
  }

  public void onPostLoad( PostLoadEvent event )
  {
    System.out.println("Checking pre delete permission.");

    // check if the current user can load this entity.  These permissions should be controlled by annotations on the class.
//    SecurityManager.instance().checkPermission(new EntityPermission(EntityOperation.LOAD, event.getId(), event.getEntity(), null, null, event.getPersister()));
  }

  public boolean onPreUpdate( PreUpdateEvent event )
  {
    System.out.println("Checking pre delete permission.");

    // check if the current user can update this entity.
//    SecurityManager.instance().checkPermission(new EntityPermission(EntityOperation.UPDATE, event.getId(), event.getEntity(), event.getState(), event.getOldState(), event.getPersister()));

    return false;
  }
}
