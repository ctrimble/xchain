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

import static org.xchain.framework.util.IoUtil.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.lifecycle.ContainerLifecycle;
import org.xchain.framework.lifecycle.Execution;

/**
 * Decorator <code>IMergeStrategy</code> which caches the result of performing a merge based on the current catalog. 
 * This assumes that for all calls made to <code>merge(...)</code> will have the same result for whatever catalog is the
 * current catalog. 
 * 
 * @author John Trimble
 * @author Josh Kennedy
 */
public class CacheMergeStrategy implements IMergeStrategy {
  private static Logger log = LoggerFactory.getLogger(CacheMergeStrategy.class);
  
  private static final int BUFFER_SIZE = 2048;
  private static final String TEMP_DIR_ATTRIBUTE_NAME = "javax.servlet.context.tempdir";
  // This is used to assist in making a prefix for temporary merged js files.
  private static final Pattern URL_FILE_NAME_PATTERN = Pattern.compile("\\A.*[/]([a-zA-Z0-9.-]+)([?].*)?\\Z");
  
  // Mapping of catalog system-ids to compressed java script files.
  private static final Map<String, URL> catalogCacheUrlMap = Collections.synchronizedMap(new HashMap<String, URL>());
  
  // Lock to ensure only on compressed js file created at a time.
  private static final Object MERGE_LOCK = new Object();
  
  private IMergeStrategy childMergeStrategy;
  
  public CacheMergeStrategy(IMergeStrategy strategy) {
    this.childMergeStrategy = strategy;
  }
  
  /**
   * If this is the first time this method has been called for the current catalog, then, in a globally
   * synchronized block, the merge is produced by delegating to the child <code>IMergeStrategy</code> instance.
   * Otherwise, the cached result is written to the output stream.
   */
  public void merge(String manifestSystemId, OutputStream output) throws Exception {
    String systemId = Execution.getSystemId();
    URL compressedUrl = createOrGetMergeUrl(systemId, manifestSystemId);
    InputStream in = null;
    try {
      in = compressedUrl.openConnection().getInputStream();
      copyStream(in, output, BUFFER_SIZE);
    } finally {
      close(in, log);
    }
  }
  
  private URL createOrGetMergeUrl(String systemId, String manifestSystemId) throws Exception {
    URL compressedUrl = catalogCacheUrlMap.get(systemId);
    if( compressedUrl == null ) {
      synchronized(MERGE_LOCK) {
        // Check to see if some other Thread did the work for us while we were waiting.
        compressedUrl = catalogCacheUrlMap.get(systemId);
        if( compressedUrl == null ) {
          // Okay... lets compress these java script files.
          File tempFile = createMergeCacheFile(systemId);
          OutputStream out = null;
          // compress
          try {
            out = new FileOutputStream(tempFile);
            childMergeStrategy.merge(manifestSystemId, out);
            compressedUrl = tempFile.toURL();
          } catch(IOException e){
            // Delete the temporary file if there is an exception... otherwise, we will end up creating a temporary file 
            // each time this apparently broken execution path runs.
            forceDeleteTempFile(tempFile);
            throw e;
          } catch( RuntimeException e) {
            // Delete the temporary file if there is an exception... otherwise, we will end up creating a temporary file 
            // each time this apparently broken execution path runs.
            forceDeleteTempFile(tempFile);
            throw e;
          } finally {
            close(out, log);
          }
          catalogCacheUrlMap.put(systemId, compressedUrl);
        }
      } // end synchronized block
    }
    return compressedUrl;
  }
  
  private File createMergeCacheFile(String systemId) throws IOException {
    File tempDir = null;
    // We get the temporary directory from a servlet context attribute. If its a string, we create a File object for
    // that path. If its a File, then we are set. If we get null or the empty string, the we throw an IOException.
    Object tempDirAttrValueObject = ContainerLifecycle.getServletContext().getAttribute(TEMP_DIR_ATTRIBUTE_NAME);
    if( tempDirAttrValueObject instanceof String ) {
      String tempDirAttrValue = (String) tempDirAttrValueObject;
      if( tempDirAttrValue == null || "".equals(tempDirAttrValue) )
        throw new IOException("Servlet context attribte '"+TEMP_DIR_ATTRIBUTE_NAME+"' was not set.");
      tempDir = new File(tempDirAttrValue);
    } else if( tempDirAttrValueObject instanceof File )
      tempDir = (File) tempDirAttrValueObject;
    
    if( tempDir == null ) {
      throw new IOException("Could not find temporary directory.");
    }
    
    // For the prefix, try to incorporate the name of the catalog if possible.
    String prefix = "compressed-";
    Matcher m = URL_FILE_NAME_PATTERN.matcher(systemId);
    if( m.matches() ) {
      String filename = m.group(1);
      if( filename != null ) {
        filename = filename.replace('.', '-');
        prefix += filename + "-";
      }
    }
    
    File tempFile = File.createTempFile(prefix, null, tempDir);
    return tempFile;
  }
  
  private static boolean forceDeleteTempFile(File tempFile) {
    boolean result = false;
    if( tempFile != null ) {
      try {
        if( log.isDebugEnabled() )
          log.debug("Forcefully removing temporary file '"+tempFile.getAbsolutePath()+"'.");
        result = tempFile.delete();
      } catch( Exception e ) {
        if( log.isWarnEnabled() )
          log.warn("Exception occurred while attempting to delete temporary file '"+tempFile.getAbsolutePath()+"'.", e);
      }
      if( log.isDebugEnabled() )
        log.debug("Result of deleting temporary file '"+tempFile.getAbsolutePath()+"' was "+result+".");
    }
    return result;
  }
}
