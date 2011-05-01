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
import org.hibernate.Query;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.PrefixMapping;
import org.xchain.impl.ChainImpl;

/**
 * Base query command class.  All commands which run within a context of a query should extend this class.
 * 
 * @author Devon Tackett
 * @author Christian Trimble
 *
 * @see org.hibernate.Query
 */
public abstract class AbstractQueryCommand
  extends ChainImpl
{
  /**
   * The location of the query to use. 
   */
  @Attribute(
        localName="query",
        type=AttributeType.JXPATH_VALUE,
        defaultValue="$" + Constants.DEFAULT_PREFIX + ":" + Constants.QUERY,
        defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
      )
      
      public abstract Query getQuery( JXPathContext context );
}
