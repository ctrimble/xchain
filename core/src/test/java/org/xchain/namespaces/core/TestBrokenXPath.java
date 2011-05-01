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
package org.xchain.namespaces.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xchain.Catalog;
import org.xchain.Command;
import org.xchain.framework.factory.CatalogFactory;
import org.xchain.framework.lifecycle.Lifecycle;

/**
 * This set of tests try to load xchain files with known bad xpaths.
 * @author Christian Trimble
 */
public class TestBrokenXPath 
{
  public static final String BASE_CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/";
  public static final String BROKEN_XPATH_ATTRIBUTE_CATALOG_URI = "resource://context-class-loader/org/xchain/namespaces/core/broken-xpath-attribute.xchain";

  protected JXPathContext context = null;
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
    context = JXPathContext.newContext(new Object());
  }
  
  @After public void tearDown() 
    throws Exception 
  {
      context = null;
  }

  @Test public void testBrokenXPathAttribute()
  {
    testLoad(BROKEN_XPATH_ATTRIBUTE_CATALOG_URI);
  }

  @Test public void testBrokenAtvAttribute()
  {
    testLoad(BASE_CATALOG_URI+"broken-atv-attribute.xchain");
  }

  @Test public void testBrokenQNameAttribute()
  {
    testLoad(BASE_CATALOG_URI+"broken-qname-attribute.xchain");
  }

  @Test public void testBrokenLiteralAttribute()
  {
    testLoad(BASE_CATALOG_URI+"broken-literal-attribute.xchain");
  }

  private void testLoad( String catalogUri )
  {
    try {
      Catalog catalog = CatalogFactory.getInstance().getCatalog(catalogUri);
      fail("The catalog '"+catalogUri+"' should not have loaded, it has an xpath that is known to be broken.");
    }
    catch( Exception e ) {
      // this should happen.
    }
  }
}
