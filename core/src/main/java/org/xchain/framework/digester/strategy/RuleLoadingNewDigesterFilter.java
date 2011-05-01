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
package org.xchain.framework.digester.strategy;

import java.util.Map;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.digester.RuleSetRegistry;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class RuleLoadingNewDigesterFilter
  extends AbstractNewDigesterFilter
{
  public static Logger log = LoggerFactory.getLogger(RuleLoadingNewDigesterFilter.class);

  public Digester newDigester( XMLReader xmlReader )
    throws Exception
  {
    // create the namespace xml filter.
    NamespaceXmlFilter filter = new NamespaceXmlFilter();
    filter.setParent(xmlReader);

    // create the digester.
    Digester digester = getParent().newDigester(filter);

    // set the digester on the namespace filter.
    filter.setDigester(digester);

    // return the digester.
    return digester;
  }

  public class NamespaceXmlFilter
    extends XMLFilterImpl
  {
    protected Digester digester = null;
    protected Map<String, Boolean> definedRuleSetMap = new HashMap<String, Boolean>();

    public void setDigester( Digester digester ) { this.digester = digester; }
    public Digester getDigester() { return digester; }

    public void startElement( String namespace, String name, String qName, Attributes attributes )
      throws SAXException
    {
      defineRules(namespace);

      for( int i = 0; i < attributes.getLength(); i++ ) {
        defineRules(attributes.getURI(i));
      }

      super.startElement( namespace, name, qName, attributes );
    }

    public void defineRules( String namespace )
    {
      if( namespace != null && !definedRuleSetMap.containsKey(namespace) ) {
        // get the rule set for the namespace;
        RuleSet ruleSet = RuleSetRegistry.getInstance().getRuleSet( namespace );

        if( ruleSet != null ) {
          if( log.isDebugEnabled() ) {
            log.debug("Loading rule set for namespace '"+namespace+"'.");
          }

          // add the rules to the digester.
          digester.addRuleSet(ruleSet);
        }
        else {
          if( log.isDebugEnabled() ) {
            log.debug("Could not load rule set for namespace '"+namespace+"'.");
          }
        }

        // mark the namespace as defined.
        definedRuleSetMap.put( namespace, Boolean.TRUE );
      }
    }
  }
}
