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
package org.xchain.framework.factory;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Catalog;
import org.xchain.CatalogLoadException;
import org.xchain.CatalogNotFoundException;
import org.xchain.framework.strategy.CachingLoadStrategy;
import org.xchain.framework.strategy.CatalogConsumerStrategy;
import org.xchain.framework.strategy.ConsumerStrategy;
import org.xchain.framework.strategy.InputSourceSourceStrategy;
import org.xchain.framework.strategy.LoadStrategy;
import org.xchain.framework.strategy.SourceStrategy;
import org.xml.sax.InputSource;

/**
 * The CatalogFactory is used to load catalogs.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Josh Kennedy
 */
public class CatalogFactory {
	private final LoadStrategy<Catalog,InputSource> loadStrategy;
	private SourceStrategy<InputSource> sourceStrategy = null;	
	private ConsumerStrategy<Catalog, InputSource> consumerStrategy = null;
	
	public static Logger log = LoggerFactory.getLogger( CatalogFactory.class );
	private static final CatalogFactory instance = new CatalogFactory();
	
	public static final CatalogFactory getInstance() {
		return instance;
	}
	
	private CatalogFactory() {
		loadStrategy = new CachingLoadStrategy<Catalog, InputSource>(200);
		sourceStrategy = new InputSourceSourceStrategy();
		consumerStrategy = new CatalogConsumerStrategy();
	}	
	
	/**
	 * Retrieve the catalog for the given systemId.
	 * 
	 * @param systemId The id of the catalog to load.
	 * 
	 * @return The catalog for the given systemId.  This will never return null.
	 * 
	 * @throws CatalogNotFoundException If no catalog could be found for the given systemId.
	 * @throws CatalogLoadException If the catalog was found, but an error was encountered while loading the catalog.
	 */
	public Catalog getCatalog(String systemId) 
		throws CatalogNotFoundException, CatalogLoadException 
	{
		Catalog catalog = null;

		try {
			catalog = loadStrategy.getObject(systemId, sourceStrategy, consumerStrategy);
		} catch (MalformedURLException mue) {
                  // The catalog url is invalid.
                  if (log.isDebugEnabled()) {
                    log.debug("Could not find catalog '" + systemId + "'.", mue);
                  } 		  
                  throw new CatalogNotFoundException("The catalog uri '"+systemId+"' is malformed.", mue);
		} catch (FileNotFoundException fnf) {
		  // The catalog could not be found.
                  if (log.isDebugEnabled()) {
                    log.debug("Could not find catalog '" + systemId + "'.", fnf);
                  }		  
                  throw new CatalogNotFoundException("The catalog uri '"+systemId+"' was not found.", fnf);
		} catch (Exception ex) {
		  // Any other exception related to not finding the catalog.
			if (log.isDebugEnabled()) {
				log.debug("Failed to load catalog '" + systemId + "' due to an exception.", ex);
			}
			
			throw new CatalogLoadException("Failed to load catalog '" + systemId + "' due to an exception.", ex);
		}

		if (catalog == null)
			throw new CatalogNotFoundException();
		
		return catalog;
	}
}

