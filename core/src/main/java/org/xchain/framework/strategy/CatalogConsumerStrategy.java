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
package org.xchain.framework.strategy;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Catalog;
import org.xchain.framework.digester.AnnotationRuleSet;
import org.xchain.framework.sax.XChainDeclFilter;
import org.xchain.framework.jsl.SaxTemplateHandler;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * ConsumerStrategy implementation for Catalogs.
 * 
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Josh Kennedy
 *
 * @see org.xchain.Catalog
 */
public class CatalogConsumerStrategy implements ConsumerStrategy<Catalog, InputSource> {
	public static Logger log = LoggerFactory.getLogger( CatalogConsumerStrategy.class );

	public Catalog consume(String systemId, SourceStrategy<InputSource> sourceStrategy, DependencyTracker tracker)
		throws Exception
	{
		// Get the input source
		InputSource inputSource = sourceStrategy.getSource(systemId);
		
	    // create the XMLReader.
	    XMLReader reader = XmlFactoryLifecycle.newXmlReader();

            reader.setErrorHandler( new FailingErrorHandler() );

    XChainDeclFilter sourceFilter = new XChainDeclFilter();
    sourceFilter.setParent(reader);
    sourceFilter.setErrorHandler( new FailingErrorHandler() );

	    // create the jsl filter.
	    SaxTemplateHandler xmlFilter = new SaxTemplateHandler();
	    xmlFilter.setParent(sourceFilter);
            xmlFilter.setErrorHandler( new FailingErrorHandler() );

	    // create the digester, passing the jsl filter.
	    Digester digester = new Digester(xmlFilter);

	    // set the digester onto the xml filter.
	    xmlFilter.setDigester(digester);

	    // add the annotation rule set to the digester.
	    digester.addRuleSet(new AnnotationRuleSet(systemId));

	    digester.setErrorHandler( new FailingErrorHandler() );
	    
	    // get the catalog object.
	    Catalog catalog = null;

            try {
              catalog = (Catalog)digester.parse( inputSource );
            }
            catch( Exception e ) {
              if( log.isErrorEnabled() ) {
                log.error("Could not create catalog for system id '"+inputSource.getSystemId()+"'.", e);
              }
              throw e;
            }
		
		return catalog;
	}

  public static class FailingErrorHandler
    implements ErrorHandler
  {
    public void warning(SAXParseException exception)
      throws SAXException
    {
      if( log.isWarnEnabled() ) {
        log.warn("SAXParseException thrown while loading catalog.", exception);
      }
    }

    public void error(SAXParseException exception)
      throws SAXException
    {
      if( log.isErrorEnabled() ) {
        log.error("SAXParseException thrown while loading catalog.", exception);
      }
      throw exception;
    }

    public void fatalError(SAXParseException exception)
      throws SAXException
    {
      if( log.isErrorEnabled() ) {
        log.error("Fatal SAXParseException thrown while loading catalog.", exception);
      }
      throw exception;
    }

  }

}

