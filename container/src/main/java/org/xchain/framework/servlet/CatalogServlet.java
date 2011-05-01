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

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.CatalogNotFoundException;
import org.xchain.CommandNotFoundException;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.jxpath.QNameVariables;
import org.xchain.framework.lifecycle.ContainerLifecycle;
import org.xchain.framework.lifecycle.LifecycleException;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.namespaces.servlet.Constants;

/**
 * This servlet can be used to bind servlet requests to xchains inside of an application.  To do this, this servlet tasks the following action:
 * <ol>
 *   <li>The requested url is translated into the name of a catalog.  This catalog is then loaded.</li>
 *   <li>The requested method is translated into a qname.  This command is loaded from the catalog.</li>
 *   <li>The servlet request and response are used to create a JXPathContext.</li>
 *   <li>The JXPathcContext is used to execute the command.</li>
 * </ol>
 *
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
public class CatalogServlet
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

    // read the base catalog name.
    baseCatalogName = config.getInitParameter(BASE_CATALOG_NAME_PARAM);
    if( baseCatalogName == null ) { baseCatalogName = DEFAULT_BASE_CATALOG_NAME; }

    // log the config.
    if( log.isDebugEnabled() ) {
      log.debug("Base Catalog Name: "+baseCatalogName);
    }
  }
  
  protected void service(HttpServletRequest request, HttpServletResponse response)
  	throws ServletException, java.io.IOException
  {
    ServletThreadContext context = new ServletThreadContext(request, response);
    try {
      ThreadLifecycle.getInstance().startThread(context);
    }
    catch( LifecycleException le ) {
      throw new ServletException("Could not start the servlet thread due to an exception.");
    }
    try {
      // All methods may be valid commands for a catalog.  No need to break out the usage into different
      // method calls.
      executeCommand( request, response );
    }
    finally {
      try {
        ThreadLifecycle.getInstance().stopThread(context);
      }
      catch( LifecycleException le ) {
        if( log.isWarnEnabled() ) {
          log.warn("An exception was thrown while cleaning up a thread.", le);
        }
      }
    }

  }

  /**
   * Execute the proper XChain based on the incoming {@link HttpServletRequest} and sending output on the {@link HttpServletResponse}.
   * The path on the request determines which catalog to use while the method of the request determines which command to execute.
   * 
   * @param request The incoming request.
   * @param response The outgoing response.
   */
  public void executeCommand( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException
  {

    try {
      // get the catalog name for the request.
      String catalogName = catalogNameForRequest( request);

      // get the command name for the request.
      QName commandName = commandNameForRequest( request );

      if( log.isDebugEnabled() ) {
        log.debug("Executing '"+commandName+"' in catalog '"+catalogName+"'.");
      }
      
      // create the context for the request.
      JXPathContext context = jXPathContext( request, response );

      // execute the command.
      boolean result = CatalogFactory.getInstance().getCatalog(catalogName).getCommand(commandName).execute(context);
//      boolean result = CommandUtil.execute(catalogName, commandName, context);
      
      if( response.isCommitted() == false && result == false ) {
        // it looks like we didn't do anything...
      }
    }
    catch( CatalogNotFoundException cae ) {
      if( log.isDebugEnabled() ) {
        log.debug("Could not find xchain catalog.", cae);
      }
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
    }
    catch( CommandNotFoundException coe ) {
      if( log.isDebugEnabled() ) {
        log.debug("Could not find xchain command.", coe);
      }
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
    }
    catch( Exception e ) {
      
      if (log.isErrorEnabled()) {
        log.error("Unable to execute xchain.", e);
      }
      
      throw new ServletException(e);
    }
  }

  /**
   * Get the catalog based on the requested path.
   * 
   * @param request The incoming HttpServletRequest.
   * 
   * @return The full requested catalog name.
   */
  protected String catalogNameForRequest( HttpServletRequest request)
  {
    StringBuffer sb = new StringBuffer();

    // append the base catalog name.
    sb.append( baseCatalogName );

    String servletPath = request.getServletPath();

    if( servletPath.startsWith("/") && baseCatalogName.endsWith("/") ) {
      sb.append(servletPath.replaceAll("\\A/(.*)\\Z", "$1"));
    }
    else if( !servletPath.startsWith("/") && !baseCatalogName.endsWith("/") ) {
      sb.append("/").append(servletPath);
    }
    else {
      sb.append(servletPath);
    }

    // return the catalog name.
    return sb.toString();
  }

  /**
   * Get the command requested based on the request method.
   * 
   * @param request The incoming HttpServletRequest.
   * 
   * @return The QName of the requested command.
   */
  protected QName commandNameForRequest( HttpServletRequest request )
  {
    return new QName( Constants.URI, request.getMethod().toLowerCase() );
  }

  /**
   * Build a new JXPathContext and populate it with the given request and response.
   * 
   * @param request The incoming HttpServletRequest.
   * @param response The outgoing HttpServletResponse.
   * 
   * @return A JXPath which contains the servlet request and response.

   */
  protected JXPathContext jXPathContext( HttpServletRequest request, HttpServletResponse response )
  {
    JXPathContext context = JXPathContext.newContext( new HashMap() );

    ((QNameVariables)context.getVariables()).declareVariable( new QName( Constants.URI, Constants.CONTEXT ), getServletConfig().getServletContext() );
    ((QNameVariables)context.getVariables()).declareVariable( new QName( Constants.URI, Constants.REQUEST ), request );
    ((QNameVariables)context.getVariables()).declareVariable( new QName( Constants.URI, Constants.RESPONSE ), response );

    return context;
  }
}
