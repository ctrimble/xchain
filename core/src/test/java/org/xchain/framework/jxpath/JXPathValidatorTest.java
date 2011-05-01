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
package org.xchain.framework.jxpath;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.apache.commons.jxpath.ri.compiler.TreeCompiler;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.commons.jxpath.JXPathException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import static javax.xml.XMLConstants.*;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;
import javax.xml.namespace.NamespaceContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.commons.jxpath.ri.compiler.*;

/**
 * A test that tests methods of validating xpaths.
 *
 * @author Christian Trimble
 */
public class JXPathValidatorTest
{
  public static Compiler compiler = new TreeCompiler();
  public static NamespaceContext xmlns = new NamespaceContextImpl();

  @Test public void testKnownExtensionFunctionPrefix()
  {
    JXPathException jxpe = testSyntaxError("xc:system-id()", xmlns);
    assertNull("A known jxpath extension function threw an exception", jxpe);
  }

  @Test public void testUnknownExtensionFunctionPrefix()
  {
    JXPathException jxpe = testSyntaxError("xv:system-id()", xmlns);
    assertTrue("An exception was not thrown for an unknown extention function prefix.", jxpe != null );
  }

  @Test public void testNestedUnknownExtensionFunctionPrefix()
  {
    JXPathException jxpe = testSyntaxError("concat(xv:system-id())", xmlns);
    assertTrue("An exception was not thrown for a nested unknown extention function prefix.", jxpe != null );
  }

  @Ignore @Test public void testUnknownExtensionFunctionName()
  {
    JXPathException jxpe = testSyntaxError("xc:systemid()", xmlns);
    assertTrue("An exception was not thrown for an unknown extension function local name.", jxpe != null );
  }

  @Test public void testKnownLocationPathPrefix()
  {
    JXPathException jxpe = testSyntaxError("xc:path", xmlns);
    assertTrue("An exception was thrown for a known location path prefix", jxpe == null );
  }

  @Test public void testUnknownLocationPathPrefix()
  {
    JXPathException jxpe = testSyntaxError("xv:path", xmlns);
    assertTrue("An exception was not thrown for an unknown location path prefix.", jxpe != null );
  }

  @Test public void testUnknownStepLocationPathPrefix()
  {
    JXPathException jxpe = testSyntaxError("xc:path/xv:path", xmlns);
    assertTrue("An exception was not thrown for an unknown step location path prefix.", jxpe != null );
  }

  @Test public void testUnknownPredicatePrefix()
  {
    JXPathException jxpe = testSyntaxError("xc:parent[xv:sibling]/xc:child", xmlns);
    assertTrue("An exception was not thrown for an unknown predicate prefix.", jxpe != null );
  }

  @Test public void testUnknownVariableReferencePrefix()
  {
    JXPathException jxpe = testSyntaxError("$xv:value", xmlns);
    assertTrue("An exception was not thrown for an unknown variable reference prefix.", jxpe != null );
  }

  @Test public void testMissingRightParen()
  {
    JXPathException jxpe = testSyntaxError("getSomething('text'", xmlns);
    assertTrue("An exception was not thrown for a missing right paren.", jxpe != null );
  }

  @Test public void testMissingSingleQuote()
  {
    JXPathException jxpe = testSyntaxError("'text", xmlns);
    assertTrue("An exception was not thrown for a missing single quote.", jxpe != null );
  }

  private JXPathException testSyntaxError(String path, NamespaceContext xmlns ) {
    JXPathException result = null;
    try {
      JXPathValidator.validate(path, xmlns);
    }
    catch( JXPathException jxpe ) {
      result = jxpe;
    }
    return result;
  }

  public static class NamespaceContextImpl
    implements NamespaceContext
  {
    Map<String, LinkedList<String>> mapping = new HashMap<String, LinkedList<String>>();
    public NamespaceContextImpl()
    {
      addMapping( XML_NS_PREFIX, NULL_NS_URI );
      addMapping( "xc", "http://www.xchain.org/core/1.0" );
      addMapping( "sax", "http://www.xchain.org/sax/1.0" );
    }

    private void addMapping( String prefix, String namespaceUri )
    {
      LinkedList<String> namespaceList = mapping.get(prefix);
      if( namespaceList == null ) {
        mapping.put( prefix, namespaceList = new LinkedList<String>() );
      }
      namespaceList.addFirst(namespaceUri);
    }

    public String getPrefix( String namespaceUri )
    {
      for( Map.Entry<String, LinkedList<String>> entry : mapping.entrySet() ) {
        if( namespaceUri.equals(entry.getValue().getFirst()) ) {
          return entry.getKey();
        }
      }
      return NULL_NS_URI;
    }

    public String getNamespaceURI( String prefix )
    {
      LinkedList<String> namespaceList = mapping.get(prefix);
      if( namespaceList == null ) {
        return NULL_NS_URI;
      }
      return namespaceList.getFirst();
    }

    public Iterator<String> getPrefixes( String namespaceUri )
    {
      List<String> prefixes = new ArrayList<String>();
      for( Map.Entry<String, LinkedList<String>> entry : mapping.entrySet() ) {
        if( namespaceUri.equals(entry.getValue().getFirst() ) ) {
          prefixes.add(entry.getKey());
        }
      }
      return Collections.unmodifiableList(prefixes).iterator();
    }
  }
}
