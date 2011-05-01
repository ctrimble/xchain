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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Christian Trimble
 * @author Jason Rose
 * @author Devon Tackett
 */
public class DependencySorter<T>
{
  /** The map of labels to vertices for this graph. */
  protected LinkedHashMap<T, Vertex<T>> vertexMap = new LinkedHashMap<T, Vertex<T>>();
  protected Comparator<T> elementComparator = null;

  public DependencySorter( Comparator<T> elementComparator )
  {
    this.elementComparator = elementComparator;
  }

  /**
   * Adds a label to the dependency graph if it is not already present.
   */
  public void add( T label )
  {
    getOrAddVertex( label );
  }

  /**
   * Retrieve the vertex for the given label.  If the label did not already have a vertex, this will
   * create a new vertex for the label.
   * 
   * @param label The label to use.
   * @return The vertex for the given label.
   */
  protected Vertex<T> getOrAddVertex( T label )
  {
    Vertex<T> vertex = vertexMap.get(label);
    if( vertex == null ) {
      vertex = new Vertex<T>(label);
      vertexMap.put(label, vertex);
    }
    return vertex;
  }

  /**
   * Adds a dependency from label to dependencyLabel.  If there is not a vertex for either label in the dependency
   * graph, then one is added.
   */
  public void addDependency( T label, T dependencyLabel )
  {
    addEdge(getOrAddVertex(label), getOrAddVertex(dependencyLabel));
  }

  /**
   * Internal implementation of adding an edge to the graph.
   */
  protected void addEdge( Vertex<T> out, Vertex<T> in )
  {
    out.getOutSet().add(in);
    in.getInSet().add(out);
  }

  /**
   * The implementation of a vertex in the graph.  It holds a set of outbound edges and inbound edges.
   */
  protected static class Vertex<T>
  {
    /** The label for this vertex.*/
    protected T label;
    /** The set of outbound dependencies for this vertex (things that depend on this vertex.) */
    protected Set<Vertex<T>> outSet = new HashSet<Vertex<T>>();
    /** The set of inbound dependencies for this vertex (things this vertex depends on.) */
    protected Set<Vertex<T>> inSet = new HashSet<Vertex<T>>();

    /** Creates a new vertex for the given label. */
    protected Vertex(T label)
    {
      this.label = label;
    }

    /**
     * Returns the label for this vertex.
     */
    public T getLabel() { return label; }
    public int hashCode() { return label.hashCode(); }
    public boolean equals( Object o ) {
      if( o instanceof Vertex ) {
        return ((Vertex)o).getLabel().equals(label);
      }
      return false;
    }

    /**
     * Returns the set of out bound dependencies.
     */
    public Set<Vertex<T>> getOutSet() { return outSet; }

    /**
     * Returns the set of inbound dependencies.
     */
    public Set<Vertex<T>> getInSet() { return inSet; }
  }

  protected static class VertexComparator<T>
    implements Comparator<Vertex<T>>
  {
    protected Comparator<T> elementComparator;
    public VertexComparator(Comparator<T> elementComparator)
    {
      this.elementComparator = elementComparator;
    }

    public Comparator<T> getLabelComparator() { return this.elementComparator; }

    public int compare( Vertex<T> v1, Vertex<T> v2 )
    {
      return elementComparator.compare(v1.getLabel(), v2.getLabel());
    }

    public boolean equals( Object o )
    {
      if( !(o instanceof VertexComparator) ) {
        return false;
      }
      return elementComparator.equals(((VertexComparator)o).getLabelComparator());
    }
  }

  public List<T> sort()
    throws DependencyCycleException
  {
    // create a set of all the 
    List<T> sorted = new ArrayList<T>();
    LinkedList<Vertex<T>> vertexQueue = new LinkedList<Vertex<T>>();
    SortedSet<Vertex<T>> deterministicSet = new TreeSet<Vertex<T>>(new VertexComparator(elementComparator));

    // seed the vertex queue with all vertexes that do not have any incoming edges.
    Iterator<Map.Entry<T,Vertex<T>>> vertexIterator = vertexMap.entrySet().iterator();
    while( vertexIterator.hasNext() ) {
      Map.Entry<T, Vertex<T>> vertexEntry = vertexIterator.next();
      if( vertexEntry.getValue().getInSet().isEmpty()) {
        vertexQueue.add(vertexEntry.getValue());
      }
    }

    // while there are items in the queue.
    while( !vertexQueue.isEmpty() ) {

      // empty the queue and do a sort of its contents.  This will give a determanistic order to the elements
      // that otherwise would come in a non deterministic order.
      deterministicSet.addAll(vertexQueue);
      vertexQueue.clear();

      for( Vertex<T> current : deterministicSet ) {
        sorted.add(current.getLabel());
        Iterator<Vertex<T>> outIterator = current.getOutSet().iterator();
        while( outIterator.hasNext() ) {
          Vertex<T> out = outIterator.next();
          // remove the current vertex from the out list of the current node.
          outIterator.remove();
          // remove the other side of the relationship.
          out.getInSet().remove(current);

          // if there are no more in nodes for this out node, then remove it from the graph.
          if( out.getInSet().isEmpty() ) {
            vertexQueue.add(out);
          }
        }
        //remove the current node from the vertex map.
        vertexMap.remove(current.getLabel());
      }

      // clean up the deterministic set.
      deterministicSet.clear();
    }

    if( !vertexMap.isEmpty() ) {
      // There are entries left in the vertex map.  A circular dependency must exist.
      Map<T, Set<T>> cycle = new HashMap<T, Set<T>>();

      // minimize the nodes to just the cycles by remove all the dependency leaves.
      vertexQueue.clear();

      for( Map.Entry<T, Vertex<T>> entry : vertexMap.entrySet() ) {
        if( entry.getValue().getOutSet().isEmpty() ) {
          vertexQueue.add(entry.getValue());
        }
      }

      while( !vertexQueue.isEmpty() ) {
        Vertex<T> current = vertexQueue.removeFirst();
        Iterator<Vertex<T>> inIterator = current.getInSet().iterator();
        while( inIterator.hasNext() ) {
          Vertex<T> in = inIterator.next();
          inIterator.remove();
          in.getOutSet().remove(current);
          if( in.getOutSet().isEmpty() ) {
            vertexQueue.add(in);
          }
        }
        vertexMap.remove(current.getLabel());
      }

      // map add the remaining entries into the cycle mapping and clear the vertex map.
      vertexIterator = vertexMap.entrySet().iterator();
      while( vertexIterator.hasNext() ) {
        Map.Entry<T, Vertex<T>> entry = vertexIterator.next();
        Set<T> outLabelSet = new HashSet<T>();
        for( Vertex<T> out : entry.getValue().getOutSet() ) {
          outLabelSet.add(out.getLabel());
        }
        cycle.put(entry.getKey(), outLabelSet);
        vertexIterator.remove();
      }
      throw new DependencyCycleException("The following nodes have a cyclic dependency: ", cycle);
    }

    return sorted;
  }
}
