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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.namespace.QName;

/**
 * A map implementation that can use multiple maps.  The maps are unmodified so if a map is attached and later modified,
 * the modifications will be present in the CompositeMap.
 *
 * @param <K> The type of key values.  Must extend {@link QName}.
 * @param <V> The key values.
 *
 * @author Christian Trimble
 */
public abstract class CompositeMap<K extends QName, V>
  extends AbstractMap<K, V>
{
  /**
   * Get a list of all the maps which make up this composite map.  The order
   * of the list is important as the first entry may be modified if the
   * put() or clear() methods are used.
   * 
   * @return A list of Map<K extends {@link QName}, V> objects.
   */
  protected abstract List<Map<K, V>> mapList();

  /**
   * Get the first map from mapList().
   * 
   * @return A Map<K extends {@link QName}, V>.
   */
  protected Map<K, V> firstMap()
  {
    List<Map<K, V>> mapList = mapList();
    if( !mapList.isEmpty() ) {
      return mapList.get(0);
    }
    return null;
  }

  /**
   * Clear out the entries in the first map.
   */
  public void clear()
  {
    Map<K, V> firstMap = firstMap();
    if( firstMap != null ) {
      firstMap.clear();
    }
  }

  /**
   * Add an entry to the first map.
   */
  public V put( K key, V value )
  {
    Map<K, V> firstMap = firstMap();
    if( firstMap != null ) {
      return firstMap.put(key, value);
    }
    return null;
  }

  public Set<Entry<K, V>>entrySet()
  {
    return new EntrySet<Entry<K, V>>();
  }

  /**
   * A set implementation to consider the size of all the attached maps.
   *
   * @param <S> The type of elements in the set.
   */
  private class EntrySet<S extends Entry<K, V>>
    extends AbstractSet<S>
  {
    /**
     * Compute the size based on all the attached maps.
     */
    public int size()
    {
      LinkedHashSet<K> keys = new LinkedHashSet<K>();

      List<Map<K, V>> mapList = mapList();
      Iterator<Map<K, V>> mapListIterator = mapList.iterator();

      while( mapListIterator.hasNext() ) {
        keys.addAll(mapListIterator.next().keySet());
      }

      return keys.size();
    }

    public Iterator<S> iterator()
    {
      return new EntryIterator<S>();
    }
  }

  /**
   * An iterator implementation to iterate over all the entries in the attached maps.
   * 
   * @param <I> The type of entries to iterate through.
   */
  private class EntryIterator<I extends Entry<K, V>>
    implements Iterator<I>
  {
    private List<Map<K, V>> mapList = null;
    private Iterator<Map<K, V>> mapListIterator = null;
    private Iterator<I> currentMapIterator = null;
    private I nextEntry = null;
    private TreeSet<I> returnedEntrySet = new TreeSet<I>(
        new Comparator<I>()
        {
          public int compare(I o1, I o2)
          {
            QName key1 = o1.getKey();
            QName key2 = o2.getKey();

            // nulls are equal.
            if( key1 == null && key2 == null ) {
              return 0;
            }

            // sort nulls to the end.
            if( key1 == null || key2 == null ) {
              return key1 == null ? -1 : 1;
            }

            // compare the qNames by namespace.
            int namespaceCompare = key1.getNamespaceURI().compareTo(key2.getNamespaceURI());

            if( namespaceCompare != 0 ) {
              return namespaceCompare;
            }
            else {
              return key1.getLocalPart().compareTo(key2.getLocalPart());
            }
          }

          public boolean equals(Object obj)
          {
            return this == obj;
          }
        }
    );

    public EntryIterator()
    {
      mapList = mapList();
      mapListIterator = mapList.iterator();
    }

    public boolean hasNext()
    {
      advanceState();

      return nextEntry != null;
    }

    public I next()
    {
      advanceState();

      I next = nextEntry;
      nextEntry = null;
      return next;
    }

    /**
     * Advance to the next entry.  This will span across all the attached maps.
     */
    private void advanceState()
    {
      // if the next entry has not been consumed.
      if( nextEntry != null ) {
        return;
      }

      while( nextEntry == null ) {
        // search for a currentMapIterator that has an element to return.
        while( (currentMapIterator == null || !currentMapIterator.hasNext()) && mapListIterator.hasNext() ) {
          currentMapIterator = (Iterator<I>) mapListIterator.next().entrySet().iterator();
        }

        // if we didn't advance to a state where the currentMapIterator has a next element, then return.
        if( currentMapIterator == null || !currentMapIterator.hasNext() ) {
          return;
        }

        nextEntry = currentMapIterator.next();

        // if the next entry is in the returnedEntrySet, then keep looking.
        if( !returnedEntrySet.add(nextEntry) ) {
          nextEntry = null;
        }
      }
    }

    public void remove()
    {
      if( currentMapIterator == null ) {
        throw new IllegalStateException("The next() method has not been called.");
      }

      currentMapIterator.remove();
    }
  }
}
