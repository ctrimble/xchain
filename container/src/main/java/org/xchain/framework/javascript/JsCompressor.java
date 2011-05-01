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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dojotoolkit.shrinksafe.Compressor;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.ShellContextFactory;

/**
 * This class is essentially a reimplementation of <code>org.dojotoolkit.shrinksafe.Main</code> for using 
 * <code>org.dojotoolkit.shrinksafe.Compressor</code>.
 * 
 * @author John Trimble
 * @author Josh Kennedy
 */
public class JsCompressor {
  private static Logger log = LoggerFactory.getLogger(JsCompressor.class);
  
  private static int DEFAULT_OPTIMIZATION_LEVEL = 0;
  
  private boolean respectNoCompressFlag = true;
  private int optimizationLevel;
  
  public JsCompressor() {
    this(DEFAULT_OPTIMIZATION_LEVEL);
  }
  
  public JsCompressor(int optimizationLevel) {
    this.optimizationLevel = optimizationLevel;
  }
  
  /**
   * Reads each URL in <code>javaScriptUrls</code>, in order, compresses the data and streams the result to the
   * <code>output</code> stream. Does not close <code>output</code>.
   * 
   * @param javaScriptUrls - An array of URLs pointing to valid Java Script content.
   * @param output - The stream to which the compressed content is written.
   * @throws IOException
   */
  public void compress(URL[] javaScriptUrls, OutputStream output) throws IOException {
    // Copied from org.dojotoolkit.shrinksafe.Main
    Global global = new Global();
    ShellContextFactory shellContextFactory = new ShellContextFactory();
    shellContextFactory.setOptimizationLevel(optimizationLevel);
    ToolErrorReporter errorReporter = new ToolErrorReporter(false, global.getErr());
    shellContextFactory.setErrorReporter(errorReporter);
    IProxy iproxy = createCompressProxy(javaScriptUrls, output);
    global.init(shellContextFactory);
    shellContextFactory.call(iproxy);
    if( iproxy.hasException() ) {
      if( iproxy.getException() instanceof IOException )
        throw (IOException) iproxy.getException();
      else if( iproxy.getException() instanceof RuntimeException )
        throw (RuntimeException) iproxy.getException();
      else 
        throw new RuntimeException(iproxy.getException());
    }
  }
  
  /**
   * Creates an IProxy object for calling <code>compressJsUrlArray</code> inside the Rhino Java Script engine.
   * @param javaScriptUrls
   * @param output
   * @return
   */
  private IProxy createCompressProxy(final URL[] javaScriptUrls, final OutputStream output) {
    IProxy iproxy = new IProxy() {
      public Object execute(Context cx) throws Exception {
        compressJsUrlArray(cx, javaScriptUrls, output);
        return null;
      }
    };
    return iproxy;
  }
  
  /**
   * Reads the content of <code>url</code> and stores it into a returned string.
   * @param url
   * @return
   * @throws IOException
   */
  private static String readContent(URL url) throws IOException {
    InputStream in = null;
    ByteArrayOutputStream out = null;
    try {
      in = url.openConnection().getInputStream();
      out = new ByteArrayOutputStream();
      copyStream(in, out, 2048);
      return out.toString();
    } finally {
      close(in, log);
      close(out, log);
    }
  }
  
  /**
   * Returns whether or not the given URL should be compressed. This returns true if the query portion of the url does 
   * not contain "compress=false" or the <code>respectNoCompressFlag</code> is false; otherwise, false is returned.
   * @param url
   * @return
   */
  private boolean shouldCompress(URL url) {
    String query = url.getQuery();
    // This is not the best way to check a parameter, but if it works...
    return query == null || !respectNoCompressFlag || !(query.toLowerCase().contains("compress=false"));
  }
  
  /**
   * Compresses each URL in <code>jsUrls</code> by using <code>org.dojotoolkit.shrinksafe.Compressor</code>. Streams
   * result to <code>output</code>. Must be called from within the Rhino Java Script Engine.
   * 
   * @param cx
   * @param jsUrls
   * @param output
   * @throws IOException
   */
  private void compressJsUrlArray(Context cx, URL[] jsUrls, OutputStream output) throws IOException {
    for( int i = 0; i < jsUrls.length; i++ ) {
      URL jsUrl = jsUrls[i]; 
      String data = readContent(jsUrl);
      String compressedData = data;
      if( shouldCompress(jsUrl) ) {
        if( log.isDebugEnabled() ) log.debug("Compressing URL: "+jsUrl);
        compressedData = Compressor.compressScript(data, 0, 1, false);
      } else if( log.isDebugEnabled() ) log.debug("Not compressing URL: "+jsUrl);
      ByteArrayInputStream sin = null;
      try {
        sin = new ByteArrayInputStream(compressedData.getBytes());
        copyStream(sin, output, 2048);
      } finally {
        close(sin, log);
      }
    }
  }
  
  /**
   * Proxy class for calling a java method from within the Rhino JavaScript engine.
   */
  private static abstract class IProxy implements ContextAction {
    private Exception exception;
    
    protected abstract Object execute(Context cx) throws Exception;
    
    public Exception getException() {
      return this.exception;
    }
    
    public boolean hasException() {
      return this.exception != null;
    }

    public Object run(Context cx) {
      this.exception = null;
      Object result;
      try {
        result = this.execute(cx);
        return result;
      } catch (Exception e) {
        this.exception = e;
        return null;
      }
    }
  }
}
