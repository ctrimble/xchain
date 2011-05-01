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
package org.xchain.framework.util;

import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Moulton
 * @author Josh Kennedy
 */
public final class NamingUtil {

	private static final Logger log = LoggerFactory.getLogger(NamingUtil.class);

	private NamingUtil() {}

	public static InitialContext getInitialContext()
	  throws NamingException
	{
    return getInitialContext(null);
  }

	public static InitialContext getInitialContext(Properties props)
	  throws NamingException
	{
		Properties properties = getJndiProperties(props);
		try {
		  if (properties != null && properties.size() > 0) {
    		log.debug("Configuring InitialContext:" + properties);
        return new InitialContext(properties);
		  } else {
        return new InitialContext();
		  }
		} catch (NamingException e) {
			log.error("Unable to obtain an InitialContext:", e);
			throw e;
		}
	}

	/**
	 * TODO: Place holder for more advanced configuration in the future
	 */
	public static Properties getJndiProperties(Properties properties) {
		return properties;
	}

}







