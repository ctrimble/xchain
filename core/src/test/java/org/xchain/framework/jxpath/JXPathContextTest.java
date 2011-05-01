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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author John Trimble
 */
public class JXPathContextTest
{
  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test public void testRelativeContext()
    throws Exception
  {
    Node root = new Node("root");
    Node child = new Node("child");
    root.getChild().add(child);

    JXPathContext rootContext = JXPathContext.newContext(root);

    JXPathContext childContext = rootContext.getRelativeContext(rootContext.getPointer("/child"));

    String rootName = (String)childContext.getValue("/name");
    assertEquals("The root node has the wrong name.", "root", rootName);

    String childName = (String)childContext.getValue("name");
    assertEquals("The context node has the wrong name.", "child", childName);
  }
  
  @Test public void testBindMethodIgnore() 
    throws Exception
  {
    JXPathContext context = JXPathContext.newContext(new Object());
    context.getVariables().declareVariable("sub-foo", new SubFoo());
    context.getVariables().declareVariable("bar", new SubBar());
    
    // This will throw a JXPathException, due to an ambiguous method lookup, unless 
    // GenericsWisePackageFunctions is used.
    String result = (String) context.getValue("take($sub-foo, $bar)", String.class);
    assertEquals("SubFoo", result);
  }
  
  public static class Foo<E> {
    public String take(E instance) { return "Foo"; }
  }
  
  public static class SubFoo extends Foo<Bar> {
    @Override
    public String take(Bar instance) { return "SubFoo"; }
  }
  
  public static class Bar { }
  
  public static class SubBar extends Bar { }

  public static class Node
  {
    protected String name = null;
    protected List<Node> childList = new ArrayList<Node>();
    public Node( String name ) {
      this.name = name;
    }

    public String getName() { return this.name; }

    public List<Node> getChild()
    {
      return childList;
    }
  }

}
