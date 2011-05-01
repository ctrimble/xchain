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
package org.xchain.framework.net.protocol.http;

import java.net.URL;
import org.xchain.framework.net.TranslatingUrlConnection;
import org.xchain.framework.net.UrlTranslationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class HttpUrlConnection
  extends TranslatingUrlConnection
{
  public static Logger log = LoggerFactory.getLogger( HttpUrlConnection.class );
  public static UrlTranslationStrategy localCacheStrategy = new LocalHttpCacheUrlTranslationStrategy();

  public HttpUrlConnection( URL url )
  {
    super(url);
  }

  public UrlTranslationStrategy getUrlTranslationStrategy( URL url )
  {
    return localCacheStrategy;
  }
}
