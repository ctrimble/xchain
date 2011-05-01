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
package org.xchain.framework.digester;

import org.apache.commons.digester.Digester;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

/**
 * A digester that has been extended to pass along LexicalHandler events to a custom lexical handler, if it is set.
 *
 * @author Christian Trimble
 */
public class ExtendedDigester
  extends Digester
  implements LexicalHandler
{
  protected LexicalHandler customLexicalHandler;

  public void setCustomLexicalHandler( LexicalHandler customLexicalHandler )
  {
    this.customLexicalHandler = customLexicalHandler;
  }

  public LexicalHandler getCustomLexicalHandler()
  {
    return this.customLexicalHandler;
  }

  /*
   * LexicalHandler methods.
   */

  public void comment( char[] characters, int start, int length )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.comment( characters, start, length );
    }
  }

  public void endCDATA( )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.endCDATA(  );
    }
  }

  public void endDTD(  )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.endDTD(  );
    }
  }

  public void endEntity( String name )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.endEntity( name );
    }
  }

  public void startCDATA( )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.startCDATA(  );
    }
  }

  public void startDTD( String name, String publicId, String systemId )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.startDTD( name, publicId, systemId );
    }
  }

  public void startEntity( String name )
    throws SAXException
  {
    if( customLexicalHandler != null ) {
      customLexicalHandler.startEntity( name );
    }
  }
}
