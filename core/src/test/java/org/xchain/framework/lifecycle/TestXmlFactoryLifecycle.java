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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

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

/**
 * @author Christian Trimble
 */
public class TestXmlFactoryLifecycle
{
  public static QName XALAN_FACTORY_NAME = new QName("http://www.xchain.org/core", "xalan");
  public static QName XSLTC_FACTORY_NAME = new QName("http://www.xchain.org/core", "xsltc");
  public static QName SAXON_FACTORY_NAME = new QName("http://www.xchain.org/core", "saxon");

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

  @Test public void testSaxTransformerFactories()
    throws Exception
  {
    assertTrue("The xalan transformer factory factory is not defined.", XmlFactoryLifecycle.getTransformerFactoryFactory(XALAN_FACTORY_NAME) != null);
    assertTrue("The xalan transformer factory is not defined.", XmlFactoryLifecycle.getTransformerFactoryFactory(XALAN_FACTORY_NAME).newInstance() != null);
    assertTrue("The xalan transformer factory is not defined.", XmlFactoryLifecycle.newTransformerFactory(XALAN_FACTORY_NAME) != null);
    assertTrue("The xsltc transformer factory factory is not defined.", XmlFactoryLifecycle.getTransformerFactoryFactory(XSLTC_FACTORY_NAME) != null);
    assertTrue("The xsltc transformer factory is not defined.", XmlFactoryLifecycle.getTransformerFactoryFactory(XSLTC_FACTORY_NAME).newInstance() != null);
    assertTrue("The xalan transformer factory is not defined.", XmlFactoryLifecycle.newTransformerFactory(XSLTC_FACTORY_NAME) != null);
    assertTrue("The saxon transformer factory factory is not defined.", XmlFactoryLifecycle.getTransformerFactoryFactory(SAXON_FACTORY_NAME) != null);
    assertTrue("The saxon transformer factory is not defined.", XmlFactoryLifecycle.getTransformerFactoryFactory(SAXON_FACTORY_NAME).newInstance() != null);
    assertTrue("The xalan transformer factory is not defined.", XmlFactoryLifecycle.newTransformerFactory(SAXON_FACTORY_NAME) != null);
  }

}
