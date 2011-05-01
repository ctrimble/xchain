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
package org.xchain.namespaces.javascript;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.LifecycleException;
import org.xchain.framework.servlet.CatalogServlet;
import org.xchain.framework.servlet.XChainListener;

/**
 * @author John Trimble
 */
public class MergeJavaScriptTest {
  private static final String CATALOG_SYSTEM_ID = "resource://context-class-loader/org/xchain/namespaces/javascript/test.xchain";
  private static final String MANIFEST_SYSTEM_ID = "resource://context-class-loader/org/xchain/namespaces/javascript/jsmanifest";
  private static final String COMPRESS_CHAIN_NAME = "test00"; 
  
  protected static CatalogServlet catalogServlet;
  
  @BeforeClass
  public static void startXChains() throws LifecycleException, ServletException {
    Mockery servletMockery = new JUnit4Mockery();
    final String systemTemporaryDirectory = System.getProperty("java.io.tmpdir");
    
    // Create a mock ServletConfig and ServletContext
    final ServletConfig servletConfig = servletMockery.mock(ServletConfig.class);
    final ServletContext servletContext = servletMockery.mock(ServletContext.class);
    servletMockery.checking(new Expectations() {{
      allowing(servletContext).getAttribute("javax.servlet.context.tempdir");
      will(returnValue(systemTemporaryDirectory));
      allowing(servletContext).getInitParameter("ccl-policy");
      will(returnValue(null));
    }});
    
    servletMockery.checking(new Expectations() {{
      // Setup the base catalog to use the context class loader.
      one(servletConfig).getInitParameter(CatalogServlet.BASE_CATALOG_NAME_PARAM);
      will(returnValue("resource://context-class-loader/org/xchain/framework/servlet/webapp"));
    }});

    // Create and initialize the CatalogServlet.
    catalogServlet = new CatalogServlet();
    catalogServlet.init(servletConfig);
    
    // Create the XChainListener.
    XChainListener xchainListener = new XChainListener();
    // Since this is not running in a proper web server, there is nothing to fire
    // a ServletContextEvent when the ServletContext is initialized.  Fire the 
    // contextInitialized event manually.  This should cause the Lifecycle
    // to start running.
    xchainListener.contextInitialized(new ServletContextEvent(servletContext));
  }
  
  @AfterClass
  public static void stopXChains() throws LifecycleException {
    Lifecycle.stopLifecycle();
  }
  
  @Test
  public void testJavaScriptMerge() throws Exception {
    JXPathContext context = org.xchain.framework.jxpath.JXPathContextFactoryImpl.newInstance().newContext(null, new Object());
    HttpServletResponse response = createHttpResponseMockery();
    ((ScopedQNameVariables)context.getVariables()).declareVariable( new QName(org.xchain.namespaces.servlet.Constants.URI, org.xchain.namespaces.servlet.Constants.RESPONSE), response );
    
    Command command = CatalogFactory.getInstance().getCatalog(CATALOG_SYSTEM_ID).getCommand(COMPRESS_CHAIN_NAME);
    command.execute(context);
    String result = ((ServletByteArrayOutputStream)response.getOutputStream()).getByteArrayOutputStream().toString();
  }
  
  protected HttpServletResponse createHttpResponseMockery() throws IOException {
    Mockery requestResponseMockery = new JUnit4Mockery();
    final HttpServletResponse response = requestResponseMockery.mock(HttpServletResponse.class);
    final ServletByteArrayOutputStream output = new ServletByteArrayOutputStream();
    requestResponseMockery.checking(new Expectations() {{
      allowing(response).getOutputStream();
      will(returnValue(output));
      
      allowing(response).setContentType("text/javascript");
      allowing(response).isCommitted();
      will(returnValue(true));
      
      allowing(response).setContentType(with(any(String.class)));
      
      allowing(response).setStatus(200);
    }});
    return response;
  }
  
  static class ServletByteArrayOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
    @Override
    public void write(int data) throws IOException {
      this.byteArrayOutputStream.write(data);
    }
    
    public ByteArrayOutputStream getByteArrayOutputStream() {
      return byteArrayOutputStream;
    }
  }
}
