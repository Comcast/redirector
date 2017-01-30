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

import com.comcast.redirector.common.Constants;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.ruleengine.model.ReturnStatementType;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

import static com.comcast.redirector.common.Constants.*;

public class LoggerUtils {
    private static final String DEFAULT = "default";
    private static final int SAT_EXPOSURE_TAIL_SIZE = 12;
    
    public static String getRedirectLog(Map<String, String> context, InstanceInfo instanceInfo) {
        String receiverId = "";
        if (context.containsKey(Constants.RECEIVER_ID)) {
            receiverId = " receiverId=" + context.get(Constants.RECEIVER_ID);
        }

        StringBuilder logStr = new StringBuilder("Redirect:")
                .append(receiverId)
                .append(getInstanceInfoString(instanceInfo))
                .append(" parameters=")
                .append(getContextString(context, instanceInfo));

        return logStr.toString();
    }
    
    private static String getContextString(Map<String, String> context, InstanceInfo instanceInfo) {
        Map<String, String> collect;
        
        if (instanceInfo == null) {
            return "";
        }
        
        if ((instanceInfo.getAppliedUrlRules() != null && instanceInfo.getAppliedUrlRules().stream().anyMatch(rule -> rule.toLowerCase().contains(DEFAULT)))
                && ((instanceInfo.getServer() != null && instanceInfo.getServer().getReturnStatementType() == ReturnStatementType.DISTRIBUTION_RULE)
                || (instanceInfo.getRuleName() != null && instanceInfo.getRuleName().toLowerCase().contains(DEFAULT)))) {
            
            collect = context.entrySet().stream()
                    .filter(map -> map.getKey().equals(SERVICE_ACCOUNT_ID)
                            || map.getKey().equals(MAC)
                            || map.getKey().equals(CLIENT_ADDRESS))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
        } else {
            String serviceAccessTokenEntry = context.get(SERVICE_ACCESS_TOKEN);
    
            collect = context.entrySet().stream()
                    .filter(map -> !map.getKey().equals(FONT_FAMILIES)
                            && !map.getKey().equals(MIME_TYPES)
                            && !map.getKey().equals(SERVICE_ACCESS_TOKEN))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            if (serviceAccessTokenEntry !=null ) {
                collect.put(SERVICE_ACCESS_TOKEN, StringUtils.right(serviceAccessTokenEntry, SAT_EXPOSURE_TAIL_SIZE));
            }
            
        }
        
        return collect.toString();
    }
    
    private static String getInstanceInfoString(InstanceInfo instanceInfo) {
        StringBuilder logStr = new StringBuilder();

        if (instanceInfo == null)
            return logStr.toString();

        if (StringUtils.isNotBlank(instanceInfo.getUrl())) {
            if (!instanceInfo.getIsAdvancedRule()) {
                logStr.append(" url=").append(instanceInfo.getUrl());
                logStr.append(" xreStack=").append(instanceInfo.getStack());
                logStr.append(" flavor=").append(instanceInfo.getFlavor());
                if (instanceInfo.getServer().getReturnStatementType() == ReturnStatementType.DISTRIBUTION_RULE) {
                    logStr.append(" usingDistribution=yes"); // Only Distribution rules do not have Rule_Name.
                    logStr.append(" flavorRule=DistributedRule"); // For splunk optimization purposes
                } else {
                    logStr.append(" flavorRule=").append(instanceInfo.getRuleName());
                }
                logStr.append(" urlRule=").append(instanceInfo.getAppliedUrlRules());
                logStr.append(" mode=Offline");
                logStr.append(" isStackBased=").append(instanceInfo.isStackBased().toString());
            } else {
                logStr.append(" url=")
                        .append(instanceInfo.getUrl())
                        .append(" flavorRule=").append(instanceInfo.getRuleName())
                        .append(" isAdvancedRule=true");
            }
        } else if (instanceInfo.isServerGroup()) {
            logStr.append(" Got Server group. flavorRule=").append(instanceInfo.getRuleName());
        } else {
            logStr.append("Unable to resolve stack/flavor:")
                    .append((instanceInfo.getServer() != null ? instanceInfo.getServer().getPath() : "NULL"))
                    .append(". Server URL is empty. Cannot redirect");
        }

        return logStr.toString();
    }
}
