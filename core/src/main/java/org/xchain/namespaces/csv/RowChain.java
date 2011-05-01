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
package org.xchain.namespaces.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Element;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Starts a new row for the CSV. Flushes the row to the output after executing its 
 * children. This chain may only be executed within the execution of a CsvWriterChain
 * (or some other chain that populates org.xchain.namespaces.csv.AbstractCsvChain.csvWriterThreadLocal);
 * although, a CsvWriterChain need not be its caller. RowChains may not have their
 * execution nested.
 * 
 * @author John Trimble
 */
@Element(localName="row")
public abstract class RowChain extends AbstractCsvChain {
  
  public boolean execute(JXPathContext context) throws Exception {
    // Initial error checking.
    if( AbstractCsvChain.cellListThreadLocal.get() != null )
      throw new Exception("Cannot nest RowChain instances.");
    
    boolean result = false;
    List<String> rowList = new ArrayList<String>();
    String[] row;
    
    try {
      AbstractCsvChain.cellListThreadLocal.set(rowList);
      result = super.execute(context);
      rowList = AbstractCsvChain.cellListThreadLocal.get();
    } finally {
      AbstractCsvChain.cellListThreadLocal.set(null);
    }
    
    CSVWriter writer = AbstractCsvChain.csvWriterThreadLocal.get();
    row = new String[rowList.size()];
    rowList.toArray(row);
    writer.writeNext(row);
    return result;
  }
  
}
