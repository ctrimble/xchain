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
import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.junit.Ignore;
import org.xchain.Command;
import org.xchain.framework.sax.SaxEventRecorder;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Christian Trimble
 */
public class TestElementCommand
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/element.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName SIMPLE_ELEMENT = new QName(JSL_NAMESPACE_URI, "simple-element");
  public static QName DYNAMIC_DYNAMIC = new QName(JSL_NAMESPACE_URI, "dynamic-dynamic");
  public static QName DYNAMIC_DYNAMIC_DYNAMIC = new QName(JSL_NAMESPACE_URI, "dynamic-dynamic-dynamic");
  public static QName DYNAMIC_TEMPLATE_DYNAMIC = new QName(JSL_NAMESPACE_URI, "dynamic-template-dynamic");
  public static QName TEMPLATE_DYNAMIC_TEMPLATE = new QName(JSL_NAMESPACE_URI, "template-dynamic-template");
  public static QName DYNAMIC_QNAME_DYNAMIC_NAMESPACE = new QName(JSL_NAMESPACE_URI, "dynamic-qname-dynamic-namespace");
  public static String TEST_1_PREFIX = "test1";
  public static String TEST_1_NAMESPACE = "http://test/1";
  public static String TEST_2_PREFIX = "test2";
  public static String TEST_2_NAMESPACE = "http://test/2";
  public static String ELEMENT_LOCAL_NAME = "element";

  protected Command command = null;

  public TestElementCommand()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void simpleElement()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(SIMPLE_ELEMENT);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // prefix mappings.
    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put("", "");

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, true);
    assertStartElement(eventIterator, "", ELEMENT_LOCAL_NAME, null);
    assertEndElement(eventIterator, "", ELEMENT_LOCAL_NAME);
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), true);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  /**
   * Tests deep documents that have alternating prefixes.
   */
  private void nestingTest( QName commandName, int depth )
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(commandName);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // prefix mappings.
    Map<String, String> prefixMappings = new HashMap<String, String>();

    // check the document.
    assertStartDocument(eventIterator);

    for( int i = 0; i <= depth; i++ ) {
      String namespace = i % 2 == 0 ? TEST_1_NAMESPACE : TEST_2_NAMESPACE;
      prefixMappings.put("test", namespace);
      assertStartPrefixMappings(eventIterator, prefixMappings, true);
      prefixMappings.clear();
      assertStartElement(eventIterator, namespace, ELEMENT_LOCAL_NAME, null);
    }

    for( int i = depth; i >= 0; i-- ) {
      String namespace = i % 2 == 0 ? TEST_1_NAMESPACE : TEST_2_NAMESPACE;

      assertEndElement(eventIterator, namespace, ELEMENT_LOCAL_NAME);

      // test the prefix mappings ending.
      prefixMappings.put("test", namespace);
      assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), true);
      prefixMappings.clear();

    }

    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);

  }

  @Test public void dynamicDynamic()
    throws Exception
  {
    nestingTest( DYNAMIC_DYNAMIC, 1 );
  }

  @Test public void dynamicDynamicDynamic()
    throws Exception
  {
    nestingTest( DYNAMIC_DYNAMIC_DYNAMIC, 2 );
  }

  @Test public void dynamicTemplateDynamic()
    throws Exception
  {
    nestingTest( DYNAMIC_TEMPLATE_DYNAMIC, 2 );
  }

  @Test public void templateDynamicTemplate()
    throws Exception
  {
    nestingTest( TEMPLATE_DYNAMIC_TEMPLATE, 2 );
  }

  @Test public void dynamicQNameDynamicNamespace()
    throws Exception
  {
    nestingTest( DYNAMIC_QNAME_DYNAMIC_NAMESPACE, 1 );
  }
}
