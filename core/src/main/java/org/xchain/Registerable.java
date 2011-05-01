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
package org.xchain;

import javax.xml.namespace.QName;

/**
 *   This interface is added to engineered commands, so that their registration information can be
 * tracked.  If a command is registered with a catalog, then isRegistered() will return true, the
 * system id will be set to the system id of the catalog, and the qname will be set to the qname of
 * the command in the catalog.  If isRegistered() returns false, then the values of system id and
 * qname are not defined.
 *
 *   If a command needs registration information, it can be declared abstract and implement this
 * interface without providing implementation for these methods.  XChains will extend the command and
 * provide implementations for the abstract methods.
 *
 * @author Christian Trimble
 */
public interface Registerable
{
  /**
   * Returns true if this command is registered with a catalog, false otherwise.
   *
   * @return true if this command is registered with a catalog, false otherwise.
   */
  public boolean isRegistered();

  /**
   * If this command is registered, then the qname for this command is returned.  Otherwise,
   * the result is undefined.
   *
   * @return the qname of this command, if it is registered.  The result is undefined otherwise.
   */
  public QName getQName();

  /**
   * If this command is registered, then the system id for the catalog it is registered in is returned.  Otherwise,
   * the result is undefined.
   *
   * @return the system id of the catalog to which this command is registered.  If the command is not registered, then the
   *         result is undefined.
   */
  public String getSystemId();

  /**
   * Sets the registration qname for this command.  This method should only be called by the XChain framework.
   *
   * @param qname the key for this command in the catalog.
   */
  public void setQName( QName qname );

  /**
   * Sets the registration system id for this command.  This method should only be called by the XChain framework.
   *
   * @param systemId the system id of the catalog where this command is registered.
   */
  public void setSystemId( String systemId );
}
