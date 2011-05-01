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

import java.util.Iterator;
import org.xml.sax.Attributes;
import static org.junit.Assert.*;

/**
 * @author Christian Trimble
 */
public class SaxEventRecorderAssert
{
  public static void assertStartDocument(Iterator<SaxEventRecorder.SaxEvent> eventIterator)
    throws Exception
  {
    assertTrue("There is not a start document event", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not a start document event.", SaxEventRecorder.EventType.START_DOCUMENT, event.getType());
  }

  public static void assertEndDocument(Iterator<SaxEventRecorder.SaxEvent> eventIterator)
    throws Exception
  {
    assertTrue("There is not an end document event", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not an end document event.", SaxEventRecorder.EventType.END_DOCUMENT, event.getType());
  }

  public static void assertStartElement(Iterator<SaxEventRecorder.SaxEvent> eventIterator, String uri, String localName, Attributes attributes)
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

  public static void assertEndElement(Iterator<SaxEventRecorder.SaxEvent> eventIterator, String uri, String localName )
    throws Exception
  {
    assertTrue("There is not an end element event.", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not an end element event.", SaxEventRecorder.EventType.END_ELEMENT, event.getType());
    assertEquals("The element local name is not correct.", localName, event.getLocalName());
    assertEquals("The element uri is not correct.", uri, event.getUri());
  }

  public static void assertCharacters(Iterator<SaxEventRecorder.SaxEvent> eventIterator, String text )
    throws Exception
  {
    assertTrue("There is not an end element event.", eventIterator.hasNext());
    SaxEventRecorder.SaxEvent event = eventIterator.next();
    assertEquals("There was not an end element event.", SaxEventRecorder.EventType.CHARACTERS, event.getType());
    assertEquals("The characters element does not have the correct value.", text, event.getText());
  }

  public static void assertNoMoreEvents(Iterator<SaxEventRecorder.SaxEvent> eventIterator)
    throws Exception
  {
    assertFalse("There were extra document events.", eventIterator.hasNext());
  }
}

