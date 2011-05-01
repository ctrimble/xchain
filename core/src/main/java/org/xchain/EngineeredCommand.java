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

import java.util.Map;
import java.util.Set;
import org.xchain.annotations.AttributeType;

import javax.xml.namespace.QName;

/**
 * The interface for engineered commands.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public interface EngineeredCommand
  extends Command, Registerable
{
  /**
   * Returns the prefix to namespace mapping for this command.
   *
   * @return the prefix to namespace mapping for this command.
   */
  public Map<String, String> getPrefixMap();

  /**
   * Returns the attribute name to attribute value mapping for this command.
   *
   * @return the attribute name to attribute value mapping for this command.
   */
  public Map<QName, String> getAttributeMap();

  /**
   * Returns the set of attribute named defined for this command.  This is the set of names
   * defined by the command's annotations.
   *
   * @return the set of attributes defined for this command.
   */
  public Set<QName> getAttributeSet();

  /**
   * Returns the details for all of the attributes defined for this command.
   *
   * @return the mapping of attribute names to attribute details for this command.
   */
  public Map<QName, AttributeDetail> getAttributeDetailMap();
}
