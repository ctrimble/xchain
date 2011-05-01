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

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.lifecycle.Execution;
import org.xchain.framework.lifecycle.ExecutionTraceElement;
import org.xchain.impl.ChainImpl;
import org.xml.sax.Locator;

/**
 * @author Josh Kennedy
 */
@Element(localName="trace")
public abstract class TraceCommand extends ChainImpl {
	public boolean execute(JXPathContext context) throws Exception {
		Logger log = LoggerFactory.getLogger(getLogger(context));
		StringBuffer buffer = new StringBuffer();
		
		List<ExecutionTraceElement> stack = Execution.getExecutionTrace();

		buffer.append(getMessage(context));
		buffer.append("\n");
		for (ExecutionTraceElement element : stack) {
			Locator locator = element.getLocator();
			buffer.append("\t running ");
			buffer.append(element.getQName().toString());
			buffer.append(" in ");
			buffer.append(element.getSystemId());
			buffer.append(" at ");
			buffer.append(locator.getLineNumber());
			buffer.append(":");
			buffer.append(locator.getColumnNumber());
			buffer.append("\n");
		}

		String level = getLevel(context);
		if (level.toLowerCase().equals("trace")) {
			log.trace(buffer.toString());
		}
		if (level.toLowerCase().equals("debug")) {
			log.debug(buffer.toString());
		}
		if (level.toLowerCase().equals("info")) {
			log.info(buffer.toString());
		}
		if (level.toLowerCase().equals("warn")) {
			log.warn(buffer.toString());
		}
		if (level.toLowerCase().equals("error")) {
			log.error(buffer.toString());
		}
		
		return false;
	}
	
	@Attribute(localName="message", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="Beginning XChain Stack trace")
	public abstract String getMessage(JXPathContext context);

	@Attribute(localName="level", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="debug")
	public abstract String getLevel(JXPathContext contect);
	
	@Attribute(localName="logger", type=AttributeType.ATTRIBUTE_VALUE_TEMPLATE, defaultValue="org.xchain.namespaces.core.TraceCommand")
	public abstract String getLogger(JXPathContext contect);
}
