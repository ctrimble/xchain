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
package org.xchain.framework.jsl;

import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerSettings;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.readers.MemoryResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.MemoryResourceStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.SAXException;

/**
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class TemplateCompiler
{
  private static Logger log = LoggerFactory.getLogger(TemplateCompiler.class);

  public static final String SOURCE_VERSION = "1.5";
  public static final String SOURCE_ENCODING = "UTF-8";
  public static final String TARGET_VERSION = "1.5";

  /** The class loader for the catalog. */
  private TemplateClassLoader templateClassLoader;

  /** The compiler that we will use for creating classes. */
  private JavaCompiler compiler;

  /** The resource reader where source files will be stored. */
  private WrappedResourceReader resourceReader;

  /** The settings for the compiler. */
  private JavaCompilerSettings settings;

  public void init(ClassLoader classLoader)
  {
    this.templateClassLoader = new TemplateClassLoader(classLoader);

    resourceReader = new WrappedResourceReader(new MemoryResourceReader(), templateClassLoader);

    settings = new JavaCompilerSettings();
    settings.setSourceVersion(SOURCE_VERSION);
    settings.setSourceEncoding(SOURCE_ENCODING);
    settings.setTargetVersion(TARGET_VERSION);

    compiler = new JavaCompilerFactory().createCompiler("eclipse");
  }

  /**
   * Compiles the source result.
   *
   * @throws SAXException if there is a compilation error in the source file.
   */
  public Class compileTemplate( SourceResult result )
    throws SAXException
  {
    // add the resource to the reader.
    try {
      resourceReader.add(result.getSourceResourceName(), result.getSource().getBytes(SOURCE_ENCODING));
    }
    catch( UnsupportedEncodingException uee ) {
      throw new SAXException("Could not build source in the encoding '"+SOURCE_ENCODING+"'.", uee);
    }

    // create the resource store.
    ResourceStore resourceStore = new MemoryResourceStore();

    // compile the result.
    CompilationResult compilationResult = compiler.compile(new String[] {result.getSourceResourceName()}, resourceReader, resourceStore, templateClassLoader, settings);

    // handle any errors.
    if( compilationResult.getErrors().length > 0 ) {
      if( log.isDebugEnabled() ) {
        log.debug("Source that would not complile:\n"+result.getSource());
        for( CompilationProblem error : compilationResult.getErrors() ) {
          log.debug("Error ("+error.getStartLine()+", "+error.getStartColumn()+"):"+error.getMessage());
        }
      }
      // TODO: Make this message better.
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Could not compile template source. Errors:\n");
      for( CompilationProblem error : compilationResult.getErrors() ) {
        stringBuilder.append("("+error.getStartLine()+", "+error.getStartColumn()+"):"+error.getMessage()+"\n");
      }
      throw new SAXException(stringBuilder.toString());
    }

    // handle any errors.
    if( log.isDebugEnabled() ) {
      for( CompilationProblem warning : compilationResult.getWarnings() ) {
        log.debug("Compilation warning("+warning.getStartLine()+", "+warning.getStartColumn()+"):"+warning.getMessage());
      }
    }

    byte[] compiledBytes = resourceStore.read(result.getClassResourceName());

    templateClassLoader.publicDefineClass(result.getClassName(), compiledBytes, 0, compiledBytes.length);
    Class templateClass = null;

    try {
      templateClass = templateClassLoader.loadClass(result.getClassName());
    }
    catch( Exception e ) {
      throw new SAXException("Could not load the class '"+result.getClassName()+"'.", e);
    }
    
    return templateClass;
  }

  private class TemplateClassLoader
    extends ClassLoader
  {
    public TemplateClassLoader(ClassLoader parent)
    {
      super(parent);
    }

    public void publicDefineClass( String name, byte[] b, int off, int len )
      throws ClassFormatError, IndexOutOfBoundsException, SecurityException
    {
      this.defineClass(name, b, off, len);
    }
  }

  public static class WrappedResourceReader
    implements ResourceReader
  {
    protected MemoryResourceReader wrapped;
    protected ClassLoader classLoader;
    public WrappedResourceReader( MemoryResourceReader wrapped, ClassLoader classLoader ) { this.wrapped = wrapped; this.classLoader = classLoader; }
    public void add( String name, byte[] data ) {
      wrapped.add(name, data);
    }
    public byte[] getBytes(String name) {
      byte[] result = wrapped.getBytes(name);
      if( result == null ) {
        try {
        int bufferSize = 0;
        byte[] buffer = new byte[10000];
        int read = 0;

        InputStream source = classLoader.getResourceAsStream(name);
        while( (read = source.read(buffer, bufferSize, buffer.length)) != -1 ) {
          bufferSize += read;
          if( bufferSize >= buffer.length ) {
            break;
          }
        }

        result = new byte[bufferSize];
        for( int i = 0; i < bufferSize; i++ ) {
          result[i] = buffer[i];
        }

        }
        catch( Exception e ) {
          e.printStackTrace();
          return null;
        }
      }
      return result;
    }

    public boolean isAvailable( String name)
    {
      boolean result = wrapped.isAvailable(name);
      if( !result ) {
        result = (classLoader.getResource(name) != null);
      }
      return result;
    }
  }

}
