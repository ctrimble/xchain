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
package org.xchain.framework.jsl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleContext;
import org.xchain.framework.lifecycle.NamespaceContext;
import org.xchain.framework.sax.ReversePrefixMappingContext;

/**
 * This handler takes a sax event stream and creates command classes that will render non-chain elements into chains that write the elements as sax events.
 *
 * @author Christian Trimble
 * @author Jason Rose
 * @author Devon Tackett
 */
public abstract class AbstractSaxTemplateHandler
  extends XMLFilterImpl
{
  /** The namespace for jsl elements. */
  public static String JSL_NAMESPACE = "http://www.xchain.org/jsl/1.0";

  /** The different element types the elements are grouped into. */
  public static enum ElementType { JSL_ELEMENT_TYPE(true), COMMAND_ELEMENT_TYPE(false), TEMPLATE_ELEMENT_TYPE(true);
    private boolean templateElement;
    ElementType( boolean templateElement ) {
      this.templateElement = templateElement;
    }
    public boolean isTemplateElement() { return this.templateElement; }
  };

  /** The element type of the previos element. */
  private ElementType previousSiblingType = null;

  /** The prefix mappings for the current element. */
  private Map<String, String> prefixMapping = new HashMap<String, String>();

  /** The element info stack for the current element. */
  protected LinkedList<ElementInfo> elementInfoStack = new LinkedList<ElementInfo>();

  /** The set of namespaces that contain xchain commands. */
  protected Set<String> commandNamespaceSet = new HashSet<String>();

  /** The compiler that compiles generated template source files. */
  protected TemplateCompiler sourceCompiler;

  /** The builder for output characters. */
  protected StringBuilder charactersBuilder = new StringBuilder();

  protected ReversePrefixMappingContext prefixMappingContext = new ReversePrefixMappingContext();

  /**
   * Returns the type of an element based on a uri.
   */
  private ElementType elementType( String uri )
  {
    ElementType result = null;
    if( JSL_NAMESPACE.equals(uri) ) { result = ElementType.JSL_ELEMENT_TYPE; }
    else if( commandNamespaceSet.contains(uri) ) { result = ElementType.COMMAND_ELEMENT_TYPE; }
    else { result = ElementType.TEMPLATE_ELEMENT_TYPE; }

    return result;
  }

  public void startDocument()
    throws SAXException
  {
    // get the current lifecycle context.
    LifecycleContext context = Lifecycle.getLifecycleContext();
    commandNamespaceSet = Collections.unmodifiableSet(context.getNamespaceContextMap().keySet());

    sourceCompiler = new TemplateCompiler();
    sourceCompiler.init(context.getClassLoader());
    
    getContentHandler().startDocument();
  }

  public void endDocument()
    throws SAXException
  {
    getContentHandler().endDocument();
  }

  /**
   * Tests the uri type and calls the correct start element method.
   */
  public void startElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    flushCharacters();

    // push the element info.
    pushElementInfo(uri, attributes);

    // remove all of the attribute that are in the jsl namespace.
    AttributesImpl cleanAttributes = new AttributesImpl();
    for( int i = 0; i < attributes.getLength(); i++ ) {
      if( !JSL_NAMESPACE.equals(uri) && !JSL_NAMESPACE.equals(attributes.getURI(i)) ) {
        cleanAttributes.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
      }
      else if( JSL_NAMESPACE.equals(uri) && !"exclude-result-prefixes".equals(localName) ) {
        cleanAttributes.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
      }
    }

    switch( elementInfoStack.getFirst().getElementType() ) {
      case JSL_ELEMENT_TYPE:
        startJslElement( uri, localName, qName, cleanAttributes );
        break;
      case COMMAND_ELEMENT_TYPE:
        startCommandElement( uri, localName, qName, cleanAttributes );
        break;
      case TEMPLATE_ELEMENT_TYPE:
        startTemplateElement( uri, localName, qName, cleanAttributes );
        break;
    }
  }

  /**
   * Handles start events for jsl elements.
   */
  protected abstract void startJslElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException;

  /**
   * Handles start events for xchain command elements.
   */
  protected abstract void startCommandElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException;

  /**
   * Handles start events for template elements.
   */
  protected abstract void startTemplateElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException;

  /**
   * Tests the uri type and calls the correct end element method.
   */
  public void endElement( String uri, String localName, String qName )
    throws SAXException
  {
    flushCharacters();

    // call the end element for the type of element.
    switch( elementInfoStack.getFirst().getElementType() ) {
      case JSL_ELEMENT_TYPE:
        endJslElement( uri, localName, qName );
        break;
      case COMMAND_ELEMENT_TYPE:
        endCommandElement( uri, localName, qName );
        break;
      case TEMPLATE_ELEMENT_TYPE:
        endTemplateElement( uri, localName, qName );
        break;
      default:
    }

    popElementInfo();
  }

  /**
   * Handles end events for jsl elements.
   */
  protected abstract void endJslElement( String uri, String localName, String qName )
    throws SAXException;

  /**
   * Handles end events for xchain command elements.
   */
  protected abstract void endCommandElement( String uri, String localName, String qName )
    throws SAXException;

  /**
   * Handles end events for template elements.
   */
  protected abstract void endTemplateElement( String uri, String localName, String qName )
    throws SAXException;

  protected void pushElementInfo(String uri, Attributes attributes)
    throws SAXException
  {
    // create the element info.
    ElementInfo elementInfo = new ElementInfo();
    elementInfo.setElementType(elementType(uri));
    elementInfo.setPreviousSiblingType(previousSiblingType);
    elementInfo.setPrefixMapping(new HashMap<String, String>(prefixMapping));
    elementInfo.setExcludeResultPrefixSet(excludeResultPrefixSet(uri, attributes));
    elementInfoStack.addFirst(elementInfo);

    // create a map of namespaces that need to go to the handler.
    Map<String, String> handlerPrefixMapping = new HashMap<String, String>();

    // if the previous 2 elements are templates, then we need to add the previous elements handler events.
    if( elementInfoStack.size() > 2 &&
        elementInfoStack.get(1).getElementType().isTemplateElement() &&
        elementInfoStack.get(2).getElementType().isTemplateElement() ) {
      handlerPrefixMapping.putAll(elementInfoStack.get(1).getHandlerPrefixMapping());
    }

    // add this elements prefix mapping to the handler prefix mapping.
    handlerPrefixMapping.putAll(prefixMapping);

    // store the handler prefix mapping into the element info.
    elementInfo.setHandlerPrefixMapping(handlerPrefixMapping);

    // create a map of namespaces that need to go to the source builder.
    Map<String, String> sourcePrefixMapping = new HashMap<String, String>();
    Set<String> sourceExcludeResultPrefixSet = new HashSet<String>();

    // if the previous element is not a template, then we need to add the previous elements source prefix mapping to the source prefix mapping.
    if( elementInfoStack.size() > 1 &&
        !elementInfoStack.get(1).getElementType().isTemplateElement() ) {
      sourcePrefixMapping.putAll(elementInfoStack.get(1).getSourcePrefixMapping());
      sourceExcludeResultPrefixSet.addAll(elementInfoStack.get(1).getSourceExcludeResultPrefixSet());
    }

    // add this elements prefix mapping to the source prefix mapping.
    sourcePrefixMapping.putAll(prefixMapping);
    sourceExcludeResultPrefixSet.addAll(elementInfo.getExcludeResultPrefixSet());

    // store the source prefix mapping into the element info.
    elementInfo.setSourcePrefixMapping(sourcePrefixMapping);
    elementInfo.setSourceExcludeResultPrefixSet(sourceExcludeResultPrefixSet);

    // clear the previous sibling variable.
    previousSiblingType = null;

    // clear the prefix mapping stack.
    prefixMapping.clear();
  }

  
  private Set<String> excludeResultPrefixSet( String uri, Attributes attributes )
    throws SAXException
  {
    // add exclude-result-prefixes info to the element info stack.
    String excludeResultPrefixes = JSL_NAMESPACE.equals(uri) ? attributes.getValue("", "exclude-result-prefixes") : attributes.getValue(JSL_NAMESPACE, "exclude-result-prefixes");
    excludeResultPrefixes = excludeResultPrefixes != null ? excludeResultPrefixes.trim() : excludeResultPrefixes;
    Set<String> excludeResultPrefixSet = new HashSet<String>();
    if( excludeResultPrefixes == null ) {
      // nothing to do.
    }
    else if( "#all".equals(excludeResultPrefixes) ) {
      excludeResultPrefixSet.add(excludeResultPrefixes);
    }
    else {
      // clean up the excluded result prefixes.
      for( String prefix : excludeResultPrefixes.split("\\s+") ) {
        // get the namespace from the prefix mapping context.
        if( "".equals(prefix) ) {
          // this is leading whitespace or an empty attribute value, ignore it.
        }
        else if( "#all".equals(prefix) ) {
          throw new SAXException("It is a static error to exclude the result prefix #all with other result prefixes.");
        }
        else if( "#default".equals(prefix) ) {
          // TODO: make sure that default exists.
          excludeResultPrefixSet.add(prefix);
        }
        else {
          // TODO: make sure that prefix exists.
          excludeResultPrefixSet.add(prefix);
        }
      }
    }
    return excludeResultPrefixSet;
  }

  protected abstract void flushCharacters()
    throws SAXException;

  public void characters( char[] characters, int start, int length )
    throws SAXException
  {
    // TODO: We may need to start a chain, if the characters is not whitespace and the character builder is, and we are in a template.
    charactersBuilder.append(characters, start, length);
  }

  /**
   * Handles ignorable whitespace found in the source document.
   */
  public void ignorableWhitespace( char[] characters, int start, int length )
    throws SAXException
  {
    charactersBuilder.append(characters, start, length);
  }

  protected void popElementInfo()
  {
    ElementInfo elementInfo = elementInfoStack.removeFirst();

    // track the previous element type.
    previousSiblingType = elementInfo.getElementType();
  }

  protected class ElementInfo
  {
    /** The type of this element. */
    private ElementType elementType           = null;

    /** The type of this elements previous sibling. */
    private ElementType previousSiblingType   = null;

    /** The prefix mappings that are defined on this element. */
    private Map<String, String> prefixMapping = null;

    /** The prefix mappings that will be passed to the start source event. */
    private Map<String, String> sourcePrefixMapping = null;

    /** The exclude result prefix set that will be passed to the start source event. */
    private Set<String> sourceExcludeResultPrefixSet = null;

    /** The prefix mappings that will be passed to the next content handler (The digester). */
    private Map<String, String> handlerPrefixMapping = null;

    /** The set of result prefixes that will be excluded for this element. */
    private Set<String> excludeResultPrefixSet = null;

    public void setElementType( ElementType elementType ) { this.elementType = elementType; }
    public ElementType getElementType() { return this.elementType; }
    public void setPreviousSiblingType( ElementType previousSiblingType ) { this.previousSiblingType = previousSiblingType; }
    public ElementType getPreviousSiblingType() { return this.previousSiblingType; }
    public void setPrefixMapping( Map<String, String> prefixMapping ) { this.prefixMapping = prefixMapping; }
    public Map<String, String> getPrefixMapping() { return this.prefixMapping; }
    public void setSourcePrefixMapping( Map<String, String> sourcePrefixMapping ) { this.sourcePrefixMapping = sourcePrefixMapping; }
    public Map<String, String> getSourcePrefixMapping() { return this.sourcePrefixMapping; }
    public void setSourceExcludeResultPrefixSet( Set<String> sourceExcludeResultPrefixSet ) { this.sourceExcludeResultPrefixSet = sourceExcludeResultPrefixSet; }
    public Set<String> getSourceExcludeResultPrefixSet() { return sourceExcludeResultPrefixSet; }
    public void setHandlerPrefixMapping( Map<String, String> handlerPrefixMapping ) { this.handlerPrefixMapping = handlerPrefixMapping; }
    public Map<String, String> getHandlerPrefixMapping() { return this.handlerPrefixMapping; }
    public void setExcludeResultPrefixSet( Set<String> excludeResultPrefixSet ) { this.excludeResultPrefixSet = excludeResultPrefixSet; }
    public Set<String> getExcludeResultPrefixSet() { return excludeResultPrefixSet; }
  }

  public void startPrefixMapping( String prefix, String uri )
    throws SAXException
  {
    // cache these.
    prefixMapping.put(prefix, uri);

    // track the general namespace context.
    prefixMappingContext.startPrefixMapping( prefix, uri );
  }

  public void endPrefixMapping( String prefix )
  {
    // the prefix mapping is cleared by pushElementInfo(String)
    prefixMappingContext.endPrefixMapping(prefix);
  }

  public void skippedEntity( String name )
    throws SAXException
  {
    //throw new SAXException("Skipped Entities are not acceppted by this handler.");
  }

  public void processingInstruction( String target, String data )
    throws SAXException
  {
    //throw new SAXException("Processing instructions are not acceppted by this handler.");
  }
}
