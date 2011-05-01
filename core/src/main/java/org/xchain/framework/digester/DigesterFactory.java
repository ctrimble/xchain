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
package org.xchain.framework.digester;

import org.apache.commons.digester.Digester;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xchain.framework.digester.strategy.RuleLoadingNewDigesterFilter;
import org.xchain.framework.lifecycle.XmlFactoryLifecycle;

/**
 * @author Christian Trimble
 * @author Mike Moulton
 */
public class DigesterFactory
{
  public static DigesterFactory instance;

  static {
    instance = new DigesterFactory();
  }

  public static DigesterFactory getInstance()
  {
    return instance;
  }

  protected NewDigesterStrategy newDigesterStrategy;

  public synchronized void addNewDigesterFilter( NewDigesterFilter newDigesterFilter )
  {
    NewDigesterFilter lastFilter = newDigesterFilter;

    // find the last filter in the chain.
    while( lastFilter.getParent() != null && lastFilter.getParent() instanceof NewDigesterFilter ) {
      lastFilter = (NewDigesterFilter)lastFilter.getParent();
    }

    // if the last filter in the chain has a null parent, then set the current new digester strategy
    // as the parent.
    if( lastFilter.getParent() == null ) {
      lastFilter.setParent( newDigesterStrategy );
    }

    // assign the newDigesterFilter as the newDigesterStrategy for the factory.
    newDigesterStrategy = newDigesterFilter;
  }

  public synchronized NewDigesterStrategy getNewDigesterStrategy()
  {
    return newDigesterStrategy;
  }

  public synchronized void setNewDigesterStrategy( NewDigesterStrategy newDigesterStrategy )
  {
    this.newDigesterStrategy = newDigesterStrategy;
  }

  public DigesterFactory()
  {
    setNewDigesterStrategy(new RootNewDigesterStrategy());
    addNewDigesterFilter(new RuleLoadingNewDigesterFilter());
  }

  public Digester newDigester( XMLReader xmlReader )
    throws Exception
  {
    return getNewDigesterStrategy().newDigester(xmlReader);
  }

  public Digester newDigester()
    throws Exception
  {
    // create a new xml reader.
    XMLReader reader = XmlFactoryLifecycle.newXmlReader();

    // create a new digester with this xml reader.
    return this.newDigester( reader );
  }

  /**
   * This strategy just creates a new digester.
   */
  public static class RootNewDigesterStrategy
    implements NewDigesterStrategy
  {
    public Digester newDigester( XMLReader xmlReader )
      throws Exception
    {
      Digester digester = null;

      if( xmlReader != null ) {
        digester = new Digester( xmlReader );
      }
      else {
        digester = new Digester();
      }

      return digester;
    }
  }
}
