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

/**
 * @author Christian Trimble
 */
public class DefaultTransformerFactoryFactory
  implements Factory<SAXTransformerFactory>
{
  public SAXTransformerFactory newInstance()
  {
    return (SAXTransformerFactory)TransformerFactory.newInstance();
  }

  public void start()
    throws LifecycleException
  {

  }

  public void stop()
  {

  }

}
