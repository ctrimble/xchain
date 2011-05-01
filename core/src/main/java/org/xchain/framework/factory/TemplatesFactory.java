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

import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xchain.framework.strategy.CachingLoadStrategy;
import org.xchain.framework.strategy.LoadStrategy;
import org.xchain.framework.strategy.SourceStrategy;
import org.xchain.framework.strategy.TemplatesConsumerStrategy;
import org.xchain.framework.strategy.InputSourceSourceStrategy;

import org.xchain.framework.sax.SaxTemplates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

/**
 * The TemplatesFactory is used to load Templates.  These templates are cached but not reloading.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Josh Kennedy
 */
public class TemplatesFactory {
	public static Logger log = LoggerFactory.getLogger( TemplatesFactory.class );	
	private static final TemplatesFactory instance = new TemplatesFactory();
	private final LoadStrategy<SaxTemplates,InputSource> loadStrategy;
	
	public static final TemplatesFactory getInstance() {
		return instance;
	}
	
	private TemplatesFactory() {
		loadStrategy = new CachingLoadStrategy<SaxTemplates, InputSource>(200);
	}
	
	/**
	 * Retrieve the Templates for the given systemId.  The templates object will be wrapped with a reloading wrapper, if reloading is on.
	 * 
	 * @param systemId The id of the Templates to load. 
	 * @param sourceStrategy The strategy to create sources to load the Templates from.
	 * @param consumerStrategy The strategy for turning Templates sources into Templates objects.
	 * 
	 * @return The Templates object for the given id.
	 */
	public SaxTemplates getTemplates(String systemId, SourceStrategy<InputSource> sourceStrategy, TemplatesConsumerStrategy consumerStrategy)
		throws Exception
	{
                // the cached templates object.
		return new ReloadingSaxTemplates(systemId, sourceStrategy, consumerStrategy);
	}

        /**
         * Loads a templates object from a standard stream source.
         */
        public SaxTemplates getTemplates(String systemId)
          throws Exception
        {
          return new ReloadingSaxTemplates( systemId, new InputSourceSourceStrategy(), new TemplatesConsumerStrategy() );
        }

  private class ReloadingSaxTemplates
    implements SaxTemplates
  {
    private String systemId;
    private SourceStrategy<InputSource> sourceStrategy;
    private TemplatesConsumerStrategy consumerStrategy;

    public ReloadingSaxTemplates( String systemId, SourceStrategy<InputSource> sourceStrategy, TemplatesConsumerStrategy consumerStrategy )
    {
      this.systemId = systemId;
      this.sourceStrategy = sourceStrategy;
      this.consumerStrategy = consumerStrategy;
    }

    public Transformer newTransformer()
      throws TransformerConfigurationException
    {
      try {
        return loadStrategy.getObject(systemId, sourceStrategy, consumerStrategy).newTransformer();
      }
      catch( TransformerConfigurationException tce ) {
        throw tce;
      }
      catch( Exception e ) {
        throw new TransformerConfigurationException("Could not create transformer for system id '"+systemId+"'.", e);
      }
    }

    public Properties getOutputProperties()
    {
      try {
        return loadStrategy.getObject(systemId, sourceStrategy, consumerStrategy).getOutputProperties();
      }
      catch( Exception e )
      {
        throw new RuntimeException("Could not access templates for system id '"+systemId+"'.", e);
      }
    }

    public TransformerHandler newTransformerHandler()
      throws TransformerConfigurationException
    {
      try {
        return loadStrategy.getObject(systemId, sourceStrategy, consumerStrategy).newTransformerHandler();
      }
      catch( TransformerConfigurationException tce ) {
        throw tce;
      }
      catch( Exception e ) {
        log.error("Could not create transformer handler for system id '"+systemId+"'.", e);
        throw new TransformerConfigurationException("Could not create transformer handler for system id '"+systemId+"'.", e);
      }
    }
  }

}
