/*
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
 * @author Alexander Ievstratiev (oievstratiev@productengine.com)
 */

package com.comcast.redirector.common.util;

public class UrlUtils {
    
    public static String buildUrl(final String finalBaseUri, final String finalEndpoint) {
        
        StringBuffer url =  new StringBuffer();
        
        if (finalBaseUri != null) {
            String baseUri = finalBaseUri.trim();
            if (baseUri.endsWith("/")) {
                url.append(baseUri.substring(0, baseUri.length() - 1));
            } else {
                url.append(baseUri);
            }
        
            if (finalEndpoint != null) {
                
                String endpoint = finalEndpoint.trim();
                
                if (endpoint.length() > 0 ) {
                    url.append("/");
    
                    if (endpoint.startsWith("/")) {
                        url.append(endpoint.substring(1, endpoint.length()));
                    } else {
                        url.append(endpoint);
                    }
                }
                
            }
            return url.toString();
        }
        
        return null;
    }

}
