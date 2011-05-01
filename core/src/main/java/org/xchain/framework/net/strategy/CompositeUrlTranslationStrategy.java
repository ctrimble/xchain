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
package org.xchain.framework.net.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.net.URL;

import org.xchain.framework.net.UrlTranslationStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class CompositeUrlTranslationStrategy
  implements UrlTranslationStrategy
{
  public static Logger log = LoggerFactory.getLogger( CompositeUrlTranslationStrategy.class );

  protected List<UrlTranslationStrategy> translatorList = new ArrayList<UrlTranslationStrategy>();

  public void setTranslatorList( List<UrlTranslationStrategy> translatorList ) { this.translatorList = translatorList; }
  public List<UrlTranslationStrategy> getTranslatorList() { return this.translatorList; }

  public URL translateUrl( URL resourceUrl )
  {
    if( log.isDebugEnabled() ) {
      log.debug("Trying "+translatorList.size()+" translators to resolve url '"+resourceUrl.toExternalForm()+"'.");
    }
    URL translatedUrl = null;
    Iterator<UrlTranslationStrategy> translatorIterator = translatorList.iterator();
    while( translatedUrl == null && translatorIterator.hasNext() ) {
      if( log.isDebugEnabled() ) {
        log.debug("Attemting to translate url.");
      }
      translatedUrl = translatorIterator.next().translateUrl(resourceUrl);
    }

    if( log.isDebugEnabled() ) {
      if( translatedUrl != null ) {
        log.debug("Translated url '"+resourceUrl.toExternalForm()+"' into '"+translatedUrl.toExternalForm()+"'.");
      }
      else {
        log.debug("Could not translated url '"+resourceUrl.toExternalForm()+"'.");
      }
    }
    return translatedUrl;
  }
}
