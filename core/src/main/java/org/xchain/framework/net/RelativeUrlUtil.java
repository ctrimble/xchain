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
package org.xchain.framework.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility class for resolving a URI relative to another URI.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class RelativeUrlUtil
{
  /**
   * Attempt to resolve the given spec URI against the given context URI.
   * 
   * @param context The context URI to resolve against.
   * @param spec The spec URI to resolve with.
   * 
   * @return The given spec URI resolved against the given context URI.
   * 
   * @see URI#relativize(URI)
   */
  public static String resolve( String context, String spec )
    throws MalformedURLException
  {
    try {
      return new URI(context).resolve(spec).toString();
    }
    catch( URISyntaxException use ) {
      MalformedURLException mue = new MalformedURLException("Could not create url for context '"+context+"' and spec '"+spec+"'.");
      mue.initCause(use);
      throw mue;
    }
  }

  /**
   * Attempt to resolve the given spec URI against the given context URI.
   * 
   * @param url The context URI to resolve against.
   * @param spec The spec URI to resolve with.
   * 
   * @return The given spec URI resolved against the given context URI.
   * 
   * @see URI#relativize(URI)
   */
  public static String resolve( URL url, String spec )
    throws MalformedURLException
  {
    String context = null;

    try {
      if( url != null ) {
        context = url.toExternalForm();
        return resolve( context, spec );
      }
      else {
        return new URI(spec).toString();
      }
    }
    catch( URISyntaxException use ) {
      MalformedURLException mue = new MalformedURLException("Could not create url for context '"+context+"' and spec '"+spec+"'.");
      mue.initCause(use);
      throw mue;
    }
  }
}
