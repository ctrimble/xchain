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
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.digester.Rule;

import java.net.URL;
import java.util.Enumeration;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import org.xchain.framework.net.UrlSourceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class RuleSetRegistryConfigurator
{
  public static Logger log = LoggerFactory.getLogger(RuleSetRegistryConfigurator.class);

  public static String RULE_SET_REGISTRATION_RESOURCE = "org/xchain/digester/rule-set-registry.xml";
  public static String RULE_SET_REGISTRATION_NAMESPACE_URI = "http://xchain.org/rule-set-registry/1.0";

  public void configure()
    throws Exception
  {
    // get all of the config files for the registry.
    Enumeration<URL> registrationUrlEnumeration = getConfigurationUrlEnumeration(RULE_SET_REGISTRATION_RESOURCE);

    // for each registration document, register the configured rule sets.
    while( registrationUrlEnumeration.hasMoreElements() ) {
      URL registrationUrl = registrationUrlEnumeration.nextElement();

      if( log.isDebugEnabled() ) {
        log.debug("Loading registration file for url '"+registrationUrl.toExternalForm()+"'.");
      }

      // process the registrations defined in this registration url.
      processRegistrationUrl( registrationUrl );

      if( log.isDebugEnabled() ) {
        log.debug("Done loading registration file for url '"+registrationUrl.toExternalForm()+"'.");
      }
    }
  }

  public Enumeration<URL> getConfigurationUrlEnumeration( String resourceName )
    throws IOException
  {
    // get the enumeration of all the matching resources.
    return Thread.currentThread().getContextClassLoader().getResources( resourceName );
  }

  public void processRegistrationUrl( URL registrationUrl )
    throws Exception
  {
    // create a digester to parse the registration url.
    Digester digester = new Digester();
    digester.addRuleSet(new RegistrationRuleSet());

    // create a source for the url.
    InputSource registrationUrlSource = UrlSourceUtil.createSaxInputSource(registrationUrl);

    // parse the registration url.
    digester.parse(registrationUrlSource);
  }

  public static class RegistrationRuleSet
    extends RuleSetBase
  {
    public static String REGISTRY_ELEMENT = "registry";
    public static String RULE_SET_ELEMENT = "rule-set";

    public static String CLASS_NAME_ATTRIBUTE = "class-name";

    public RegistrationRuleSet()
    {
      this.namespaceURI = RULE_SET_REGISTRATION_NAMESPACE_URI;
    }

    public void addRuleInstances( Digester digester )
    {
      digester.setNamespaceAware(true);
      digester.setRuleNamespaceURI(getNamespaceURI());

      digester.addRule(REGISTRY_ELEMENT, new LoadRuleSetRegistryRule() );

      String ruleSetPath = REGISTRY_ELEMENT+"/"+RULE_SET_ELEMENT;
      digester.addRule(ruleSetPath, new RegisterRuleSetRule());
    }

    public static class LoadRuleSetRegistryRule
      extends Rule
    {
      public void begin( String namespace, String name, Attributes attributes )
        throws Exception
      {
        RuleSetRegistry registry = RuleSetRegistry.getInstance();

        // push the registry on the top of the stack.
        digester.push(registry);
      }
    }

    public static class RegisterRuleSetRule
      extends Rule
    {
      public void begin( String namespace, String name, Attributes attributes )
        throws Exception
      {
        String className = getValue( attributes, "", CLASS_NAME_ATTRIBUTE );

        if( className == null ) {
          throw new Exception("The class name is a required attribute for rule-set tags.");
        }

        // create an instance of the class specified for the rule set.
        Object object = Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();

        // verify that the class is an instance of RuleSet.
        if( !( object instanceof RuleSet ) ) {
          throw new Exception("The class-name attribute of rule-set must specify a subclass of org.apache.commons.digester.RuleSet.");
        }

        // cast the created class to a rule set.
        RuleSet ruleSet = (RuleSet)object;

        // get the rule set registry from the digester.
        RuleSetRegistry registry = (RuleSetRegistry)digester.peek();

        // add the rule set to the registry.
        registry.addRuleSet(ruleSet);
        
      }
    }
  }

  public static String getValue( Attributes attributes, String namespace, String name )
  {
    String value = null;

    int index = attributes.getIndex( namespace, name );

    if( index >= 0 ) {
      value = attributes.getValue(index);
    }

    return value;
  }
}
