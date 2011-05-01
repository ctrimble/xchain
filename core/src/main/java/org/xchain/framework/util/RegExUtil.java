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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing parsing operations.
 *
 * @author Christian Trimble
 * @author Josh Kennedy
 */
public class RegExUtil
{
  private static Logger log = LoggerFactory.getLogger(RegExUtil.class);

  /**
   * Compiles a regular expression and logs any problems to this class' log.
   */
  public static Pattern compilePattern( String regex, String errorMessage )
  {
    return compilePattern( regex, log, errorMessage );
  }

  /**
   * Compiles a regular expression and logs an problems to the provided log.  If
   * a compile error occures, then null is returned.  This class will not rethrow the
   * error, so that it's use in a static context will not cause a ClassNotFoundException.
   */
  public static Pattern compilePattern( String regex, Logger log, String errorMessage )
  {
    Pattern pattern = null;
    try{
      // Compile the pattern.
      pattern = Pattern.compile(regex);
    }
    catch( PatternSyntaxException pse ) {
      if( log.isErrorEnabled() ) {
        log.error(errorMessage, pse);
      }
      pse.printStackTrace();
    }
    return pattern;
  }
  
  /**
   * Exists to allow an upgrade path from Commons Logging to SLF4J
   * 
   * @param regex
   * @param log
   * @param errorMessage
   * @return
   */
  @Deprecated
  public static Pattern compilePattern( String regex, org.apache.commons.logging.Log log, String errorMessage )
  {
    Pattern pattern = null;
    try{
      // Compile the pattern.
      pattern = Pattern.compile(regex);
    }
    catch( PatternSyntaxException pse ) {
      if( log.isErrorEnabled() ) {
        log.error(errorMessage, pse);
      }
      pse.printStackTrace();
    }
    return pattern;
  }
}
  
