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

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.parsers.SAXParser;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import org.xchain.framework.net.DependencyTracker;

import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.framework.sax.SaxTemplates;
import org.xchain.framework.sax.SaxTemplatesHandler;

/**
 * ConsumerStrategy implementation for Templates.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
public class TemplatesConsumerStrategy implements ConsumerStrategy<SaxTemplates, InputSource> {
	
	public TemplatesConsumerStrategy() {
	}

	public SaxTemplates consume(String systemId,
			SourceStrategy<InputSource> sourceStrategy,
			DependencyTracker tracker)
		throws Exception
	{   
            // get the source.
	    InputSource source = sourceStrategy.getSource(systemId);

            // create the xml reader.
            // NOTE: config option needed here to pick the xml parser used to load templates.
            XMLReader reader = XmlFactoryLifecycle.newXmlReader();

            // create a templates handler.
            SaxTemplatesHandler templatesHandler = XmlFactoryLifecycle.newTemplatesHandler();
            reader.setContentHandler(templatesHandler);

            // parser the source.
            reader.parse(source);

            // return the templats object.
            return templatesHandler.getTemplates();
	}
	
}
