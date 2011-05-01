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
import java.util.Set;
import java.util.LinkedList;

/**
 * The <code>PrefixMapping</code> class keeps track of the current NamespaceURI for a Prefix.  The NamespaceURIs for a Prefix are kept in a stack.  
 * When a new NamespaceURI is added for a Prefix it will be added to the top of the stack.  The Prefix will then resolve to that
 * NamespaceURI until the NamespaceURI is either popped or a new NamespaceURI is added.  If the NamespaceURI is popped then the
 * previous NamespaceURI registered for that Prefix will be resolved for the Prefix.  If there was no previous NamespaceURI
 * registered for the Prefix then the Prefix will resolve to <code>null</code>.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 */
public class PrefixMappingContext
{
  // The Prefix to NamespaceURI list mapping.
  protected Map<String, LinkedList<String>> prefixMap = new HashMap<String, LinkedList<String>>();

  public PrefixMappingContext()
  {
  }

  /**
   * Add a NamespaceURI for a Prefix, making the NamespaceURI the current NamespaceURI for the Prefix.
   * 
   * @deprecated replaced with startPrefixMapping, to match the org.xml.sax.ContentHandler interface.
   * @param prefix The Prefix to add the NamespaceURI under.
   * @param namespaceUri The NamespaceURI.
   */
  public void pushPrefixMapping( String prefix, String namespaceUri )
  {
    startPrefixMapping( prefix, namespaceUri );
  }

  public void startDocument()
  {

  }

  public void endDocument()
  {

  }

  public void startPrefixMapping( String prefix, String namespaceUri )
  {
    // get the element for the prefix.
    LinkedList<String> namespaceUriList = prefixMap.get(prefix);

    if( namespaceUriList == null ) {
      namespaceUriList = new LinkedList<String>();
      prefixMap.put( prefix, namespaceUriList );
    }

    namespaceUriList.addFirst( namespaceUri );
  }

  /**
   * Remove the current NamespaceURI added for the given Prefix.
   *
   * @deprecated replaced with the endPrefixMapping method, to match the org.xml.sax.ContentHandler interface.
   * @param prefix The prefix to remove the NamespaceURI from.
   */
  public void popPrefixMapping( String prefix )
  {
    endPrefixMapping( prefix );
  }

  /**
   * Ends the mapping for the specified prefix.
   */
  public void endPrefixMapping( String prefix )
  {
    LinkedList<String> namespaceUriList = prefixMap.get(prefix);

    if( namespaceUriList != null ) {
      namespaceUriList.removeFirst();

      if( namespaceUriList.isEmpty() ) {
        prefixMap.remove(prefix);
      }
    }
  }

  /**
   * Find the current NamespaceURI for the Prefix.
   *  
   * @param prefix The Prefix to lookup.
   * 
   * @return The current NamespaceURI for the Prefix if found.  Null if there is no current NamespaceURI for the Prefix.
   */
  public String lookUpNamespaceUri( String prefix )
  {
    LinkedList<String> namespaceUriList = prefixMap.get(prefix);

    if( namespaceUriList == null || namespaceUriList.isEmpty() ) {
      return null;
    }
    else {
      return namespaceUriList.getFirst();
    }
  }

  /**
   * @return The set of all Prefixes with a current NamespaceURI.
   */
  public Set<String> prefixSet()
  {
    return prefixMap.keySet();
  }

  /**
   * Returns true if this context maps the prefix to the namespace, false otherise.
   *
   * @return true if this context maps the prefix to the namespace, false otherwise.
   */
  public boolean contains( String prefix, String namespace )
  {
    String currentNamespace = lookUpNamespaceUri(prefix);
    return currentNamespace != null && currentNamespace.equals(namespace);
  }
}
