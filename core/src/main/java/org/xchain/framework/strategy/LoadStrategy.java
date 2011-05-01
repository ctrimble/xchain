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



/**
 * Interface for loading strategies.  A loading strategy is responsible for invoking the consumer with the given
 * source strategy and creating the final object.
 *
 * @param <T> The type of object to be loaded.
 * @param <S> The source class to be loaded from.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
public interface LoadStrategy<T, S> {
	/**
	 * Load the object identified by the given systemId.
	 * 
	 * @param systemId The identifier for the object to be loaded.
	 * @param sourceStrategy The strategy for getting the source for the object.
	 * @param consumerStrategy The strategy for turning the source into a proper object.
	 * 
	 * @return The specified object.
	 */
	public T getObject(String systemId, SourceStrategy<S> sourceStrategy, ConsumerStrategy<T, S> consumerStrategy) throws Exception;
}
