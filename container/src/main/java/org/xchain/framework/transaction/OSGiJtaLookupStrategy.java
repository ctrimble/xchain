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

import org.xchain.framework.transaction.AbstractJtaLookupStrategy;

/**
 * This JTA transaction strategy for the TransactionCommand, enables JTA transaction lookup via
 * JNDI lookup in an OSGi environment as per the OSGi 4.2 Enterprise draft specification.
 *  
 * @author John Trimble
 */
public class OSGiJtaLookupStrategy extends AbstractJtaLookupStrategy {

  @Override
  protected String getTransactionManagerName() {
    return "osgi:services/javax.transaction.TransactionManager";
  }

  @Override
  protected String getUserTransactionName() {
    return "osgi:services/javax.transaction.UserTransaction";
  }

}
