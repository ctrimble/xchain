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

import org.xchain.namespaces.jsl.AbstractTemplateCommand;
import org.xchain.namespaces.jsl.TemporaryCommand;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.commons.digester.Digester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class SaxTemplateHandler
  extends AbstractSaxTemplateHandler
{
  public static Logger log = LoggerFactory.getLogger(SaxTemplateHandler.class);

  public static String TEMPLATE_LOCAL_NAME = "template";
  public static String VALUE_OF_LOCAL_NAME = "value-of";
  public static String TEXT_LOCAL_NAME     = "text";
  public static String COMMENT_LOCAL_NAME  = "comment";
  public static String ELEMENT_LOCAL_NAME  = "element";
  public static String ATTRIBUTE_LOCAL_NAME  = "attribute";

  public static String TEMPORARY_CHAIN_LOCAL_NAME = "temporary";

  public static enum CharacterTarget{ NONE, SOURCE_BUILDER, DIGESTER };

  protected static Pattern whitespacePattern = null;
  static {
    try {
      whitespacePattern = Pattern.compile("\\A\\s*\\z");
    }
    catch( PatternSyntaxException pse ) {
      if( log.isErrorEnabled() ) {
        log.error("Could not compile the whitespace pattern.", pse);
      }
    }
  }

  /** A control stack for character targets. */
  protected LinkedList<CharacterTarget> charactersTargetStack = new LinkedList<CharacterTarget>();

  /** A control stack for ignorable whitespace. */
  protected LinkedList<CharacterTarget> ignorableWhitespaceTargetStack = new LinkedList<CharacterTarget>();

  /** The template builder that will be used to create the source files. */
  protected TemplateSourceBuilder sourceBuilder = new TemplateSourceBuilder();

  /** The digester that we will inject templates into. */
  protected Digester digester = null;

  /** The depth of jsl template elements in the current event stream. */
  protected int templateElementDepth = 0;

  public void setDigester( Digester digester ) { this.digester = digester; }

  //
  // Handlers for JSL element events.
  //

  /**
   * Handles start element events for all JSL elements.
   */
  protected void startJslElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    // scan for jsl elements that come out of order.
    if( TEMPLATE_LOCAL_NAME.equals( localName ) ) {
      templateElementDepth++;
    }
    else if( templateElementDepth == 0 ) {
      throw new SAXException("The element {"+uri+"}"+localName+"was found outside of a {"+JSL_NAMESPACE+"}template element.");
    }

    boolean startSource = elementInfoStack.size() == 1 || elementInfoStack.get(1).getElementType() == ElementType.COMMAND_ELEMENT_TYPE;
    // if the parent element is null or a command element, then we need to start a new template source.
    if( startSource ) {
      sourceBuilder.startSource(elementInfoStack.getFirst().getSourcePrefixMapping(), elementInfoStack.getFirst().getSourceExcludeResultPrefixSet(), TEMPLATE_LOCAL_NAME.equals(localName));
    }

    if( TEMPLATE_LOCAL_NAME.equals( localName ) ) {
      startJslTemplateElement( uri, localName, qName, attributes );
    }
    else if( VALUE_OF_LOCAL_NAME.equals( localName ) ) {
      startJslValueOfElement( uri, localName, qName, attributes );
    }
    else if( TEXT_LOCAL_NAME.equals( localName ) ) {
      startJslTextElement( uri, localName, qName, attributes );
    }
    else if( COMMENT_LOCAL_NAME.equals( localName ) ) {
      startJslCommentElement( uri, localName, qName, attributes );
    }
    else if( ELEMENT_LOCAL_NAME.equals( localName ) ) {
      startJslDynamicElement( uri, localName, qName, attributes );
    }
    else if( ATTRIBUTE_LOCAL_NAME.equals( localName ) ) {
      startJslDynamicAttribute( uri, localName, qName, attributes );
    }
    else {
      throw new SAXException("Unknown template element '{"+uri+"}"+localName+"'.");
    }

    if( startSource ) {
      // send start prefix mapping events to the digester.
      for( Map.Entry<String, String> mapping : elementInfoStack.getFirst().getHandlerPrefixMapping().entrySet() ) {
        getContentHandler().startPrefixMapping(mapping.getKey(), mapping.getValue());
      }

      // create a new attributes object with any attributes that are in an xchain namespace.
      AttributesImpl digesterAttributes = new AttributesImpl();
      for( int i = 0; i < attributes.getLength(); i++ ) {
        if( commandNamespaceSet.contains(attributes.getURI(i)) ) {
          digesterAttributes.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
        }
      }

      // send the start template element to the digester.
      getContentHandler().startElement( JSL_NAMESPACE, TEMPORARY_CHAIN_LOCAL_NAME, temporaryChainQName(), digesterAttributes );
    }
  }

  /**
   * Handles end element events for all JSL elements.
   */
  protected void endJslElement( String uri, String localName, String qName )
    throws SAXException
  {
    if( TEMPLATE_LOCAL_NAME.equals( localName ) ) {
      // this element is a flag, ignore it.
      endJslTemplateElement( uri, localName, qName );
    }
    else if( VALUE_OF_LOCAL_NAME.equals( localName ) ) {
      endJslValueOfElement( uri, localName, qName );
    }
    else if( TEXT_LOCAL_NAME.equals( localName ) ) {
      endJslTextElement( uri, localName, qName );
    }
    else if( COMMENT_LOCAL_NAME.equals( localName ) ) {
      endJslCommentElement( uri, localName, qName );
    }
    else if( ELEMENT_LOCAL_NAME.equals( localName ) ) {
      endJslDynamicElement( uri, localName, qName );
    }
    else if( ATTRIBUTE_LOCAL_NAME.equals( localName ) ) {
      endJslDynamicAttribute( uri, localName, qName );
    }
    else {
      throw new SAXException("Unknown template element '{"+uri+"}"+localName+"'.");
    }

    // if the parent element is null or a command element, then we need to end the template source.
    if( elementInfoStack.size() == 1 || elementInfoStack.get(1).getElementType() == ElementType.COMMAND_ELEMENT_TYPE ) {
      handleSource(sourceBuilder.endSource());

      // send end prefix mapping events to the digester.
      for( String prefix : elementInfoStack.getFirst().getHandlerPrefixMapping().keySet() ) {
        getContentHandler().endPrefixMapping(prefix);
      }
    }

    if( TEMPLATE_LOCAL_NAME.equals( localName ) ) {
      templateElementDepth--;
      if( templateElementDepth < 0 ) {
        throw new SAXException("The template element depth has come out of synch.");
      }
    }

  }

  private void handleSource( SourceResult templateSource )
    throws SAXException
  {
      AbstractTemplateCommand replacementCommand = null;

      try {
        replacementCommand = (AbstractTemplateCommand)sourceCompiler.compileTemplate(templateSource).newInstance();
      }
      catch( Exception e ) {
        throw new SAXException("Could not instantiate template class.", e);
      }

      // remove the temporary command off of the digesters stack.
      TemporaryCommand temporaryCommand = (TemporaryCommand)digester.pop();

      // add all of the temporary commands children to the replacement command.
      replacementCommand.getCommandList().addAll(temporaryCommand.getCommandList());
      replacementCommand.setLocator(temporaryCommand.getLocator());

      // push the replacement command onto the stack.
      digester.push(replacementCommand);

      // pass the end element event to the digester.
      getContentHandler().endElement( JSL_NAMESPACE, TEMPORARY_CHAIN_LOCAL_NAME, temporaryChainQName());
  }

  private String temporaryChainQName()
  {
    String jslNamespacePrefix = prefixMappingContext.lookUpPrefix(JSL_NAMESPACE);
    return "".equals(jslNamespacePrefix) ? TEMPORARY_CHAIN_LOCAL_NAME : jslNamespacePrefix + ":" + TEMPORARY_CHAIN_LOCAL_NAME;
  }

  protected void startJslTemplateElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
  }

  protected void endJslTemplateElement( String uri, String localName, String qName )
    throws SAXException
  {
    // configure the character target stacks.
    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();
  }

  /**
   * Handles start element events for JSL value-of elements.
   */
  protected void startJslValueOfElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    // configure the character target stacks.
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    String select = attributes.getValue("", "select");

    if( select == null ) {
      throw new SAXException("The {"+JSL_NAMESPACE+"}"+VALUE_OF_LOCAL_NAME+" requires a select attribute.");
    }

    for( Map.Entry<String, String> entry : elementInfoStack.getFirst().getPrefixMapping().entrySet() ) {
      sourceBuilder.appendContextStartPrefixMapping(entry.getKey(), entry.getValue());
    }

    sourceBuilder.appendValueOf(select);

    for( Map.Entry<String, String> entry : elementInfoStack.getFirst().getPrefixMapping().entrySet() ) {
      sourceBuilder.appendContextEndPrefixMapping(entry.getKey());
    }

  }

  /**
   * Handles end element events for JSL value-of elements.
   */
  protected void endJslValueOfElement( String uri, String localName, String qName )
    throws SAXException
  {
    // configure the character target stacks.
    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();
  }

  /**
   * Handles start element events for JSL text elements.
   */
  protected void startJslTextElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    // push characters target.
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    // push ignorable whitespace target.
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
  }

  /**
   * Handles end element events for JSL text elements.
   */
  protected void endJslTextElement( String uri, String localName, String qName )
    throws SAXException
  {
    // pop characters target.
    charactersTargetStack.removeFirst();

    // pop ignorable whitespace target.
    ignorableWhitespaceTargetStack.removeFirst();
  }

  /**
   * Handles start element events for JSL comment elements.
   */
  protected void startJslCommentElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    // whitespace needs to go to the source builder.
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    // start the comment.
    sourceBuilder.appendStartComment();
  }

  /**
   * Handles end element events for JSL comment elements.
   */
  protected void endJslCommentElement( String uri, String localName, String qName )
    throws SAXException
  {
    // end the comment.
    sourceBuilder.appendEndComment();

    // reset the whitespace targets.
    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();
  }

  /**
   * Handles start element events for JSL element elements.
   */
  protected void startJslDynamicElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    // whitespace needs to go to the source builder.
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    // start the element
    String name = attributes.getValue("", "name");
    String namespace = attributes.getValue("", "namespace");

    // get the element info for the head of the stack.
    ElementInfo elementInfo = elementInfoStack.getFirst();

    sourceBuilder.startStartElement();

    // send the prefix mappings to the source builder.
    for( Map.Entry<String, String> prefixMapping : elementInfo.getPrefixMapping().entrySet() ) {
      sourceBuilder.appendStartPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
    }

    // send any exclude result prefix events that need to go out.
    for( String excludeResultPrefix : elementInfo.getExcludeResultPrefixSet() ) {
      sourceBuilder.appendStartExcludeResultPrefix(excludeResultPrefix);
    }

    sourceBuilder.appendStartDynamicElement(name, namespace);

    sourceBuilder.endStartElement();
  }

  /**
   * Handles end element events for JSL element elements.
   */
  protected void endJslDynamicElement( String uri, String localName, String qName )
    throws SAXException
  {
    ElementInfo elementInfo = elementInfoStack.getFirst();

    sourceBuilder.startEndElement();
    sourceBuilder.appendEndDynamicElement();

    // send any exclude result prefix events that need to go out.
    for( String excludeResultPrefix : elementInfo.getExcludeResultPrefixSet() ) {
      sourceBuilder.appendEndExcludeResultPrefix(excludeResultPrefix);
    }

    // send the prefix mappings to the source builder.
    for( Map.Entry<String, String> prefixMapping : elementInfo.getPrefixMapping().entrySet() ) {
      sourceBuilder.appendEndPrefixMapping(prefixMapping.getKey());
    }

    sourceBuilder.endEndElement();

    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();
  }

  /**
   * Handles start element events for JSL attribute elements.
   */
  protected void startJslDynamicAttribute( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    ElementInfo elementInfo = elementInfoStack.getFirst();
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    // send the prefix mappings to the source builder.
    for( Map.Entry<String, String> prefixMapping : elementInfo.getPrefixMapping().entrySet() ) {
      sourceBuilder.appendStartPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
    }

    // send any exclude result prefix events that need to go out.
    for( String excludeResultPrefix : elementInfo.getExcludeResultPrefixSet() ) {
      sourceBuilder.appendStartExcludeResultPrefix(excludeResultPrefix);
    }

    String name = attributes.getValue("", "name");
    String namespace = attributes.getValue("", "namespace");
    sourceBuilder.appendStartDynamicAttribute(name, namespace);
  }

  /**
   * Handles end element events for JSL element elements.
   */
  protected void endJslDynamicAttribute( String uri, String localName, String qName )
    throws SAXException
  {
    ElementInfo elementInfo = elementInfoStack.getFirst();

    sourceBuilder.appendEndDynamicAttribute();

    // send any exclude result prefix events that need to go out.
    for( String excludeResultPrefix : elementInfo.getExcludeResultPrefixSet() ) {
      sourceBuilder.appendEndExcludeResultPrefix(excludeResultPrefix);
    }

    // send the prefix mappings to the source builder.
    for( Map.Entry<String, String> prefixMapping : elementInfo.getPrefixMapping().entrySet() ) {
      sourceBuilder.appendEndPrefixMapping(prefixMapping.getKey());
    }

    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();
  }

  /**
   * Handles start element events for elements in an xchain namespace.
   */
  protected void startCommandElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    // configure the character target stacks.
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    // get the element info.
    ElementInfo elementInfo = elementInfoStack.getFirst();

    // if this parent element is not a command, then we need to append a command call and start
    if( elementInfoStack.size() > 1 && elementInfoStack.get(1).getElementType() != ElementType.COMMAND_ELEMENT_TYPE ) {
      sourceBuilder.appendCommandCall();
    }

    for( Map.Entry<String, String> prefixMapping : elementInfo.getHandlerPrefixMapping().entrySet() ) {
      getContentHandler().startPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
    }

    // send the start element to the digester.
    getContentHandler().startElement( uri, localName, qName, attributes );
  }


  /**
   * Handles end element events for elements in an xchain namespace.
   */
  protected void endCommandElement( String uri, String localName, String qName )
    throws SAXException
  {
    // pop the character target stacks.
    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();

    // get the element info.
    ElementInfo elementInfo = elementInfoStack.getFirst();

    // we do not need to do anything to the source builder.


    // send the end element to the digester.
    getContentHandler().endElement( uri, localName, qName );

    for( Map.Entry<String, String> prefixMapping : elementInfo.getHandlerPrefixMapping().entrySet() ) {
      getContentHandler().endPrefixMapping(prefixMapping.getKey());
    }
  }

  /**
   * Handles start element events for template elements.
   */
  protected void startTemplateElement( String uri, String localName, String qName, Attributes attributes )
    throws SAXException
  {
    if( templateElementDepth == 0 ) {
      throw new SAXException("The element {"+uri+"}"+localName+" was found outside of a template element.");
    }

    // push characters target.
    charactersTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    // push ignorable whitespace target.
    ignorableWhitespaceTargetStack.addFirst(CharacterTarget.SOURCE_BUILDER);

    boolean startSource = elementInfoStack.size() == 1 || elementInfoStack.get(1).getElementType() == ElementType.COMMAND_ELEMENT_TYPE;
    // if the parent element is null or a command element, then we need to start a new template source.
    if( startSource ) {
      sourceBuilder.startSource(elementInfoStack.getFirst().getSourcePrefixMapping(), elementInfoStack.getFirst().getSourceExcludeResultPrefixSet(), false);
    }

    try {
    // get the element info for the head of the stack.
    ElementInfo elementInfo = elementInfoStack.getFirst();

    // let the source builder know that we are starting a start element.
    sourceBuilder.startStartElement();

    // send the prefix mappings to the source builder.
    for( Map.Entry<String, String> prefixMapping : elementInfo.getPrefixMapping().entrySet() ) {
      sourceBuilder.appendStartPrefixMapping(prefixMapping.getKey(), prefixMapping.getValue());
    }

    // send any exclude result prefix events that need to go out.
    for( String excludeResultPrefix : elementInfo.getExcludeResultPrefixSet() ) {
      sourceBuilder.appendStartExcludeResultPrefix(excludeResultPrefix);
    }

    // send the attributes to the source builder.
    for( int i = 0; i < attributes.getLength(); i++) {
      sourceBuilder.appendAttributeValueTemplate(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getValue(i));
    }

    // append the start element.
    sourceBuilder.appendStartElement(uri, localName, qName);

    // let the source builder know that we are ending a start element.
    sourceBuilder.endStartElement();
    }
    catch( SAXException e ) {
      throw e;
    }
    catch( Exception e ) {
      throw new SAXException(e);
    }

    // we need to do this just before we stop templating, so this kind of code should be in start command, or end jsl template and end template when there was not
    // a nested command.  We need to delay this, since the passed through template must provide the namespaces state from all of the consumed elements.
    if( startSource ) {
      // send start prefix mapping events to the digester.
      for( Map.Entry<String, String> mapping : elementInfoStack.getFirst().getHandlerPrefixMapping().entrySet() ) {
        getContentHandler().startPrefixMapping(mapping.getKey(), mapping.getValue());
      }

      // create a new attributes object with any attributes that are in an xchain namespace.
      AttributesImpl digesterAttributes = new AttributesImpl();
      for( int i = 0; i < attributes.getLength(); i++ ) {
        if( commandNamespaceSet.contains(attributes.getURI(i)) ) {
          digesterAttributes.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
        }
      }

      // send the start template element to the digester.
      getContentHandler().startElement( JSL_NAMESPACE, TEMPORARY_CHAIN_LOCAL_NAME, temporaryChainQName(), digesterAttributes );
    }
  }

  /**
   * Handles end element events for template elements.
   */
  public void endTemplateElement( String uri, String localName, String qName )
    throws SAXException
  {
    // pop the character target stacks.
    charactersTargetStack.removeFirst();
    ignorableWhitespaceTargetStack.removeFirst();

    // get the element info for the head of the stack.
    ElementInfo elementInfo = elementInfoStack.getFirst();

    // let the source builder know that we are starting a start element.
    sourceBuilder.startEndElement();

    // append the end element.
    sourceBuilder.appendEndElement(uri, localName, qName);

    // send any exclude result prefix events that need to go out.
    for( String excludeResultPrefix : elementInfo.getExcludeResultPrefixSet() ) {
      sourceBuilder.appendEndExcludeResultPrefix(excludeResultPrefix);
    }

    // send the prefix mappings to the source builder.
    for( Map.Entry<String, String> prefixMapping : elementInfo.getPrefixMapping().entrySet() ) {
      sourceBuilder.appendEndPrefixMapping(prefixMapping.getKey());
    }

    // let the source builder know that we are ending a start element.
    sourceBuilder.endEndElement();

    boolean startSource = elementInfoStack.size() == 1 || elementInfoStack.get(1).getElementType() == ElementType.COMMAND_ELEMENT_TYPE;
    // if the parent element is null or a command element, then we need to start a new template source.
    if( startSource ) {
      handleSource(sourceBuilder.endSource());

      // send end prefix mapping events to the digester.
      for( String prefix : elementInfoStack.getFirst().getHandlerPrefixMapping().keySet() ) {
        getContentHandler().endPrefixMapping(prefix);
      }
    }
  }

  //
  // Handlers for element body content events.
  //

  /**
   * Handles characters found in the source document.
   */
  public void flushCharacters()
    throws SAXException
  {
    if( !charactersTargetStack.isEmpty() ) {
      // switch on the characters target.
      switch( charactersTargetStack.getFirst() ) {
        case NONE:
          break;
        case SOURCE_BUILDER:
          if( !whitespacePattern.matcher(charactersBuilder).matches() ) {
            // if we are in an xchain command, then we need to start a new source.
            boolean startSource = elementInfoStack.size() == 0 || elementInfoStack.get(0).getElementType() == ElementType.COMMAND_ELEMENT_TYPE;

            if( startSource ) {
              // don't send any mappings for the element.
              sourceBuilder.startSource(new HashMap<String, String>(), new HashSet<String>(), false);
              getContentHandler().startElement( JSL_NAMESPACE, TEMPORARY_CHAIN_LOCAL_NAME, temporaryChainQName(), new AttributesImpl() );
            }

            sourceBuilder.appendCharacters(charactersBuilder.toString());

            if( startSource ) {
              handleSource(sourceBuilder.endSource());
            }
          }
          break;
        case DIGESTER:
          char[] characters = charactersBuilder.toString().toCharArray();
          getContentHandler().characters(characters, 0, characters.length);
          break;
      }
      charactersBuilder = new StringBuilder();
    }
  }
}
