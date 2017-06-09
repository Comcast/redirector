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

package com.comcast.redirector.api.filter;

import com.comcast.redirector.api.auth.model.AccessMode;
import com.comcast.redirector.api.auth.model.Operation;
import com.comcast.redirector.api.auth.model.Permission;
import com.comcast.redirector.common.RedirectorConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.comcast.redirector.common.RedirectorConstants.*;
import static com.comcast.redirector.common.RedirectorConstants.DISTRIBUTION_WITH_DEFAULT_CONTROLLER_PATH;
import static com.comcast.redirector.common.RedirectorConstants.END_TO_END_PATH;

public class Request {
    private static final String QUERY_PARAM_START = "?";
    public static final String ENDPOINT_MAPPING = "data";

    private String url;
    private String normalizedUrl;
    private String method;

    public Request(String url, String method) {
        this.url = url;
        this.method = method;

        this.normalizedUrl = normalize(url);
    }

    private static String normalize(String url) {
        int queryParamStart = url.indexOf("?");
        return url.replace(url.substring(queryParamStart > 0 ? queryParamStart : url.length() , url.length()), "")
                .replace(url.substring(0, url.indexOf(ENDPOINT_MAPPING) + ENDPOINT_MAPPING.length()), "");
    }

    public boolean isUrlMatchingEndpoint(String endpoint) {
        String endpointMapping = ENDPOINT_MAPPING + RedirectorConstants.DELIMETER + endpoint;
        return  Objects.equals(endpoint, "*") || url.toLowerCase().indexOf(endpointMapping.toLowerCase()) > 0;
    }

    public boolean isUrlMatchingEntity(String entity) {
        boolean isLast = StringUtils.endsWithIgnoreCase(url, RedirectorConstants.DELIMETER + entity);
        boolean isInTheMiddle = StringUtils.containsIgnoreCase(url, RedirectorConstants.DELIMETER + entity + RedirectorConstants.DELIMETER);
        boolean isLastBeforeQueryParam = StringUtils.containsIgnoreCase(url, RedirectorConstants.DELIMETER + entity + QUERY_PARAM_START);

        return  isLast || isInTheMiddle || isLastBeforeQueryParam;
    }

    public boolean isUrlMatchingRequestUrl(Request request) {
        return this.url.contains(request.url);
    }

    public boolean isMethodEquals(Request request) {
        return request != null && Objects.equals(method, request.method);
    }

    public boolean accessModeEquals(Permission permission) {
        return permission != null && AccessMode.fromOperation(Operation.fromHttpMethod(method)) == permission.getAccessMode();
    }

    public String getNormalizedUrl() {
        return normalizedUrl;
    }

    public String getApplicationFromUrl(AppExtractionStrategy strategy) {
        return strategy.extract(normalizedUrl);
    }

    public String getMethod() {
        return method;
    }

    public interface AppExtractionStrategy {
        String extract(String url);
        boolean isEligible(String url);
    }

    public static Boolean isUrlStartingWithOneOfEndpointsInList(String url, List<String> endpoints) {
        return endpoints.stream().filter(url::startsWith).count() != 0;
    }

    public static class AppNameExtractionStrategies {
        static Request.AppExtractionStrategy EXTRACT_LEADING_APP = new Request.AppExtractionStrategy() {
            private List<String> endpointsWithLeadingServiceName = Arrays.asList(
                    REDIRECTOR_TEST_SUITE_PATH, STACKS_CONTROLLER_PATH, RULES_CONTROLLER_PATH,
                    URL_RULES_CONTROLLER_PATH, WHITELISTED_CONTROLLER_PATH, WHITELISTED_UPDATES_CONTROLLER_PATH,
                    SERVERS_CONTROLLER_PATH, DISTRIBUTION_CONTROLLER_PATH, DELIMETER + MODEL_RELOAD_PATH,
                    PENDING_CONTROLLER_PATH, SUMMARY_PATH);

            @Override
            public boolean isEligible(String url) {
                return isUrlStartingWithOneOfEndpointsInList(url, endpointsWithLeadingServiceName);
            }

            @Override
            public String extract(String url) {
                //delete endpoint name
                url = url.substring(url.indexOf(RedirectorConstants.DELIMETER, 1) + 1, url.length());

                String[] leadingWords = {"export/", "runAuto/"};
                for (String word : leadingWords) {
                    if (url.startsWith(word)) {
                        url = url.replace(word, "");
                    }
                }

                //if there are some path segments after app name
                int indexOfApplicationEnd = url.indexOf("/");
                if (indexOfApplicationEnd < 0) {
                    indexOfApplicationEnd = url.length();
                }
                return  url.substring(0, indexOfApplicationEnd);
            }
        };

        static Request.AppExtractionStrategy EXTRACT_TRAILING_APP = new Request.AppExtractionStrategy() {
            private List<String> endpoints = Arrays.asList(
                    DISTRIBUTION_WITH_DEFAULT_CONTROLLER_PATH, END_TO_END_PATH
            );

            @Override
            public boolean isEligible(String url) {
                return isUrlStartingWithOneOfEndpointsInList(url, endpoints);
            }

            @Override
            public String extract(String url) {
                if (url.endsWith(RedirectorConstants.DELIMETER)) {
                    url = url.substring(0, url.length());
                }
                return url.substring(url.lastIndexOf(RedirectorConstants.DELIMETER) + 1, url.length());
            }
        };

        public static Stream<AppExtractionStrategy> stream() {
            return Stream.of(EXTRACT_LEADING_APP, EXTRACT_TRAILING_APP);
        }
    }
}
