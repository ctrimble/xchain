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
package org.xchain.framework.digester;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @author Christian Trimble
 */
public class XChainParseException
  extends Exception
{
  protected Locator locator;

  public XChainParseException( Locator locator, String message )
  {
    super( message );
    setLocator(locator);
  }

  public XChainParseException( Locator locator, Throwable cause )
  {
    super( cause );
    setLocator(locator);
  }

  public XChainParseException( Locator locator, String message, Throwable cause )
  {
    super( message, cause );
    setLocator(locator);
  }

  public Locator getLocator() { return this.locator; }
  protected void setLocator( final Locator locator ) {
    if( locator == null ) {
      this.locator = null;
    }
    else {
      this.locator = new LocatorImpl(locator);
    }
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    if( locator != null ) {
      sb.append(locator.getSystemId()).append(":").append(locator.getLineNumber()).append(":").append(locator.getColumnNumber()).append(" - ");
    }
    if( getMessage() != null ) {
      sb.append(getMessage());
    }
    else {
      sb.append("No message.");
    }
    return sb.toString();
  }
}
