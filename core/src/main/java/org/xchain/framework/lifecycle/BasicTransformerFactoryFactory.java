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

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class BasicTransformerFactoryFactory
  implements Factory<SAXTransformerFactory>
{
  private static Logger log = LoggerFactory.getLogger(BasicTransformerFactoryFactory.class);

  private Class factoryClass = null;

  public BasicTransformerFactoryFactory( String factoryClassName )
  {
    this(loadFactoryClass( factoryClassName ));
  }

  public BasicTransformerFactoryFactory( Class factoryClass )
  {
    checkClassCompatibility( factoryClass );
    this.factoryClass = factoryClass;
  }

  public SAXTransformerFactory newInstance()
  {
    SAXTransformerFactory factory = null;
    try {
      factory = (SAXTransformerFactory)factoryClass.newInstance();
    }
    catch( Exception e ) {
      throw new IllegalStateException("The transformer factory class '"+factoryClass+"' could not be constructed and cast to SAXTransformerFactory, but was not filtered by the constructor.", e);
    }
    return factory;
  }

  public void start()
    throws LifecycleException
  {

  }

  public void stop()
  {

  }

  private static Class loadFactoryClass( String factoryClassName )
  {
    try {
      return Thread.currentThread().getContextClassLoader().loadClass( factoryClassName );
    }
    catch( Exception e ) {
      throw new IllegalArgumentException("The class '"+factoryClassName+"' could not be found in the context class loader.", e);
    }
  }

  private static void checkClassCompatibility( Class<?> factoryClass )
  {
    if( !SAXTransformerFactory.class.isAssignableFrom( factoryClass ) ) {
      throw new IllegalArgumentException("The class '"+factoryClass+"' does not implement SAXTransformerFactory.");
    }

    try {
      factoryClass.getConstructor();
    }
    catch( Exception e ) {
      throw new IllegalArgumentException("The class '"+factoryClass+"' does not provide a default constructor.", e);
    }
  }
}
