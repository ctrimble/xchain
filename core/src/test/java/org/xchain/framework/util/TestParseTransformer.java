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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * @author Christian Trimble
 * @author Devon Tackett
 */
public class TestParseTransformer
{
  @Test public void parseOneAttribute()
    throws Exception
  {
    Matcher matcher = Pattern.compile("\\A").matcher("name=\"value\"");

    Map<String, String> attributes = ParserUtil.parseAttributeMap(matcher);

    assertEquals("The attributes list has the wrong number of attributes.", 1, attributes.size());
    assertMapEntry(attributes, "name", "value");
  }

  @Test public void parseTransformerOneAttribute()
    throws Exception
  {
    String data = "system-id=\"file\"";
    ParsedTransformer parsedTransformer = ParserUtil.parseTransformer(data);

    // check the sizes
    assertParsedTransformerSizes( parsedTransformer, 1, 0, 0 );
    assertMapEntry(parsedTransformer.getAttributes(), "system-id", "file");
  }

  @Test public void parseTransformerManyAttributes()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("attribute-one=\"value-one\" attribute-two = \"value-two\" attribute-three=  \"value-three\" attribute-four  =\"value-four\"");

    assertParsedTransformerSizes( parsedTransformer, 4, 0, 0 );
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-one", "value-one");
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-two", "value-two");
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-three", "value-three");
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-four", "value-four");
  }

  @Test public void parseTransformerOneParameter()
    throws Exception
  {
    ParsedTransformer parsedTransformer = ParserUtil.parseTransformer("parameters parameter-one=\"value-one\"");

    // check the sizes
    assertParsedTransformerSizes( parsedTransformer, 0, 1, 0 );
    assertMapEntry(parsedTransformer.getParameters(), "parameter-one", "value-one");
  }

  @Test public void parseTransformerManyParameters()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("parameters parameter-one=\"value-one\" parameter-two = \"value-two\" parameter-three=  \"value-three\" parameter-four  =\"value-four\"");

    assertParsedTransformerSizes( parsedTransformer, 0, 4, 0 );
    assertMapEntry(parsedTransformer.getParameters(), "parameter-one", "value-one");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-two", "value-two");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-three", "value-three");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-four", "value-four");
  }

  @Test public void parseTransformerOneOutputProperty()
    throws Exception
  {
    ParsedTransformer parsedTransformer = ParserUtil.parseTransformer("output properties property-one=\"value-one\"");

    // check the sizes
    assertParsedTransformerSizes( parsedTransformer, 0, 0, 1 );
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-one", "value-one");
  }

  @Test public void parseTransformerManyOutputProperties()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("output properties property-one=\"value-one\" property-two = \"value-two\" property-three=  \"value-three\" property-four  =\"value-four\"");

    assertParsedTransformerSizes( parsedTransformer, 0, 0, 4 );
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-one", "value-one");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-two", "value-two");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-three", "value-three");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-four", "value-four");
  }

  @Test public void parseTransformerAttributesPrameters()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("attribute-one = \"attribute-value-one\" attribute-two = \"attribute-value-two\" parameters parameter-one = \"parameter-value-one\" parameter-two = \"parameter-value-two\"");

    assertParsedTransformerSizes( parsedTransformer, 2, 2, 0 );
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-one", "attribute-value-one");
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-two", "attribute-value-two");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-one", "parameter-value-one");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-two", "parameter-value-two");
  }

  @Test public void parseTransformerAttributesOutputProperties()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("attribute-one = \"attribute-value-one\" attribute-two = \"attribute-value-two\" output properties property-one = \"property-value-one\" property-two = \"property-value-two\"");

    assertParsedTransformerSizes( parsedTransformer, 2, 0, 2 );
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-one", "attribute-value-one");
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-two", "attribute-value-two");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-one", "property-value-one");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-two", "property-value-two");
  }

  @Test public void parseTransformerPrametersOutputProperties()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("parameters parameter-one=\"parameter-value-one\" parameter-two = \"parameter-value-two\" output properties property-one=\"property-value-one\" property-two=\"property-value-two\"");

    assertParsedTransformerSizes( parsedTransformer, 0, 2, 2 );
    assertMapEntry(parsedTransformer.getParameters(), "parameter-one", "parameter-value-one");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-two", "parameter-value-two");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-one", "property-value-one");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-two", "property-value-two");
  }

  @Test public void parseTransformerAttributesPrametersOutputProperties()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("attribute-one = \"attribute-value-one\" attribute-two=\"attribute-value-two\" parameters parameter-one=\"parameter-value-one\" parameter-two = \"parameter-value-two\" output properties property-one=\"property-value-one\" property-two=\"property-value-two\"");

    assertParsedTransformerSizes( parsedTransformer, 2, 2, 2 );
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-one", "attribute-value-one");
    assertMapEntry(parsedTransformer.getAttributes(), "attribute-two", "attribute-value-two");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-one", "parameter-value-one");
    assertMapEntry(parsedTransformer.getParameters(), "parameter-two", "parameter-value-two");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-one", "property-value-one");
    assertMapEntry(parsedTransformer.getOutputProperties(), "property-two", "property-value-two");
  }

  @Test public void parseTransformerMisspelledParameters()
    throws Exception
  {
    String data = "attribute1=\"value1\" attribute2=\"value2\" paremeter parameter1=\"value1\"";
    try {
      ParsedTransformer parsedTransformer = ParserUtil.parseTransformer(data);
      fail("parseTransformer() parsed an illegal string.");
    }
    catch( ParseException pe ) {
      assertEquals("The location of the failure was wrong.", 39, pe.getLocation());
      assertEquals("The data of the failer was wrong.", data, pe.getData());
    }
    catch( Exception e ) {
      fail("Wrong type of exception thrown from parseTransformer(String):"+e.getClass().getName());
    }
  }

  @Test public void parseTransformerMisspelledOutputParameters()
    throws Exception
  {
    String data = "attribute1=\"value1\" attribute2=\"value2\" output priperties property1=\"value1\"";
    try {
      ParsedTransformer parsedTransformer = ParserUtil.parseTransformer(data);
      fail("parseTransformer() parsed an illegal string.");
    }
    catch( ParseException pe ) {
      assertEquals("The location of the failure was wrong.", 39, pe.getLocation());
      assertEquals("The data of the failer was wrong.", data, pe.getData());
    }
    catch( Exception e ) {
      fail("Wrong type of exception thrown from parseTransformer(String):"+e.getClass().getName());
    }
  }

  @Test public void parseTransformerWithNamedEntities()
    throws Exception
  {
    ParsedTransformer parsedTransformer =
      ParserUtil.parseTransformer("attribute = \"&quot;&apos;&lt;&gt;&amp;\" &quot;&apos;&lt;&gt;&amp; = \"value\"");

    assertParsedTransformerSizes( parsedTransformer, 2, 0, 0 );
    assertMapEntry(parsedTransformer.getAttributes(), "attribute", "\"\'<>&");
    assertMapEntry(parsedTransformer.getAttributes(), "\"\'<>&", "value");
  }

  private void assertParsedTransformerSizes( ParsedTransformer parsedTransformer, int attributesSize, int parametersSize, int outputPropertiesSize )
    throws Exception
  {
    assertEquals("The wrong number of attributes were found.", attributesSize, parsedTransformer.getAttributes().size());
    assertEquals("The wrong number of parameters were found.", parametersSize, parsedTransformer.getParameters().size());
    assertEquals("The wrong number of output properties were found.", outputPropertiesSize, parsedTransformer.getOutputProperties().size());
  }

  private void assertMapEntry( Map<String, String> map, String key, String value )
    throws Exception
  {
    assertTrue("Could not find the attribute '"+key+"'.", map.keySet().contains(key));
    assertEquals("The value for '"+key+"' was wrong.", value, map.get(key));
  }
  
}
