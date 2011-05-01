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
package org.xchain.framework.scanner;

import static org.xchain.framework.util.AnnotationUtil.hasAnnotation;
import static org.xchain.framework.util.AnnotationUtil.getAnnotationValue;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.xchain.framework.scanner.MarkerResourceLocator;
import org.xchain.framework.scanner.ScannerLifecycle;
import org.xchain.framework.scanner.ScanNode;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.MemberValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractScanner
{
   private static final Logger log = LoggerFactory.getLogger(AbstractScanner.class);

   protected RootUrlLocator rootUrlLocator;
   protected ClassLoader classLoader;

   public AbstractScanner(RootUrlLocator rootUrlLocator)
   {
     this( rootUrlLocator, Thread.currentThread().getContextClassLoader() );
   }
   
   public AbstractScanner(RootUrlLocator rootUrlLocator, ClassLoader classLoader)
   {
     this.rootUrlLocator = rootUrlLocator;
     this.classLoader = classLoader;
   }
   
   /* Pattern to match path names with a *-INF directory at the base, such as WEB-INF, OSGI-INF, etc. */
   private static final Pattern INF_DIR_SUBPATH_PATTERN = Pattern.compile("^[/\\\\]?[^/\\\\]+-INF[/\\\\].*$", Pattern.CASE_INSENSITIVE);
   
   /**
    * Returns true if the given name represents a path to a class file that does not have an INF directory at its 
    * base. For example, <code>isLoadableClassFile('org/my/package/name/Foo.class')</code> will return true but
    * <code>isLoadableClassFile('org/my/package/name/Foo')</code> and <code>isLoadableClassFile('WEB-INF/classes/org/my/package/name/Foo.class')</code>
    * will return false.
    * 
    * @param name
    * @return
    */
   public static boolean isLoadableClassFile(String name) {
     return name.endsWith(".class") && !INF_DIR_SUBPATH_PATTERN.matcher(name).matches();
   }
   
   public static String toClassName(ScanNode scanNode)
   {
      return scanNode.getResourceName().replaceAll("\\A(.*)\\.class\\Z", "$1").replaceAll("[/\\\\]", ".");
   }
   
    protected void scan()
      throws ScanException
    {
      ScannerLifecycle scanLifecycle = ScannerLifecycle.getInstance();
      LinkedList<ScanNode> stack = new LinkedList<ScanNode>();
      try {
        stack.addFirst(scanLifecycle.scanNode(rootUrlLocator));
      }
      catch( Exception e ) {
        throw new ScanException("Could not build the root scan node.", e);
      }

      while( !stack.isEmpty() ) {
        ScanNode scanNode = stack.removeFirst();
        stack.addAll(0, scanNode.getChildMap().values());

        if( scanNode.isFile() ) {
          scanNode(scanNode);
        }
        else {
          if( log.isDebugEnabled() ) {
            log.debug("Scan node "+scanNode.getResourceName()+" not handled, it is a directory.");
          }
        }
      }
   }

   public abstract void scanNode(ScanNode node)
     throws ScanException;

   /**
   protected ClassFile getClassFile(String name) throws IOException 
   {
      InputStream stream = classLoader.getResourceAsStream(name);
      DataInputStream dstream = new DataInputStream(stream); 

      try 
      { 
         return new ClassFile(dstream); 
      } 
      finally 
      { 
         dstream.close(); 
         stream.close(); 
      }
   }
   */
   
   public static String componentFilename(String name)
   {
      return name.substring( 0, name.lastIndexOf(".class") ) + ".component.xml";
   }

}
