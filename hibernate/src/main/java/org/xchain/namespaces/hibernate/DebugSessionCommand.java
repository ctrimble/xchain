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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.hibernate.HibernateLifecycle;
import org.xchain.impl.ChainImpl;

/**
 * @author Josh Kennedy
 */
@Element(localName="debug-session")
public abstract class DebugSessionCommand extends ChainImpl {
	public static Logger log = LoggerFactory.getLogger(DebugSessionCommand.class);
	
	public boolean execute(JXPathContext context) throws Exception {
		try {
			Session session = null;
		    Transaction transaction = null;
	
		    String message = getMessage(context);
		    log.debug(message);
		    // get the session from the context.
		    QName name = getName(context);
		    session = HibernateLifecycle.getCurrentSession(name);
		    
		    log.debug("Getting session for {}", name.toString());
		    
		    // if we didn't find the session, then bail out.
		    if( session == null ) {
		      throw new IllegalStateException("Session not found.");
		    }
		    
		    // Dump Session data to log
		    logSession(session);
		    
		    transaction = session.getTransaction();
		    
		    if (transaction != null) {
		    	log.debug("Transaction Active: {}", transaction.isActive());
		    }
		}
		catch (Exception e) {
			log.error("Error inspecting Session: {}", e.getMessage(), e);
		}
	    
		return false;
	}
	
	private void logSession(Session session) {
		log.debug("Connected, Dirty, Open:  {}", new boolean[]{session.isConnected(), session.isDirty(), session.isOpen()});
		log.debug("CacheMode:  {}", session.getCacheMode().toString());
		log.debug("EntityMode: {}", session.getEntityMode().toString());
		log.debug("FlushMode:  {}", session.getFlushMode().toString());
	}
	
	@Attribute(localName="name",
	           type=AttributeType.QNAME,
	           defaultValue= "{http://www.xchain.org/hibernate}session-factory")
	public abstract QName getName(JXPathContext context);
	
	@Attribute(localName="message", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="")
	public abstract String getMessage(JXPathContext context);
}
