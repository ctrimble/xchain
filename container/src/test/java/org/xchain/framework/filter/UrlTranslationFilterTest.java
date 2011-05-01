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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xchain.framework.SimpleServletOutputStream;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
@RunWith(JMock.class)
public class UrlTranslationFilterTest {
  
  protected Mockery filterMockery = new JUnit4Mockery();
  
  protected FilterConfig filterConfig;

  @Test
  public void initializationTest()
    throws Exception
  {
    UrlTranslationFilter translationFilter = new UrlTranslationFilter();

    final ServletContext servletContext = filterMockery.mock(ServletContext.class);
    
    filterConfig = filterMockery.mock(FilterConfig.class);
    filterMockery.checking(new Expectations() {{
      allowing(filterConfig).getServletContext();
      will(returnValue(servletContext));

      allowing(filterConfig).getInitParameter(UrlTranslationFilter.ENABLED_PARAM_NAME);
      will(returnValue("true"));

      one(filterConfig).getInitParameter(UrlTranslationFilter.CONFIG_RESOURCE_URL_PARAM_NAME);
      will(returnValue("org/xchain/framework/filter/translation-filter-test.xml"));
    }});
    
    translationFilter.init(filterConfig);
    
    assertTrue(translationFilter.isEnabled());
  }
  
  @Test
  public void filterTest()
    throws Exception
  {
    
    UrlTranslationFilter translationFilter = new UrlTranslationFilter();
    
    URL controlUrl = new URL("http://api.flickr.com/crossdomain.xml");
    ByteArrayOutputStream controlOutputStream = new ByteArrayOutputStream();
    InputStream controlInputStream = controlUrl.openConnection().getInputStream();
    byte[] buffer = new byte[2048];
    int lengthRead;
    while( (lengthRead = controlInputStream.read(buffer)) != -1 ) {
      controlOutputStream.write(buffer, 0, lengthRead);
    }

    final HttpServletRequest request = filterMockery.mock(HttpServletRequest.class);
    final HttpServletResponse response = filterMockery.mock(HttpServletResponse.class);
    final ServletContext servletContext = filterMockery.mock(ServletContext.class);
    
    // Create a SimpleServletOutputStream to hold the response from the servlet.
    final SimpleServletOutputStream output = new SimpleServletOutputStream();
    filterMockery.checking(new Expectations() {{
      // Request the index.xchain
      allowing(request).getServletPath();
      will(returnValue("/test/crossdomain.xml"));

      // Return the SimpleServletOutputStream.
      one(response).getOutputStream();
      will(returnValue(output));
      
      allowing(response).setContentLength(with(any(int.class)));

      allowing(servletContext).getMimeType("/test/crossdomain.xml");
      will(returnValue("text/xml"));
      
      allowing(response).setContentType(with(any(String.class)));

    }});    

    filterConfig = filterMockery.mock(FilterConfig.class);
    filterMockery.checking(new Expectations() {{
      allowing(filterConfig).getServletContext();
      will(returnValue(servletContext));

      allowing(filterConfig).getInitParameter(UrlTranslationFilter.ENABLED_PARAM_NAME);
      will(returnValue("true"));

      one(filterConfig).getInitParameter(UrlTranslationFilter.CONFIG_RESOURCE_URL_PARAM_NAME);
      will(returnValue("org/xchain/framework/filter/translation-filter-test.xml"));
    }});
    
    translationFilter.init(filterConfig);
    
    assertTrue(translationFilter.isEnabled());
    
    translationFilter.doFilter(request, response, null);
    assertEquals(controlOutputStream.toString(), output.getOutput());
  }  
  
  @Test
  public void initializationFailureTest()
    throws Exception
  {
    UrlTranslationFilter translationFilter = new UrlTranslationFilter();
 
    final ServletContext servletContext = filterMockery.mock(ServletContext.class);
    filterConfig = filterMockery.mock(FilterConfig.class);
    filterMockery.checking(new Expectations() {{
      allowing(filterConfig).getServletContext();
      will(returnValue(servletContext));

      allowing(filterConfig).getInitParameter(UrlTranslationFilter.ENABLED_PARAM_NAME);
      will(returnValue("true"));

      one(filterConfig).getInitParameter(UrlTranslationFilter.CONFIG_RESOURCE_URL_PARAM_NAME);
      will(returnValue("org/xchain/framework/filter/translation-filter-failure-test.xml"));
    }});
    
    translationFilter.init(filterConfig);
    
    assertFalse(translationFilter.isEnabled());
  }  
}
