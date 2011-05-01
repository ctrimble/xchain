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
public class TestExcludeResultPrefixes
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/exclude-result-prefixes.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName UNUSED_NAMESPACE = new QName(JSL_NAMESPACE_URI, "unused-namespace");
  public static QName USED_NAMESPACE = new QName(JSL_NAMESPACE_URI, "used-namespace");
  public static QName DEEP_USED_NAMESPACE = new QName(JSL_NAMESPACE_URI, "deep-used-namespace");
  public static QName TEMPLATE_ELEMENT_DEEP_USED_NAMESPACE = new QName(JSL_NAMESPACE_URI, "template-element-deep-used-namespace");
  public static QName XCHAIN_ELEMENT_DEEP_USED_NAMESPACE = new QName(JSL_NAMESPACE_URI, "xchain-element-deep-used-namespace");
  public static QName ALL_NAMESPACES = new QName(JSL_NAMESPACE_URI, "all-namespaces");
  public static String TEST_1_PREFIX = "test1";
  public static String TEST_1_NAMESPACE = "http://test/1";
  public static String TEST_2_PREFIX = "test2";
  public static String TEST_2_NAMESPACE = "http://test/2";
  public static String ELEMENT_LOCAL_NAME = "element";

  protected Command command = null;

  public TestExcludeResultPrefixes()
  {
    catalogUri = CATALOG_URI;
  }

  @Test public void unusedNamespaces()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(UNUSED_NAMESPACE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, false);
    assertStartElement(eventIterator, TEST_1_NAMESPACE, "element", null);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), false);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void usedNamespaces()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(USED_NAMESPACE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, false);
    assertStartElement(eventIterator, TEST_1_NAMESPACE, "element", null);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), false);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void deepUsedNamespaces()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(DEEP_USED_NAMESPACE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    Map<String, String> deepPrefixMappings = new HashMap<String, String>();
    deepPrefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, false);
    assertStartElement(eventIterator, TEST_1_NAMESPACE, "element", null);
    assertStartPrefixMappings(eventIterator, deepPrefixMappings, false);
    assertStartElement(eventIterator, TEST_2_NAMESPACE, "element", null);
    assertEndElement(eventIterator, TEST_2_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, deepPrefixMappings.keySet(), false);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), false);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void templateElementDeepUsedNamespace()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(TEMPLATE_ELEMENT_DEEP_USED_NAMESPACE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    Map<String, String> deepPrefixMappings = new HashMap<String, String>();
    deepPrefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, false);
    assertStartElement(eventIterator, TEST_1_NAMESPACE, "element", null);
    assertStartPrefixMappings(eventIterator, deepPrefixMappings, false);
    assertStartElement(eventIterator, TEST_2_NAMESPACE, "element", null);
    assertEndElement(eventIterator, TEST_2_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, deepPrefixMappings.keySet(), false);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), false);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void xchainElementDeepUsedNamespace()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(XCHAIN_ELEMENT_DEEP_USED_NAMESPACE);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    Map<String, String> deepPrefixMappings = new HashMap<String, String>();
    deepPrefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, false);
    assertStartElement(eventIterator, TEST_1_NAMESPACE, "element", null);
    assertStartPrefixMappings(eventIterator, deepPrefixMappings, false);
    assertStartElement(eventIterator, TEST_2_NAMESPACE, "element", null);
    assertEndElement(eventIterator, TEST_2_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, deepPrefixMappings.keySet(), false);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), false);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void allNamespaces()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(ALL_NAMESPACES);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(true);
    recorder.setTrackCharactersEvents(true);

    Map<String, String> prefixMappings = new HashMap<String, String>();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    Map<String, String> deepPrefixMappings = new HashMap<String, String>();
    deepPrefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, false);
    assertStartElement(eventIterator, TEST_1_NAMESPACE, "element", null);
    assertStartPrefixMappings(eventIterator, deepPrefixMappings, false);
    assertStartElement(eventIterator, TEST_2_NAMESPACE, "element", null);
    assertEndElement(eventIterator, TEST_2_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, deepPrefixMappings.keySet(), false);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, "element");
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), false);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }
}
