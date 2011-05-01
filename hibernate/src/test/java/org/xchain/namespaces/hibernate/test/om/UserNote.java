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

import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.validator.NotNull;

/**
 * @author Christian Trimble
 */
@javax.persistence.Entity
public class UserNote
  extends Entity
{
  private User user;
  
  /**
   * The user name for this user. Only admins can change a username.
   */
  @NotNull
  private String text;

  public UserNote() {
    super();
  }

  @ManyToOne
  @LazyToOne(LazyToOneOption.NO_PROXY)
  @Fetch(FetchMode.SELECT)
  public User getUser()
  {
    return user;
  }

  public void setUser( User user )
  {
    this.user = user;
  }

  public String getText()
  {
    return text;
  }

  public void setText( String text )
  {
    this.text = text;
  }
}
