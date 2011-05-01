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

import java.util.LinkedList;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.xchain.Locatable;
import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.framework.sax.TransformerHandlerStage;
import org.xchain.impl.ChainImpl;

import org.xml.sax.Locator;
import javax.xml.namespace.QName;


/**
 * <p>The &lt;sax:transformer/&gt; adds a transform to a sax pipeline.  Currently, XChains supports both XSLT and STX transformations.  To add a transform to
 * a pipeline, include a &lt;sax:transformer/&gt; element between the source and result of a &lt;sax:pipeline/&gt; element.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   &lt;sax:source .../&gt;
 *   ...
 *   &lt;sax:transformer system-id="'relative-uri-of-template'"/&gt;
 *   ...
 *   &lt;sax:result .../&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * <p>Parameters can be passed to a template by including &lt;sax:parameter/&gt; elements inside the &lt;sax:transformer/&gt; element.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   &lt;sax:source .../&gt;
 *   ...
 *   &lt;sax:transformer system-id="'relative-uri-of-template'"&gt;
 *     &lt;sax:parameter name="'name'" value="'value'"/&gt;
 *   &lt;/sax:transformer&gt;
 *   ...
 *   &lt;sax:result .../&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * <p>Since the transformer element is a command, you can optionally include a template by adding a conditional element around it.  For example, you can add a template based on some test
 * using the $lt;xchain:if/&gt; element.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   &lt;sax:source .../&gt;
 *   ...
 *   &lt;xchain:if test="$test"/&gt;
 *     &lt;sax:transformer system-id="'relative-uri-of-template'"/&gt;
 *   &lt:/xcahin:if&gt;
 *   ...
 *   &lt;sax:result .../&gt;
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Josh Kennedy
 */
@Element(localName="transformer")
public abstract class TransformerCommand
  extends ChainImpl
  implements Locatable
{
  /** The log for the transformer command. */
  private static Logger log = LoggerFactory.getLogger(TransformerCommand.class);

  /**
   * The thread local stack of transformers currently being used by this transformer.
   */
  protected static ThreadLocal<LinkedList<Transformer>> transformerThreadLocal = new ThreadLocal<LinkedList<Transformer>>();

  /**
   * <p>The system id of the stylesheet.</p>
   * @param context the JXPathContext to evaluate against.
   */
  @Attribute(localName="system-id", type=AttributeType.JXPATH_VALUE)
  public abstract String getSystemId( JXPathContext context )
    throws Exception;

  public boolean execute( JXPathContext context )
    throws Exception
  {
    if( log.isDebugEnabled() ) {
      log.debug("Creating transformer stage for system id '"+getSystemId( context )+"'.");
    }

    Templates templates = null;
    TransformerHandler transformerHandler = null;
    Transformer transformer = null;

    try {
      // create the transformer handler for the templates object.
      transformerHandler = XmlFactoryLifecycle.newTransformerHandler(java.net.URI.create(getLocator().getSystemId()).resolve(getSystemId(context)).toString());

      // get the transformer from the transformer handler  object.
      transformer = transformerHandler.getTransformer();

      transformer.setURIResolver(PipelineCommand.getPipelineConfig().getUriResolver());
      if( PipelineCommand.getPipelineConfig().getErrorListener() != null ) {
        transformer.setErrorListener(PipelineCommand.getPipelineConfig().getErrorListener());
      }
    }
    catch( Exception e ) {
      if( log.isErrorEnabled() ) {
        log.error("Could not create transformer for system id '"+getSystemId( context )+"' due to an exception.", e);
      }
      throw e;
    }

    try {
      // push the transformer on the stack.
      pushCurrentTransformer( transformer );

      // allow the child templates to configure the transformer.
      super.execute( context );
    }
    finally {

      // pop the current transformer.
      popCurrentTransformer();
    }

    if( log.isDebugEnabled() ) {
      log.debug("Adding transformer to compositeStage.");
    }

    if( log.isDebugEnabled() ) {
      log.debug("Adding transformer to stage.");
    }

    // add the transformer to the pipeline.
    PipelineCommand.getPipelineConfig().getCompositeStage().addStage(new TransformerHandlerStage(transformerHandler));
    
    // return false, allowing other pipelines to execute.
    return false;
  }

  /**
   * <p>Returns the current transfrormer stack.  The current transformer holds transformers that are being configured by the current thread.</p>
   * @return the stack of transformers being configured by the current thread.
   */
  public static LinkedList<Transformer> getCurrentTransformerStack() 
  {
    LinkedList<Transformer> currentTransformerStack = transformerThreadLocal.get();

    if( currentTransformerStack == null ) {
      currentTransformerStack = new LinkedList<Transformer>();
      transformerThreadLocal.set(currentTransformerStack);
    }

    return currentTransformerStack;
  }

  /**
   * <p>Pushes a transformer onto the stack of transfromers for this thread.</p>
   * @param transformer the transformer to push onto the current transfromer stack.
   */
  public static void pushCurrentTransformer( Transformer transformer )
  {
    getCurrentTransformerStack().addFirst( transformer );
  }

  /**
   * <p>Pops the current transformer off of the stack of transfromers for this thread.</p>
   * @return the transformer that was on the top of the current transformer stack.
   */
  public static Transformer popCurrentTransformer()
  {
    return getCurrentTransformerStack().removeFirst();
  }

  /**
   * <p>Returns the current transformer on the top of the stack, without removing it from the stack.</p>
   * @return the current transformer for this thread.
   */
  public static Transformer getCurrentTransformer()
  {
    return getCurrentTransformerStack().getFirst();
  }
}
