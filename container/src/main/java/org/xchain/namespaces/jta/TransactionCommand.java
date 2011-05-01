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
package org.xchain.namespaces.jta;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.jxpath.ScopedQNameVariables;
import org.xchain.framework.jxpath.Scope;
import org.xchain.framework.lifecycle.ContainerContext;
import org.xchain.impl.ChainImpl;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.xml.namespace.QName;

/**
 * Demarcate the bounds of a JTA UserTransaction
 * 
 * <code class="source">
 * &lt;jta:transaction xmlns:jta="http://www.xchain.org/jta/1.0" timeout="90"/&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 * @author John Trimble
 */
@Element(localName="transaction")
public abstract class TransactionCommand
  extends ChainImpl
{
  public static Logger log = LoggerFactory.getLogger(TransactionCommand.class);

  /**
   * The transaction timeout
   */
  @Attribute(localName="timeout",
             type=AttributeType.JXPATH_VALUE,
             defaultValue="'90'")
  public abstract Integer getTimeout( JXPathContext context );

  @Attribute(localName="result",
             type=AttributeType.QNAME,
             defaultValue= "{" + Constants.URI + "}" + Constants.USER_TRANSACTION)
  public abstract QName getResult( JXPathContext context );

  public boolean execute( JXPathContext context )
    throws Exception
  {
    boolean managingTransaction = false;
    boolean result  = false;
    UserTransaction transaction = ContainerContext.getJtaLookupStrategy().getUserTransaction();

    if (transaction != null) {
      try {
        // if there is not a transaction running, then start one.
        if (Status.STATUS_ACTIVE != transaction.getStatus()) {
          managingTransaction = true;
          transaction.setTransactionTimeout(this.getTimeout(context));
          transaction.begin();
        }

        // put the transaction into the context.
        ((ScopedQNameVariables)context.getVariables()).declareVariable( getResult( context ), transaction, Scope.execution );

        // execute the chain 
        result = super.execute( context );

        // roll back the transaction.
        if( managingTransaction ) {
          transaction.commit();
        }
      } catch (Exception e) {
        if( managingTransaction ) {
          if( transaction.getStatus() != Status.STATUS_NO_TRANSACTION ) {
            transaction.rollback();
          }
        }
        throw e;
      }
      finally {
        // TODO: If we defined the transaction variable, then we should remove it.
      }
    } else {
      // TODO: Should this throw an IllegalStateException?  Returning true seems like the wrong behavior.
      result = true;
    }
    return result;
  }

}
