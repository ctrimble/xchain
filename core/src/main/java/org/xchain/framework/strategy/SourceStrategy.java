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
 * Interface for source strategies.  A source strategy is responsible for getting the source data
 * for a resource identified by the given systemId.
 *
 * @param <S> The source type to be returned.
 *
 * @author Devon Tackett
 * @author Christian Trimble
 */
public interface SourceStrategy<S> {
	/**
	 * Get the data source for the given systemId.
	 * 
	 * @param systemId The identifier for the source.
	 * 
	 * @return The source.
	 */
	public S getSource(String systemId) throws Exception;
}
