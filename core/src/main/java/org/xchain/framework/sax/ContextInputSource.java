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

import org.xml.sax.InputSource;
import org.apache.commons.jxpath.JXPathContext;
import java.io.InputStream;
import java.io.Reader;

/**
 * An InputSource implementation that contains a JXPathContext.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class ContextInputSource
  extends InputSource
{
  protected JXPathContext context = null;

  public ContextInputSource() {}
  public ContextInputSource( JXPathContext context )
  {
    this.context = context;
  }

  public void setContext( JXPathContext context )
  {
    this.context = context;
  }

  public JXPathContext getContext()
  {
    return this.context;
  }

  public InputStream getByteStream()
  {
    throw new UnsupportedOperationException("ByteStreams are not supported by ContextInputSource.");
  }

  public void setByteStream( InputStream inputStream )
  {
    throw new UnsupportedOperationException("ByteStreams are not supported by ContextInputSource.");
  }

  public void setEncoding( String encoding )
  {
    throw new UnsupportedOperationException("Encoding is not supported by ContextInputSource.");
  }

  public String getEncoding()
  {
    throw new UnsupportedOperationException("Encoding is not supported by ContextInputSource.");
  }

  public void setCharacterStream( Reader characterStream )
  {
    throw new UnsupportedOperationException("Readers are not supported by ContextInputSource.");
  }

  public Reader getCharacterStream()
  {
    throw new UnsupportedOperationException("Readers are not supported by ContextInputSource.");
  }
}
