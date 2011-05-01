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
import java.net.URLConnection;

import java.io.InputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of static methods for converting URLs into SAX InputSources and JAXP Source objects.
 *
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 * @author John Trimble
 */
public class UrlSourceUtil
{
  public static Logger log = LoggerFactory.getLogger(UrlSourceUtil.class);

  /**
   * Create an InputSource from the given URL.
   */
  public static InputSource createSaxInputSource( URL url )
    throws IOException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Creating input source for url '"+url.toString()+"'.");
    }

    // create the new InputSource.
    InputSource source = new InputSource();

    // get a connection to the url.
    URLConnection connection = url.openConnection();

    // open the connection.
    connection.connect();

    // set the byte stream from the URL.
    source.setByteStream( new ReportingInputStream(url.toExternalForm(), connection.getInputStream()));
    //source.setByteStream( connection.getInputStream());

    // set the encoding.
    source.setEncoding( connection.getContentEncoding() );

    // set the url to the system identifier.
    source.setSystemId( url.toExternalForm() );

    // return the source.
    return source;
  }

  /**
   * A utility function that builds javax.xml.transform sources for urls.  This method contructs the
   * Source object by using the URLs openConnection().getInputStream() method.  This allows the urls
   * content handler to be the source of the byte stream.  The systemId is set to the external form
   * of the url to aid in relative url resolution.
   */
  public static Source createTransformSource( URL url )
    throws IOException
  {
    // get a connection to the url.
    URLConnection connection = url.openConnection();

    // open the connection.
    connection.connect();

    // NOTE: it seems strange to not specify the encoding of the stream, but the StreamSource docs state that
    // this information should be taken from the XML declaration.
    return createTransformSource( url.toExternalForm(), connection.getInputStream() );
  }

  public static Source createTransformSource( String systemId, InputStream inputStream )
  {
    if( log.isDebugEnabled() ) {
      log.debug("Creating source for url '"+systemId+"'.");
    }

    // create the new stream source.
    StreamSource source = new StreamSource();

    // set the intput stream from the url.
    source.setInputStream( new ReportingInputStream(systemId, inputStream) );
    //source.setInputStream( inputStream );

    // set the system identifier.
    source.setSystemId( systemId );

    // return the source.
    return source;
  }

  /**
   * An InputStream wrapper which will report the SystemId and the first few lines of the file if
   * it finalized before being properly closed.  This normally occurs when an exception is encountered
   * while processing the wrapped InputStream.
   */
  public static class ReportingInputStream
    extends InputStream
  {
    /** The maximum number of lines to display on error. */
    private static final int BUFFERED_LINE_COUNT = 10;
    /** The systemId of the resource. */
    private String systemId;
    private volatile boolean closed = false;
    private InputStream wrapped;
    private String stackTrace;

    public ReportingInputStream( String systemId, InputStream wrapped )
    {
      if( wrapped == null )
        throw new IllegalArgumentException("InputStream for \""+systemId+"\" cannot be null!");
      this.systemId = systemId;
      this.wrapped = wrapped;

      // create the first few lines of a stack trace to print if the close fails.
      StringBuffer stackTraceBuffer = new StringBuffer();
      StackTraceElement[] stackTraceArray = new Throwable().fillInStackTrace().getStackTrace();
      for( int i = 0; i < stackTraceArray.length && i < BUFFERED_LINE_COUNT; i++ ) {
        stackTraceBuffer.append(stackTraceArray[i]).append("\n");
      }
      stackTrace = stackTraceBuffer.toString();
    }

    public int available() 
      throws IOException
    {
      return wrapped.available();
    }

    public void close()
      throws IOException
    {
      closed = true;
      wrapped.close();
    }

    public void mark( int readLimit ) {
      wrapped.mark( readLimit );
    }

    public boolean markSupported()
    {
      return wrapped.markSupported();
    }

    public int read()
      throws IOException
    {
      return wrapped.read();
    }

    public int read( byte[] b )
      throws IOException
    {
      return wrapped.read( b );
    }

    public int read( byte[] b, int off, int len )
      throws IOException
    {
      return wrapped.read( b, off, len );
    }

    public void reset()
      throws IOException
    {
      wrapped.reset();
    }

    public long skip( long n )
      throws IOException
    {
      return wrapped.skip( n );
    }

    public void finalize()
    {
      if( !closed ) {
        if( log.isErrorEnabled() ) {
          log.error("IO STREAM CLOSED BY FINALIZE FOR '"+systemId+"'\n"+stackTrace);
        }
      }
      wrapped = null;
    }
  }
}
