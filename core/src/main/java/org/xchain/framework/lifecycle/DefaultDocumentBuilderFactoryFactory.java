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

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author John Trimble
 */
public class DefaultDocumentBuilderFactoryFactory implements Factory<DocumentBuilderFactory> {

  public DocumentBuilderFactory newInstance() {
    // get the document builder factory.
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // configure the factory to be namespace aware.
    factory.setNamespaceAware(true);
    return factory;
  }

  public void start() throws LifecycleException {
    // TODO Auto-generated method stub
    
  }

  public void stop() {
    // TODO Auto-generated method stub
    
  }

}
