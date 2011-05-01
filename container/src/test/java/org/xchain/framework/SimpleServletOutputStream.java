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
package org.xchain.framework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * SimpleServletOutputStream
 * 
 * Servlet responses must have a ServletOutputStream for the Servlet
 * to write the response.  This is a simple implementation that
 * wraps a ByteArrayOutputStream.
 *
 * @author Devon Tackett
 */
public class SimpleServletOutputStream
  extends ServletOutputStream
{
  private ByteArrayOutputStream output = new ByteArrayOutputStream();
  
  @Override
  public void write(int data) throws IOException {
    output.write(data);
  }

  /**
   * @return The content that was written to this output stream.
   */
  public String getOutput() {
    return output.toString();
  } 
}
