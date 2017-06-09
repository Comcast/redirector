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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.common.util;


import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class StacksHelper {

    /**
     * @param flavorName   flavor name e.g. 1.50 service name e.g. xreGuide
     * @param stackDataSet Set of {@link StackData} instances containing stack paths and hosts for each path
     * @param whitelisted
     * @return true when Set of {@link StackData} have more than zero whitelisted hosts for the flavorName otherwise return false
     */
    public static boolean isActiveAndWhitelistedHostsForFlavor(String flavorName, Set<StackData> stackDataSet, Whitelisted whitelisted) {
        for (StackData stackData : stackDataSet) {
            if (flavorName.equals(stackData.getFlavor())) {
                if (stackData.getHosts().get().size() > 0) {
                    if (isWhitelistedStack(stackData, whitelisted)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param stackData   {@link StackData} instance containing stack path and hosts for path
     * @param whitelisted {@link Whitelisted}
     * @return true if stack is whitelisted otherwise return false
     */
    public static boolean isWhitelistedStack(StackData stackData, Whitelisted whitelisted) {
        String stackPathWithoutServiceName = getStackPathWithoutServiceName(stackData.getPath());
        String stackPath = getStackPath(stackPathWithoutServiceName);
        if (getWhitelistSet(whitelisted).contains(stackPath)) {
            return true;
        }
        return false;
    }

    /**
     * @param stackPathWithoutServiceName of stack path without serviceName e.g. /PO/POC5/1.40
     * @return stack e.g. /PO/POC5/1.40
     */
    public static String getStackPath(String stackPathWithoutServiceName) {
        return StringUtils.substringBeforeLast(stackPathWithoutServiceName, RedirectorConstants.DELIMETER);
    }

    /**
     * @param stackPathWithoutServiceName of stack path without serviceName e.g. /PO/POC5/1.40
     * @return flavor e.g. 1.40
     */
    public static String getFlavorPath(String stackPathWithoutServiceName) {
        return StringUtils.substringAfterLast(stackPathWithoutServiceName, RedirectorConstants.DELIMETER);
    }

    /**
     * @param fullPath of full path e.g. /PO/POC5/1.40/xreGuide
     * @return serviceName e.g. xreGuide
     */
    public static String getServiceName(String fullPath) {
        return StringUtils.substringAfterLast(fullPath, RedirectorConstants.DELIMETER);
    }

    /**
     * @param fullPath of full path e.g. /PO/POC5/1.40/xreGuide
     * @return stack path without serviceName e.g. /PO/POC5/1.40
     */
    public static String getStackPathWithoutServiceName(String fullPath) {
        return  StringUtils.substringBeforeLast(fullPath, RedirectorConstants.DELIMETER);
    }

    public static Set<String> getWhitelistSet(Whitelisted whitelisted) {
        return Sets.newHashSet(whitelisted.getPaths());
    }
}
