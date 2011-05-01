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
package org.xchain.framework.strategy;

import org.xchain.framework.net.DependencyTracker;


/**
 * Interface for a Consumer.  Consumers will take a resource identified by a systemId and load
 * it from a SourceStrategy.  The resource will be parsed and formatted into a proper Object. 
 *
 * @param <T> The object type to be constructed by the consumer.
 * @param <S> The data source for the consumer.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
public interface ConsumerStrategy<T, S> {
	/**
	 * Load the requested object identified by the given systemId from the given SourceStrategy.  When
	 * loading information from the source the DependencyTracker must be used when resolving Uri's
	 * to dependencies.
	 * 
	 * @param systemId The identifier for the object to be loaded.
	 * @param sourceStrategy The strategy for getting the source.
	 * @param tracker The dependency tracker.
	 * 
	 * @return The loaded object.  Null if no such object could be found.
	 */
	public T consume(String systemId, SourceStrategy<S> sourceStrategy, DependencyTracker tracker) throws Exception;
}
