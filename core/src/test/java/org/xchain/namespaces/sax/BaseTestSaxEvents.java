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
package org.xchain.namespaces.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import javax.xml.transform.sax.SAXResult;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xchain.Catalog;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.sax.SaxEventRecorder;
import org.xml.sax.Attributes;

/**
 * @author Christian Trimble
 */
public abstract class BaseTestSaxEvents
{
  protected String catalogUri = null;
  protected JXPathContext context = null;
  protected SAXResult result = null;
  protected SaxEventRecorder recorder = null;
  protected Catalog catalog = null;
  
  @BeforeClass public static void setUpLifecycle()
  	throws Exception
  {
	  Lifecycle.startLifecycle();	  
  }

  @AfterClass public static void tearDownLifecycle()
  	throws Exception
  {
	  Lifecycle.stopLifecycle();  
  }   

  @Before public void setUp()
    throws Exception
  {
    context = JXPathContext.newContext(new Object());
    recorder = new SaxEventRecorder();
    result = new SAXResult(recorder);
    context.getVariables().declareVariable("result", result);
    context.getVariables().declareVariable("executed", Boolean.FALSE);
    catalog = CatalogFactory.getInstance().getCatalog(catalogUri);
  }

  @After public void tearDown()
    throws Exception
  {
    context = null;
    recorder = null;
    result = null;
  }

  public void assertStartDocument(Iterator<SaxEventRecorder.SaxEvent> eventIterator)
    throws Exception
  {
    assertTrue("There is not a start document event", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not a start document event.", SaxEventRecorder.EventType.START_DOCUMENT, event.getType());
  }

  public void assertEndDocument(Iterator<SaxEventRecorder.SaxEvent> eventIterator)
    throws Exception
  {
    assertTrue("There is not an end document event", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not an end document event.", SaxEventRecorder.EventType.END_DOCUMENT, event.getType());
  }

  public void assertStartElement(Iterator<SaxEventRecorder.SaxEvent> eventIterator, String uri, String localName, Attributes attributes)
    throws Exception
  {
    assertTrue("There is not a start element event.", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not a start element event.", SaxEventRecorder.EventType.START_ELEMENT, event.getType());
    assertEquals("The element local name is not correct.", localName, event.getLocalName());
    assertEquals("The element uri is not correct.", uri, event.getUri());

    // only check the attributes if they are not null.
    if( attributes != null ) {
      assertEquals("The element has the wrong number of attributes.", attributes.getLength(), event.getAttributes().getLength());
      for( int i = 0; i < event.getAttributes().getLength(); i++ ) {
        int attributesIndex = attributes.getIndex(event.getAttributes().getURI(i), event.getAttributes().getLocalName(i));
        assertTrue("The attribute does not contain the attribute {"+event.getAttributes().getURI(i)+"}"+event.getAttributes().getLocalName(i)+".", -1 != attributesIndex);
        assertEquals("The attribute {"+event.getAttributes().getURI(i)+"}"+event.getAttributes().getLocalName(i)+" has the wrong value.", attributes.getValue(attributesIndex), event.getAttributes().getValue(i));
      }
    }
  }

  public void assertEndElement(Iterator<SaxEventRecorder.SaxEvent> eventIterator, String uri, String localName )
    throws Exception
  {
    assertTrue("There is not an end element event.", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not an end element event.", SaxEventRecorder.EventType.END_ELEMENT, event.getType());
    assertEquals("The element local name is not correct.", localName, event.getLocalName());
    assertEquals("The element uri is not correct.", uri, event.getUri());
  }

  public void assertCharacters(Iterator<SaxEventRecorder.SaxEvent> eventIterator, String text )
    throws Exception
  {
    assertTrue("There is not an end element event.", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not an end element event.", SaxEventRecorder.EventType.CHARACTERS, event.getType());
    assertEquals("The characters element does not have the correct value.", text, event.getText());
  }

  public void assertNoMoreEvents(Iterator<SaxEventRecorder.SaxEvent> eventIterator)
    throws Exception
  {
    assertFalse("There were extra document events.", eventIterator.hasNext());
  }
}
