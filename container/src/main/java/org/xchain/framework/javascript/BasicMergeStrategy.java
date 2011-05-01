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
package org.xchain.framework.javascript;

import static org.xchain.framework.util.IoUtil.close;
import static org.xchain.framework.util.IoUtil.copyStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.net.UrlFactory;

/**
 * Port of Chris's original code from <code>org.xchain.namespaces.javascript.MergeJavascript</code> for merging 
 * multiple JavaScript files together. 
 * 
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
public class BasicMergeStrategy implements IMergeStrategy {
  private static Logger log = LoggerFactory.getLogger(BasicMergeStrategy.class);
  
  public void merge(String manifestSystemId, OutputStream output) throws Exception {
    // parse the manifest and output the files as we encounter them.
    String jsSystemId = null;
    String absoluteJsSystemId = null;
    URL manifestUrl = null;
    URL jsUrl = null;
    InputStream manifestIn = null;
    InputStream jsIn = null;
    OutputStream servletOut = null;
    LineNumberReader reader = null;
    PrintWriter servletWriter = null;
    try {
      servletWriter = new PrintWriter(new OutputStreamWriter(servletOut = output));

      servletWriter.append("//\n// Manifest System Id: ").append(manifestSystemId).append("\n//\n").flush();

      // get the url for the absolute system id.
      manifestUrl = UrlFactory.getInstance().newUrl(manifestSystemId);

      // create a reader that will allow us to track the line number of the system id we are working with.
      reader = new LineNumberReader(new InputStreamReader(manifestIn = manifestUrl.openStream()));

      while( (jsSystemId = reader.readLine()) != null ) {
        absoluteJsSystemId = URI.create(manifestSystemId).resolve(jsSystemId).toString();
        servletWriter.append("//\n// Script System Id: ").append(absoluteJsSystemId).append("\n//\n").flush();
        try {
          jsUrl = UrlFactory.getInstance().newUrl(absoluteJsSystemId);
          jsIn = jsUrl.openStream();
          copyStream(jsIn, servletOut, 2048);
        }
        catch( Exception e ) {
          if( log.isErrorEnabled() ) {
            log.error("Could not copy the contents of '"+absoluteJsSystemId+" to the servlet output stream.", e);
          }
          throw e;
        }
        finally {
          close(jsIn, log);
        }
      }
    }
    catch( Exception e ) {
      if( log.isErrorEnabled() ) {
        log.error("There was an error with the system id defined in '"+manifestSystemId+"'"+(reader != null ? " line number '"+reader.getLineNumber()+"'" : "")+".");
      }
      throw e;
    }
    finally {
      close(manifestIn, log);
    }
  }

}
