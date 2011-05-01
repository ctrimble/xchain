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
package org.xchain.namespaces.core;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.impl.ChainImpl;

/**
 * @author Josh Kennedy
 */
@Element(localName="debug-message")
public abstract class DebugMessageCommand extends ChainImpl {
	public boolean execute(JXPathContext context) throws Exception {
		Logger log = LoggerFactory.getLogger(getLogger(context));
		
		String level = getLevel(context);
		String message = getMessage(context);
		
		if (level.toLowerCase().equals("trace")) {
			log.trace(message);
		}
		if (level.toLowerCase().equals("debug")) {
			log.debug(message);
		}
		if (level.toLowerCase().equals("info")) {
			log.info(message);
		}
		if (level.toLowerCase().equals("warn")) {
			log.warn(message);
		}
		if (level.toLowerCase().equals("error")) {
			log.error(message);
		}
		
		return false;
	}
	
	@Attribute(localName="message", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="Empty Message")
	public abstract String getMessage(JXPathContext context);
	
	@Attribute(localName="level", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="debug")
	public abstract String getLevel(JXPathContext contect);
	
	@Attribute(localName="logger", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="org.xchain.namespaces.core.DebugMessageCommand")
	public abstract String getLogger(JXPathContext contect);
}
