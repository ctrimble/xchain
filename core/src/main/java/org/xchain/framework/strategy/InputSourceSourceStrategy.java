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
package org.xchain.framework.strategy;

import java.net.URL;

import org.xchain.framework.net.UrlFactory;
import org.xchain.framework.net.UrlSourceUtil;
import org.xml.sax.InputSource;

/**
 * SourceStrategy implementation for InputSource objects.
 * 
 * @author Devon Tackett
 * @author Christian Trimble
 *
 * @see org.xml.sax.InputSource
 */
public class InputSourceSourceStrategy implements SourceStrategy<InputSource> {

	public InputSource getSource(String systemId)
		throws Exception
	{
	    URL url = UrlFactory.getInstance().newUrl( systemId );

	    InputSource inputSource = UrlSourceUtil.createSaxInputSource( url );

	    return inputSource;
	}

}
