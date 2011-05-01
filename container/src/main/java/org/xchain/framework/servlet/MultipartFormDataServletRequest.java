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
package org.xchain.framework.servlet;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;

import java.io.File;
import java.lang.reflect.Array;

/**
 *
 * This code was adapted from code originally written by Darius Robinson.
 *
 * @author Darius Robinson
 * @author Christian Trimble
 * @author John Trimble
 * @author Josh Kennedy
 * @version 1.0
 */
public class MultipartFormDataServletRequest
  extends HttpServletRequestWrapper {
  
  public static Logger log = LoggerFactory.getLogger(MultipartFormDataServletRequest.class);

  private Map<String, String[]> parameterMap = new HashMap<String, String[]>();
  private Map<String, FileItem[]> fileItemMap = new HashMap<String, FileItem[]>();

  public MultipartFormDataServletRequest(HttpServletRequest request, long maxSize, int sizeThreshold, String repositoryPath)
    throws FileUploadException
  {
    super(request);

    // Create the disk file item factory.
    DiskFileItemFactory factory = createDiskFileItemFactory(sizeThreshold, repositoryPath);
    ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
    // maximum size before a FileUploadException will be thrown
    servletFileUpload.setSizeMax(maxSize);
    
    // parse the request.
    Iterator<FileItem> fileItemIterator = servletFileUpload.parseRequest(request).iterator(); // Oye! Unchecked type conversion.
    
    // create temporary maps for parameters and file items.
    Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();
    Map<String, List<FileItem>> fileItemMap = new HashMap<String, List<FileItem>>();
    
    // populate the maps.
    while( fileItemIterator.hasNext() ) {
      FileItem fileItem = fileItemIterator.next();
      if (fileItem.isFormField()) {
        putListMapValue(parameterMap, fileItem.getFieldName(), fileItem.getString());
      } else {
        putListMapValue(fileItemMap, fileItem.getFieldName(), fileItem);
      }
    }
    
    // convert the array lists.
    convertListMapToArrayMap(parameterMap, this.parameterMap, String.class);
    convertListMapToArrayMap(fileItemMap, this.fileItemMap, FileItem.class);

    if( log.isDebugEnabled() ) {
      logFileItemMap();
    }

  }

  public FileItem getFileItem( String name )
  {
    FileItem fileItem = null;
    FileItem[] fileItemArray = (FileItem[])fileItemMap.get(name);

    if( fileItemArray != null )
      fileItem = fileItemArray[0];

    return fileItem;
  }

  public Enumeration<String> getFileItemNames()
  {
    return new IteratorEnumeration<String>(fileItemMap.keySet().iterator());
  }

  public Map<String, FileItem[]> getFileItemMap()
  {
    return fileItemMap;
  }

  public FileItem[] getFileItemValues( String name )
  {
    return (FileItem[])fileItemMap.get(name);
  }

  public String getParameter( String name )
  {
    String parameter = null;
    String[] parameterList = (String[])parameterMap.get(name);

    if( parameterList != null && parameterList.length > 0 ) {
      parameter = parameterList[0];
    }

    return parameter;
  }

  public Enumeration<String> getParameterNames() { return new IteratorEnumeration<String>(parameterMap.keySet().iterator()); }

  public Map<String, String[]> getParameterMap() { return parameterMap; }

  public String[] getParameterValues( String name ) { return (String[])parameterMap.get(name); }

  private static <K,V> Map<K, V[]> convertListMapToArrayMap(Map<K, List<V>> listMap, Map<K, V[]> arrayMap, Class<V> elementType) {
    if( arrayMap == null ) 
      arrayMap = new HashMap<K, V[]>();
    for( Map.Entry<K, List<V>> e : listMap.entrySet() ) {
      V[] valueArray = (V[]) Array.newInstance(elementType, e.getValue().size());
      arrayMap.put(e.getKey(), e.getValue().toArray(valueArray));
    }
    return arrayMap;
  }

  private static <K, V> void putListMapValue( Map<K, List<V>> map, K key, V value )
  {
    List<V> valueArray = map.get(key);

    if( valueArray == null ) {
      valueArray = new ArrayList<V>();
      map.put(key, valueArray);
    }
    valueArray.add(value);
  }

  private static DiskFileItemFactory createDiskFileItemFactory( int sizeThreshold, String repositoryPath ) throws FileUploadException {
    DiskFileItemFactory factory = new DiskFileItemFactory();
    
    // the location for saving data that is larger than getSizeThreshold()
    File repository = new File(repositoryPath);
    factory.setRepository(repository);
    
    // maximum size that will be stored in memory
    factory.setSizeThreshold(sizeThreshold);
    
    // Check to see if repository exists; if not, try to create it; if this fails, throw an exception. 
    if( repository.exists() ) {
      if( !repository.isDirectory() ) {
        throw new FileUploadException("Cannot upload files because the specified temporary "
                        + "directory is of type file. (" + repository.getAbsolutePath() + ")");
      }
    } else if( !repository.mkdir() ) {
      throw new FileUploadException("Cannot upload files because the specified temporary "
                        + " does not exist, and attempts to create it have failed. ("
                        + repository.getAbsolutePath() + ")");
          
    }
    return factory;
  }

  protected void logFileItemMap()
  {
    log.debug("File item map has "+fileItemMap.keySet().size()+" keys.");
    Iterator<Map.Entry<String, FileItem[]>> entryIterator = fileItemMap.entrySet().iterator();
    while( entryIterator.hasNext() ) {
      Map.Entry<String, FileItem[]> entry = entryIterator.next();
      String key = entry.getKey();
      FileItem[] fileItems = entry.getValue();

      log.debug("Key '"+key+"' has "+fileItems.length+" entries.");

      for( int i = 0; i < fileItems.length; i++ ) {
        log.debug("Name='"+fileItems[i].getName()+"', size="+fileItems[i].getSize()+", content-type='"+fileItems[i].getContentType()+"'");
      }
    }
  }
}

class IteratorEnumeration<T>
  implements Enumeration<T>
{
  Iterator<T> wrappedIterator = null;
  public IteratorEnumeration( Iterator<T> wrappedIterator ) { this.wrappedIterator = wrappedIterator; }
  public boolean hasMoreElements() { return wrappedIterator.hasNext(); }
  public T nextElement() { return wrappedIterator.next(); }
}
