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

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class Pipeline
{
  public static Logger log = LoggerFactory.getLogger( Pipeline.class );

  protected CompositeStage compositeStage = null;
  protected InputSource source = null;
  protected XMLReader reader = null;

  public CompositeStage getCompositeStage() { return this.compositeStage; }
  public void setCompositeStage( CompositeStage compositeStage ) { this.compositeStage = compositeStage; }
  public void setSource( InputSource source ) { this.source = source; }
  public void setXmlReader( XMLReader reader ) { this.reader = reader; }
  public XMLReader getXmlReader() { return reader; }

  /**
   * Executes the pipeline.  The stage list is bound to the xml reader, then the parse method is called on the xml reader, passing
   * in the source object.  This method should only be executed once, and then the pipeline should be thrown away.
   */
  public void execute()
    throws Exception
  {
    // bind the reader to the content handler.
    reader.setContentHandler(compositeStage.getContentHandler());

    // attach an error handler to the transformer handler.
    reader.setErrorHandler( new ErrorHandler() {
      public void error( SAXParseException e ) {
        log.error(e.getMessage(), e);
        throw new RuntimeException("An error was thrown while processing the document.", e);
      }

      public void fatalError( SAXParseException e ) {
        log.error(e.getMessage(), e);
        throw new RuntimeException("A fatal error was thrown while processing the document.", e);
      }

      public void warning( SAXParseException e ) {
        log.warn("SAX Parser Warning.", e);
      }
    });


    if( log.isDebugEnabled() ) {
      log.debug("Running pipeline.");
    }

    // parse the source document.
    reader.parse(source);
  }
}
