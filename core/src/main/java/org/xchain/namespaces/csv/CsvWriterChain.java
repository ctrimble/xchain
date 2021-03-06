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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Chain for production of CSV data. All other CSV chains should execute within the execution of
 * this chain.
 * 
 * @author John Trimble
 */
@Element(localName="csv-writer")
public abstract class CsvWriterChain extends AbstractCsvChain {
  
  @Attribute(localName="output-stream", type=AttributeType.JXPATH_VALUE)
  public abstract OutputStream getOutputStream(JXPathContext context);
  public abstract boolean hasOutputStream();
  
  @Attribute(localName="csv-writer", type=AttributeType.JXPATH_VALUE)
  public abstract CSVWriter getCSVWriter(JXPathContext context);
  public abstract boolean hasCSVWriter();
  
  public boolean execute(JXPathContext context) throws Exception {
    // Do some initial error checking.
    if( !hasOutputStream() && !hasCSVWriter() )
      throw new Exception("Must set either an output stream or a csv writer.");
    if( AbstractCsvChain.csvWriterThreadLocal.get() != null )
      throw new Exception("Nesting of CsvWriterChain instances is not permitted.");
    
    boolean result = false;
    CSVWriter writer = null;
    // If we have an output stream, use it to create a new CSVWriter.
    if( hasOutputStream() ) {
      OutputStream output = this.getOutputStream(context);
      Writer streamWriter = new OutputStreamWriter(output);
      // writer = new CSVWriter(streamWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
      writer = new CSVWriter(streamWriter);
    }
    
    // If we have a CSVWriter... then lets use it.
    if( hasCSVWriter() )
      writer = this.getCSVWriter(context);
    
    try {
      // We put the writer in a thread local so that the row and cell chains can make use of it.
      AbstractCsvChain.csvWriterThreadLocal.set(writer);
      result = super.execute(context);
    } finally {
      AbstractCsvChain.csvWriterThreadLocal.set(null);
    }
    
    writer.close();
    return result;
  }
}
