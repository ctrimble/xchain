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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xchain.framework.net.UrlFactory;

/**
 * @author John Trimble
 * @author Josh Kennedy
 */
public class MergeUtil {
  private static Logger log = LoggerFactory.getLogger(MergeUtil.class);
  
  public static URL[] parseManifest(String manifestSystemId) throws IOException {
    List<URL> jsUrlList = new ArrayList<URL>();
    LineNumberReader manifestReader = null;
    InputStream manifestIn = null;
    try {
      // get the url for the absolute system id.
      URL manifestUrl = UrlFactory.getInstance().newUrl(manifestSystemId);
      // create a reader that will allow us to track the line number of the system id we are working with.
      manifestReader = new LineNumberReader(new InputStreamReader(manifestIn = manifestUrl.openStream()));
      String jsSystemId;
      while( ( jsSystemId = manifestReader.readLine() ) != null ) {
        String absoluteJsSystemId = URI.create(manifestSystemId).resolve(jsSystemId).toString();
        jsUrlList.add(UrlFactory.getInstance().newUrl(absoluteJsSystemId));
      }
    } finally {
      close(manifestReader, log);
      close(manifestIn, log);
    }
    
    return jsUrlList.toArray(new URL[jsUrlList.size()]);
  }
 
}
