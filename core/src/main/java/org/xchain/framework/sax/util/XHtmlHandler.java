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

import org.xchain.framework.sax.HandlerWrapper;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

/**
 * This class wraps an apache serializer for html.  It will remap the html namespace (http://www.w3c.org/1999/xhtml) into the
 * default namespace and then pass the elements along.  It also caches namespace mapping to prevent extra namespaces from being passed to the document.
 *
 * @author Christian Trimble
 */
public class XHtmlHandler
  extends HandlerWrapper
{
  public static String HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

  protected NamespaceContext inputNamespaceContext = new NamespaceContext();

  public void startElement( String namespaceUri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    if( HTML_NAMESPACE.equals(namespaceUri) ) {
      super.startElement("", localName, localName, filterAttributes(attributes));
    }
    else {
      super.startElement(namespaceUri, localName, qName, attributes);
    }
  }

  public void endElement( String namespaceUri, String localName, String qName )
    throws SAXException
  {
    if( HTML_NAMESPACE.equals(namespaceUri) ) {
      super.endElement("", localName, localName);
    }
    else {
      super.endElement(namespaceUri, localName, qName);
    }
  }

  public void startPrefixMapping( String prefix, String namespaceUri )
    throws SAXException
  {
    inputNamespaceContext.startPrefixMapping(prefix, namespaceUri);

    // only pass the mapping if it is not the html namespace.
    if( HTML_NAMESPACE.equals(namespaceUri) ) {
      // translate this into the default mapping.
      super.startPrefixMapping("", "");
    }
    else if( inputNamespaceContext.isPrefixMappingNeeded(prefix) ) {
      // only pass mappings that are needed.
      super.startPrefixMapping(prefix, namespaceUri);
    }
    
  }

  public void endPrefixMapping( String prefix )
    throws SAXException
  {
    String namespaceUri = inputNamespaceContext.lookupUri( prefix );

    if( HTML_NAMESPACE.equals(namespaceUri) ) {
      super.endPrefixMapping("");
    }
    else if( inputNamespaceContext.isPrefixMappingNeeded(prefix) ) {
      super.endPrefixMapping(prefix);
    }

    inputNamespaceContext.endPrefixMapping(prefix);
  }

  public Attributes filterAttributes( Attributes attributes )
  {
    if( attributes.getLength() == 0 ) {
      return attributes;
    }

    // if any of the attributes is in the html namespace, then filter them into the default namespace.
    AttributesImpl filteredAttributes = new AttributesImpl();
    for( int i = 0; i < attributes.getLength(); i++ ) {
      if( HTML_NAMESPACE.equals(attributes.getURI(i)) ) {
        if( attributes.getIndex(attributes.getLocalName(i)) == -1 ) {
          filteredAttributes.addAttribute("", attributes.getLocalName(i), attributes.getLocalName(i), attributes.getType(i), attributes.getValue(i));
        }
      }
      else {
        filteredAttributes.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
      }
    }

    return filteredAttributes;
  }
}
