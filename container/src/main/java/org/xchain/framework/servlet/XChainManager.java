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

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;

import org.xchain.CatalogNotFoundException;
import org.xchain.CommandNotFoundException;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.jxpath.QNameVariables;
import org.xchain.framework.lifecycle.ContainerLifecycle;
import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.namespaces.servlet.Constants;
import org.xchain.framework.sax.XChainDeclFilter;
import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlSourceUtil;

import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This is an INCOMPLETE servlet management interface for the xchains framework.  Currently, all that this servlet supports is the rendering of xchain files with
 * all xchain-stylesheet processing instructions rendered.
 *
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
public class XChainManager
  extends HttpServlet
{
  public static Logger log = LoggerFactory.getLogger(CatalogServlet.class);

  public static final String DEFAULT_BASE_CATALOG_NAME = "resource://" + ContainerLifecycle.SERVLET_CONTEXT_ATHORITY + "/";
  public static final String BASE_CATALOG_NAME_PARAM = "base-catalog-name";

  protected String baseCatalogName = null;

  public void init( ServletConfig config )
    throws ServletException
  {
    super.init(config);
  }

  public void service( ServletRequest request, ServletResponse response )
    throws ServletException, IOException
  {
    service((HttpServletRequest)request, (HttpServletResponse)response);
  }
  
  protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException
  {
    List<String> pathInfoSegments = pathInfoSegments(request);

    if( pathInfoSegments.size() == 0 ) {
      managementScreen(request, response);
      return;
    }

    List<String> remainingSegments = pathInfoSegments.subList( 1, pathInfoSegments.size() );
    String commandName = pathInfoSegments.get(0);

    if( "render".equals(commandName) ) {
      render(request, response, remainingSegments);
    }
    else {
      unknownCommand(request, response, commandName);
    }
  }

  /**
   * Handles requests to the management screen.
   */
  protected void managementScreen( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException
  {
    ServletOutputStream out = response.getOutputStream();

    out.println("<html>");
    out.println("  <head>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    NOT IMPLEMENTED");
    out.println("  </body>");
    out.println("</html>");
  }

  /**
   * Handles requests to the render url screen.
   */
  protected void render( HttpServletRequest request, HttpServletResponse response, List<String> pathSegments )
    throws ServletException, IOException
  {
    // get the url to render.
    String systemId = request.getParameter("system-id");

    if( systemId == null ) {
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/xml");

    URL url = null;
    InputSource inputSource = null;

    try {
      // get the url to copy out.
      url = UrlFactory.getInstance().newUrl( systemId );

      // get the input source for the url.
      inputSource = UrlSourceUtil.createSaxInputSource( url );

      // create the XMLReader.
      XMLReader reader = XmlFactoryLifecycle.newXmlReader();
      reader.setErrorHandler( new FailingErrorHandler() );

      // set up the source filter.
      XChainDeclFilter sourceFilter = new XChainDeclFilter();
      sourceFilter.setParent(reader);
      sourceFilter.setErrorHandler( new FailingErrorHandler() );

      // create a serializer for the response.
      Properties outputProperties = OutputPropertiesFactory.getDefaultMethodProperties( "xml" );
      outputProperties.setProperty("media-type", "text/xml");
      Serializer serializer = SerializerFactory.getSerializer( outputProperties );
      serializer.setOutputStream(response.getOutputStream());
      sourceFilter.setContentHandler(serializer.asContentHandler());

      // parser the url.
      sourceFilter.parse(inputSource);
    }
    catch( Exception e ) {
      errorScreen(request, response, e);
    }
    finally {
      close(inputSource);
      close(response.getOutputStream());
    }
  }

  /**
   * Handles requests to unknown command.
   */
  protected void unknownCommand( HttpServletRequest request, HttpServletResponse response, String commandName )
    throws ServletException, IOException
  {
    ServletOutputStream out = response.getOutputStream();
    out.println("<html>");
    out.println("  <head>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    UNKNOWN_COMMAND:"+commandName);
    out.println("  </body>");
    out.println("</html>");
  }

  protected void errorScreen( HttpServletRequest request, HttpServletResponse response, Throwable t )
    throws ServletException, IOException
  {
    PrintWriter out = new PrintWriter(response.getOutputStream());
    t.printStackTrace(out);
  }

  private static List<String> pathInfoSegments(HttpServletRequest request)
  {
    List<String> pathInfoSegments = null;

    if( request.getPathInfo() == null ) {
      return Collections.emptyList();
    }

    String pathInfo = request.getPathInfo();
    if( pathInfo.startsWith("/") ) {
      pathInfo = pathInfo.substring(1);
    }

    return Arrays.asList(pathInfo.split("/"));
  }

  private static void close( InputSource inputSource )
  {
    try {
      if( inputSource != null && inputSource.getByteStream() != null ) {
        close(inputSource.getByteStream());
      }
      else if( inputSource != null && inputSource.getCharacterStream() != null ) {
        close(inputSource.getCharacterStream());
      }
    }
    catch( Throwable t ) {
      if( log.isWarnEnabled() ) {
        log.warn("Exception thrown while closing input source.");
      }
    }
  }

  public static void close( InputStream in )
  {
    try {
      if( in != null ) {
        in.close();
      }
    }
    catch( Throwable t ) {
      if( log.isWarnEnabled() ) {
        log.warn("Exception thrown while closing input stream.");
      }
    }
  }

  public static void close( Reader reader )
  {
    try {
      if( reader != null ) {
        reader.close();
      }
    }
    catch( Throwable t ) {
      if( log.isWarnEnabled() ) {
        log.warn("Exception thrown while closing reader.");
      }
    }
  }

  public static void close( OutputStream out )
  {
    try {
      if( out != null ) {
        out.close();
      }
    }
    catch( Throwable t ) {
      if( log.isWarnEnabled() ) {
        log.warn("Exception thrown while closing output stream.");
      }
    }
  }


  public static class FailingErrorHandler
    implements ErrorHandler
  {
    public void warning(SAXParseException exception)
      throws SAXException
    {
      if( log.isWarnEnabled() ) {
        log.warn("SAXParseException thrown while loading catalog.", exception);
      }
    }

    public void error(SAXParseException exception)
      throws SAXException
    {
      if( log.isErrorEnabled() ) {
        log.error("SAXParseException thrown while loading catalog.", exception);
      }
      throw exception;
    }

    public void fatalError(SAXParseException exception)
      throws SAXException
    {
      if( log.isErrorEnabled() ) {
        log.error("Fatal SAXParseException thrown while loading catalog.", exception);
      }
      throw exception;
    }
  }
}
