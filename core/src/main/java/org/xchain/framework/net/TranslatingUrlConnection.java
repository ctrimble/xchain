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

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public abstract class TranslatingUrlConnection
  extends UrlConnectionWrapper
{
  public static Logger log = LoggerFactory.getLogger( TranslatingUrlConnection.class );

  public TranslatingUrlConnection( URL url )
  {
    super(url);

    URL translatedUrl = null;

    // get the url translation strategy for this authority.
    UrlTranslationStrategy urlTranslationStrategy = getUrlTranslationStrategy(url);

    if( urlTranslationStrategy != null ) {
      if( log.isDebugEnabled() ) {
        log.debug("Starting translation of url '"+url.toExternalForm()+"' with translation strategy of type '"+urlTranslationStrategy.getClass().getName()+"'.");
      }

      // translate the url.
      translatedUrl = urlTranslationStrategy.translateUrl( url );

    }

    if( translatedUrl == null ) {
      try {
        translatedUrl = new URL(url.toExternalForm());
      }
      catch( Exception e ) {
        throw new RuntimeException("Could not create url for '"+url.toExternalForm()+"'.", e);
      }
    }

    // log the translated url we are using.
    if( log.isDebugEnabled() ) {
      log.debug("The url '"+url.toExternalForm()+"' translated to '"+translatedUrl.toExternalForm()+"'.");
    }

    try {
      this.wrapped = translatedUrl.openConnection();
    }
    catch( Exception e ) {
      throw new RuntimeException("Could not open a connection to '"+url.toExternalForm()+"'.");
    }
  }

  public abstract UrlTranslationStrategy getUrlTranslationStrategy( URL url );
}
