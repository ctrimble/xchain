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
package org.xchain.namespaces.hibernate.test.om;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.validator.NotNull;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
 */
@javax.persistence.Entity
public class User implements Serializable {
  /**
   * The user name for this user. Only admins can change a username.
   */
  @NotNull
  private String username;

  /**
   * The name for this user. Only an admin or the user can update this field.
   */
  @NotNull
  private String name;

  /**
   * We need to show that only admins can edit this field, but the user can view it (view is not realy a CRUD permission with hibenrate).
   */
  private Set<UserNote> userNoteSet = new HashSet<UserNote>();

  /**
   * We need to show that admins can change this field to any value, but The owner of this user can only set it to false, not true.
   */
  private boolean active;

  public User() {
    super();
  }

  public User(String username, String name) {
    super();
    this.username = username;
    this.name = name;
  }

  @Id
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @OneToMany(mappedBy = "user")
  public Set<UserNote> getUserNoteSet() {
    return this.userNoteSet;
  }

  public void setUserNoteSet(Set<UserNote> userNoteSet) {
    this.userNoteSet = userNoteSet;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }
}
