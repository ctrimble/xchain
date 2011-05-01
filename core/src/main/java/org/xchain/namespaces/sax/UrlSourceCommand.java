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
import org.xchain.Locatable;
import org.xchain.annotations.Element;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.sax.CommandXmlReader;
import org.xchain.framework.sax.ContextInputSource;
import org.xchain.impl.ChainImpl;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.Locator;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@Element(localName="url-source")
public abstract class UrlSourceCommand
  extends ChainImpl
  implements Locatable
{
  public static Logger log = LoggerFactory.getLogger(UrlSourceCommand.class);

  @Attribute(localName="system-id", type=AttributeType.JXPATH_VALUE)
  public abstract String getSystemId( JXPathContext context );
  /**
   * The name of the parameter.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    InputSource inputSource = null;
    XMLReader xmlReader = null;
    DependencyTracker dependencyTracker = DependencyTracker.getInstance();
    String systemId = getSystemId(context);
    URL url = UrlFactory.getInstance().newUrl(getLocator().getSystemId(), systemId);
    inputSource = new InputSource();
    inputSource.setSystemId(systemId);
    inputSource.setByteStream(url.openStream());

    // set the source in the pipeline config.
    PipelineCommand.getPipelineConfig().setSource(inputSource);
    if( xmlReader != null ) {
      PipelineCommand.getPipelineConfig().setXmlReader(xmlReader);
    }

    return false;
  }
}
