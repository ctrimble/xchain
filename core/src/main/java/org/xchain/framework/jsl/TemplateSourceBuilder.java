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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.SAXException;

/**
 * A utility class to build jsl template command classes.
 *
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class TemplateSourceBuilder
{
  public static Logger log = LoggerFactory.getLogger(TemplateSourceBuilder.class);

  public static final String TEMPLATE_PACKAGE = "org.xchain.namespaces.jsl";
  public static final String BASE_TEMPLATE_NAME = "TemplateCommand";

  public static final String FIXED_PART_REGEX = "((?:[^{]+|\\{\\{)+)";
  public static final String DYNAMIC_PART_REGEX = "(?:\\{((?:[^\\}\'\"]*|\"[^\"]*\"|\'[^\']*\')*)\\})";
  public static final String ENCODING_REGEX = "(?:([\u0000-\u007f]+)|([^\u0000-\u007f]+))";

  public static Pattern ATTRIBUTE_VALUE_TEMPLATE_PATTERN = null;
  public static Pattern ENCODING_PATTERN = null;

  static {
    try {
      ATTRIBUTE_VALUE_TEMPLATE_PATTERN = Pattern.compile("\\G"+FIXED_PART_REGEX+"|"+DYNAMIC_PART_REGEX+"|\\Z");
    }
    catch( PatternSyntaxException pse ) {
      log.error("Could not compile attribute value template pattern.", pse);
    }
    try {
      ENCODING_PATTERN = Pattern.compile(ENCODING_REGEX);
    }
    catch( PatternSyntaxException pse ) {
      log.error("Could not compile encoding pattern.", pse);
    }
  }

  /** A stack of the contexts for the source files we are working on. */
  private LinkedList<Context> contextStack = new LinkedList<Context>();

  private int commandId = 0;

  public void pushContext( Context context )
  {
    contextStack.addFirst(context);
  }

  public Context popContext()
  {
    return contextStack.removeFirst();
  }

  public int nextCommandId()
  {
    return commandId++;
  }

  public void startSource(Map<String, String> transitionPrefixMapping, Set<String> transitionExcludeResultPrefixSet, boolean excludeResultPrefixBoundary)
  {
    Context context = new Context();
    context.setTransitionPrefixMapping(transitionPrefixMapping);
    context.setTransitionExcludeResultPrefixSet(transitionExcludeResultPrefixSet);
    context.setExcludeResultPrefixBoundary(excludeResultPrefixBoundary);

    pushContext(context);

    // add the source result to the context.
    context.setCommandIndex(nextCommandId());

    startVirtualChain();
  }

  public SourceResult endSource()
  {
    // clean up all of the virtual chains.
    Context context = contextStack.getFirst();
    while( !context.getVirtualChainContextStack().isEmpty() ) {
      endVirtualChain();
    }

    // pop the current context off of the stack.
    context = popContext();

    StringBuilder sourceBuilder = new StringBuilder();

    sourceBuilder.append("package ").append(TEMPLATE_PACKAGE).append(";\n");
    sourceBuilder.append("\n");

    // add all of the imports.
    sourceBuilder.append("import org.apache.commons.jxpath.JXPathContext;\n");
    sourceBuilder.append("import org.xchain.framework.sax.CommandHandler;\n");
    sourceBuilder.append("import org.xml.sax.Attributes;\n");
    sourceBuilder.append("import org.xml.sax.ContentHandler;\n");
    sourceBuilder.append("import org.xml.sax.SAXException;\n");
    sourceBuilder.append("import org.xml.sax.helpers.AttributesImpl;\n");
    sourceBuilder.append("import org.apache.commons.jxpath.JXPathContext;\n");
    sourceBuilder.append("import javax.xml.namespace.QName;\n");
    sourceBuilder.append("\n");

    // add the class definition.
    sourceBuilder.append("public class ").append(BASE_TEMPLATE_NAME).append(context.getCommandIndex()).append("\n");
    sourceBuilder.append("  extends AbstractTemplateCommand\n");
    sourceBuilder.append("{\n");

    // add the execute template method.  This is the hook into the abstract template command.
    sourceBuilder.append("  public ").append(BASE_TEMPLATE_NAME).append(context.getCommandIndex()).append("()\n");
    sourceBuilder.append("  {\n");
    sourceBuilder.append("    super(").append(context.getElementCount()).append(");\n");
    sourceBuilder.append("  }\n");
    sourceBuilder.append("  public boolean executeTemplate(JXPathContext context)\n");
    sourceBuilder.append("    throws Exception\n");
    sourceBuilder.append("  {\n");
    sourceBuilder.append("    boolean result = false;\n");
    sourceBuilder.append("    Exception exception = null;\n");
    sourceBuilder.append("    CommandHandler handler = getContentHandler();\n");

    if( context.getExcludeResultPrefixBoundary() ) {
      sourceBuilder.append("    handler.startExcludeResultPrefixContext();\n");
    }

    // we need to initialize the context of the output document with all of the namespace
    // prefixes that occur above this commands first element. 
    for( Map.Entry<String, String> mapping : context.getTransitionPrefixMapping().entrySet() ) {
      sourceBuilder.append("  handler.startPrefixMapping("+stringConstant(mapping.getKey())+", "+stringConstant(mapping.getValue())+");\n");
    }

    for( String excludeResultPrefix : context.getTransitionExcludeResultPrefixSet() ) {
      sourceBuilder.append("  handler.startExcludeResultPrefix(").append(stringConstant(excludeResultPrefix)).append(");\n");
    }

    // code to call the first virtual chain.
    sourceBuilder.append("    try {\n");
    sourceBuilder.append("      result = virtualChain0(context);\n");
    sourceBuilder.append("    }\n");
    sourceBuilder.append("    catch( Exception e ) {\n");
    sourceBuilder.append("      exception = e;\n");
    sourceBuilder.append("    }\n");

    // TODO: skip end prefix mappings if we are handling an error.

    for( String excludeResultPrefix : context.getTransitionExcludeResultPrefixSet() ) {
      sourceBuilder.append("  handler.endExcludeResultPrefix(").append(stringConstant(excludeResultPrefix)).append(");\n");
    }

    // close all of the namespace prefixes that were set above this element.
    for( Map.Entry<String, String> mapping : context.getTransitionPrefixMapping().entrySet() ) {
      sourceBuilder.append("  handler.endPrefixMapping("+stringConstant(mapping.getKey())+");\n");
    }

    if( context.getExcludeResultPrefixBoundary() ) {
      sourceBuilder.append("    handler.endExcludeResultPrefixContext();\n");
    }

    sourceBuilder.append("    if( exception != null ) {\n");
    sourceBuilder.append("      throw exception;\n");
    sourceBuilder.append("    }\n");

    // close the execute template method.
    sourceBuilder.append("    return result;\n");
    sourceBuilder.append("  }\n");

    // if there has not been a virtual chain created, then we need to add one.

    // append the source builder.
    sourceBuilder.append(context.getMethodBuilder());

    sourceBuilder.append("}\n");

    SourceResult sourceResult = new SourceResult();
    sourceResult.setSourceResourceName("org/xchain/namespaces/jsl/"+BASE_TEMPLATE_NAME+context.getCommandIndex()+".java");
    sourceResult.setClassResourceName("org/xchain/namespaces/jsl/"+BASE_TEMPLATE_NAME+context.getCommandIndex()+".class");
    sourceResult.setClassName("org.xchain.namespaces.jsl."+BASE_TEMPLATE_NAME+context.getCommandIndex());
    sourceResult.setSource(sourceBuilder.toString());

    return sourceResult;
  }

  /**
   * Signals the start of a new virtual command.  This method adds a call to the current virtual
   * method context and then creates a new virtual method context.
   */
  public void startVirtualChain()
  {
    // get the context.
    Context context = contextStack.getFirst();

    // get the virtual chain context index.
    int index = context.nextVirtualChainIndex();
    String virtualChainName = "virtualChain"+index;

    // if there is currently a virtual chain on the stack, then it needs to call this virual chain.
    if( !contextStack.getFirst().getVirtualChainContextStack().isEmpty() ) {

      // change the mode to virtual chain.
      changeBodyMode(BodyMode.VIRTUAL_CHAIN);

      // get the current virtual chain builder.
      StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();

      // append the call to the new virtual chain.
      indent(bodyBuilder).append("      result = ").append(virtualChainName).append("(context);\n");
    }

    // push the virtual chain context onto the stack.
    contextStack.getFirst().getVirtualChainContextStack().addFirst(new VirtualChainContext(virtualChainName));
  }

  /**
   * Signals the end of a virtual chain.  The method removes the current virtual chain context from the stack and 
   * uses it to build the virtual method.
   */
  public void endVirtualChain()
  {
    // change the mode to top level.
    changeBodyMode(BodyMode.TOP_LEVEL);

    // remove the top virtual chain builder from the stack.
    VirtualChainContext virtualChainContext = contextStack.getFirst().getVirtualChainContextStack().removeFirst();

    StringBuilder methodBuilder = contextStack.getFirst().getMethodBuilder();

    methodBuilder.append("private boolean ").append(virtualChainContext.getName()).append("(JXPathContext context)\n");
    methodBuilder.append("  throws Exception\n");
    methodBuilder.append("{\n");
    methodBuilder.append("  // the result and exception for this virtual chain\n");
    methodBuilder.append("  boolean result = false;\n");
    methodBuilder.append("  Exception exception = null;\n");

    methodBuilder.append("  // the target for content handler events.\n");
    methodBuilder.append("  CommandHandler handler = getContentHandler();\n");

    methodBuilder.append("  // variables for building attribute objects.\n");
    methodBuilder.append("  AttributesImpl attributes = new AttributesImpl();\n");
    methodBuilder.append("  StringBuilder attributeValueBuilder = new StringBuilder();\n");

    methodBuilder.append("  // variables for processing value-of elements.\n");
    methodBuilder.append("  Object valueOfObject = null;\n");
    methodBuilder.append("  String attributeValueString = null;\n");

    // TODO: This should be a stack and the super classes stack should be removed.
    methodBuilder.append("  // variables for parsing dynamic qNames.\n");
    methodBuilder.append("  QName qName = null;\n");
 
    methodBuilder.append("  // variables for processing comment elements.\n");

    // add the indexes of the children chains.
    methodBuilder.append("  int[] commandChildrenIndecies = {");
    Iterator<Integer> childrenIndexIterator = virtualChainContext.getCommandIndexList().iterator();
    while( childrenIndexIterator.hasNext() ) {
      methodBuilder.append(childrenIndexIterator.next());
      if( childrenIndexIterator.hasNext() ) {
        methodBuilder.append(", ");
      }
    }
    methodBuilder.append("};\n");

    // add the indices of the children elements.
    methodBuilder.append("  int[] elementChildrenIndecies = {");
    Iterator<Integer> elementIndexIterator = virtualChainContext.getElementIndexList().iterator();
    while( elementIndexIterator.hasNext() ) {
      methodBuilder.append(elementIndexIterator.next());
      if( elementIndexIterator.hasNext() ) {
        methodBuilder.append(", ");
      }
    }
    methodBuilder.append("};\n");

    // add the body portion of the method, this contains all of the sax output code and command calls.
    methodBuilder.append(virtualChainContext.getBodyBuilder());

    // add the post process code.
    methodBuilder.append("  return virtualPostProcess( context, exception, result, commandChildrenIndecies);\n");

    // close the virtual chain.
    methodBuilder.append("}\n");
  }

  public void appendCommandCall()
  {
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // get the index for the next command child and increment the command count in the context.
    Integer commandIndex = context.getCommandCount();
    context.setCommandCount(commandIndex+1);

    // if the virtual chain context has a depth greater than 1, then we need a virtual chain here.
    if( virtualChainContext.getElementDepth() > 0 ) {
      startVirtualChain();
      virtualChainContext = context.getVirtualChainContextStack().getFirst();
    }

    // add the command index to the current virtual context.
    virtualChainContext.getCommandIndexList().add(commandIndex);

    // start the chain body mode if needed.
    changeBodyMode(BodyMode.CHAIN);

    virtualChainContext.getToExecuteIndexList().add(commandIndex);
  }

  public void startStartElement()
  {
    // push virtual chain is needed.
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // get the index for the next element child and increment the element count in the context.
    Integer elementIndex = context.getElementCount();
    context.setElementCount(elementIndex+1);

    // track the current element index.
    context.getElementIndexStack().addFirst(elementIndex);

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    // track the element indices that are in this virtual chain context.
    virtualChainContext.getElementIndexList().add(elementIndex);
    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()+1);
  }

  public void endStartElement()
  {

  }

  public void startEndElement()
  {
    // start the test for this element.
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // close virtual chain contexts, until we find one with an element depth of at least one.
    while( virtualChainContext.getElementDepth() == 0 ) {
      endVirtualChain();
      virtualChainContext = context.getVirtualChainContextStack().getFirst();
    }

    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);

    // get the index for the next element child and increment the element count in the context.
    Integer elementIndex = context.getElementIndexStack().getFirst();

    // get the string builder.
    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    // TODO: track that the end element has been output.
    indent(bodyBuilder).append("if( isElementStarted(").append(elementIndex).append(") ) {\n");
  }

  public void endEndElement()
  {
    // end the test for this element.
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // get the index for the next element child and increment the element count in the context.
    context.getElementIndexStack().removeFirst();

    // get the string builder.
    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    // TODO: track that the end element has been output.
    indent(bodyBuilder).append("}\n");

    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()-1);
  }

  public void appendStartPrefixMapping( String prefix, String uri )
  {
    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);
    String escapedPrefix = stringConstant(prefix);
    String escapedUri = stringConstant(uri);
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("handler.startPrefixMapping(").append(escapedPrefix).append(", ").append(escapedUri).append(");\n");
    // TODO: We need to track the new namespace.
    indent(bodyBuilder).append("context.registerNamespace(").append(escapedPrefix).append(", ").append(escapedUri).append(");\n");
  }

  public void appendEndPrefixMapping( String prefix )
  {
    String escapedPrefix = stringConstant(prefix);
    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    // TODO: We need to replace the namespace that was specified for this prefix.
    indent(bodyBuilder).append("context.registerNamespace(").append(escapedPrefix).append(", ").append(escapedPrefix).append(");\n");
    indent(bodyBuilder).append("handler.endPrefixMapping(").append(escapedPrefix).append(");\n");
  }

  public void appendStartExcludeResultPrefix( String prefix )
  {
    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);
    String escapedPrefix = stringConstant(prefix);
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("handler.startExcludeResultPrefix(").append(escapedPrefix).append(");\n");
  }

  public void appendEndExcludeResultPrefix( String prefix )
  {
    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);
    String escapedPrefix = stringConstant(prefix);
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("handler.endExcludeResultPrefix(").append(escapedPrefix).append(");\n");
  }

  public void appendContextStartPrefixMapping( String prefix, String uri )
  {
    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);
    String escapedPrefix = stringConstant(prefix);
    String escapedUri = stringConstant(uri);
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("context.registerNamespace(").append(escapedPrefix).append(", ").append(escapedUri).append(");\n");
  }

  public void appendContextEndPrefixMapping( String prefix )
  {
    String escapedPrefix = stringConstant(prefix);
    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("context.registerNamespace(").append(escapedPrefix).append(", ").append(escapedPrefix).append(");\n");
  }

  public void appendAttributeValueTemplate( String uri, String localName, String qName, String attributeValueTemplate )
    throws SAXException
  {
    // escape the values that will be passed on.
    String escapedUri = stringConstant(uri);
    String escapedLocalName = stringConstant(localName);
    String escapedQName = stringConstant(qName);

    // change the mode of the virtual chain to BodyMode.START_ELEMENT_EVENTS if needed.
    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    // get the string builder.
    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();

    // parse the attribute value template and build code to create it.
    Iterator<String> attributeValueIterator = parseAttributeValueTemplate(attributeValueTemplate).iterator();
    while( attributeValueIterator.hasNext() ) {
      // add code for the fixed part.
      indent(bodyBuilder).append("attributeValueBuilder.append(").append(stringConstant(attributeValueIterator.next())).append(");\n");

      // if there is a dynamic part, then add it.
      if( attributeValueIterator.hasNext() ) {
        indent(bodyBuilder).append("attributeValueString = (String)context.getValue(").append(stringConstant(attributeValueIterator.next())).append(", String.class);\n");
        indent(bodyBuilder).append("attributeValueBuilder.append(attributeValueString != null ? attributeValueString : \"\");\n");
      }
    }

    // add the code to add the attribute.
    indent(bodyBuilder).append("attributes.addAttribute(").append(escapedUri).append(", ").append(escapedLocalName).append(", ").append(escapedQName).append(", \"CDATA\", attributeValueBuilder.toString());\n");
    indent(bodyBuilder).append("attributeValueBuilder = new StringBuilder();\n");
  }

  /**
   * Appends code to send a start element event to the handler.
   */
  public void appendStartElement( String uri, String localName, String qName )
  {
    Context context = contextStack.getFirst();

    Integer elementIndex = context.getElementIndexStack().getFirst();

    // escape the values that will be passed on.
    String escapedUri = stringConstant(uri);
    String escapedLocalName = stringConstant(localName);
    String escapedQName = stringConstant(qName);

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    // get the string builder.
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();
    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()+1);
    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    // TODO: we need to track the element that the we output to the handler.
    indent(bodyBuilder).append("trackStartElement(").append(elementIndex).append(");\n");
    indent(bodyBuilder).append("handler.startElement(").append(escapedUri).append(", ").append(escapedLocalName).append(", ").append(escapedQName).append(", attributes);\n");
    indent(bodyBuilder).append("attributes.clear();\n");
  }

  /**
   * Appends code to send an end element event to the handler.
   */
  public void appendEndElement( String uri, String localName, String qName )
  {
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();
    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()-1);

    // get the index for the next element child and increment the element count in the context.
    Integer elementIndex = context.getElementIndexStack().getFirst();

    // if this element has any children that are filters, then they need to be backed out, before we can move on.
    // TODO: execute the children that are filters.

    // escape the values that will be passed on.
    String escapedUri = stringConstant(uri);
    String escapedLocalName = stringConstant(localName);
    String escapedQName = stringConstant(qName);

    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);

    // get the string builder.
    StringBuilder bodyBuilder = context.getVirtualChainContextStack().getFirst().getBodyBuilder();

    // TODO: track that the end element has been output.
    indent(bodyBuilder).append("trackEndElement(").append(elementIndex).append(");\n");
    indent(bodyBuilder).append("handler.endElement(").append(escapedUri).append(", ").append(escapedLocalName).append(", ").append(escapedQName).append(");\n");
  }

  /**
   * Appends code to output a static string to the handler as a characters(char[], int, int) event to the handler.
   */
  public void appendCharacters( String characters )
  {
    String escapedCharacters = stringConstant(characters);

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("handler.characters(").append(escapedCharacters).append(".toCharArray(), 0, ").append(characters.length()).append(");\n");
  }

  public void appendIgnorableWhitespace( String ignorableWhitespace )
  {
    String escapedWhitespace = stringConstant(ignorableWhitespace);

    // TODO: should we change the body mode to START_ELEMENT_EVENTS here?

    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("handler.ignorableWhitespace(").append(escapedWhitespace).append(".toCharArray(), 0, ").append(ignorableWhitespace.length()).append(");\n");
  }

  /**
   * Appends code to output an xpath to the handler as a characters(char[], int, int) event to the handler.
   * This should support disable-output-escaping="yes|no" and output the javax.xml.transform.disable-output-escaping processing instruction
   * if needed.
   */
  public void appendValueOf( String jxpath )
  {
    String escapedJXPath = stringConstant(jxpath);

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    StringBuilder bodyBuilder = contextStack.getFirst().getVirtualChainContextStack().getFirst().getBodyBuilder();
    indent(bodyBuilder).append("valueOfObject = context.getValue(").append(escapedJXPath).append(");\n");
    indent(bodyBuilder).append("if( valueOfObject != null ) {\n");
    indent(bodyBuilder).append("  char[] valueOfChars = valueOfObject.toString().toCharArray();\n");
    indent(bodyBuilder).append("  handler.characters(valueOfChars, 0, valueOfChars.length);\n");
    indent(bodyBuilder).append("}\n");
  }

  public void appendStartComment()
  {
    Context context = contextStack.getFirst();

    Integer elementIndex = context.getElementCount();
    context.setElementCount(elementIndex+1);
    context.getElementIndexStack().addFirst(elementIndex);
    context.getVirtualChainContextStack().getFirst().getElementIndexList().add(elementIndex);

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    // get the string builder.
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();
    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()+1);
    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    indent(bodyBuilder).append("trackStartElement(").append(elementIndex).append(");\n");
    indent(bodyBuilder).append("handler.startComment();\n");
  }

  public void appendEndComment()
  {
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // get the index for the next element child and increment the element count in the context.
    Integer elementIndex = context.getElementIndexStack().getFirst();

    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);

    // get the string builder.
    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    // TODO: track that the end element has been output.
    indent(bodyBuilder).append("if( isElementStarted(").append(elementIndex).append(") ) {\n");
    indent(bodyBuilder).append("  trackEndElement(").append(elementIndex).append(");\n");
    indent(bodyBuilder).append("  handler.endComment();\n");
    indent(bodyBuilder).append("}\n");

    // get the index for the next element child and increment the element count in the context.
    context.getElementIndexStack().removeFirst();
    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()-1);
  }

  /**
   * Appends code for the start of a <jsl:element/> tag.
   *
   * @param name the required name attribute.
   * @param namespace the optional namespace attribute.
   */
  public void appendStartDynamicElement( String name, String namespace )
  {
    Context context = contextStack.getFirst();

    Integer elementIndex = context.getElementIndexStack().getFirst();

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    // get the string builder.
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // this is counted in the start start element.
    //virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()+1);

    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    // TODO: we need to track the element that the we output to the handler.
    indent(bodyBuilder).append("trackStartElement(").append(elementIndex).append(");\n");

    // add the code to process the name and namespace attributes.
    indent(bodyBuilder).append("qName = dynamicQName(context, ").append(stringConstant(name)).append(", ").append(stringConstant(namespace)).append(", true);\n");
    indent(bodyBuilder).append("getDynamicElementStack().addFirst(qName);\n");
    indent(bodyBuilder).append("handler.startElement(qName.getNamespaceURI(), qName.getLocalPart(), toPrefixedQName(qName), attributes);\n");
    indent(bodyBuilder).append("attributes.clear();\n");
  }

  /**
   * Appends code for the end of a <jsl:element/> tag.
   */
  public void appendEndDynamicElement()
  {
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    // get the index for the next element child and increment the element count in the context.
    Integer elementIndex = context.getElementIndexStack().getFirst();

    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);

    // get the string builder.
    StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

    // TODO: track that the end element has been output.
    indent(bodyBuilder).append("trackEndElement(").append(elementIndex).append(");\n");
    indent(bodyBuilder).append("qName = getDynamicElementStack().removeFirst();\n");
    indent(bodyBuilder).append("handler.endElement(qName.getNamespaceURI(), qName.getLocalPart(), toPrefixedQName(qName));\n");

    // this is counted in the endEndElement.
    // virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()-1);
  }

  /**
   * Appends code for the start of a <jsl:attribute/> tag.
   *
   * @param name the required name attribute.
   * @param namespace the required namespace attribute.
   */
  public void appendStartDynamicAttribute( String name, String namespace )
  {
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();
    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()+1);

    changeBodyMode(BodyMode.START_ELEMENT_EVENTS);

    // get the string builder.
    StringBuilder bodyBuilder = context.getVirtualChainContextStack().getFirst().getBodyBuilder();

    // add the code to process the name and namespace attributes.
    indent(bodyBuilder).append("qName = dynamicQName(context, ").append(stringConstant(name)).append(", ").append(stringConstant(namespace)).append(", false);\n");
    indent(bodyBuilder).append("getDynamicElementStack().addFirst(qName);\n");
    indent(bodyBuilder).append("handler.startAttribute(qName.getNamespaceURI(), qName.getLocalPart(), toPrefixedQName(qName) );\n");
    indent(bodyBuilder).append("attributes.clear();\n");
  }

  /**
   * Appends code for the end of a <jsl:attribute/> tag.
   */
  public void appendEndDynamicAttribute()
  {
    Context context = contextStack.getFirst();
    VirtualChainContext virtualChainContext = context.getVirtualChainContextStack().getFirst();

    changeBodyMode(BodyMode.END_ELEMENT_EVENTS);

    // get the string builder.
    StringBuilder bodyBuilder = context.getVirtualChainContextStack().getFirst().getBodyBuilder();

    // TODO: track that the end element has been output.
    indent(bodyBuilder).append("qName = getDynamicElementStack().removeFirst();\n");
    indent(bodyBuilder).append("handler.endAttribute(qName.getNamespaceURI(), qName.getLocalPart(), toPrefixedQName(qName) );\n");

    virtualChainContext.setElementDepth(virtualChainContext.getElementDepth()-1);
  }

  public void changeBodyMode( BodyMode newMode )
  {
    VirtualChainContext virtualChainContext = contextStack.getFirst().getVirtualChainContextStack().getFirst();

    // if the body mode is changing, then we need to add code to the close the current mode
    // and start the new mode.
    if( newMode != virtualChainContext.getBodyMode() ) {
      StringBuilder bodyBuilder = virtualChainContext.getBodyBuilder();

      // stop the old mode.
      switch( virtualChainContext.getBodyMode() ) {
        case CHAIN:
          indent(bodyBuilder).append("  result = executeChildren(context, new int[]{");
          Iterator<Integer> toExecuteIndexIterator = virtualChainContext.getToExecuteIndexList().iterator();
          while( toExecuteIndexIterator.hasNext() ) {
            Integer toExecuteIndex = toExecuteIndexIterator.next();
            bodyBuilder.append(toExecuteIndex);
            if( toExecuteIndexIterator.hasNext() ) {
              bodyBuilder.append(", ");
            }
          }
          bodyBuilder.append("});\n");
          virtualChainContext.getToExecuteIndexList().clear();
        case VIRTUAL_CHAIN:
          decrementIndent();
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("  catch( Exception e ) {\n");
          indent(bodyBuilder).append("    exception = e;\n");
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("}\n");
          break;
        case START_ELEMENT_EVENTS: 
          decrementIndent();
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("  catch( SAXException e ) {\n");
          indent(bodyBuilder).append("    registerSaxException(e);\n");
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("  catch( Exception e ) {\n");
          indent(bodyBuilder).append("    registerSaxException(new SAXException(e));\n");
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("  finally {\n");
          indent(bodyBuilder).append("    attributes.clear();\n");
          indent(bodyBuilder).append("    attributeValueBuilder = new StringBuilder();\n");
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("}\n");
          break;
        case END_ELEMENT_EVENTS:
          decrementIndent();
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("  catch( SAXException e ) {\n");
          indent(bodyBuilder).append("    registerSaxException(e);\n");
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("  catch( Exception e ) {\n");
          indent(bodyBuilder).append("    registerSaxException(new SAXException(e));\n");
          indent(bodyBuilder).append("  }\n");
          indent(bodyBuilder).append("}\n");
        case TOP_LEVEL:
          break;
        default:
          throw new IllegalStateException("Unknown virtual chain body mode encountered.");
      }

      // start the new mode.
      switch( newMode ) {
        case CHAIN:
        case VIRTUAL_CHAIN:
        case START_ELEMENT_EVENTS:
          indent(bodyBuilder).append("if( exception == null && !result && !hasSaxExceptionFired() ) {\n");
          indent(bodyBuilder).append("  try {\n");
          incrementIndent();
          break;
        case END_ELEMENT_EVENTS:
          indent(bodyBuilder).append("if( !hasSaxExceptionFired() ) {\n");
          indent(bodyBuilder).append("  try {\n");
          incrementIndent();
          break;
        case TOP_LEVEL:
          break;
        default:
          throw new IllegalStateException("Unknown virtual chain body mode encountered.");
      }
    }

    virtualChainContext.setBodyMode(newMode);
  }

  public StringBuilder indent(StringBuilder builder)
  {
    int indent = contextStack.getFirst().getIndent();

    for( int i = 0; i < indent; i++ ) {
      builder.append("  ");
    }

    return builder;
  }

  public void incrementIndent()
  {
    contextStack.getFirst().setIndent(contextStack.getFirst().getIndent()+1);
  }

  public void decrementIndent()
  {
    contextStack.getFirst().setIndent(contextStack.getFirst().getIndent()-1);
  }

  /**
   * Parses an attribute value template into fixed and dynamic parts.  This list will always start with a fixed part and
   * then include alternating dynamic and fixed parts.
   */
  public static List<String> parseAttributeValueTemplate( String attributeValueTemplate )
    throws SAXException
  {
    // the result.
    ArrayList<String> result = new ArrayList<String>();

    // create the matcher.
    Matcher matcher = ATTRIBUTE_VALUE_TEMPLATE_PATTERN.matcher(attributeValueTemplate);

    while( matcher.find() ) {
      String fixedPart = matcher.group(1);
      String dynamicPart = matcher.group(2);

      if( result.isEmpty() && fixedPart == null ) {
        result.add("");
      }

      if( fixedPart != null ) {
        result.add(fixedPart.replaceAll("\\{\\{", "{").replaceAll("\\}\\}", "}"));
      }
      if( dynamicPart != null ) {
        result.add(dynamicPart);
      }
    }

    if( !matcher.hitEnd() ) {
      throw new SAXException("The attribute value template '"+attributeValueTemplate+"' has an error between characters "+matcher.regionStart()+" and "+matcher.regionEnd()+".");
    }

    return result;
  }

  /**
   * The context for a sax template command.
   */
  public static class Context
  {
    private LinkedList<VirtualChainContext> virtualChainContextStack = new LinkedList<VirtualChainContext>();
    private StringBuilder methodBuilder = new StringBuilder();
    private int commandCount = 0;
    private int elementCount = 0;
    private StringBuilder headerBuilder = new StringBuilder();
    private StringBuilder footerBuilder = new StringBuilder();
    private int indent = 0;
    private int commandIndex = 0;
    private int nextVirtualChainIndex = 0;
    private Map<String, String> transitionPrefixMapping;
    private Set<String> transitionExcludeResultPrefixSet;
    private boolean excludeResultPrefixBoundary = false;
    private LinkedList<Integer> elementIndexStack = new LinkedList<Integer>();

    /** Returns the stack of virtual chain contexts. */
    public LinkedList<VirtualChainContext> getVirtualChainContextStack() { return virtualChainContextStack; }

    /** Returns the string builder used to store completed methods. */
    public StringBuilder getMethodBuilder() { return methodBuilder; }

    public void setCommandCount( int commandCount ) { this.commandCount = commandCount; }
    public int getCommandCount() { return this.commandCount; }

    public void setElementCount( int elementCount ) { this.elementCount = elementCount; }
    public int getElementCount() { return this.elementCount; }

    public StringBuilder getHeaderBuilder() { return this.headerBuilder; }
    public StringBuilder getFooterBuilder() { return this.footerBuilder; }

    public void setIndent( int indent ) { this.indent = indent; }
    public int getIndent() { return this.indent; }

    public void setCommandIndex( int commandIndex ) { this.commandIndex = commandIndex; }
    public int getCommandIndex() { return this.commandIndex; }

    public int nextVirtualChainIndex() {
      return nextVirtualChainIndex++;
    }

    public Map<String, String> getTransitionPrefixMapping() { return transitionPrefixMapping; }
    public void setTransitionPrefixMapping( Map<String, String> transitionPrefixMapping ) { this.transitionPrefixMapping = transitionPrefixMapping; }

    public Set<String> getTransitionExcludeResultPrefixSet() { return transitionExcludeResultPrefixSet; }
    public void setTransitionExcludeResultPrefixSet( Set<String> transitionExcludeResultPrefixSet ) { this.transitionExcludeResultPrefixSet = transitionExcludeResultPrefixSet; }

    public boolean getExcludeResultPrefixBoundary() { return excludeResultPrefixBoundary; }
    public void setExcludeResultPrefixBoundary( boolean excludeResultPrefixBoundary ) { this.excludeResultPrefixBoundary = excludeResultPrefixBoundary; }

    public LinkedList<Integer> getElementIndexStack() { return elementIndexStack; }
  }

  public static enum BodyMode
  {
    /** The body mode when we are at the top level of a virtual method body. */
    TOP_LEVEL,
    /** The body mode when we are inside a set of chain calls. */
    CHAIN,
    /** The body mode when we are inside a set of handler calls that start elements or send character events. */
    START_ELEMENT_EVENTS,
    /** The body mode when we are inside a set of handler calls that end elements. */
    END_ELEMENT_EVENTS,
    VIRTUAL_CHAIN
  }

  /**
   * The context for a virtual chain method.
   */
  public static class VirtualChainContext
  {
    private String name = null;
    private StringBuilder bodyBuilder = new StringBuilder();
    private List<Integer> commandIndexList = new ArrayList<Integer>();
    private List<Integer> elementIndexList = new ArrayList<Integer>();
    private List<Integer> toExecuteIndexList = new ArrayList<Integer>();
    private int elementDepth = 0;
    private BodyMode bodyMode = BodyMode.TOP_LEVEL;

    public VirtualChainContext( String name )
    {
      this.name = name;
    }

    /** Returns the name of this virtual chain method. */
    public String getName() { return name; }

    /** Returns the builer for the body of this method. */
    public StringBuilder getBodyBuilder() { return bodyBuilder; }

    /** Returns the list of child command indecies. */
    public List<Integer> getCommandIndexList() { return commandIndexList; }

    public List<Integer> getElementIndexList() { return elementIndexList; }

    public List<Integer> getToExecuteIndexList() { return toExecuteIndexList; }

    /** Returns the current body mode. */
    public BodyMode getBodyMode() { return this.bodyMode; }

    /** Sets the current body mode. */
    public void setBodyMode( BodyMode bodyMode ) { this.bodyMode = bodyMode; };

    public int getElementDepth() { return this.elementDepth; }
    public void setElementDepth( int elementDepth ) { this.elementDepth = elementDepth; }
  }

  public static String stringConstant( String source )
  {
    // is the source is null, return null.
    if( source == null ) {
      return "((String)null)";
    }

    // the string buffer for the result.
    StringBuilder builder = new StringBuilder();

    // the initial double quote.
    builder.append('\"');

    // iterate over all of the characters, encoding all chars over 7 bits into utf escape sequences.
    Matcher matcher = ENCODING_PATTERN.matcher(source);

    while(matcher.find()) {
      if( matcher.group(1) != null ) {
        builder.append(matcher.group(1)
          .replaceAll("\\\\", "\\\\\\\\")
          .replaceAll("\\\"", "\\\\\"")
          .replaceAll("\\\'", "\\\\\'")
          .replaceAll("\r", "\\\\r")
          .replaceAll("\t", "\\\\t")
          .replaceAll("\b", "\\\\b")
          .replaceAll("\n", "\\\\n")
          .replaceAll("\f", "\\\\f"));
      }
      else {
        // there has to be a better way to do this formatting...
        String toUnicode = matcher.group(2);
        for( int i = 0; i < toUnicode.length(); i++ ) {
          builder.append("\\u");
          String codePoint = Integer.toHexString(matcher.group(2).codePointAt(i));
          for( int j = codePoint.length(); j < 4; j++ ) { builder.append("0"); }
          builder.append(codePoint);
        }
      }
    }

    // terminating double quote.
    builder.append('\"');

    return builder.toString();
  }
}
