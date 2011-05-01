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
package org.xchain.framework.filter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import java.io.File;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xchain.framework.servlet.MultipartFormDataServletRequest;

/**
 * @author John Trimble
 * @author Josh Kennedy
 */
public class MultipartFormDataFilter
  implements Filter
{
  public static Logger log = LoggerFactory.getLogger(MultipartFormDataFilter.class);

  /* maxSize */
  public static final String MAX_SIZE_PARAMETER_NAME = "max-size";

  /* sizeThreshold */
  public static final String SIZE_THRESHOLD_PARAMETER_NAME = "size-threshold";

  /* tempDir */
  public static final String REPOSITORY_PATH_PARAMETER_NAME = "repository-path";

  public static final long DEFAULT_MAX_SIZE = 1000000;
  public static final int DEFAULT_SIZE_THRESHOLD = 4096;

  public long maxSize;
  public int sizeThreashold;
  public String repositoryPath;

  public void init(FilterConfig filterConfig)
    throws javax.servlet.ServletException
  {

    // get the systems default temp dir
    File tempDir = (File)filterConfig.getServletContext().getAttribute("javax.servlet.context.tempdir");
    String DEFAULT_REPOSITORY_PATH = (tempDir != null) ? tempDir.getPath() : null;

    // Read in the max size param value.
    maxSize = getLongInitParameter( filterConfig, MAX_SIZE_PARAMETER_NAME, DEFAULT_MAX_SIZE );

    // Read in the size threshold param value.
    sizeThreashold = getIntInitParameter( filterConfig, SIZE_THRESHOLD_PARAMETER_NAME, DEFAULT_SIZE_THRESHOLD);

    // Read in the repository path param value.
    repositoryPath = getStringInitParameter( filterConfig, REPOSITORY_PATH_PARAMETER_NAME, DEFAULT_REPOSITORY_PATH);

    if (repositoryPath == null)
    {
      throw new ServletException("Unable to determine DEFAULT_REPOSITORY_PATH, please specify a path using the 'repository-path' config param, or ensure that 'javax.servlet.context.tempdir' is set properly.");
    }

    log.info("Using max size param of: " + maxSize);
    log.info("Using threashold size of: " + sizeThreashold);
    log.info("Using repository path: " + repositoryPath);

  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws java.io.IOException, javax.servlet.ServletException
  {
    if( log.isDebugEnabled() ) {
      log.debug("Processing multipart form data.");
    }

    HttpServletRequest httpServletRequest = (HttpServletRequest)request;
    HttpServletResponse httpServletResponse = (HttpServletResponse)response;
    // check the content type.
    String contentType = httpServletRequest.getHeader("Content-Type");

    // if this is a multipart form data request, then wrap the request object.
    if ( contentType != null && contentType.startsWith("multipart/form-data") && !(request instanceof MultipartFormDataServletRequest) ) {
      try {
        request = new MultipartFormDataServletRequest( httpServletRequest, maxSize, sizeThreashold, repositoryPath);
      }
      catch( FileUploadException fue ) {
        if( fue instanceof FileUploadBase.SizeLimitExceededException ) {
          log.debug("File to large.", fue);
          httpServletResponse.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
          return;
        }
//        From the apache commons fileupload api: Deprecated. As of commons-fileupload 1.2, the presence of a content-length header is no longer required.
//        else if( fue instanceof FileUploadBase.UnknownSizeException ) {
//          httpServletResponse.sendError(HttpServletResponse.SC_LENGTH_REQUIRED);
//          return;
//        }
        else if( fue instanceof FileUploadBase.InvalidContentTypeException ) {
          log.debug("Invalid content type.", fue);
          httpServletResponse.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE );
          return;
        }
        else {
          if( log.isDebugEnabled() )
            log.debug("File upload failed.", fue);
          throw new ServletException("Could not upload file.", fue);
        }
      }
    }

    log.debug("Passing to next filter.");
    // do the chain.
    filterChain.doFilter(request, response);
  }

  public void destroy() {
    // nothing to do.
  }

  protected static long getLongInitParameter( FilterConfig filterConfig, String parameterName, long defaultValue )
    throws ServletException
  {
    long value = defaultValue;
    String parameterValue = filterConfig.getInitParameter(parameterName);
    if( parameterValue != null ) {
      try {
        value = Long.parseLong(parameterValue);
      }
      catch (NumberFormatException nfe) {
        throw new ServletException("Could not parse " + parameterName +
                                   " parameter for filter " +
                                   filterConfig.getFilterName() + ".", nfe);
      }
    }
    return value;
  }

  protected static int getIntInitParameter( FilterConfig filterConfig, String parameterName, int defaultValue )
    throws ServletException
  {
    int value = defaultValue;
    String parameterValue = filterConfig.getInitParameter(parameterName);
    if( parameterValue != null ) {
      try {
        value = Integer.parseInt(parameterValue);
      }
      catch (NumberFormatException nfe) {
        throw new ServletException("Could not parse " + parameterName +
                                   " parameter for filter " +
                                   filterConfig.getFilterName() + ".", nfe);
      }
    }
    return value;
  }

  protected static String getStringInitParameter( FilterConfig filterConfig, String parameterName, String defaultValue )
    throws ServletException
  {
    // get the value.
    String value = defaultValue;
    String parameterValue = filterConfig.getInitParameter(parameterName);
    if( parameterValue != null ) {
        value = parameterValue;
    }

    return value;
  }

}
