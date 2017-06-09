/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.endpoint.http.springconfig;

import com.comcast.redirector.endpoint.http.Constants;
import com.comcast.redirector.endpoint.http.context.HttpEndpointContext;
import com.comcast.redirector.endpoint.http.logging.AccessLoggingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {HttpEndpointContext.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] {WebConfig.class, TestReportWebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {Constants.HTTP_ENDPOINT_PREFIX + "/*"};
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{new AccessLoggingFilter()};
    }
}
