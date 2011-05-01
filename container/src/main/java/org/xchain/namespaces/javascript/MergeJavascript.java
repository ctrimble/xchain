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
package org.xchain.namespaces.javascript;

import static org.xchain.framework.util.IoUtil.*;

import java.net.URI;

import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.annotations.PrefixMapping;
import org.xchain.framework.javascript.BasicMergeStrategy;
import org.xchain.framework.javascript.CacheMergeStrategy;
import org.xchain.framework.javascript.CompressJavaScriptMergeStrategy;
import org.xchain.framework.javascript.IMergeStrategy;
import org.xchain.framework.lifecycle.Execution;
import org.xchain.framework.lifecycle.Lifecycle;
import org.xchain.namespaces.servlet.Constants;

import org.apache.commons.jxpath.JXPathContext;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic javascript merging command.
 * Will cache results for a merge of the compression flag is set.
 *
 * @author Mike Moulton
 * @author John Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
@Element(localName="merge-javascript")
public abstract class MergeJavascript
  implements Command 
{
  private static Logger log = LoggerFactory.getLogger(MergeJavascript.class);

  private static final BasicMergeStrategy BASIC_MERGE_STRATEGY = new BasicMergeStrategy();
  
  @Attribute(
    localName="response",
    type=AttributeType.JXPATH_VALUE,
    defaultValue="$servlet:response",
    defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
  )
  public abstract HttpServletResponse getResponse( JXPathContext context );

  @Attribute(localName="manifest", type=AttributeType.JXPATH_VALUE)
  public abstract String getManifest( JXPathContext context );
  
  @Attribute(localName="compress", defaultValue="'false'", type=AttributeType.JXPATH_VALUE)
  public abstract Boolean getCompress( JXPathContext context );
  
  @Attribute(localName="optimization-level", defaultValue="0", type=AttributeType.JXPATH_VALUE)
  public abstract Integer getOptimizationLevel( JXPathContext context );
  
  @Attribute(localName="debug", defaultValue="'false'", type=AttributeType.JXPATH_VALUE)
  public abstract Boolean getDebugChain( JXPathContext context );

  public IMergeStrategy createMergeStrategy( JXPathContext context ) {
    // We compress the files if either the compression flag is set and build monitoring is disabled, or the compression
    // and debug flags are both set.
    if( this.getCompress(context) && ( !Lifecycle.getLifecycleContext().getConfigContext().isMonitored() || getDebugChain(context) ) )
      return new CacheMergeStrategy(new CompressJavaScriptMergeStrategy(this.getOptimizationLevel(context)));
    return BASIC_MERGE_STRATEGY;
  }
  
  public boolean execute( JXPathContext context )
    throws Exception
  {
    IMergeStrategy mergeStrategy = createMergeStrategy(context);
    HttpServletResponse response = getResponse(context);
    String manifestSystemId = getManifest(context);

    if( manifestSystemId == null ) {
      return true;
    }

    // resolve the system id into an absolute URL.
    String absoluteManifestSystemId = URI.create(Execution.getSystemId()).resolve(manifestSystemId).toString();

    // set the status code to ok.
    response.setStatus(HttpServletResponse.SC_OK);

    // leave the content type header, it will be set by the pipeline.
    response.setContentType("text/javascript");

    try {
      mergeStrategy.merge(absoluteManifestSystemId, response.getOutputStream());
    } finally {
      close(response.getOutputStream(), log);
    }
    // allow other xchains to run.
    return true;
  }

}
