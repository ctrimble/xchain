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
package org.xchain.namespaces.hibernate;

import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.impl.ChainImpl;
import javax.xml.namespace.QName;

/**
 * <p>NOTE: The hibernate session command is currently broken.  This implementation will create duplicate sessions
 * when used with the HibernateLifecycle.getCurrentSession() function.  This tag will be removed in the 0.4.0 version
 * of XChains.</p>
 *
 * <p>The <code>session</code> command creates a new Hibernate session and stores it into the context.  
 * The location of the session in the context is determined by the <code>result</code> attribute.</p>
 * 
 * <p>Once the <code>session</code> command is complete, the Hibernate session will be closed.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  ...
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
@Element(localName="session")
public abstract class SessionCommand
  extends ChainImpl
{
  public static Logger log = LoggerFactory.getLogger(SessionCommand.class);

  /**
   * Where the session is stored. 
   */
  @Attribute(localName="result",
             type=AttributeType.QNAME,
             defaultValue= "{" + Constants.URI + "}" + Constants.SESSION)             
  public abstract QName getResult( JXPathContext context );

  @Attribute(localName="name",
             type=AttributeType.QNAME,
             defaultValue= "{http://www.xchain.org/hibernate}session-factory")
  public abstract QName getName( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    if( log.isErrorEnabled() ) {
      log.error("This tag has been removed in XChains 0.3.0.  Remove this tag and replace references to the $hibernate:session varaible with a call to hibernate:current-session()");
    }
    throw new UnsupportedOperationException("This tag has been removed in XChains 0.3.0.  Remove this tag and replace references to the $hibernate:session varaible with a call to hibernate:current-session()");
  }

}
