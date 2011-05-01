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

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.xchain.impl.ChainImpl;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.hibernate.HibernateLifecycle;
import javax.xml.namespace.QName;

/**
 * <p>The <code>transaction</code> command starts a Hibernate transaction.  Any children commands will be run within
 * the context of this transaction.  When all children commands have been executed, the transaction will be 
 * committed unless any exceptions are thrown up to this command.  If an unhandled exception is encountered 
 * the transaction will be rolled back.</p>
 * 
 * <p>If the <code>propagate-result</code> attribute is <code>true</code> then the chain of command will continue if and only if the children
 * commands would continue the chain of command.  If the <code>propagate-result</code> attribute is <code>false</code> then the chain of
 * command will continue regardless of the result of the children commands.</p>
 * 
 * <p>This must reference an active <code>session</code>.</p>
 * 
 * <code class="source">
 * &lt;xchain:session xmlns:xchain="http://www.xchain.org/hibernate/1.0"&gt;
 *  &lt;xchain:transaction&gt;
 *    ...
 *  &lt;/xchain:transaction&gt;
 * &lt;/xchain:session&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 * 
 * @see SessionCommand
 */
@Element(localName="transaction")
public abstract class TransactionCommand
  extends ChainImpl
{
  public static Logger log = LoggerFactory.getLogger(TransactionCommand.class);

  /**
   * The timeout of queries on this transaction in seconds. 
   */
  @Attribute(localName="timeout",
             type=AttributeType.JXPATH_VALUE,
             defaultValue="'90'")
  public abstract Integer getTimeout( JXPathContext context );

  /**
   * Whether to continue the chain of command based on the children's result. 
   */
  @Attribute(localName="propagate-result",
             type=AttributeType.JXPATH_VALUE,
             defaultValue="'false'")
  public abstract Boolean getPropagateResult( JXPathContext context );

  @Attribute(localName="name",
             type=AttributeType.QNAME,
             defaultValue= "{http://www.xchain.org/hibernate}session-factory")
  public abstract QName getName( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    boolean result = false;
    Session session = null;
    Transaction transaction = null;

    // get the session from the context.
    session = HibernateLifecycle.getCurrentSession(getName( context ));

    // if we didn't find the session, then bail out.
    if( session == null ) {
      throw new IllegalStateException("Session not found.");
    }

    try {
      // start the transaction.
      transaction = session.getTransaction();
      transaction.setTimeout(getTimeout(context).intValue());
      transaction.begin();

      // execute the children.
      result = super.execute(context);

      // commit the transaction.
//      transaction = session.getTransaction();
      transaction.commit();

    }
    catch( Exception e ) {
      // log that we have bailed out of a transaction.
    	if (log.isInfoEnabled())
    		log.info("Exception causing transaction to rollback.", e);

      // rollback the transaction.
      if (transaction != null) {
        transaction.rollback();
      }

      throw e;
    }

    // if we are not propagating the result, then set the result to false.
    if( !getPropagateResult(context).booleanValue() ) {
      result = false;
    }

    // return the result
    return result;
  }

}
