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

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import org.apache.commons.digester.RuleSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class RuleSetRegistry
{
  public static Logger log = LoggerFactory.getLogger( RuleSetRegistry.class );

  public static RuleSetRegistry instance = new RuleSetRegistry();

  static {
    // load all of the rules defined in the classpath.
    try {
      new RuleSetRegistryConfigurator().configure();
    }
    catch( Exception e ) {
      if( log.isWarnEnabled() ) {
        log.warn("Could not load all rule sets defined in the classpath.", e);
      }
    }
  }

  public static RuleSetRegistry getInstance()
  {
    return instance;
  }

  protected Map<String, RuleSet> ruleSetMap = Collections.synchronizedMap(new HashMap<String, RuleSet>());

  public void addRuleSet( RuleSet ruleSet )
  {
    getRuleSetMap().put( ruleSet.getNamespaceURI(), ruleSet );
  }

  public Map<String, RuleSet> getRuleSetMap()
  {
    return ruleSetMap;
  }

  public RuleSet getRuleSet( String namespace )
  {
    return (RuleSet)getRuleSetMap().get(namespace);
  }
}
