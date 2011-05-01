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
package org.xchain.framework.sax;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.Result;
import org.xml.sax.ContentHandler;

/**
 * @author Mike Moulton
 * @author Christian Trimble
 */
public class TransformerHandlerStage
  implements Stage
{
  protected TransformerHandler transformerHandler;

  public TransformerHandlerStage( TransformerHandler transformerHandler )
  {
    this.transformerHandler = transformerHandler;
  }

  protected TransformerHandler getTransformerHandler() { return this.transformerHandler; }

  public ContentHandler getContentHandler()
  {
    return transformerHandler;
  }

  public void setResult( Result result )
  {
    transformerHandler.setResult(result);
  }

  /**
   * Returns the internet media type for this stage
   */
  public String getMediaType()
  {
    String mediaType = transformerHandler.getTransformer().getOutputProperties().getProperty("media-type");
    return (mediaType != null && !"".equals(mediaType)) ? mediaType : null;
  }

  /**
   * Returns the character encoding for this stage
   */
  public String getEncoding()
  {
    String encoding = transformerHandler.getTransformer().getOutputProperties().getProperty("encoding");
    return (encoding != null && !"".equals(encoding)) ? encoding : null;
  }

}
