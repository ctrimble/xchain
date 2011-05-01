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
package org.xchain.framework.digester;

import javax.xml.namespace.NamespaceContext;
import org.apache.commons.digester.Digester;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static javax.xml.XMLConstants.*;
import javax.xml.namespace.NamespaceContext;

/**
 * @author Christian Trimble
 */
public class DigesterNamespaceContext
  implements NamespaceContext
{
  private Map<String, String> xmlns = null;

  public DigesterNamespaceContext( Digester digester )
  {
    this.xmlns = digester.getCurrentNamespaces();
  }

  public String getNamespaceURI( String prefix )
  {
    if( prefix == null ) {
      throw new IllegalArgumentException("A null prefix cannot be used with a NamespaceContext object.");
    }
    else if( !xmlns.containsKey(prefix) ) {
      return NULL_NS_URI;
    }
    else if( XML_NS_PREFIX.equals(prefix) ) {
      return XML_NS_URI;
    }
    else if( XMLNS_ATTRIBUTE.equals(prefix) ) {
      return XMLNS_ATTRIBUTE_NS_URI;
    }
    else {
      return xmlns.get(prefix);
    }
  }

  public String getPrefix( String namespaceUri )
  {
    if( namespaceUri == null ) {
      throw new IllegalArgumentException("A null namespace URI cannot be used with a NamespaceContext object.");
    }
    else if( XML_NS_URI.equals(namespaceUri) ) {
      return XML_NS_PREFIX;
    }
    else if( XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri) ) {
      return XMLNS_ATTRIBUTE;
    }
    else {
      for( Map.Entry<String, String> entry : xmlns.entrySet() ) {
        if( namespaceUri.equals(entry.getValue()) ) {
          return entry.getKey();
        }
      }
      return null;
    }
  }

  public Iterator<String> getPrefixes( String namespaceUri )
  {
    if( namespaceUri == null ) {
      throw new IllegalArgumentException("A null namespace URI cannot be used with a NamespaceContext object.");
    }
    else if( XML_NS_URI.equals(namespaceUri) ) {
      return Collections.singleton(XML_NS_PREFIX).iterator();
    }
    else if( XMLNS_ATTRIBUTE_NS_URI.equals(namespaceUri) ) {
      return Collections.singleton(XMLNS_ATTRIBUTE).iterator();
    }
    else {
      ArrayList<String> prefixes = new ArrayList<String>();
      for( Map.Entry<String, String> entry : xmlns.entrySet() ) {
        if( namespaceUri.equals(entry.getValue()) ) {
          prefixes.add(entry.getKey());
        }
      }
      return Collections.unmodifiableList(prefixes).iterator();
    }
  }
}
