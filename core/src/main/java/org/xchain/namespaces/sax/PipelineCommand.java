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

import javax.xml.transform.Result;
import javax.xml.transform.URIResolver;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.SourceLocator;

import org.apache.commons.jxpath.JXPathContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.XMLReaderFactory;

import org.xchain.impl.ChainImpl;

import org.xchain.framework.lifecycle.XmlFactoryLifecycle;
import org.xchain.framework.sax.Pipeline;
import org.xchain.framework.sax.CompositeStage;
import org.xchain.framework.sax.UrlFactoryEntityResolver;
import org.xchain.framework.net.UrlFactoryUriResolver;
import org.xchain.framework.net.DependencyTracker;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

/**
 * <p>The &lt;sax:pipeline&gt; command creates a sax pipeline, and then executes it. A pipeline should start with
 * a source, then be followed by zero or more transformations, followed by a result.</p>
 *
 * <code class="source">
 * &lt;sax:pipeline xmlns:sax="http://www.xchain.org/sax/1.0"&gt;
 *   ...
 * &lt;/sax:pipeline&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
@Element(localName="pipeline")
public abstract class PipelineCommand
  extends ChainImpl
{
  /** The log for this class. */
  private static Logger log = LoggerFactory.getLogger( PipelineCommand.class );

  /**
   * <p>The ThreadLocal stack of composite stages.  The current pipeline being built is on the top of the stack.</p>
   */
  private static ThreadLocal<LinkedList<PipelineConfig>> pipelineConfigStackThreadLocal = new ThreadLocal<LinkedList<PipelineConfig>>();

  /**
   * <p>Returns the composite stage stack for this thread.</p>
   * @returns the current stack of pipeline configuration objects for this thread.
   */
  public static LinkedList<PipelineConfig> getPipelineConfigStack()
  {
    LinkedList<PipelineConfig> pipelineConfigStack = pipelineConfigStackThreadLocal.get();

    if( pipelineConfigStack == null ) {
      pipelineConfigStack = new LinkedList<PipelineConfig>();
      pipelineConfigStackThreadLocal.set(pipelineConfigStack);
    }

    return pipelineConfigStack;
  }

  /**
   * <p>Pushes a pipeline configuration object on the stack.</p>
   * @param pipelineConfig the pipeline configuration object to push.
   */
  public static void pushPipelineConfig( PipelineConfig pipelineConfig )
  {
    getPipelineConfigStack().addFirst(pipelineConfig);
  }

  /**
   * <p>Pops a top pipeline configuration object off of the stack.</p>
   * @return the pipeline configuration object popped off of the stack.
   */
  public static PipelineConfig popPipelineConfig()
  {
    return getPipelineConfigStack().removeFirst();
  }

  /**
   * <p>Returns the top item on the pipeline configuration stack, without removing it from the stack.</p>
   * @return the pipeline configuration object current on the top of the stack.
   */
  public static PipelineConfig getPipelineConfig()
  {
    return getPipelineConfigStack().getFirst();
  }

  /**
   * <p>The uri resolver to use when building this pipeline.</p>
   * @param context the JXPathContext to evaluate against.
   */
  @Attribute(localName="uri-resolver", type=AttributeType.JXPATH_VALUE)
  public abstract URIResolver getUriResolver( JXPathContext context );
  public abstract boolean hasUriResolver();

  public URIResolver getUriResolverSafe( JXPathContext context )
  {
    URIResolver uriResolver = hasUriResolver()?getUriResolver(context):null;

    if( uriResolver == null ) {
      if (log.isDebugEnabled()) {
    	log.debug("The uri resolver could not not be found in context.  The default uri resolver will be created.");
      }
      
      uriResolver = DependencyTracker.getInstance().createDependencyUriResolver(new UrlFactoryUriResolver());
    }

    return uriResolver;
  }

  /**
   * Constructs a new PipelineCommand object.
   */
  public PipelineCommand()
  {

  }

  /**
   * <p>Constructs a new Pipeline and places it into the context at path.  Nested commands can be used to
   * add transforms to the pipeline.</p>
   * @param context the JXPathContext to evaluate against.
   */
  public boolean execute( JXPathContext context )
    throws Exception
  {
    PipelineConfig config = new PipelineConfig();

    // create the dependency uri resolver.

    try {
      pushPipelineConfig(config);

      config.setCompositeStage(new CompositeStage());
      config.setUriResolver(getUriResolverSafe(context));
      config.setEntityResolver(new UrlFactoryEntityResolver());
      config.setErrorListener(new ErrorListener()
      {
      public void error( TransformerException e ) {
        SourceLocator sourceLocator = e.getLocator();
        StringBuffer message = new StringBuffer();

        if( sourceLocator != null ) {
          message.append(sourceLocator.getSystemId()+":"+sourceLocator.getLineNumber()+":"+sourceLocator.getColumnNumber()+" - ");
        }
        message.append(e.getMessage());

        log.error(message.toString(), e);
        throw new RuntimeException(message.toString(), e);
      }

      public void fatalError( TransformerException e ) {
        SourceLocator sourceLocator = e.getLocator();
        StringBuffer message = new StringBuffer();

        if( sourceLocator != null ) {
          message.append(sourceLocator.getSystemId()+":"+sourceLocator.getLineNumber()+":"+sourceLocator.getColumnNumber()+" - ");
        }
        message.append(e.getMessage());

        log.error(message.toString(), e);
        throw new RuntimeException(message.toString(), e);
      }

      public void warning( TransformerException e ) {
        log.warn("SAX Parser Warning.", e);
      }
      });

      // call all of the child commands.
      super.execute(context);

      // put a pipeline in the context to add to.
      Pipeline pipeline = new Pipeline();

      // set the composite stage.
      pipeline.setCompositeStage(config.getCompositeStage());

      if( config.getXmlReader() == null ) {
        // set the xml reader on the pipeline.
        pipeline.setXmlReader(XmlFactoryLifecycle.newXmlReader());
      }
      else {
        pipeline.setXmlReader(config.getXmlReader());
      }

      if( config.getEntityResolver() != null ) {
        pipeline.getXmlReader().setEntityResolver(config.getEntityResolver());
      }

      // set the source on the pipeline.
      pipeline.setSource(config.getSource());

      // execute the pipeline.
      pipeline.execute();

    }
    finally {
      // reset the stage list.
      popPipelineConfig();

      // DependencyUriResolverFactory.destroyDependencyUriResolver( uriResolver );
    }
    // the pipeline is built and executed.
    return false;
  }

  /**
   * <p>The configuration for a pipeline that is being assembled.<p>
   */
  public static class PipelineConfig
  {
    protected URIResolver uriResolver = null;
    protected ErrorListener errorListener = null;
    protected CompositeStage compositeStage = new CompositeStage();
    protected XMLReader reader = null;
    protected InputSource source = null;
    protected Result result = null;
    protected EntityResolver entityResolver = null;

    public URIResolver getUriResolver() { return uriResolver; }
    public void setUriResolver( URIResolver uriResolver ) { this.uriResolver = uriResolver; }
    public ErrorListener getErrorListener() { return this.errorListener; }
    public void setErrorListener( ErrorListener errorListener ) { this.errorListener = errorListener; }
    public CompositeStage getCompositeStage() { return this.compositeStage; }
    public void setCompositeStage( CompositeStage compositeStage ) { this.compositeStage = compositeStage; }
    public XMLReader getXmlReader() { return this.reader; }
    public void setXmlReader(XMLReader reader) { this.reader = reader; }
    public InputSource getSource() { return this.source; } 
    public void setSource( InputSource source ) { this.source = source; }
    public EntityResolver getEntityResolver() { return this.entityResolver; }
    public void setEntityResolver( EntityResolver entityResolver ) { this.entityResolver = entityResolver; }
  }
}
