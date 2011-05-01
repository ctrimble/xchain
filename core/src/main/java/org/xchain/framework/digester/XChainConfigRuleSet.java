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
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import org.xml.sax.Attributes;

import org.xchain.framework.util.AttributesUtil;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.ConfigContext;

/**
 * <config:config xmlns:xchain-config="http://xchain.org/config/1.0">
 *   ...
 * </config:config>
 *
 * @author Christian Trimble
 * @author Mike Moulton
 * @author Josh Kennedy
 */
public class XChainConfigRuleSet
  extends RuleSetBase
{
  public static final Logger log = LoggerFactory.getLogger( XChainConfigRuleSet.class );

  public static final String NAMESPACE_URI = "http://xchain.org/config/1.0";

  public static final String CONFIG_TAG_NAME = "config";
  public static final String MONITOR_TAG_NAME = "monitor";
  public static final String CATALOG_CACHE_SIZE_TAG_NAME = "catalog-cache-size";
  public static final String TEMPLATES_CACHE_SIZE_TAG_NAME = "templates-cache-size";
  public static final String RESOURCE_URL_TAG_NAME = "resource-base-url";
  public static final String SOURCE_URL_TAG_NAME = "source-base-url";
  public static final String WEBAPP_URL_TAG_NAME = "webapp-base-url";
  public static final String SYSTEM_ID_ATTRIBUTE = "system-id";

  public XChainConfigRuleSet()
  {
    this.namespaceURI = NAMESPACE_URI;
  }

  public void addRuleInstances(Digester digester)
  {
    if( log.isDebugEnabled() ) {
      log.debug("Adding xchain config rules to digester.");
    }

    // set up the namespace in the digester.
    digester.setNamespaceAware(true);
    digester.setRuleNamespaceURI(namespaceURI);

    // set the rules for the url factory tag
    digester.addRule( CONFIG_TAG_NAME, new ConfigLoadRule() );
    digester.addRule( CONFIG_TAG_NAME + "/" + CATALOG_CACHE_SIZE_TAG_NAME, new CatalogCacheSizeRule() );
    digester.addRule( CONFIG_TAG_NAME + "/" + TEMPLATES_CACHE_SIZE_TAG_NAME, new TemplatesCacheSizeRule() );
    digester.addRule( CONFIG_TAG_NAME + "/" + MONITOR_TAG_NAME, new MonitorRule() );
    digester.addRule( CONFIG_TAG_NAME + "/" + RESOURCE_URL_TAG_NAME, new ResourceUrlRule() );
    digester.addRule( CONFIG_TAG_NAME + "/" + SOURCE_URL_TAG_NAME, new SourceUrlRule() );
    digester.addRule( CONFIG_TAG_NAME + "/" + WEBAPP_URL_TAG_NAME, new WebappUrlRule() );
  }

  public static class ConfigLoadRule
    extends Rule
  {
    public void begin( String namespaceUri, String name, Attributes attributes )
      throws Exception
    {
    }

    public void end( String namespaceUri, String name )
    {
    }
  }

  public static class MonitorRule
    extends Rule
  {
    public void body( String namespaceUri, String name, String body )
      throws Exception
    {
      if( body != null && !"".equals( body ) ) {
        ConfigContext context = Lifecycle.getLifecycleContext().getConfigContext();
        context.setMonitored( Boolean.parseBoolean( body ) );
      }
    }
  }

  public static class CatalogCacheSizeRule
    extends Rule
  {
    public void body( String namespaceUri, String name, String body )
      throws Exception
    {
      if( body != null && !"".equals( body ) ) {
        ConfigContext context = Lifecycle.getLifecycleContext().getConfigContext();
        try {
          context.setCatalogCacheSize( Integer.parseInt( body ) );
        }
        catch ( NumberFormatException e ) {
          if( log.isWarnEnabled() ) {
            log.warn("Unable to parse '" + CATALOG_CACHE_SIZE_TAG_NAME + "' value of '" + body + "'", e);
          }
        }
      }
    }
  }

  public static class TemplatesCacheSizeRule
    extends Rule
  {
    public void body( String namespaceUri, String name, String body )
      throws Exception
    {
      if( body != null && !"".equals( body ) ) {
        ConfigContext context = Lifecycle.getLifecycleContext().getConfigContext();
        try {
          context.setTemplatesCacheSize( Integer.parseInt( body ) );
        }
        catch ( NumberFormatException e ) {
          if( log.isWarnEnabled() ) {
            log.warn("Unable to parse '" + TEMPLATES_CACHE_SIZE_TAG_NAME + "' value of '" + body + "'", e);
          }
        }
      }
    }
  }

  public static class ResourceUrlRule
    extends Rule
  {
    public void begin( String namespaceUri, String name, Attributes attributes )
      throws Exception
    {
      String systemId = AttributesUtil.getAttribute( attributes, NAMESPACE_URI, SYSTEM_ID_ATTRIBUTE );
      if( systemId != null && !"".equals(systemId) ) {
        ConfigContext context = Lifecycle.getLifecycleContext().getConfigContext();
        context.getResourceUrlList().add(new URL(systemId));
      }
    }

    public void end( String namespaceUri, String name )
    {
    }
  }

  public static class SourceUrlRule
    extends Rule
  {
    public void begin( String namespaceUri, String name, Attributes attributes )
      throws Exception
    {
      String systemId = AttributesUtil.getAttribute( attributes, NAMESPACE_URI, SYSTEM_ID_ATTRIBUTE );
      if( systemId != null && !"".equals(systemId) ) {
        ConfigContext context = Lifecycle.getLifecycleContext().getConfigContext();
        context.getSourceUrlList().add(new URL(systemId));
      }
    }

    public void end( String namespaceUri, String name )
    {
    }
  }

  public static class WebappUrlRule
    extends Rule
  {
    public void begin( String namespaceUri, String name, Attributes attributes )
      throws Exception
    {
      String systemId = AttributesUtil.getAttribute( attributes, NAMESPACE_URI, SYSTEM_ID_ATTRIBUTE );
      if( systemId != null && !"".equals(systemId) ) {
        ConfigContext context = Lifecycle.getLifecycleContext().getConfigContext();
        context.getWebappUrlList().add(new URL(systemId));
      }
    }

    public void end( String namespaceUri, String name )
    {
    }
  }

}
