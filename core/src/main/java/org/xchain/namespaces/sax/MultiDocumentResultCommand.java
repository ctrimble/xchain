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
package org.xchain.namespaces.sax;

import org.apache.commons.jxpath.JXPathContext;

import org.xchain.Command;
import org.xchain.framework.sax.MultiDocumentResult;
import org.xchain.annotations.Element;
import javax.xml.transform.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The &lt;sax:multi-document-result/&gt; result adds a MultiDocumentResult element to the sax pipeline.</p>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@Element(localName="multi-document-result")
public class MultiDocumentResultCommand
  implements Command
{
  private static Logger log = LoggerFactory.getLogger(MultiDocumentResultCommand.class);

  /**
   * The name of the parameter.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    Result result = new MultiDocumentResult();

    // set the result in the pipeline config.
    PipelineCommand.getPipelineConfig().getCompositeStage().setResult(result);

    return false;
  }
}
