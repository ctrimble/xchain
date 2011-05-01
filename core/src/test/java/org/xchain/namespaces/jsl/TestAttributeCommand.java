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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.Assert.fail;

/**
 * @author Christian Trimble
 */
public class TestAttributeCommand
  extends BaseTestSaxEvents
{
  public static String CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/jsl/attribute.xchain";
  public static String JSL_NAMESPACE_URI = "http://www.xchain.org/jsl/1.0";
  public static QName TEMPLATE_ATTRIBUTE = new QName(JSL_NAMESPACE_URI, "template-attribute");
  public static QName ELEMENT_ATTRIBUTE = new QName(JSL_NAMESPACE_URI, "element-attribute");
  public static QName NO_PREFIX_WITH_MAPPING = new QName(JSL_NAMESPACE_URI, "no-prefix-with-mapping");
  public static QName NO_PREFIX_WITHOUT_MAPPING = new QName(JSL_NAMESPACE_URI, "no-prefix-without-mapping");
  public static QName CONFLICTING_PREFIX_DECL_WITH_PARENT = new QName(JSL_NAMESPACE_URI, "conflicting-prefix-decl-with-parent");
  public static QName CONFLICTING_PREFIX_DECL_WITH_ATTRIBUTE = new QName(JSL_NAMESPACE_URI, "conflicting-prefix-decl-with-attribute");
  public static String TEST_1_PREFIX = "test1";
  public static String TEST_1_NAMESPACE = "http://test/1";
  public static String TEST_2_PREFIX = "test2";
  public static String TEST_2_NAMESPACE = "http://test/2";
  public static String ELEMENT_LOCAL_NAME = "element";

  protected Command command = null;

  public TestAttributeCommand()
  {
    catalogUri = CATALOG_URI;
  }

  /**
   * Tests deep documents that have alternating prefixes.
   */
  private void attributeTest( QName commandName, int depth, boolean withNamespaces )
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(commandName);

    // track document and element events.
    recorder.setTrackDocumentEvents(true);
    recorder.setTrackElementEvents(true);
    recorder.setTrackPrefixMappingEvents(withNamespaces);
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

      if( withNamespaces ) {
        prefixMappings.put("test", namespace);
        assertStartPrefixMappings(eventIterator, prefixMappings, true);
        prefixMappings.clear();
      }
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute(namespace, "attribute", "test:attribute", "CDATA", "this is the attribute value");
      assertStartElement(eventIterator, "", ELEMENT_LOCAL_NAME, attributes);
    }

    for( int i = depth; i >= 0; i-- ) {
      String namespace = i % 2 == 0 ? TEST_1_NAMESPACE : TEST_2_NAMESPACE;

      assertEndElement(eventIterator, "", ELEMENT_LOCAL_NAME);

      if( withNamespaces ) {
        // test the prefix mappings ending.
        prefixMappings.put("test", namespace);
        assertEndPrefixMappings(eventIterator, prefixMappings.keySet(), true);
        prefixMappings.clear();
      }
    }

    assertEndDocument(eventIterator);
    assertNoMoreEvents(eventIterator);

  }

  private void attributeExceptionTest( QName commandName )
    throws Exception
  {
    try {
      // get the command.
      Command command = catalog.getCommand(commandName);

      // track document and element events.
      recorder.setTrackDocumentEvents(true);
      recorder.setTrackElementEvents(true);
      recorder.setTrackPrefixMappingEvents(true);
      recorder.setTrackCharactersEvents(true);

      // execute the command.
      command.execute(context);

      fail("The command "+commandName+" should have caused an Exception to be thrown.");
    }
    catch( Exception e ) {
    }
  }

  @Test public void templateAttribute()
    throws Exception
  {
    attributeTest( TEMPLATE_ATTRIBUTE, 0, true );
  }

  @Test public void elementAttribute()
    throws Exception
  {
    attributeTest( ELEMENT_ATTRIBUTE, 0, true );
  }

  @Test public void noPrefixWithoutMapping()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NO_PREFIX_WITHOUT_MAPPING);

    // track document and element events.
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "attribute", "attribute", "CDATA", "this is the attribute value");
    assertStartElement(eventIterator, "", ELEMENT_LOCAL_NAME, attributes);
    assertEndElement(eventIterator, "", ELEMENT_LOCAL_NAME);
  }

  @Test public void noPrefixWithMapping()
    throws Exception
  {
    // get the command.
    Command command = catalog.getCommand(NO_PREFIX_WITH_MAPPING);

    // track document and element events.
    recorder.setTrackElementEvents(true);

    // execute the command.
    command.execute(context);

    Iterator<SaxEventRecorder.SaxEvent> eventIterator = recorder.getEventList().iterator();
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "attribute", "attribute", "CDATA", "this is the attribute value");
    assertStartElement(eventIterator, TEST_1_NAMESPACE, ELEMENT_LOCAL_NAME, attributes);
    assertEndElement(eventIterator, TEST_1_NAMESPACE, ELEMENT_LOCAL_NAME);
  }

  @Test public void conflictingPrefixDeclWithParent()
    throws Exception
  {
    attributeExceptionTest(CONFLICTING_PREFIX_DECL_WITH_PARENT);
  }

  @Test public void conflictingPrefixDeclWithAttribute()
    throws Exception
  {
    attributeExceptionTest(CONFLICTING_PREFIX_DECL_WITH_ATTRIBUTE);
  }
}
