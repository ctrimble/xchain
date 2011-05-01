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

import java.net.Socket;
import java.nio.channels.Channel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;

/**
 * A collection of useful static methods for doing io operation.
 *
 * @author Mike Moulton
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 */
public class IoUtil
{
  /**
   * Closes an input stream if it is not null.  If an IOException is raised during closing of the stream, it is ignored.
   *
   * @param in the input stream to close, or null.
   */
  public static void close( InputStream in, Logger log )
  {
    if( in != null ) {
      try {
        in.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
          log.debug("An exception was thrown while closing an input stream.", ioe);
        }
      }
    }
  }
  
  /**
   * Exists to provide an upgrade path from Commons Logging to SLF4J
   * @param in
   * @param log
   */
  @Deprecated
  public static void close( InputStream in, org.apache.commons.logging.Log log )
  {
    if( in != null ) {
      try {
        in.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
          log.debug("An exception was thrown while closing an input stream.", ioe);
        }
      }
    }
  }

  /**
   * Closes an output stream if it is not null.  If an IOException is raised during closing of the stream, it is ignored.
   *
   * @param out the output stream to close, or null.
   */
  public static void close( OutputStream out, Logger log )
  {
    if( out != null ) {
      try {
        out.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
          log.debug("An exception was thrown while closing an output stream.", ioe);
        }
      }
    }
  }
  
  /**
   * Exists to provide an upgrade path from Commons Logging to SLF4J
   * 
   * @param out
   * @param log
   */
  @Deprecated
  public static void close( OutputStream out, org.apache.commons.logging.Log log )
  {
    if( out != null ) {
      try {
        out.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
          log.debug("An exception was thrown while closing an output stream.", ioe);
        }
      }
    }
  }

  public static void close( Socket socket, Logger log )
  {
    if( socket != null ) {
      try {
        socket.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
          log.debug("An exception was thrown while closing a socket.", ioe);
        }
      }
    }
  }
  
  /**
   * Exists to provide an upgrade path from Commons Logging to SLF4J
   * 
   * @param socket
   * @param log
   */
  @Deprecated
  public static void close( Socket socket, org.apache.commons.logging.Log log )
  {
    if( socket != null ) {
      try {
        socket.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
          log.debug("An exception was thrown while closing a socket.", ioe);
        }
      }
    }
  }

  public static void close( Reader reader, Logger log )
  {
    if( reader != null ) {
      try {
        reader.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
           log.debug("An exception was thrown while closing a reader.", ioe);
        }
      }
    }
  }
  
  /**
   * Exists to provide an upgrade path from Commons Logging to SLF4J
   * 
   * @param reader
   * @param log
   */
  @Deprecated
  public static void close( Reader reader, org.apache.commons.logging.Log log )
  {
    if( reader != null ) {
      try {
        reader.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
           log.debug("An exception was thrown while closing a reader.", ioe);
        }
      }
    }
  }

  public static void close( Writer writer, Logger log )
  {
    if( writer != null ) {
      try {
        writer.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
           log.debug("An exception was thrown while closing a writer.", ioe);
        }
      }
    }
  }
  
  /**
   * Exists to provide an upgrade path from Commons Logging to SLF4J
   * 
   * @param writer
   * @param log
   */
  @Deprecated
  public static void close( Writer writer, org.apache.commons.logging.Log log )
  {
    if( writer != null ) {
      try {
        writer.close();
      }
      catch( IOException ioe ) {
        if( log != null && log.isDebugEnabled() ) {
           log.debug("An exception was thrown while closing a writer.", ioe);
        }
      }
    }
  }
  
  public static void close( Channel channel, Logger log )
  {
    if( channel != null ) {
      try {
        channel.close();
      } catch( Exception e ) {
        log.debug("An exception was thrown while closing a channel.", e);
      }
    }
  }

  /**
   * Copies the input stream bytes into the output stream bytes, using the specified
   * buffer size when transfering the bytes.  This method does not close the streams
   * when it completes.
   */
  public static void copyStream( InputStream in, OutputStream out, int bufferSize )
    throws IOException
  {
    byte[] buffer = new byte[bufferSize];
    int read = 0;
    while( (read = in.read(buffer)) >= 0 ) {
      out.write(buffer, 0, read);
    }
  }
  
  /**
   * Copies the content of a reader into a writer, using the specified buffer size while
   * transfering the characters.
   */
  public static void copyStream(Reader reader, Writer writer, int buffer_length) throws IOException {
    char[] buffer = new char[buffer_length];
    for( int read = reader.read(buffer); read > -1; read = reader.read(buffer) )
      writer.write(buffer, 0, read);
  }
  
	/**
	 * Utility method to perform a file copy from a File to a File
	 * 
	 * @param srcFile
	 * @param dstFile
	 * @return
	 */
	public static boolean fileCopy(File srcFile, File dstFile, Logger log) {
		if (srcFile == null || !srcFile.exists() || !srcFile.isFile()) {
			log.warn("Could not access source file '{}'.", srcFile.getAbsolutePath());
			return false;
		}
		
		try {
			return fileCopy(new FileInputStream(srcFile), dstFile, log);
		} catch (FileNotFoundException e) {
			log.warn("Could not create InputStream from '{}'.", srcFile.getAbsolutePath(), e);
			return false;
		}
	}
	
	/**
	 * Utility method to perform a file copy from an InputStream to a File
	 * 
	 * @param in
	 * @param dstFile
	 * @param log
	 * @return
	 */
	public static boolean fileCopy(InputStream in, File dstFile, Logger log) {
		// Perform a Check
		if (in == null || dstFile == null) {
			log.warn("Either InputSteam or Destination File was false.");
			return false;
		}
		
		try {
			if (dstFile.createNewFile()) {
				OutputStream out = new FileOutputStream(dstFile);
				
				byte[] buf = new byte[1024];
				int len;
				
				while ((len = in.read(buf)) > 0){
					out.write(buf, 0, len);
				}
				
				in.close();
				out.close();
			}
			else {
				// Could not create file (probably exists already)
				if (dstFile.exists()) {
					log.warn("Could not create '{}' since it already exists", dstFile.getAbsolutePath());
					return false;
				}
				else {
					log.warn("Error creating new file '{}'.", dstFile.getAbsolutePath());
					return false;
				}
			}
		} catch (IOException e) {
			log.warn("Error copying file to the destination", e);
			return false;
		}
		
		log.info("Copied data from InputStream to '{}'", dstFile.getAbsolutePath());
		return true;
	}
	
	/**
	 * Utility method to return an InputStream from either a File on the system path, or 
	 * from the ConextClassLoader to a resource on the Class Path.  Prefers Local System
	 * over Class Path.  Throws File Not Found Exception if it does not exist in either place.
	 * 
	 * @param name
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static InputStream getFileStream(String name, Logger log) throws FileNotFoundException {
		File srcFile = new File(name);
		
		try {
			if (srcFile != null && srcFile.exists() && srcFile.isFile()) {
				log.info("Found '{}' on File System.", name);
				// Since it is a real file, grab a stream to it and return (Should not be able to throw exception)
				return new FileInputStream(srcFile);
			}
		}
		catch (FileNotFoundException e) {
			// We don't care if it's not found on the file system.
			log.debug("Could not open stream to file '{}'", name);
		}
		
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		
		if (stream != null) {
			log.info("Resource found from Class Loader '{}'.", name);
			return stream;
		}
		
		// Can only get here if the File is not found on either the ClassPath or the InputStream
		throw new FileNotFoundException("Could not find file '" + name + "'");
	}
}
