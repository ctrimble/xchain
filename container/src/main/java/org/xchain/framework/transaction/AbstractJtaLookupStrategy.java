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
package org.xchain.framework.transaction;

import org.xchain.framework.util.NamingUtil;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Mike Moulton
 */
public abstract class AbstractJtaLookupStrategy
  implements JtaLookupStrategy
{

	public TransactionManager getTransactionManager()
	  throws NamingException
	{
    TransactionManager manager = null;
    try {
      InitialContext context = NamingUtil.getInitialContext();
      manager = (TransactionManager) context.lookup( getTransactionManagerName() );
    } catch ( NamingException e ) {
      throw e;
    }
    return manager;
  }

	public UserTransaction getUserTransaction()
	  throws NamingException
	{
    UserTransaction transaction = null;
    try {
      InitialContext context = NamingUtil.getInitialContext();
      transaction = (UserTransaction) context.lookup( getUserTransactionName() );
    } catch ( NamingException e ) {
      throw e;
    }
    return transaction;
	}

	abstract protected String getTransactionManagerName();

	abstract protected String getUserTransactionName();

}






