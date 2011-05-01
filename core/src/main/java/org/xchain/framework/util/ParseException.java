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
package org.xchain.framework.util;

/**
 * Signals that an exception was encountered while parsing data.
 *
 * @author Christian Trimble
 * @author Jason Rose
 * @author Devon Tackett
 */
public class ParseException
  extends Exception
{
  protected String data;
  protected int location;

  public ParseException( String data, int location )
  {
    super();
    this.data = data;
    this.location = location;
  }

  public ParseException( String message, String data, int location )
  {
    super(message);
    this.data = data;
    this.location = location;
  }

  public ParseException( String data, int location, Throwable cause )
  {
    super(cause);
    this.data = data;
    this.location = location;
  }

  public ParseException( String message, String data, int location, Throwable cause )
  {
    super(message, cause);
    this.data = data;
    this.location = location;
  }

  public void setData( String data )
  {
    this.data = data;
  }

  /**
   * @return The data being parsed.
   */
  public String getData()
  {
    return this.data;
  }

  public void setLocation( int location )
  {
    this.location = location;
  }

  /**
   * @return The location in the data where the exception occurred.
   */
  public int getLocation()
  {
    return this.location;
  }
}
