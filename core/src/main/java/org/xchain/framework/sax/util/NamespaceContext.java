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
package org.xchain.framework.sax.util;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * This class helps sax content handlers track the state of the prefix mappings while processing
 * sax event streams.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class NamespaceContext
{
  protected Map<String, LinkedList<String>> prefixStackMap = new HashMap<String, LinkedList<String>>();

  protected LinkedList<String> getStack( String prefix, boolean create )
  {
    LinkedList<String> prefixMapping = prefixStackMap.get(prefix);
    if( prefixMapping == null && create ) {
      prefixStackMap.put(prefix, prefixMapping = new LinkedList<String>());
    }

    return prefixMapping;
  }  

  /**
   * Clears all of the mappings in this context.
   */
  public void clear()
  {
    prefixStackMap.clear();
  }

  /**
   * Pushes a new prefix mapping onto the context.
   */
  public void startPrefixMapping( String prefix, String uri )
  {
    getStack(prefix, true).addFirst(uri);
  }

  /**
   * Pops a prefix mapping off of the context.
   */
  public String endPrefixMapping( String prefix )
  {
    String uri = null;
    LinkedList<String> stack = getStack(prefix, false);
    if( stack != null ) {
      uri = stack.removeFirst();
    }
    return uri;
  }

  /**
   * Returns the namespace for the given prefix.
   * Defaults to "" for no namespace
   */
  public String lookupUri( String prefix )
  {
    String uri = "";
    LinkedList<String> prefixMapping = prefixStackMap.get(prefix);
    if( prefixMapping != null ) {
      uri = prefixMapping.getFirst();
    }
    return uri;
  }

  /**
   * Returns true if the last two mappings on the stack for this prefix are not equal.  This can be used to filter
   * unneeded namespace mappings.  This method should be used after all startPrefixMapping are called for an element or
   * before all endPrefixMappings are called for an element.
   */
  public boolean isPrefixMappingNeeded( String prefix )
  {
    LinkedList<String> prefixMapping = prefixStackMap.get(prefix);
    if( prefixMapping != null && prefixMapping.size() > 1 ) {
      return !prefixMapping.get(0).equals(prefixMapping.get(1));
    }
    return true;
  }
}
