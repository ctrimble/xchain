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
public class TestPrefixMapping
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/prefix-mapping.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName UNUSED_NAMESPACE_OUTSIDE = new QName(JSL_NAMESPACE_URI, "unused-namespace-outside");
  public static QName USED_NAMESPACE_OUTSIDE = new QName(JSL_NAMESPACE_URI, "used-namespace-outside");
  public static QName UNUSED_NAMESPACE_JSL_TEMPLATE = new QName(JSL_NAMESPACE_URI, "unused-namespace-jsl-template");
  public static QName USED_NAMESPACE_JSL_TEMPLATE = new QName(JSL_NAMESPACE_URI, "used-namespace-jsl-template");
  public static QName UNUSED_NAMESPACE_TEMPLATE_ELEMENT = new QName(JSL_NAMESPACE_URI, "unused-namespace-template-element");
  public static QName USED_NAMESPACE_TEMPLATE_ELEMENT = new QName(JSL_NAMESPACE_URI, "used-namespace-template-element");
  public static QName UNUSED_NAMESPACE_XCHAIN = new QName(JSL_NAMESPACE_URI, "unused-namespace-xchain");
  public static QName USED_NAMESPACE_XCHAIN = new QName(JSL_NAMESPACE_URI, "used-namespace-xchain");
  public static String TEST_1_PREFIX = "test1";
  public static String TEST_1_NAMESPACE = "http://test/1";
  public static String TEST_2_PREFIX = "test2";
  public static String TEST_2_NAMESPACE = "http://test/2";
  public static String ELEMENT_LOCAL_NAME = "element";

  protected Command command = null;

  public TestPrefixMapping()
  {
    catalogUri = CATALOG_URI;
  }

  private void executeOneLevel( QName commandName, Map prefixMappings, String elementNamespace, String elementLocalName)
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

    // check the document.
    assertStartDocument(eventIterator);
    assertStartPrefixMappings(eventIterator, prefixMappings, true);
    assertStartElement(eventIterator, elementNamespace, elementLocalName, null);
    assertEndElement(eventIterator, elementNamespace, elementLocalName);
    assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), true);
    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);
  }

  @Test public void unusedNamespaceOutside()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);

    executeOneLevel( UNUSED_NAMESPACE_OUTSIDE, prefixMappings, "", ELEMENT_LOCAL_NAME );
  }

  @Test public void usedNamespaceOutside()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);

    executeOneLevel( USED_NAMESPACE_OUTSIDE, prefixMappings, TEST_1_NAMESPACE, ELEMENT_LOCAL_NAME );
  }

  @Test public void unusedNamespaceJslTempate()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    prefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    executeOneLevel( UNUSED_NAMESPACE_JSL_TEMPLATE, prefixMappings, "", ELEMENT_LOCAL_NAME );
  }

  @Test public void usedNamespaceJslTemplate()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    prefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    executeOneLevel( USED_NAMESPACE_JSL_TEMPLATE, prefixMappings, TEST_2_NAMESPACE, ELEMENT_LOCAL_NAME );
  }

  @Test public void unusedNamespaceTemplateElement()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    prefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    executeOneLevel( UNUSED_NAMESPACE_TEMPLATE_ELEMENT, prefixMappings, "", ELEMENT_LOCAL_NAME );
  }

  @Test public void usedNamespaceTemplateElement()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    prefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    executeOneLevel( USED_NAMESPACE_TEMPLATE_ELEMENT, prefixMappings, TEST_2_NAMESPACE, ELEMENT_LOCAL_NAME );
  }

  @Test public void unusedNamespaceXChain()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    prefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    executeOneLevel( UNUSED_NAMESPACE_XCHAIN, prefixMappings, "", ELEMENT_LOCAL_NAME );
  }

  @Test public void usedNamespaceXChain()
    throws Exception
  {
    HashMap prefixMappings = new HashMap();
    prefixMappings.put(TEST_1_PREFIX, TEST_1_NAMESPACE);
    prefixMappings.put(TEST_2_PREFIX, TEST_2_NAMESPACE);

    executeOneLevel( USED_NAMESPACE_XCHAIN, prefixMappings, TEST_2_NAMESPACE, ELEMENT_LOCAL_NAME );
  }
}
