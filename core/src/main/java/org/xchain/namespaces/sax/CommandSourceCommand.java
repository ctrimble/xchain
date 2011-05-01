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

import java.net.URL;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Command;
import org.xchain.annotations.Element;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.framework.sax.ContextInputSource;
import org.xchain.impl.ChainImpl;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * <p>The &lt;sax:command-source/&gt; command is used in a sax pipeline to get source nodes from a command.  When using this element, it is important to
 * understand when it's children will not be executed until the surrounding pipeline element has finished building the pipeline.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   &lt;sax:command-source&gt; 
 *     ...
 *   &lt;/sax:command-source&gt;
 *   ...
 *   &lt;sax:result&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="command-source")
public class CommandSourceCommand
  extends ChainImpl
{
  public static Logger log = LoggerFactory.getLogger(CommandSourceCommand.class);

  /**
   * The name of the parameter.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    InputSource inputSource = null;
    XMLReader xmlReader = null;
    DependencyTracker dependencyTracker = DependencyTracker.getInstance();
    URL url = null;

      // create a catalog input source.
      inputSource = new ContextInputSource( context );

      // create an xml reader for the command.
      xmlReader = new CommandXmlReader( new ChildrenCommand() );

      // set the url for the catalog.

      // tell the dependency tracker that we are not caching.
      // TODO: let the cache know that this document will change.

    if( url != null ) {
      dependencyTracker.dependencyFound( url );
    }

    // set the source in the pipeline config.
    PipelineCommand.getPipelineConfig().setSource(inputSource);
    if( xmlReader != null ) {
      PipelineCommand.getPipelineConfig().setXmlReader(xmlReader);
    }

    return false;
  }

  public class ChildrenCommand
    implements Command
  {
    public boolean execute( JXPathContext context )
      throws Exception
    {
      return CommandSourceCommand.super.execute(context);
    }
  }
}
