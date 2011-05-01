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
package org.xchain.namespaces.hibernate.test.command;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.namespaces.hibernate.test.om.Person;

/**
 * @author Devon Tackett
 * @author Christian Trimble
 */
@Element(localName="iterate-empty")
public abstract class TestIterateEmpty
  implements Command
{
	@Attribute(localName="source", type=AttributeType.JXPATH_VALUE)
	public abstract Iterator getSource( JXPathContext context );
	public abstract boolean hasSource();				

	@Attribute(localName="result", type=AttributeType.QNAME)
	public abstract String getResult( JXPathContext context );
	public abstract boolean hasResult();	  

	public boolean execute( JXPathContext context )
		throws Exception
	{
		Iterator<Person> sourceIterator = getSource(context);
		
		context.setValue(getResult(context), !sourceIterator.hasNext());
		
		// Allow execution to continue.
		return false;
	}
}
