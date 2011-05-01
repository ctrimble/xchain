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
package org.xchain.framework.lifecycle;

import java.net.URL;
import java.util.Iterator;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.net.protocol.resource.ResourceUrlConnection;
import org.xchain.framework.net.protocol.resource.ServletContextUrlTranslationStrategy;
import org.xchain.framework.net.strategy.BaseUrlUrlTranslationStrategy;
import org.xchain.framework.net.strategy.CompositeUrlTranslationStrategy;

/**
 * @author John Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@LifecycleClass(uri="http://www.xchain.org/framework/lifecycle")
public class ContainerLifecycle {
  public static final Logger log = LoggerFactory.getLogger( ContainerLifecycle.class );
  public static final String SERVLET_CONTEXT_ATHORITY = "servlet-context";

  private static ServletContext servletContext = null;
  
  public static void setServletContext( ServletContext context ) {
    servletContext = context;
  }

  public static ServletContext getServletContext() {
    return servletContext;
  }

  /**
   * Setup Servlet Context resource access.
   * @param context
   */
  @StartStep(localName="servlet", after="config")
  public static void startLifecycle( LifecycleContext context ) 
  {
    ConfigContext configContext = context.getConfigContext();

    // configure the servlet-context authority in the URLFactory
    if( ContainerLifecycle.getServletContext() != null ) {

      ServletContextUrlTranslationStrategy.setServletContext( ContainerLifecycle.getServletContext() );
      CompositeUrlTranslationStrategy servletContextStrategy = new CompositeUrlTranslationStrategy();

      // configure the URLFactory for file monitoring if requested
      if( configContext.isMonitored() ) {

        if( log.isDebugEnabled() ) {
          log.debug( "ServletStep: Monitoring is enabled, configuring URL translation strategies..." );
        }

        // configure the resource protocol 'servlet-context' authority for monitoring if available
        if( !configContext.getWebappUrlList().isEmpty() ) {
          for( Iterator<URL> it = configContext.getWebappUrlList().iterator(); it.hasNext(); ) {
            URL baseUrl = it.next();
            BaseUrlUrlTranslationStrategy baseUrlStrategy =
              new BaseUrlUrlTranslationStrategy( baseUrl, BaseUrlUrlTranslationStrategy.URL_FACTORY_URL_SOURCE );
            servletContextStrategy.getTranslatorList().add( baseUrlStrategy );
            if( log.isDebugEnabled() ) {
              log.debug( "    Adding webapp URL: " + baseUrl );
            }
          }
        }
      }

      // now add the servlet context strategy
      servletContextStrategy.getTranslatorList().add( new ServletContextUrlTranslationStrategy() );

      ResourceUrlConnection.registerUrlTranslationStrategy( SERVLET_CONTEXT_ATHORITY,
                                                            servletContextStrategy );
    }
  }
  
  @StopStep(localName="servlet")
  public static void stopLifecycle( LifecycleContext context ) 
  {
    // cleanup the servlet-context authority in the URLFactory
    if( ContainerLifecycle.getServletContext() != null ) {
      ResourceUrlConnection.getUrlTranslationStrategyMap().keySet().remove( SERVLET_CONTEXT_ATHORITY );
      ServletContextUrlTranslationStrategy.setServletContext( null );
      ContainerLifecycle.setServletContext( null );
    }
  }
}
