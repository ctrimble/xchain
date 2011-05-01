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
package org.xchain.framework.jxpath;

import org.apache.commons.jxpath.ri.JXPathContextFactoryReferenceImpl;
import org.apache.commons.jxpath.JXPathContextFactoryConfigurationError;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.util.TypeUtils;

/**
 * Implementation of JXPathContextFactory that produces a JXPathContext with an instance of ScopedQNameVariables.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * 
 * @see JXPathContextFactoryReferenceImpl
 * @see ScopedQNameVariables
 */
public class JXPathContextFactoryImpl
  extends JXPathContextFactoryReferenceImpl
{
  static {
    TypeUtils.setTypeConverter(new XChainTypeConverter());
  }

  public JXPathContextFactoryImpl() {}

  public JXPathContext newContext( JXPathContext parentContext, Object contextBean )
    throws JXPathContextFactoryConfigurationError
  {
//    return new JXPathContextImpl(parentContext, contextBean);
    return new ScopedJXPathContextImpl(parentContext, contextBean, Scope.request);
  }
}
