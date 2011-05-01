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
package org.xchain.framework.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.security.Principal;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.framework.lifecycle.ThreadContext;
import org.xchain.framework.lifecycle.ThreadLifecycle;
import org.xchain.framework.security.Identity;

/**
 * @author Christian Trimble
 */
public class TestIdentityFunctions 
{
  public static final String CATALOG_URI = "resource://context-class-loader/org/xchain/framework/security/security-functions.xchain";
  public static final String NAMESPACE_URI = "http://www.xchain.org/security/1.0";
  public static final QName IDENTITY_FUNCTION = new QName(NAMESPACE_URI, "identity-function");
  public static final QName PRINCIPAL_FUNCTION = new QName(NAMESPACE_URI, "principal-function");
  public static final QName PRINCIPAL_NAME_FUNCTION = new QName(NAMESPACE_URI, "principal-name-function");
  public static final String RESULT = "result";

  protected ThreadContext threadContext = null;
  protected JXPathContext context = null;
  protected Catalog catalog = null;
  protected Command command = null;
  
  @BeforeClass public static void setUpLifecycle()
    throws Exception
  {
    Lifecycle.startLifecycle();    
  }

  @AfterClass public static void tearDownLifecycle()
    throws Exception
  {
    Lifecycle.stopLifecycle();  
  }    

  @Before public void setUp() 
    throws Exception 
  {
    threadContext = new ThreadContext();
    ThreadLifecycle.getInstance().startThread(threadContext);
    context = JXPathContext.newContext(new Object());
    catalog = CatalogFactory.getInstance().getCatalog(CATALOG_URI);
  }
  
  @After public void tearDown() 
    throws Exception 
  {
      ThreadLifecycle.getInstance().stopThread(threadContext);
      threadContext = null;
      context = null;
  }
    
  @Test public void testIdentityFunction() 
    throws Exception 
  {
    // Get the command.
    Command command = catalog.getCommand(IDENTITY_FUNCTION);

    // execute the command.
    command.execute(context);
      
    // Get the result.
    Identity identity = (Identity)context.getValue("$" + RESULT, Identity.class);

    assertNotNull("security:identity() did not return the current identity.", identity);
  }

  @Test public void testPrincipalFunction() 
    throws Exception 
  {
    // Get the command.
    Command command = catalog.getCommand(PRINCIPAL_FUNCTION);

    // execute the command.
    command.execute(context);
      
    // Get the result.
    Principal principal = (Principal)context.getValue("$" + RESULT, Principal.class);

    assertNotNull("security:pricipal() did not return the current principal.", principal);
      
  }

  @Test public void testPrincipalNameFunction() 
    throws Exception 
  {
    // Get the command.
    Command command = catalog.getCommand(PRINCIPAL_NAME_FUNCTION);

    // execute the command.
    command.execute(context);
      
    // Get the result.
    String principalName = (String)context.getValue("$" + RESULT, String.class);

    assertNotNull("security:principal-name() did not return the current principal name.", principalName);
      
  }  
}
