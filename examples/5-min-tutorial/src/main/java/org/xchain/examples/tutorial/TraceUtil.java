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
package org.xchain.examples.tutorial;

import org.xchain.Locatable;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.impl.ChainImpl;
import org.xchain.framework.sax.CommandHandler;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.namespaces.sax.PipelineCommand;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
public class TraceUtil
{
  public static final String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
  public static final String DIV_ELEMENT = "div";
  public static final String SPAN_ELEMENT = "span";
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
  private static final AttributesImpl EXECUTE_DIV_ATTRIBUTES = new AttributesImpl();
  private static final AttributesImpl CHILDREN_DIV_ATTRIBUTES = new AttributesImpl();
  private static final AttributesImpl POST_PROCESS_DIV_ATTRIBUTES = new AttributesImpl();
  private static final AttributesImpl LABEL_SPAN_ATTRIBUTES = new AttributesImpl();
  private static final AttributesImpl VALUE_SPAN_ATTRIBUTES = new AttributesImpl();

  static {
    // add the attributes to the execute div.
    EXECUTE_DIV_ATTRIBUTES.addAttribute("", "class", "class", "CDATA", "trace-execute");

    // add the attributes to the post process div.
    POST_PROCESS_DIV_ATTRIBUTES.addAttribute("", "class", "class", "CDATA", "trace-post-process");

    // add the attributes to the children div.
    CHILDREN_DIV_ATTRIBUTES.addAttribute("", "class", "class", "CDATA", "trace-children");

    LABEL_SPAN_ATTRIBUTES.addAttribute("", "class", "class", "CDATA", "trace-label");
    VALUE_SPAN_ATTRIBUTES.addAttribute("", "class", "class", "CDATA", "trace-value");
  }

  public static String xhtmlPrefix(JXPathContext context)
    throws SAXException
  {
    String prefix = context.getPrefix(XHTML_NAMESPACE_URI);
    if( prefix == null ) {
      throw new SAXException("The namespace '"+XHTML_NAMESPACE_URI+"' must be bound to a prefix.");
    }
    return prefix;
  }

  public static String createQName(String prefix, String localName)
    throws SAXException
  {
    if( prefix == null || "".equals(prefix) ) {
      return localName;
    }
    return prefix+":"+localName;
  }

  public static void startDiv(ContentHandler handler, String xhtmlPrefix, Attributes attributes)
    throws SAXException
  {
    handler.startElement(XHTML_NAMESPACE_URI, DIV_ELEMENT, createQName(xhtmlPrefix, DIV_ELEMENT), attributes);
  }

  public static void endDiv(ContentHandler handler, String xhtmlPrefix)
    throws SAXException
  {
    handler.endElement(XHTML_NAMESPACE_URI, DIV_ELEMENT, createQName(xhtmlPrefix, DIV_ELEMENT));
  }

  public static void startSpan( ContentHandler handler, String xhtmlPrefix, Attributes attributes) 
    throws SAXException
  {
    handler.startElement(XHTML_NAMESPACE_URI, SPAN_ELEMENT, createQName(xhtmlPrefix, SPAN_ELEMENT), attributes);
  }

  public static void endSpan(ContentHandler handler, String xhtmlPrefix) 
    throws SAXException
  {
    handler.endElement(XHTML_NAMESPACE_URI, SPAN_ELEMENT, createQName(xhtmlPrefix, SPAN_ELEMENT));
  }

  public static void span( ContentHandler handler, String xhtmlPrefix, Attributes attributes, String text )
    throws SAXException
  {
    startSpan(handler, xhtmlPrefix, attributes);
    characters(handler, text);
    endSpan(handler, xhtmlPrefix);
  }

  private static void characters( ContentHandler handler, String text )
    throws SAXException
  {
    char[] characters = text.toCharArray();
    handler.characters(characters, 0, characters.length);
  }

  public static void startExecuteBlock( ContentHandler handler, String xhtmlPrefix, String name )
    throws SAXException
  {
    startDiv(handler, xhtmlPrefix, EXECUTE_DIV_ATTRIBUTES);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "phase:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, "start execute");
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "name:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, name);
    startDiv(handler, xhtmlPrefix, CHILDREN_DIV_ATTRIBUTES);
  }

  public static void endExecuteBlock( ContentHandler handler, String xhtmlPrefix, String name, boolean result )
    throws SAXException
  {
    endDiv(handler, xhtmlPrefix);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "phase:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, "end execute");
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "name:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, name);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "result:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, ""+result);
    endDiv(handler, xhtmlPrefix);
  }

  public static void endExecuteBlock( ContentHandler handler, String xhtmlPrefix, String name, Exception exception )
    throws SAXException
  {
    endDiv(handler, xhtmlPrefix);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "phase:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, "end execute");
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "name:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, name);
    if( exception != null ) {
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "exception:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, exception.toString());
    }
    endDiv(handler, xhtmlPrefix);
  }

  public static void startPostProcessBlock( ContentHandler handler, String xhtmlPrefix, String name, Exception exception )
    throws SAXException
  {
    startDiv(handler, xhtmlPrefix, POST_PROCESS_DIV_ATTRIBUTES);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "phase:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, "start post process");
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "name:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, name);
    startDiv(handler, xhtmlPrefix, CHILDREN_DIV_ATTRIBUTES);
  }

  public static void endPostProcessBlock( ContentHandler handler, String xhtmlPrefix, String name, boolean result )
    throws SAXException
  {
    endDiv(handler, xhtmlPrefix);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "phase:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, "end post process");
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "name:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, name);
    span(handler, xhtmlPrefix, LABEL_SPAN_ATTRIBUTES, "handled:");
    span(handler, xhtmlPrefix, VALUE_SPAN_ATTRIBUTES, ""+result);
    endDiv(handler, xhtmlPrefix);

  }
}
