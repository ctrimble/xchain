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
package org.xchain.framework.sax;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;

/**
 * The <code>PrefixMapping</code> class keeps track of the current NamespaceURI for a Prefix.  The NamespaceURIs for a Prefix are kept in a stack.  
 * When a new NamespaceURI is added for a Prefix it will be added to the top of the stack.  The Prefix will then resolve to that
 * NamespaceURI until the NamespaceURI is either popped or a new NamespaceURI is added.  If the NamespaceURI is popped then the
 * previous NamespaceURI registered for that Prefix will be resolved for the Prefix.  If there was no previous NamespaceURI
 * registered for the Prefix then the Prefix will resolve to <code>null</code>.
 *
 * @author Christian Trimble
 */
public class ReversePrefixMappingContext
  extends PrefixMappingContext
{
  // The Prefix to NamespaceURI list mapping.
  protected Map<String, LinkedList<String>> namespaceMap = new HashMap<String, LinkedList<String>>();

  public ReversePrefixMappingContext()
  {
  }

  private void removeMapping( String prefix, String namespaceUri )
  {
    LinkedList<String> prefixList = namespaceMap.get(namespaceUri);
    if( prefixList != null ) {
      int firstIndex = prefixList.indexOf(prefix);
      if( firstIndex >= 0 ) {
        prefixList.remove(firstIndex);
        if( prefixList.isEmpty() ) {
          namespaceMap.remove(namespaceUri);
        }
      }
    }
  }

  private void addMapping( String prefix, String namespaceUri )
  {
    LinkedList<String> prefixList = namespaceMap.get(namespaceUri);
    if( prefixList == null ) {
      namespaceMap.put(namespaceUri, prefixList = new LinkedList<String>());
    }
    prefixList.addFirst(prefix);
  }

  public void startDocument()
  {
    super.startDocument();
  }

  public void endDocument()
  {
    super.endDocument();
  }

  public void startPrefixMapping( String prefix, String namespaceUri )
  {
    super.startPrefixMapping( prefix, namespaceUri );
    addMapping(prefix, namespaceUri);
  }

  /**
   * Ends the mapping for the specified prefix.
   */
  public void endPrefixMapping( String prefix )
  {
    // get the current namespace for this prefix.
    String currentNamespaceUri = prefixMap.get(prefix).getFirst();

    // end the prefix with the parent class.
    super.endPrefixMapping( prefix );

    // remove the mapping for the prefix.
    removeMapping( prefix, currentNamespaceUri );
  }

  /**
   * Find the current NamespaceURI for the Prefix.
   *  
   * @param namespaceUri The Namespace URI to lookup.
   * 
   * @return The current NamespaceURI for the Prefix if found.  Null if there is no current NamespaceURI for the Prefix.
   */
  public String lookUpPrefix( String namespaceUri )
  {
    LinkedList<String> prefixList = namespaceMap.get(namespaceUri);

    if( prefixList == null ) {
      return null;
    }
    else {
      return prefixList.getFirst();
    }
  }
}
