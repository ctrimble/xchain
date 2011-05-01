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
package org.xchain.framework.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.net.UrlFactory;
import org.xml.sax.Attributes;

/**
 * <p>This filter specifies URL translation for the the webapp.</p>
 * 
 * <p>The 'enabled' parameter defines whether the filter is enabled.  This is assumed to be 'true'
 * if it is not present.</p>
 * 
 * <p>The 'config-resource-url' parameter defines the location of the filter config file relative
 * to the context class loader.</p>
 *
 * <h4>Example filter element:</h4>
 *
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;UrlTranslationFilter&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.xchain.framework.filter.UrlTranslationFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;config-resource-url&lt;/param-name&gt;
 *     &lt;param-value&gt;META-INF/translation-config.xml&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;enabled&lt;/param-name&gt;
 *     &lt;param-value&gt;true&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * <h4>Example filter-mapping element:</h4>
 *
 * <pre>
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;UrlTranslationFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/redirect/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping>
 * </pre>
 * 
 * Filter configuration file is in the namespace "http://xchain.org/container/url-translation-filter-config/1.0".
 * The config can have any number of <code>entry</code> elements.  Each <code>entry</code> element must have a <code>pattern</code>
 * and a <code>location</code> attribute. The <code>pattern</code> attribute is a regular expression to match incoming requests.  If
 * the pattern matches then the request will be redirectd to the <code>location</code> attribute.  The <code>location</code> attribute
 * can contain markers in the form of ${<i>num</i>} where <i>num</i> is a group number from the matched pattern.
 * 
 * <h4>Example filter config:</h4>
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;config:config xmlns:config="http://xchain.org/container/url-translation-filter-config/1.0"&gt;
 *   &lt;config:entry pattern="\A/redirect/(.*)\Z" location="http://www.other.domain.com/${1}"/&gt;
 * &lt;/config:config&gt;
 * </pre>
 *
 * @author Devon Tackett
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
public class UrlTranslationFilter
  implements Filter
{
  // Local logger
  private static Logger log = LoggerFactory.getLogger(UrlTranslationFilter.class);

  public static String CONFIG_RESOURCE_URL_PARAM_NAME = "config-resource-url";
  public static String ENABLED_PARAM_NAME = "enabled";

  private boolean enabled = true;
  private Map<Pattern, String>translationMap = new LinkedHashMap<Pattern, String>();
  private ServletContext servletContext;

  public void init( FilterConfig filterConfig )
    throws ServletException
  {
    servletContext = filterConfig.getServletContext();
    if (filterConfig.getInitParameter(ENABLED_PARAM_NAME) != null) {
      this.enabled = Boolean.valueOf(filterConfig.getInitParameter(ENABLED_PARAM_NAME)).booleanValue();
    }
    
    // Logger whether the filter is enabled.
    if (log.isDebugEnabled()) {
      if (this.enabled)
        log.debug("UrlTranslationFilter is ENABLED");
      else
        log.debug("UrlTranslationFilter is DISABLED");
    }
    
    if (enabled) {     
      try {
        URL configResourceUrl = getConfigurationURL(filterConfig);
        
        if (configResourceUrl == null) {
          throw new Exception("Configuration file could not be found.");
        }
        
        loadConfiguration(configResourceUrl);
      } catch (Exception ex) {
        if (log.isWarnEnabled()) {
          log.warn("Failed to configure UrlTranslationFilter.", ex);
        }        
        enabled = false;
      }  
    }
  }
  
  /**
   * Load the configuration from the given URL.
   * 
   * @param configResourceUrl The URL to the configuration file.
   */
  private void loadConfiguration(URL configResourceUrl)
    throws Exception
  {
    Digester digester = new Digester();
    digester.push(this);
    new ConfigurationRuleSet().addRuleInstances(digester);
    digester.parse(configResourceUrl);
  }
  
  /**
   * Rule set for digesting the configuration file.
   */
  private static class ConfigurationRuleSet
    extends RuleSetBase
  {
    private static String CONFIG_ELEMENT = "config";
    private static String ENTRY_ELEMENT = "entry";
    
    private static String PATTERN_ATTRIBUTE_NAME = "pattern";
    private static String LOCATION_ATTRIBUTE_NAME = "location";
    
    private static String NAMESPACE_URI = "http://xchain.org/container/url-translation-filter-config/1.0";

    public void addRuleInstances( Digester digester )
    {
      digester.setNamespaceAware(true);
      digester.setRuleNamespaceURI(NAMESPACE_URI);

      digester.addRule(CONFIG_ELEMENT, new ConfigRule());
      digester.addRule(CONFIG_ELEMENT + "/" + ENTRY_ELEMENT, new EntryRule());
    }

    public static class ConfigRule
      extends Rule
    {
      @Override
      public void begin( String namespace, String name, Attributes attributes )
        throws Exception
      {
        // Ensure that the UrlTranlationFilter is on the stack.
        Object top = digester.peek();
        if (!(top instanceof UrlTranslationFilter)) {
          throw new Exception("UrlTranlationFilter not found on the digester stack.");
        }
      }
    }
    
    public static class EntryRule
      extends Rule
    {
      @Override
      public void begin(String namespace, String name, Attributes attributes)
          throws Exception {
        UrlTranslationFilter filter = (UrlTranslationFilter)digester.peek();
        // Add the translation.
        filter.addTranslation(Pattern.compile(attributes.getValue(PATTERN_ATTRIBUTE_NAME)), attributes.getValue(LOCATION_ATTRIBUTE_NAME));
      }
    }  
  }
  
  /**
   * Add a translation for the filter.
   * 
   * @param regEx The pattern to match on.
   * @param location The location to translate to.
   */
  private void addTranslation(Pattern regEx, String location) {
    translationMap.put(regEx, location);
  }
  
  /**
   * Get the URL to the configuration file from the FilterConfig.
   * 
   * @param filterConfig The FilterConfig to check.
   * 
   * @return The URL to the configuration file.
   */
  private URL getConfigurationURL(FilterConfig filterConfig)
    throws Exception
  {
    URL configResourceUrl = null;
    
    String configResourceUrlParameter = filterConfig.getInitParameter(CONFIG_RESOURCE_URL_PARAM_NAME);
    
    if (configResourceUrlParameter != null && configResourceUrlParameter.trim().length() != 0) {
      configResourceUrl = Thread.currentThread().getContextClassLoader().getResource( configResourceUrlParameter );
    } else {
        throw new Exception("Configuration is not specified.");
    }    
    
    return configResourceUrl;
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
    throws IOException, ServletException
  {
    // Perform Url Translation if enabled.
    if (this.enabled) {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      HttpServletResponse httpResponse = (HttpServletResponse)response;

      URL url = null;
      URLConnection connection = null;
      InputStream in = null;
      OutputStream out = null;
      String path = httpRequest.getServletPath();
      boolean matchFound = false;

      try {
        // Check the incoming path against the registered patterns.
        for (Pattern pattern : translationMap.keySet()) {
          Matcher match = pattern.matcher(path);
          if (match.matches()) {
            // Match found.  Get the URI the pattern translates to.
            String uri = translationMap.get(pattern);
            
            // Perform group substitution.
            for(int group = 1; group <= match.groupCount(); group++) {
              uri = uri.replaceAll("\\$[{]" + group + "[}]", match.group(group));
            }
            
            // Create a new URL from the translated uri.
            url = UrlFactory.getInstance().newUrl(uri);
            matchFound = true;
            break;
          }
        }        
        
        if (!matchFound) {
          // No match found.  Let the request go through.
          chain.doFilter(request, response);
          return;
        }

        if (log.isDebugEnabled()) {
          log.debug("doFilter: redirecting " + path + " to " + url);
        }

        // get a connection to the url.
        connection = url.openConnection();

        // set the headers.
        httpResponse.setContentLength(connection.getContentLength());
        // try to set the content type header defined in the container.
        String servletContentType = servletContext.getMimeType(path);
        if( servletContentType != null ) {
          httpResponse.setContentType(servletContentType);
        }
  
        // get the streams.
        in = connection.getInputStream();
        out = response.getOutputStream();
  
        // create buffer and length for coping.
        byte[] buffer = new byte[1024];
        int length = 0;
  
        // transfer the bytes.
        while( (length = in.read(buffer)) > 0 ) {
          out.write( buffer, 0, length );
        }
      }
      catch( MalformedURLException mue ) {
        throw new ServletException(mue);
      }
      catch( UnknownServiceException use ) {
        throw new ServletException("The protocol '"+url.getProtocol()+"' does not support input.", use);
      }
      finally {
        // close the streams.
        if( in != null ) {
          try { in.close(); }
          catch( IOException ioe ) { }
        }
        if( out != null ) {
          try { out.close(); }
          catch( IOException ioe ) { }
        }
      }
    }
    else
      chain.doFilter(request, response);

  }
  
  /**
   * @return Whether the UrlTranslationFilter is enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }

  public void destroy()
  {
    // There is no need to clean anything up.
  }
}
