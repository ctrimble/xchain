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

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.xchain.annotations.Attribute;
import org.xchain.annotations.AttributeType;
import org.xchain.annotations.Element;

/**
 * XChain for populating the next cell in the current row. This chain may only be 
 * executed within the execution of a RowChain (or some other chain that populates 
 * org.xchain.namespaces.csv.AbstractCsvChain.cellListThreadLocal).
 * 
 * @author John Trimble
 */
@Element(localName="cell")
public abstract class CellCommand extends AbstractCsvChain {
  
  @Attribute(localName="select", type=AttributeType.JXPATH_VALUE)
  public abstract String getSelect( JXPathContext context );
  public abstract boolean hasSelect();
  
  public boolean execute(JXPathContext context) throws Exception {
    if( !hasSelect() ) 
      throw new Exception("Must have a select value.");
    List<String> cellList = AbstractCsvChain.cellListThreadLocal.get();
    cellList.add(getSelect(context));
    return false;
  }
}
