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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 * @author Josh Kennedy
 */
public class DependencySorterTest
{
  public static Logger log = LoggerFactory.getLogger(DependencySorterTest.class);

  public static QName LABEL_1 = new QName("http://www.xchain.org/test", "label1");
  public static QName LABEL_2 = new QName("http://www.xchain.org/test", "label2");
  public static QName LABEL_3 = new QName("http://www.xchain.org/test", "label3");
  public static QName LABEL_4 = new QName("http://www.xchain.org/test", "label4");
  public static QName LABEL_5 = new QName("http://www.xchain.org/test", "label5");
  public static QName LABEL_6 = new QName("http://www.xchain.org/test", "label6");

  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test public void testNoEdges()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.add(LABEL_1);
    sorter.add(LABEL_2);
    sorter.add(LABEL_3);

    List<QName> sorted = sorter.sort();

    // test the size.
    assertEquals("The sorted list has the wrong size.", 3, sorted.size());
    
    // make sure that the list contains the 3 lables.
    assertTrue("The sorted list does not contain label 1.", sorted.contains(LABEL_1));
    assertTrue("The sorted list does not contain label 2.", sorted.contains(LABEL_2));
    assertTrue("The sorted list does not contain label 3.", sorted.contains(LABEL_3));
  }

  @Test public void testTwoEdges()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.addDependency(LABEL_1, LABEL_2);
    sorter.addDependency(LABEL_2, LABEL_3);

    List<QName> sorted = sorter.sort();

    // test the size.
    assertEquals("The sorted list has the wrong size.", 3, sorted.size());
    
    // make sure that the list contains the 3 lables.
    Iterator<QName> iterator = sorted.iterator();
    assertEquals("Label 1 should have been the first entry.", LABEL_1, iterator.next());
    assertEquals("Label 2 should have been the second entry.", LABEL_2, iterator.next());
    assertEquals("Label 3 should have been the third entry.", LABEL_3, iterator.next());
  }

  @Test public void testTwoEdgesDefinedInReverse()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.addDependency(LABEL_2, LABEL_3);
    sorter.addDependency(LABEL_1, LABEL_2);

    List<QName> sorted = sorter.sort();

    // test the size.
    assertEquals("The sorted list has the wrong size.", 3, sorted.size());
    
    // make sure that the list contains the 3 lables.
    Iterator<QName> iterator = sorted.iterator();
    assertEquals("Label 1 should have been the first entry.", LABEL_1, iterator.next());
    assertEquals("Label 2 should have been the second entry.", LABEL_2, iterator.next());
    assertEquals("Label 3 should have been the third entry.", LABEL_3, iterator.next());
  }

  @Test public void testTwoLabelCycle()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.addDependency(LABEL_2, LABEL_1);
    sorter.addDependency(LABEL_1, LABEL_2);

    try {
      List<QName> sorted = sorter.sort();
      fail("Dependency cycle not detected.");
    }
    catch( DependencyCycleException dce ) {
      // make sure that the cycle is in the exception.
      Map cycle = dce.getCycle();
      assertTrue("Label 1 should have been in the cycle.", cycle.containsKey(LABEL_1));
      assertTrue("Label 2 should have been in the cycle.", cycle.containsKey(LABEL_2));

      Set label1DependencySet = (Set)cycle.get(LABEL_1);
      Set label2DependencySet = (Set)cycle.get(LABEL_2);

      assertEquals("Label 1 should have 1 dependency.", 1, label1DependencySet.size());
      assertTrue("Label 1 should be dependent on label 2.", label1DependencySet.contains(LABEL_2));
      assertEquals("Label 2 should have 1 dependency.", 1, label2DependencySet.size());
      assertTrue("Label 2 should be dependent on label 1.", label2DependencySet.contains(LABEL_1));
    }
  }

  @Test public void testThreeLabelCycle()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.addDependency(LABEL_1, LABEL_2);
    sorter.addDependency(LABEL_2, LABEL_3);
    sorter.addDependency(LABEL_3, LABEL_1);

    try {
      List<QName> sorted = sorter.sort();
      fail("Dependency cycle not detected.");
    }
    catch( DependencyCycleException dce ) {
      // make sure that the cycle is in the exception.
      Map cycle = dce.getCycle();
      assertTrue("Label 1 should have been in the cycle.", cycle.containsKey(LABEL_1));
      assertTrue("Label 2 should have been in the cycle.", cycle.containsKey(LABEL_2));
      assertTrue("Label 3 should have been in the cycle.", cycle.containsKey(LABEL_3));

      Set label1DependencySet = (Set)cycle.get(LABEL_1);
      Set label2DependencySet = (Set)cycle.get(LABEL_2);
      Set label3DependencySet = (Set)cycle.get(LABEL_3);

      assertEquals("Label 1 should have 1 dependency.", 1, label1DependencySet.size());
      assertTrue("Label 1 should be dependent on label 2.", label1DependencySet.contains(LABEL_2));
      assertEquals("Label 2 should have 1 dependency.", 1, label2DependencySet.size());
      assertTrue("Label 2 should be dependent on label 3.", label2DependencySet.contains(LABEL_3));
      assertEquals("Label 3 should have 1 dependency.", 1, label3DependencySet.size());
      assertTrue("Label 3 should be dependent on label 3.", label3DependencySet.contains(LABEL_1));
    }
  }

  @Test public void testThreeLabelCycleWithOtherLabels()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.addDependency(LABEL_1, LABEL_2);
    sorter.addDependency(LABEL_2, LABEL_3);
    sorter.addDependency(LABEL_3, LABEL_1);
    sorter.addDependency(LABEL_3, LABEL_4);
    sorter.addDependency(LABEL_4, LABEL_5);
    sorter.addDependency(LABEL_6, LABEL_1);

    try {
      List<QName> sorted = sorter.sort();
      fail("Dependency cycle not detected.");
    }
    catch( DependencyCycleException dce ) {
      // make sure that the cycle is in the exception.
      Map cycle = dce.getCycle();
      assertTrue("Label 1 should have been in the cycle.", cycle.containsKey(LABEL_1));
      assertTrue("Label 2 should have been in the cycle.", cycle.containsKey(LABEL_2));
      assertTrue("Label 3 should have been in the cycle.", cycle.containsKey(LABEL_3));

      Set label1DependencySet = (Set)cycle.get(LABEL_1);
      Set label2DependencySet = (Set)cycle.get(LABEL_2);
      Set label3DependencySet = (Set)cycle.get(LABEL_3);

      assertEquals("Label 1 should have 1 dependency.", 1, label1DependencySet.size());
      assertTrue("Label 1 should be dependent on label 2.", label1DependencySet.contains(LABEL_2));
      assertEquals("Label 2 should have 1 dependency.", 1, label2DependencySet.size());
      assertTrue("Label 2 should be dependent on label 3.", label2DependencySet.contains(LABEL_3));
      assertEquals("Label 3 should have 1 dependency.", 1, label3DependencySet.size());
      assertTrue("Label 3 should be dependent on label 3.", label3DependencySet.contains(LABEL_1));
    }
  }

  @Test public void testUndeterministicOrder()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    sorter.add(LABEL_5);
    sorter.add(LABEL_3);
    sorter.add(LABEL_1);
    sorter.add(LABEL_2);
    sorter.add(LABEL_6);
    sorter.add(LABEL_4);

    List<QName> sorted = sorter.sort();

    // test the size.
    assertEquals("The sorted list has the wrong size.", 6, sorted.size());

    // make sure that the list contains the 3 lables.
    assertEquals("The sorted list has label 1 out of order.", LABEL_1, sorted.get(0));
    assertEquals("The sorted list has label 2 out of order.", LABEL_2, sorted.get(1));
    assertEquals("The sorted list has label 3 out of order.", LABEL_3, sorted.get(2));
    assertEquals("The sorted list has label 4 out of order.", LABEL_4, sorted.get(3));
    assertEquals("The sorted list has label 5 out of order.", LABEL_5, sorted.get(4));
    assertEquals("The sorted list has label 6 out of order.", LABEL_6, sorted.get(5));
  }

  @Test public void testPartialUndeterministicOrder()
    throws Exception
  {
    DependencySorter<QName> sorter = new DependencySorter<QName>(new LexicographicQNameComparator());

    // everyone depends on 3
    sorter.addDependency(LABEL_3, LABEL_6);
    sorter.addDependency(LABEL_3, LABEL_1);
    sorter.addDependency(LABEL_3, LABEL_2);
    sorter.addDependency(LABEL_3, LABEL_5);
    sorter.addDependency(LABEL_3, LABEL_4);

    // 1 depends on 6
    sorter.addDependency(LABEL_6, LABEL_1);

    // 4 depends on 2
    sorter.addDependency(LABEL_2, LABEL_4);
  
    List<QName> sorted = sorter.sort();
  
    // test the size.
    assertEquals("The sorted list has the wrong size.", 6, sorted.size());

    // make sure that the list contains the 3 lables.
    assertEquals("The sorted list has label 3 out of order.", LABEL_3, sorted.get(0));
    assertEquals("The sorted list has label 2 out of order.", LABEL_2, sorted.get(1));
    assertEquals("The sorted list has label 5 out of order.", LABEL_5, sorted.get(2));
    assertEquals("The sorted list has label 6 out of order.", LABEL_6, sorted.get(3));
    assertEquals("The sorted list has label 1 out of order.", LABEL_1, sorted.get(4));
    assertEquals("The sorted list has label 4 out of order.", LABEL_4, sorted.get(5));
  }
}
