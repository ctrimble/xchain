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

import org.xchain.annotations.Element;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.impl.ChainImpl;
import org.xchain.framework.sax.SerializerStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>The &lt;sax:serializer/&gt; element adds a serialization stage to the pipeline.</p>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@Element(localName="serializer")
public abstract class SerializerCommand
  extends ChainImpl
{
  private static Logger log = LoggerFactory.getLogger( SerializerCommand.class );

  @Attribute(localName="method", type=AttributeType.JXPATH_VALUE, defaultValue="'xml'")
  public abstract String getMethod( JXPathContext context )
    throws Exception;

  @Attribute(localName="indent", type=AttributeType.JXPATH_VALUE)
  public abstract Boolean getIndent( JXPathContext context )
    throws Exception;
  public abstract boolean hasIndent();

  public boolean execute( JXPathContext context )
    throws Exception
  {
    Boolean indent = null;
    if( hasIndent() ) {
      indent = getIndent(context);
    }

    // add the serializer to the pipeline.
    PipelineCommand.getPipelineConfig().getCompositeStage().addStage( new SerializerStage( getMethod( context ), indent ) );
    
    // return false, allowing other pipelines to execute.
    return false;
  }
}
