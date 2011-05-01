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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ext.LexicalHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The SaxEventRecorder is a DefaultHandler implementation that will simply record all incoming SAX events.  The events are stored as SaxEvent objects.
 * The EventList is in the same order as the original SAX events.  By default no SAX events are tracked.  Types of event tracking can be enabled with {@link #setTrackDocumentEvents(boolean)},
 * {@link #setTrackElementEvents(boolean)}, {@link #setTrackCharactersEvents(boolean)}, and {@link #setTrackPrefixMappingEvents(boolean)}.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class SaxEventRecorder
  extends DefaultHandler
  implements LexicalHandler
{
  /** The list of recorded SAX events. */
  protected List<SaxEvent> eventList = new ArrayList<SaxEvent>();
  /** Whether document events should be tracked. */
  protected boolean trackDocumentEvents = false;
  /** Whether element events should be tracked. */
  protected boolean trackElementEvents = false;
  /** Whether character events should be tracked. */
  protected boolean trackCharactersEvents = false;
  /** Whether prefix mapping events should be tracked. */
  protected boolean trackPrefixMappingEvents = false;
  /** Whether comment events should be tracked. */
  protected boolean trackCommentEvents = false;

  /**
   * @return The recorded list of SAX events.
   */
  public List<SaxEvent> getEventList() { return eventList; }
  
  /** 
   * Set whether document SAX events should be tracked.
   */
  public void setTrackDocumentEvents( boolean trackDocumentEvents ) { this.trackDocumentEvents = trackDocumentEvents; }
  
  /**
   * Set whether element SAX events should be tracked.
   */
  public void setTrackElementEvents( boolean trackElementEvents ) { this.trackElementEvents = trackElementEvents; }
  
  /**
   * Set whether character SAX events should be tracked.
   */
  public void setTrackCharactersEvents( boolean trackCharactersEvents ) { this.trackCharactersEvents = trackCharactersEvents; }
  
  /**
   * Set whether prefix mapping events should be tracked.
   */
  public void setTrackPrefixMappingEvents( boolean trackPrefixMappingEvents ) { this.trackPrefixMappingEvents = trackPrefixMappingEvents; }

  public void setTrackCommentEvents( boolean trackCommentEvents ) { this.trackCommentEvents = trackCommentEvents; }

  public void startDocument()
  {
    if( trackDocumentEvents ) {
      SaxEvent saxEvent = new SaxEvent();
      saxEvent.setType(EventType.START_DOCUMENT);
      eventList.add(saxEvent);
    }
  }

  public void endDocument()
  {
    if( trackDocumentEvents ) {
      SaxEvent saxEvent = new SaxEvent();
      saxEvent.setType(EventType.END_DOCUMENT);
      eventList.add(saxEvent);
    }
  }

  public void startElement( String uri, String localName, String qName, Attributes attributes )
  {
    if( trackElementEvents ) {
      SaxEvent saxEvent = new SaxEvent();
      saxEvent.setType(EventType.START_ELEMENT);
      saxEvent.setUri(uri);
      saxEvent.setLocalName(localName);
      saxEvent.setQName(qName);
      saxEvent.setAttributes(new AttributesImpl(attributes));
      eventList.add(saxEvent);
    }
  }

  public void endElement( String uri, String localName, String qName )
  {
    if( trackElementEvents ) {
      SaxEvent saxEvent = new SaxEvent();
      saxEvent.setType(EventType.END_ELEMENT);
      saxEvent.setUri(uri);
      saxEvent.setLocalName(localName);
      eventList.add(saxEvent);
    }
  }

  public void characters( char[] characters, int start, int length )
  {
    if( trackCharactersEvents ) {
      // if the previous event was characters, then append.
      if( !eventList.isEmpty() && eventList.get(eventList.size()-1).getType() == EventType.CHARACTERS ) {
        SaxEvent saxEvent = eventList.get(eventList.size()-1);
        saxEvent.setText(new StringBuilder().append(saxEvent.getText()).append( characters, start, length ).toString());
      }
      else {
        SaxEvent saxEvent = new SaxEvent();
        saxEvent.setType(EventType.CHARACTERS);
        saxEvent.setText(new StringBuilder().append( characters, start, length ).toString());
        eventList.add(saxEvent);
      }
    }
  }

  public void startPrefixMapping( String prefix, String uri )
  {
    if( trackPrefixMappingEvents ) {
      if( !eventList.isEmpty() && eventList.get(eventList.size()-1).getType() == EventType.START_PREFIX_MAPPING ) {
        eventList.get(eventList.size()-1).getPrefixMapping().put(prefix, uri);
      }
      else {
        SaxEvent saxEvent = new SaxEvent();
        saxEvent.setType(EventType.START_PREFIX_MAPPING);
        HashMap<String, String> prefixMapping = new HashMap<String, String>();
        prefixMapping.put(prefix, uri);
        saxEvent.setPrefixMapping(prefixMapping);
        eventList.add(saxEvent);
      }
    }
  }

  public void endPrefixMapping( String prefix )
  {
    if( trackPrefixMappingEvents ) {
      if( !eventList.isEmpty() && eventList.get(eventList.size()-1).getType() == EventType.END_PREFIX_MAPPING ) {
        eventList.get(eventList.size()-1).getPrefixSet().add(prefix);
      }
      else {
        SaxEvent saxEvent = new SaxEvent();
        saxEvent.setType(EventType.END_PREFIX_MAPPING);
        Set<String> prefixSet = new HashSet<String>();
        prefixSet.add(prefix);
        saxEvent.setPrefixSet(prefixSet);
        eventList.add(saxEvent);
      }
    }
  }

  public void comment( char[] comment, int start, int length )
  {
    if( trackCommentEvents ) {
      SaxEvent saxEvent = new SaxEvent();
      saxEvent.setType(EventType.COMMENT);
      saxEvent.setText(new StringBuilder().append( comment, start, length ).toString());
      eventList.add(saxEvent);
    }
  }
  public void startDTD( String name, String publicId, String systemId ) { }
  public void endDTD() { }
  public void startCDATA() { }
  public void endCDATA() { }
  public void startEntity( String name ) { } 
  public void endEntity( String name ) { }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for( SaxEvent event : eventList ) {
      sb.append(event.toString()).append("\n");
    }
    return sb.toString();
  }

  public static enum EventType
  {
    START_DOCUMENT,
    END_DOCUMENT,
    START_PREFIX_MAPPING,
    END_PREFIX_MAPPING,
    START_ELEMENT,
    END_ELEMENT,
    CHARACTERS,
    COMMENT
  }

  /**
   * This class represents a recorded SAX event.
   */
  public static class SaxEvent
  {
    private EventType type;
    private String text;
    private String uri;
    private String localName;
    private String qName;
    private Attributes attributes;
    private Map<String, String> prefixMapping;
    private Set<String> prefixSet;

    public EventType getType() { return this.type; }
    public void setType( EventType type ) { this.type = type; }
    public String getText() { return this.text; }
    public void setText( String text ) { this.text = text; }
    public String getUri() { return uri; }
    public void setUri( String uri ) { this.uri = uri; }
    public String getLocalName() { return localName; }
    public void setLocalName( String localName ) { this.localName = localName; }
    public String getQName() { return qName; }
    public void setQName( String qName ) { this.qName = qName; }
    public Map<String, String> getPrefixMapping() { return this.prefixMapping; }
    public void setPrefixMapping( Map<String, String> prefixMapping ) { this.prefixMapping = prefixMapping; }
    public Set<String> getPrefixSet() { return this.prefixSet; }
    public void setPrefixSet( Set<String> prefixSet ) { this.prefixSet = prefixSet; }
    public Attributes getAttributes() { return this.attributes; }
    public void setAttributes( Attributes attributes ) { this.attributes = attributes; }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("  ").append(type).append("\n");
      if( uri != null ) {
        sb.append("  ").append("uri:").append(uri).append("\n");
      }
      if( localName != null ) {
        sb.append("  ").append("localName:").append(localName).append("\n");
      }
      if( prefixMapping != null ) {
        for( Map.Entry<String, String> entry : prefixMapping.entrySet() ) {
          sb.append("  ").append("prefix:").append(entry.getKey()).append(" namespace:").append(entry.getValue()).append("\n");
        }
      }
      return sb.toString();
    }
  }

}
