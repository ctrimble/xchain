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
import org.xchain.framework.sax.HandlerWrapper;
import org.xchain.annotations.Element;

import javax.xml.transform.sax.SAXResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The &lt;sax:null-result&gt; element creates a sax result that does nothing with the sax events it receives from the pipeline.</p>
 * a source, then be followed by zero or more transformations, followed by a result.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   ...
 *   &lt;sax:null-result/&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@Element(localName="null-result")
public class NullResultCommand
  implements Command
{
  public static Logger log = LoggerFactory.getLogger(NullResultCommand.class);

  public boolean execute( JXPathContext context )
    throws Exception
  {
    SAXResult result = new SAXResult();

    // configure the null result.
    result.setHandler(new HandlerWrapper());
    result.setSystemId("resource://context-class-loader/com/meltmedia/xchain/sax/null");

    // set the result in the pipeline config.
    PipelineCommand.getPipelineConfig().getCompositeStage().setResult(result);

    return false;
  }
}
