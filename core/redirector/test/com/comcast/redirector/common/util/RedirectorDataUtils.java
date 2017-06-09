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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.common.util;

import java.util.Set;

public class RedirectorDataUtils {
    public static String getServerString(String id, String flavor) {
        return "<server>\n" +
                "            <name>" + id + " Server</name>\n" +
                "            <url>{protocol}://{host}:{port}/{urn}</url>\n" +
                "            <path>" + flavor + "</path>\n" +
                "            <description>" + id + " server route</description>\n" +
                "        </server>";
    }

    public static String getFlavorRuleString(String id, String expression, String flavor) {
        return "<if id=\"" + id + "\">\n" + expression +
                "        <return>\n" +
                "            <server isNonWhitelisted=\"false\">\n" +
                "                <name>" + id + "</name>\n" +
                "                <url>{protocol}://{host}:{port}/{urn}</url>\n" +
                "                <path>" + flavor + "</path>\n" +
                "                <description>" + id + " server route</description>\n" +
                "            </server>\n" +
                "        </return>\n" +
                "    </if>";
    }

    public static String getUrlRuleString(String id, String expression, String urlRule) {
        return "<if id=\"" + id + "\">\n" + expression + "<return>\n" + urlRule + "</return>\n</if>";
    }

    public static String getUrlRuleString(String id, String param, String value, String urlRule) {
        return getUrlRuleString(id,"<equals><param>" + param + "</param><value>" + value + "</value></equals>", urlRule);
    }

    public static String getEqualsRuleString(String id, String param, String value, String returnFlavor) {
        return getFlavorRuleString(
                id,
                "<equals><param>" + param + "</param><value>" + value + "</value></equals>",
                returnFlavor);
    }

    public static String getUrlRuleResult(String urn, String protocol, String port, String ipProtocolVersion) {
        return "<urlRule>\n" +
                "            <urn>" + urn + "</urn>\n" +
                "            <protocol>" + protocol + "</protocol>\n" +
                "            <port>" + port + "</port>\n" +
                "            <ipProtocolVersion>" + ipProtocolVersion + "</ipProtocolVersion>\n" +
                "        </urlRule>";
    }

    public static String getDistributionRuleString(String id, String percent, String flavor) {
        return "<rule>\n" +
                "            <id>" + id + "</id>\n" +
                "            <percent>" + percent + "</percent>\n" +
                "            <server>\n" +
                "                <name>distribution server 1</name>\n" +
                "                <url>{protocol}://{host}:{port}/{urn}</url>\n" +
                "                <path>" + flavor + "</path>\n" +
                "                <description>" + percent + "% distribution server</description>\n" +
                "            </server>\n" +
                "        </rule>";
    }

    public static String getDistributionStatementString(String... rule) {
        String body = "";
        for (String r : rule) {
            body += r;
        }

        return "<distribution>" + body + "</distribution>";
    }

    public static String getWhitelistString(Set<String> stacks) {
        String prefix = "<whitelisted>";
        String body = "";
        for (String stack : stacks) {
            body += "<paths>" + stack + "</paths>";
        }
        String suffix = "</whitelisted>";

        return prefix + body + suffix;
    }

    public static String getNamespacedListString(String name, String description, Set<String> values) {
        String prefix =  "<namespaced_list name=\"" + name + "\">\n" +
                "        <description>" + description + "</description>\n";

        String body = "";
        for (String value : values) {
            body += "<value>" + value + "</value>";
        }

        String suffix = "</namespaced_list>";
        return prefix + body + suffix;
    }
}
