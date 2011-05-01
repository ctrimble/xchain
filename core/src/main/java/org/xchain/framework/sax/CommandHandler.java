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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.xchain.framework.sax.util.NamespaceContext;

/**
 * This handler provides caching and translation services for command based sax sources.  The following features are implemented:
 *
 * 1) Attributes can be assigned after a startElement event by calling attribute( String namespace, String localName, String qName );
 * 2) The handler can be set into comment mode, which will translate all calls into comments.
 * 3) The handler can be flushed, to force all outstanding events to the content handler.  This will be a partial flush when in comment mode, since
 * comments are passed in one call, instead of many.

 * Modes:
 * 1) Standard mode.
 * 2) Element building mode.
 *   2.1) Attribute building mode.
 * 3) Comment building mode.
 *
 * Element building is terminated by:
 * 1) A text event (characters or ignorable whitespace.)
 * 2) Another start element event.
 * 3) A processing instruction.
 * 4) An end element event.
 *
 * Attribute building is terminated by:
 * 1) An end attribute event.
 *
 * Comment building is terminated by:
 * 1) an end comment event.
 *
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public abstract class CommandHandler
  implements ContentHandler, DTDHandler, LexicalHandler
{
  public static Logger log = LoggerFactory.getLogger(CommandHandler.class);

  /** The buffer used to redirect character events. */
  protected StringBuilder redirectBuilder = new StringBuilder();

  /** The depth of redirection elements. */
  protected int redirectDepth = 0;

  /** The next element that will be output. */
  protected Element nextElement = null;

  /** The namespace context for this handler. */
  protected PrefixMappingContext inputNamespaceContext = new PrefixMappingContext();

  /** The namespace context for the output handler. */
  protected PrefixMappingContext outputNamespaceContext = new PrefixMappingContext();

  /** The exclude result prefix context. */
  protected LinkedList<PrefixMappingContext> excludeNamespaceContextStack = new LinkedList<PrefixMappingContext>();

  /** The stack of elements from the root element to the current output element. */
  protected LinkedList<Element> elementStack = new LinkedList<Element>();

  /** The map of prefix mappings for the next startElement event. */
  protected HashMap<String, String> nextPrefixMapping = new HashMap<String, String>();

  public abstract ContentHandler contentHandler();
  public abstract DTDHandler dtdHandler();
  public abstract LexicalHandler lexicalHandler();

  public void setDocumentLocator(Locator locator)
  {
    contentHandler().setDocumentLocator(locator);
  }

  public void startDocument()
    throws SAXException
  {
    excludeNamespaceContextStack.addFirst(new PrefixMappingContext());
    contentHandler().startDocument();
  }

  public void endDocument()
    throws SAXException
  {
    contentHandler().endDocument();
    excludeNamespaceContextStack.removeFirst();
  }

  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    nextPrefixMapping.put(prefix, uri);
  }

  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    inputNamespaceContext.endPrefixMapping(prefix);
  }

  public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
  {
    if( redirectCharacters() ) {
      redirectBuilder.append(ch, start, length);
    }
    else {
      contentHandler().ignorableWhitespace(ch, start, length);
    }
  }

  public void processingInstruction(String target, String data)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      // pass the processing instruction through.
      contentHandler().processingInstruction(target, data);
    }
  }

  public void skippedEntity(String name)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      contentHandler().skippedEntity(name);
    }
  }

  public void notationDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      fireOutstandingEvents();
      dtdHandler().notationDecl(name, publicId, systemId);
    }
  }

  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      fireOutstandingEvents();
      dtdHandler().unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  /**
   * Sends text to the content handler.  This will flush any outstanding events and then send
   * a characters(char[], int, int) event to the content handler.
   */
  public void characters(char[] ch, int offset, int length )
    throws SAXException
  {
    if( redirectCharacters() ) {
      redirectBuilder.append(ch, offset, length);
    }
    else {
      // fire any events that are waiting.
      fireOutstandingEvents();

      // send the characters through.
      contentHandler().characters(ch, 0, length);
    }
  }

  /**
   * Sets up a start element event that will be sent to the content handler.  When the simple handler detects that the
   * creation of an element is complete, then start prefix mapping events and a start element event will be fired.
   */
  public void startElement(String namespace, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      fireOutstandingEvents();

      // CHANGE: push the next element.
      // add the prefix mappings that need to be added to this element.
      // clear the current prefix mappings.

      // update the input namespace context.
      for( Map.Entry<String, String> prefixMapping : nextPrefixMapping.entrySet() ) {
        inputNamespaceContext.startPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
      }

      nextElement = new Element(namespace, localName, qName);

      // copy all of the prefix mappings that were found for this element.
      nextElement.getPrefixMappings().putAll(nextPrefixMapping);
      nextPrefixMapping.clear();

      for( int i = 0; i < attributes.getLength(); i++ ) {
        nextElement.addAttribute( attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getValue(i) );
      }

      elementStack.addFirst(nextElement);
    }
  }

  /**
   * Sends the end element event to the content handler.
   */
  public void endElement(String namespace, String localName, String qName)
    throws SAXException
  {
    if( !redirectCharacters() ) {

      // fires outstanding events.
      fireOutstandingEvents();

      // remove the first element from the stack.
      Element element = elementStack.removeFirst();

      // make sure that this is the correct element.
      // TODO: add sanity checks.

      // end the element.
      contentHandler().endElement(element.getNamespace(), element.getLocalName(), element.getQName());

      for( String prefix : element.getPrefixMappings().keySet() ) {
        contentHandler().endPrefixMapping(prefix);
        outputNamespaceContext.endPrefixMapping(prefix);
      }
    }
  }

  public void startAttribute( String namespace, String localName, String qName )
    throws SAXException
  {
    if( !redirectCharacters() ) {
      startRedirectCharacters();
    }

    redirectDepth++;

    if( redirectDepth == 1 && nextElement != null ) {
      // add any missing prefix mappings to the next element.
      for( Map.Entry<String, String> prefixMapping : nextPrefixMapping.entrySet() ) {
        String currentMapping = nextElement.getPrefixMappings().get(prefixMapping.getKey());
        if( currentMapping == null ) {
          currentMapping = outputNamespaceContext.lookUpNamespaceUri(prefixMapping.getKey());
        }
        if( currentMapping == null || currentMapping.equals(prefixMapping.getValue()) ) {
          inputNamespaceContext.startPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
          elementStack.getFirst().getPrefixMappings().put(prefixMapping.getKey(), prefixMapping.getValue());
        }
        else {
          throw new SAXException("Cannot map prefix '"+prefixMapping.getKey()+"' to '"+prefixMapping.getValue()+"' because it is already mapped to '"+currentMapping+"'.");
        }
      }
    }
    else {
      for( Map.Entry<String, String> prefixMapping : nextPrefixMapping.entrySet() ) {
        inputNamespaceContext.startPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
      }
    }
    nextPrefixMapping.clear();
  }

  public void endAttribute( String namespace, String localName, String qName )
    throws SAXException
  {
    redirectDepth--;
    if( !redirectCharacters() ) {
      // if there is an element to add this attribute to, then add it.
      if( nextElement != null ) {
        nextElement.addAttribute(namespace, localName, qName, redirectBuilder.toString());
      }
      endRedirectCharacters();
    }
  }

  public void endAttribute( String qName )
    throws SAXException
  {
    String[] qNameSplit = qName.split(":", 2);
    String prefix = qNameSplit.length == 2 ? qNameSplit[0] : null;
    String localName = qNameSplit.length == 2 ? qNameSplit[1] : qNameSplit[0];

    endAttribute( inputNamespaceContext.lookUpNamespaceUri(prefix), localName, qName );
  }

  /**
   * Starts a new context for removing result prefixes. 
   */
  public void startExcludeResultPrefixContext()
    throws SAXException
  {
    excludeNamespaceContextStack.addFirst(new PrefixMappingContext());
  }

  /**
   * Ends the current context for removing result prefixes.
   */
  public void endExcludeResultPrefixContext()
    throws SAXException
  {
    excludeNamespaceContextStack.removeFirst();
  }

  public void startExcludeResultPrefix( String prefix )
    throws SAXException
  {
    String namespace = null;

    // try to get the namespace from the next prefix mapping, since this happens before outstanding events
    // are fired.
    if( "#all".equals(prefix) ) {
      namespace = "#all";
    } 
    else if( nextPrefixMapping.containsKey(prefix) ) {
      namespace = nextPrefixMapping.get(prefix);
    }
    else {
      namespace = inputNamespaceContext.lookUpNamespaceUri(prefix);
    }

    if( namespace == null ) {
      throw new SAXException("The prefix '"+prefix+"' was excluded, but is not defined in the namespace context.");
    }

    excludeNamespaceContextStack.getFirst().startPrefixMapping(prefix, namespace);
  }

  public void endExcludeResultPrefix( String prefix )
    throws SAXException
  {
    excludeNamespaceContextStack.getFirst().endPrefixMapping(prefix);
  }

  public void startDTD(String name, String publicId, String systemId)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.startDTD(name, publicId, systemId);
      }
    }
  }

  public void endDTD()
    throws SAXException
  {
    if( !redirectCharacters() ) {
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.endDTD();
      }
    }
  }

  public void startEntity(String name)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.startEntity(name);
      }
    }
  }

  public void endEntity(String name)
    throws SAXException
  {
    if( !redirectCharacters() ) {
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.endEntity(name);
      }
    }
  }

  public void startCDATA()
    throws SAXException
  {
    if( !redirectCharacters() ) {
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.startCDATA();
      }
    }
  }

  public void endCDATA()
    throws SAXException
  {
    if( !redirectCharacters() ) {
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.endCDATA();
      }
    }
  }

  /**
   * Adds a comment to this handler.  If the handler is in comment mode, then an escaped comment will be added to the sax stream,
   * otherwise a standard comment is passed on.
   *
   * @param comment the character array containing the comment.
   * @param offset the offset into the array to start the characters.
   * @param length the length of the comment starting at the offset.
   */
  public void comment( char[] comment, int offset, int length )
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("comment('"+(new StringBuilder().append(comment, offset, length).toString())+"') called.");
    }

    if( !redirectCharacters() ) {

      // fire any events that are waiting.
      fireOutstandingEvents();

      // get the lexical handler.
      LexicalHandler lexicalHandler = lexicalHandler();

      // only send the event if the lexical handler is defined.
      if( lexicalHandler != null ) {
        lexicalHandler.comment(comment, offset, length);
      }
    }
  }

  /**
   * Adds a comment to this handler.  This method turns the comment string into a character array then calls comment(char[], int, int).
   *
   * @param comment the text of the comment.
   */
  public void comment( String comment )
    throws SAXException
  {
    if( comment != null ) {
      char[] commentArray = comment.toCharArray();
      comment(commentArray, 0, commentArray.length);
    }
  }

  /**
   * Starts a comment section.  While in a comment section, all events are translated into a comment, to be passed on once the
   * comment section ends.
   */
  public void startComment()
    throws SAXException
  {
    if( !redirectCharacters() ) {
      // flush any outstanding events.
      fireOutstandingEvents();

      // start the redirection of characters.
      startRedirectCharacters();
    }

    // increment the depth.
    redirectDepth++;
  }

  /**
   * Stops translating all events as a single comment and passes the comment along.
   */
  public void endComment()
    throws SAXException
  {
    redirectDepth--;

    if( !redirectCharacters() ) {
      char[] comment = redirectBuilder.toString().toCharArray();
      LexicalHandler lexicalHandler = lexicalHandler();
      if( lexicalHandler != null ) {
        lexicalHandler.comment(comment, 0, comment.length);
      }

      endRedirectCharacters();
    }
  }

  private boolean redirectCharacters()
  {
    return redirectDepth > 0;
  }

  private void startRedirectCharacters()
  {
    redirectBuilder.delete(0, redirectBuilder.length());
  }

  private void endRedirectCharacters()
  {
    redirectBuilder.delete(0, redirectBuilder.length());
  }

  /**
   * Fires any element events that have been cached by this content handler.
   */
  private void fireOutstandingEvents()
    throws SAXException
  {
    if( log.isDebugEnabled() ) {
      log.debug("fireOutstandingEvents() called.");
    }

    // if there is a next element.
    if( nextElement != null ) {

      // the set of all of the namespaces that are required by this element.
      Map<String, String> requiredPrefixMappings = new HashMap<String, String>();
      requiredPrefixMappings.put(parsePrefix(nextElement.getQName()), nextElement.getNamespace());

      // create the set of attributes for this element.
      AttributesImpl attributes = new AttributesImpl();
      Iterator<Attribute> attributeIterator = nextElement.getAttributeMap().values().iterator();
      while( attributeIterator.hasNext() ) {
        Attribute attribute = (Attribute)attributeIterator.next();

        attributes.addAttribute(attribute.getNamespace(), attribute.getLocalName(), attribute.getQName(), "CDATA", attribute.getValue());
        if( attribute.getNamespace() != null && !"".equals(attribute.getNamespace()) ) {
          requiredPrefixMappings.put(parsePrefix(attribute.getQName()), attribute.getNamespace());
        }
      }

      // ASSERT: the attributes for this element are all constructed.
      // ASSERT: we know all of the required prefix mappings for this element.
      // ASSERT: the requiredPrefixMappings is a subset of nextElement.getPrefixMappings()

      // remove the required mappings from the nextElement, we will put them back when the trimming of the prefixes is done.
      nextElement.getPrefixMappings().keySet().removeAll(requiredPrefixMappings.keySet());

      // remove all of the mappings that will have no effect on the output document.
      removeUnneededMappings(outputNamespaceContext, nextElement.getPrefixMappings());
      removeUnneededMappings(outputNamespaceContext, requiredPrefixMappings);

      // remove all excluded prefix mappings from the namespace mappings left in nextElement.getPrefixMappings().  These are the namespaces that are
      // not required right now.
      if( !excludeNamespaceContextStack.isEmpty() ) {
        PrefixMappingContext excludeResultContext = excludeNamespaceContextStack.getFirst();
        if( excludeResultContext.contains("#all", "#all") ) {
          nextElement.getPrefixMappings().clear();
        } 
        else { 
          Iterator<Map.Entry<String, String>> mappingIterator = nextElement.getPrefixMappings().entrySet().iterator();
          while( mappingIterator.hasNext() ) {
            Map.Entry<String, String> entry = mappingIterator.next();
            if( excludeResultContext.contains(entry.getKey(), entry.getValue()) ) {
              mappingIterator.remove();
            }
          }
        }
      }

      // add all of the required prefix mappings back.
      nextElement.getPrefixMappings().putAll(requiredPrefixMappings);

      // ASSERT: all prefixes that must be output by this element are now in nextElement.getPrefixMappings().

      // output all of the namespace prefixes.
      for( Map.Entry<String, String> prefixMapping : nextElement.getPrefixMappings().entrySet() ) {
        contentHandler().startPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
        outputNamespaceContext.startPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
      }

      // ASSERT: the prefix mappings are now all defined for the context of this element.

      // fire the start event for the element.
      contentHandler().startElement(nextElement.getNamespace(), nextElement.getLocalName(), nextElement.getQName(), attributes );

      // ASSERT: the start element has been fired for this element.

      if( log.isDebugEnabled() ) {
        log.debug("Adding element '"+nextElement.getQName()+"' to the node stack.");
      }

      // clear the next element.
      nextElement = null;
    }
  }

  /**
   * Removes mappings from the map prefixMappings that are already defined in prefixMappingContext.
   *
   * @param prefixMappingContext the prefix mapping context that will be checked for mappings.
   * @param prefixMappings the prefix map that will have unneeded mappings removed.
   */
  private static void removeUnneededMappings( PrefixMappingContext prefixMappingContext, Map<String, String> prefixMappings )
  {
    Iterator<Map.Entry<String, String>> prefixMappingIterator = prefixMappings.entrySet().iterator();
    while( prefixMappingIterator.hasNext() ) {
      Map.Entry<String, String> prefixMappingEntry = prefixMappingIterator.next();
      if( prefixMappingContext.contains(prefixMappingEntry.getKey(), prefixMappingEntry.getValue()) ) {
        prefixMappingIterator.remove();
      }
    }
  }

  /**
   * Parses a prefix from a QName.  If the qName does not have a prefix, then "" is returned, otherwise the prefix portion of the QName is returned.
   *
   * @param qName the QName to parse.
   * @return the prefix part of the QName, or the empty string if there is not a prefix in the QName.
   */
  private static String parsePrefix( String qName )
  {
    String qNameSplit[] = qName.split(":", 2);
    return qNameSplit.length == 2 ? qNameSplit[0] : "";
  }

  public static class Node
  {
    protected String qName = null;
    protected String namespace = null;
    protected String localName = null;
  
    public Node( String namespace, String localName, String qName ) {
      this.qName = qName;
      this.namespace = namespace;
      this.localName = localName;
    }
 
    public String getQName() { return this.qName; }
    public String getNamespace() { return this.namespace; }
    public String getLocalName() { return this.localName; }
  }

  public static class Element
    extends Node
  {
    protected Map<String, String> prefixMapping = new HashMap<String, String>();
    protected Map<QName, Attribute> attributeMap = new LinkedHashMap<QName, Attribute>();

    public Element( String namespace, String localName, String qName ) {
      super(namespace, localName, qName);
    }

    public Map<QName, Attribute> getAttributeMap() { return this.attributeMap; }
    public void addAttribute( String namespace, String localName, String qName, String value )
    {
      attributeMap.put(new QName(namespace, localName), new Attribute(namespace, localName, qName, value));
    }
    public Map<String, String> getPrefixMappings() { return this.prefixMapping; }
  }

  public static class Attribute
    extends Node
  {
    protected String value = null;

    public Attribute( String namespace, String localName, String qName, String value )
    {
      super( namespace, localName, qName );
      this.value = value;
    }

    public String getValue() { return this.value; }
  }
}
