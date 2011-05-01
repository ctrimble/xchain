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
package org.xchain.framework.servlet;

import static java.lang.String.format;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.xchain.framework.lifecycle.ContainerLifecycle;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.NOPCCLPolicy;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.framework.osgi.OSGiCCLPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet listener to manage the XChain lifecycle
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
public class XChainListener
  implements ServletContextListener
{
  public static Logger log = LoggerFactory.getLogger( XChainListener.class );
  public static final String CCL_POLICY_PARAM = "ccl-policy";
  
  /**
   * Starts the XChain lifecycle for use in a servlet environment
   */
  public void contextInitialized(ServletContextEvent event)
  {
    try {
      initialBootStrappingConfig(event.getServletContext());
      
      if( log.isInfoEnabled() ) {
        log.info("Starting the XChain lifecycle");
      }

      // save the servlet context for future use
      ContainerLifecycle.setServletContext( event.getServletContext() );
      
      // start the xchain lifecycle
      Lifecycle.startLifecycle();
    }
    catch( Exception e ) {
      if( log.isErrorEnabled() ) {
        log.error("Could not start the XChain lifecycle", e);
      }

      // stop the lifecycle since there were errors
      try {
        if( Lifecycle.isRunning() ) {
          Lifecycle.stopLifecycle();
        }
      }
      catch( Exception ex ) {
        // ignore any stop exceptions      
      }

      throw new RuntimeException("Could not start the XChain lifecycle", e);
    }
  }

  /*
   * XChain configuration that must be performed *before* the life cycle starts, and hence, before XChain processes
   * its main configuration in 'xchain-config.xml'.
   */
  protected void initialBootStrappingConfig(ServletContext context) {
    log.info("Performing initial boot strapping configuration.");
    
    // Set the context class loader policy. This must be done before the XChain lifecycle is started.
    
    String cclPolicyName = context.getInitParameter(CCL_POLICY_PARAM);
    CCLPolicyType cclPolicy;
    
    if( cclPolicyName == null ) 
      cclPolicy = CCLPolicyType.NONE; 
    else
      cclPolicy = CCLPolicyType.valueOf(cclPolicyName);
    
    log.info("Setting CCL policy to '{}'.", cclPolicy);
    
    switch(cclPolicy) {
    case OSGI:
      ThreadLifecycle.getInstance().setCCLPolicy(new OSGiCCLPolicy());
      break;
    case NONE:
      ThreadLifecycle.getInstance().setCCLPolicy(new NOPCCLPolicy());
      break;
    default:
      throw new IllegalStateException(format("Unsupported CCL policy '%s'.", cclPolicy));  
    }
  }
  

  /**
   * Stops the XChain lifecycle
   */
  public void contextDestroyed(ServletContextEvent event)
  {
    try {
      if( log.isInfoEnabled() ) {
        log.info("Stopping the XChain lifecycle");
      }

      // stop the xchain lifecycle
      Lifecycle.stopLifecycle();

      // clear java.beans.Introspector
      java.beans.Introspector.flushCaches();
    }
    catch( Exception e ) {
      if( log.isErrorEnabled() ) {
        log.error("Could not stop the XChain lifecycle", e);
      }
    }
  }

}
