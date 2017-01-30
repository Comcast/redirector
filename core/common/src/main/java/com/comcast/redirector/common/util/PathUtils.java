/**
 * Copyright 2016 Comcast Cable Communications Management, LLC 
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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.common.util;

import org.apache.commons.lang3.StringUtils;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.dataaccess.cache.ZKPathHelperConstants.STACKS_PATH;

/**
 * Util class for getting serviceName, Stack and Flavor from full service path:
 */
public class PathUtils {
    /**
     * @param value  of full path e.g. /services/PO/POC5/1.40/xreGuide
     * @return stack e.g. /PO/POC5/1.40
     */
    public static String getStackFromPath(String value) {
        return value.substring(value.indexOf(STACKS_PATH) + STACKS_PATH.length(), value.lastIndexOf(DELIMETER));
    }

    /**
     * @param value  of full path e.g. /services/PO/POC5/1.40/xreGuide
     * @return flavor e.g. 1.40
     */
    public static String getFlavor(String value) {
        String stack = getStackFromPath(value);
        return getFlavorFromXREStackPath(stack);
    }

    /**
     * @param value of XRE Stack path e.g. /PO/POC5/1.40
     * @return flavor e.g. 1.40
     */
    public static String getFlavorFromXREStackPath(String value) {
        return (StringUtils.isNotBlank(value)) ? value.substring(value.lastIndexOf(DELIMETER) + 1) : "";
    }

    /**
     * @param value of full path e.g. /services/PO/POC5/1.40/xreGuide
     * @return application name  e.g. xreGuide
     */
    public static String getAppFromPath(String value) {
        return (StringUtils.isNotBlank(value)) ? value.substring(value.lastIndexOf(DELIMETER) + 1) : "";
    }
}
