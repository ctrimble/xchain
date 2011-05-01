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
package org.xchain.namespaces.jsl;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.xchain.Command;
import org.xchain.framework.sax.SaxEventRecorder;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Devon Tackett
 */
public class TestTemplateCommand
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/simple-template.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName ELEMENT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "element");
  public static QName NESTED_ELEMENTS_TEMPLATE = new QName(JSL_NAMESPACE_URI, "nested-elements");
  public static QName COMPLEX_NESTED_ELEMENTS_TEMPLATE = new QName(JSL_NAMESPACE_URI, "complex-nested-elements");
  public static QName ATTRIBUTE_VALUE_TEMPLATE_TEMPLATE = new QName(JSL_NAMESPACE_URI, "attribute-value-template");
  public static QName VALUE_OF_TEMPLATE = new QName(JSL_NAMESPACE_URI, "value-of");
  public static QName TEXT_TEMPLATE = new QName(JSL_NAMESPACE_URI, "text");
  public static QName EXECUTE_TEMPLATE = new QName(JSL_NAMESPACE_URI, "execute-template");
  public static QName TEMPLATE_SIMPLE = new QName(JSL_NAMESPACE_URI, "template-simple");
  public static QName TEMPLATE_COMPLEX = new QName(JSL_NAMESPACE_URI, "template-complex");
  public static QName TEMPLATE_COMPOSITE = new QName(JSL_NAMESPACE_URI, "template-composite");

  protected Command command = null;

  public TestTemplateCommand()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void testElement()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ELEMENT_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testNestedElements()
    throws Exception
  {
    Command command = catalog.getCommand(NESTED_ELEMENTS_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "element", null);
    assertEndElement(eventIterator, "", "element");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testComplexNestedElements()
    throws Exception
  {
    Command command = catalog.getCommand(COMPLEX_NESTED_ELEMENTS_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "\n          text1\n          ");
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text2");
    assertEndElement(eventIterator, "", "element");
    assertCharacters(eventIterator, "\n          text3\n        ");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testAttributeValueTemplate()
    throws Exception
  {
    context.getVariables().declareVariable("attribute-value", "value");

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    Command command = catalog.getCommand(ATTRIBUTE_VALUE_TEMPLATE_TEMPLATE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();
    AttributesImpl attributes = new AttributesImpl();

    // check the document.
    assertStartDocument(eventIterator);

    attributes.addAttribute("", "attribute", "attribute", "CDATA", "value");
    attributes.addAttribute("", "concat-attribute", "concat-attribute", "CDATA", "prevaluepost");
    assertStartElement(eventIterator, "", "element", attributes);
    attributes.clear();

    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
  
  @Test public void testValueOf()
    throws Exception
  {
    context.getVariables().declareVariable("value-of", "value");

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    Command command = catalog.getCommand(VALUE_OF_TEMPLATE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "value");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
   
  @Test public void testText()
    throws Exception
  {
    Command command = catalog.getCommand(TEXT_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "value");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testExecuteTemplateSimple()
    throws Exception
  {
    Command command = catalog.getCommand(EXECUTE_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    context.getVariables().declareVariable("template-name", TEMPLATE_SIMPLE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void testExecuteTemplateComplex()
    throws Exception
  {
    Command command = catalog.getCommand(EXECUTE_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    context.getVariables().declareVariable("template-name", TEMPLATE_COMPLEX);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);

  }

  @Test public void testExecuteTemplateComposite()
    throws Exception
  {
    Command command = catalog.getCommand(EXECUTE_TEMPLATE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackCharactersEvents(true);

    context.getVariables().declareVariable("template-name", TEMPLATE_COMPOSITE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartElement(eventIterator, "", "element", null);

    // the simple template
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");
    assertEndElement(eventIterator, "", "element");

    // the complex template.
    assertStartElement(eventIterator, "", "element", null);
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");

    // the two root nodes template.
    assertCharacters(eventIterator, "text");
    assertStartElement(eventIterator, "", "element", null);
    assertCharacters(eventIterator, "text");
    assertEndElement(eventIterator, "", "element");

    assertEndElement(eventIterator, "", "element");
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);

  }
}
