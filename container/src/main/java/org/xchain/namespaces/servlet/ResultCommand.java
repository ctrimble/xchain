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
package org.xchain.namespaces.servlet;

import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.jxpath.JXPathContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.Command;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;
import org.xchain.annotations.PrefixMapping;
import org.xchain.namespaces.sax.PipelineCommand;
import org.xchain.framework.util.ContentType;

/**
 * Set the current Pipeline result to be written out to a ServletResponse.  If the <code>media-type</code> attribute is not
 * specified then the media type of the current Pipeline will be used.  If the Pipeline does not specify a media type and
 * the <code>media-type</code> attribute is not specified then the media type will default to <code>text/html</code>.
 * 
 * <code class="source">
 * &lt;xchain:result xmlns:xchain="http://www.xchain.org/container/1.0"/&gt;
 * </code>
 *
 * @author Mike Moulton
 * @author Devon Tackett
 * @author Christian Trimble
 * @author Jason Rose
 * @author Josh Kennedy
 */
@Element(localName="result")
public abstract class ResultCommand
  implements Command
{

  public static Logger log = LoggerFactory.getLogger(ResultCommand.class);

  /**
   * The media type for the response. 
   */
  @Attribute(localName="media-type", type=AttributeType.JXPATH_VALUE)
  public abstract String getMediaType( JXPathContext context );
  public abstract boolean hasMediaType();
  
  /**
   * The location of the ServletResponse. 
   */
  @Attribute(
      localName="result",
      type=AttributeType.JXPATH_VALUE,
      defaultValue="$" + Constants.DEFAULT_PREFIX + ":" + Constants.RESPONSE,
      defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
     )
  public abstract ServletResponse getResult(JXPathContext context);
  
  /**
   * The flag to disable response caching.
   */
  @Attribute(localName="disable-caching", type=AttributeType.JXPATH_VALUE, defaultValue="'false'")
  public abstract Boolean getDisableCaching( JXPathContext context );

  @Attribute(
      localName="request",
      type=AttributeType.JXPATH_VALUE,
      defaultValue="$" + Constants.DEFAULT_PREFIX + ":" + Constants.REQUEST,
      defaultPrefixMappings={@PrefixMapping(uri=Constants.URI, prefix=Constants.DEFAULT_PREFIX)}
     )
  public abstract ServletRequest getRequest(JXPathContext context);

  public boolean execute( JXPathContext context )
    throws Exception
  {
    ServletResponse response = getResult(context);

    // set the result in the pipeline config's composite stage
    Result result = new StreamResult( response.getOutputStream() );
    PipelineCommand.getPipelineConfig().getCompositeStage().setResult(result);

    // set the content type for the response
    ContentType contentType;

    String requestedMediaType = hasMediaType() ? getMediaType( context ) : PipelineCommand.getPipelineConfig().getCompositeStage().getMediaType();
    String requestedEncoding = PipelineCommand.getPipelineConfig().getCompositeStage().getEncoding();

    String charset = ContentType.DEFAULT_CHARSET;
    if (requestedEncoding != null) {
      charset = ContentType.ATTR_CHARSET + "=" + requestedEncoding;
    }

    if (requestedMediaType != null) {
      contentType = new ContentType( requestedMediaType + ";" + charset );
    } else {
      contentType = ContentType.TEXT_HTML;
    }

    // If the browser does not accept XHTML and we are requesting XHTML, default to HTML
    String acceptHeader = ((HttpServletRequest)getRequest( context )).getHeader( "Accept" );
    if ( contentType.mediaTypeMatch( ContentType.TEXT_XHTML ) && !ContentType.acceptsContentType( acceptHeader, ContentType.TEXT_XHTML ) ) {
      contentType = ContentType.TEXT_HTML;
    }

    response.setContentType( contentType.toString() );
    
    if( getDisableCaching(context) ) {
      disableCaching((HttpServletResponse) response);
    }

    return false;
  }
  
  protected void disableCaching(HttpServletResponse response) {
    response.setHeader("Cache-Control", "must-revalidate");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Paragma", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0L);
  }
}
