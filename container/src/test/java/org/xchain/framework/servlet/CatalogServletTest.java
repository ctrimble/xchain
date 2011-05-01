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

import static org.junit.Assert.assertTrue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author John Trimble
 */
@RunWith(JMock.class)
public class CatalogServletTest
{
  protected Mockery servletMockery = new JUnit4Mockery();
  
  protected ServletConfig servletConfig;
  protected ServletContext servletContext;
  protected CatalogServlet catalogServlet;
  protected XChainListener xchainListener;
  
  @Test public void testServlet()
  	throws Exception
  {
	  // Create a mock ServletConfig and ServletContext
	  servletConfig = servletMockery.mock(ServletConfig.class);
	  servletContext = servletMockery.mock(ServletContext.class);

	  servletMockery.checking(new Expectations() {{
		  // Setup the base catalog to use the context class loader.
		  one(servletConfig).getInitParameter(CatalogServlet.BASE_CATALOG_NAME_PARAM);
		  will(returnValue("resource://context-class-loader/org/xchain/framework/servlet/webapp"));
      allowing(servletContext).getInitParameter("ccl-policy");
      will(returnValue(null));
	  }});

	  // Create and initialize the CatalogServlet.
	  catalogServlet = new CatalogServlet();
	  catalogServlet.init(servletConfig);

	  // Create the XChainListener.
	  xchainListener = new XChainListener();
	  // Since this is not running in a proper web server, there is nothing to fire
	  // a ServletContextEvent when the ServletContext is initialized.  Fire the 
	  // contextInitialized event manually.  This should cause the Lifecycle
	  // to start running.
	  xchainListener.contextInitialized(new ServletContextEvent(servletContext));
	  
	  // Assert that the Lifecycle is running.
	  assertTrue("The lifecycle claims it is not running when it should.", Lifecycle.isRunning());

	  // There is nothing to fire the contextDestroyed event, so call it manually.
	  // This should cause the Lifecycle to stop.
	  xchainListener.contextDestroyed(new ServletContextEvent(servletContext));
	  
	  // Assert that the Lifecycle is stopped.
	  assertTrue("The lifecycle claims it is running when it should not.", !Lifecycle.isRunning());
	  
	  // Assert that all expected mockery calls are satisfied.
	  servletMockery.assertIsSatisfied();
  }
}
