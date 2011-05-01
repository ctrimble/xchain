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
package org.xchain.framework.javascript;

import java.io.OutputStream;

/**
 * Interface for defining the mechanism by which the content of a given set of URLs is to be merged. This requires the
 * use of a manifest indicating each resource to merge. The format of the manifest is simple: each line contains a 
 * relative system-id pointing to a file to be merged. 
 * 
 * @author John Trimble
 *
 */
public interface IMergeStrategy {
  /**
   * Merges the content of each relative system-id and writes the result to the <code>output</code>. Note, the 
   * <code>output</code> stream is not closed by this method; the caller is responsible for closing the <code>output</code>.
   * 
   * @param manifestSystemId - A system-id pointing to the manifest. The manifest dictates whose content is to be merged.
   * @param output
   * @throws Exception
   */
  public void merge(String manifestSystemId, OutputStream output) throws Exception;
}
