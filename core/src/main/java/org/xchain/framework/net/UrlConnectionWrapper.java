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

import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.security.Permission;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A URLConnection implementation which wraps another URLConnection.
 *
 * @author Christian Trimble
 * @author Jason Rose
 * @author Devon Tackett
 * @author Josh Kennedy
 * @author John Trimble
 */
public class UrlConnectionWrapper
  extends URLConnection
{
  public static Logger log = LoggerFactory.getLogger(UrlConnectionWrapper.class);
  private static final boolean ERROR_LOG_NULL_INPUTSTREAM = true;
  protected URLConnection wrapped = null;

  public UrlConnectionWrapper( URL url )
  {
    super(url);
  }

  public URLConnection getWrapped() { return this.wrapped; }
  public void setWrapped( URLConnection wrapped ) { this.wrapped = wrapped; }

  public void connect()
    throws IOException
  {
    if( !connected ) {
      wrapped.connect();

      connected = true;
    }
  }

  public void addRequestProperty( String key, String value )
  {
    wrapped.addRequestProperty(key, value);
  }

  public boolean getAllowUserInteraction()
  {
    return wrapped.getAllowUserInteraction();
  }

  public Object getContent()
    throws IOException
  {
    return wrapped.getContent();
  }

  public Object getContent( Class[] classes )
    throws IOException
  {
    return wrapped.getContent(classes);
  }

  public String getContentEncoding()
  {
    return wrapped.getContentEncoding();
  }

  public int getContentLength()
  {
    return wrapped.getContentLength();
  }

  public String getContentType()
  {
    return wrapped.getContentType();
  }

  public long getDate()
  {
    return wrapped.getDate();
  }

  public boolean getDefaultUseCaches()
  {
    return wrapped.getDefaultUseCaches();
  }

  public boolean getDoInput()
  {
    return wrapped.getDoInput();
  }

  public boolean getDoOutput()
  {
    return wrapped.getDoOutput();
  }

  public long getExpiration()
  {
    return wrapped.getExpiration();
  }

  public String getHeaderField( int n )
  {
    return wrapped.getHeaderField( n );
  }

  public String getHeaderField( String name )
  {
    return wrapped.getHeaderField( name );
  }

  public long getHeaderFieldDate( String name, long defaultValue )
  {
    return wrapped.getHeaderFieldDate( name, defaultValue );
  }

  public int getHeaderFieldInt( String name, int defaultValue )
  {
    return wrapped.getHeaderFieldInt( name, defaultValue );
  }

  public String getHeaderFieldKey( int n )
  {
    return wrapped.getHeaderFieldKey( n );
  }

  public Map<String, List<String>> getHeaderFields()
  {
    return wrapped.getHeaderFields();
  }

  public long getIfModifiedSince()
  {
    return wrapped.getIfModifiedSince();
  }

  public InputStream getInputStream()
    throws IOException
  {
    InputStream istream = wrapped.getInputStream();
    if( istream == null && ERROR_LOG_NULL_INPUTSTREAM ) {
      // if the protocol doesn't support input, it should of thrown an exception.
      Bundle bundle = FrameworkUtil.getBundle(getClass());
      log.error(String.format(
          "The url '%s', translated from the url '%s', returned a null InputStream. The TCCL is '%s' and this class's CL is '%s'. This class is from bundle '%s' with ID '%s'.", 
          wrapped.getURL().toExternalForm(), 
          getURL().toExternalForm(),
          Thread.currentThread().getContextClassLoader(),
          getClass().getClassLoader(),
          (bundle == null? null : bundle.getSymbolicName()),
          (bundle == null? null : bundle.getBundleId()) ));
    }
    return istream;
  }

  public long getLastModified()
  {
    return UrlUtil.getInstance().lastModified(wrapped);
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    return wrapped.getOutputStream();
  }

  public Permission getPermission()
    throws IOException
  {
    return wrapped.getPermission();
  }

  public Map<String, List<String>> getRequestProperties()
  {
    return wrapped.getRequestProperties();
  }

  public String getRequestProperty( String key )
  {
    return wrapped.getRequestProperty( key );
  }

  public boolean getUseCaches()
  {
    return wrapped.getUseCaches();
  }

  public void setAllowUserInteraction( boolean allowUserInteraction )
  {
    wrapped.setAllowUserInteraction( allowUserInteraction );
  }

  public void setDefaultUseCaches( boolean defaultUseCaches )
  {
    wrapped.setDefaultUseCaches( defaultUseCaches );
  }

  public void setDoInput( boolean doInput )
  {
    wrapped.setDoInput( doInput );
  }

  public void setDoOutput( boolean doOutout )
  {
    wrapped.setDoOutput( doOutput );
  }

  public void setIfModifiedSince( long ifModifiedSince )
  {
    wrapped.setIfModifiedSince( ifModifiedSince );
  }

  public void setRequestProperty( String key, String value )
  {
    wrapped.setRequestProperty( key, value );
  }

  public void setUseCaches( boolean useCaches )
  {
    wrapped.setUseCaches( useCaches );
  }
}
