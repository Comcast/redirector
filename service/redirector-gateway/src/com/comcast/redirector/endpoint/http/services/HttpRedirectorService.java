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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.endpoint.http.services;

import com.comcast.redirector.RedirectorGateway;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.endpoint.http.model.Location;
import com.comcast.redirector.endpoint.http.model.RedirectLocations;
import com.comcast.redirector.metrics.Metrics;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.stream.Collectors;

public class HttpRedirectorService implements RedirectorService {
    private static final Logger log = LoggerFactory.getLogger(HttpRedirectorService.class);

    private RedirectorGateway redirectorGateway;

    ZKConfig zkConfig;

    public HttpRedirectorService(RedirectorGateway redirectorGateway) {
        this.redirectorGateway = redirectorGateway;
        zkConfig = ConfigLoader.doParse(Config.class);
    }

    public RedirectLocations redirect(String appName, Map<String, String> requestParams, HttpHeaders headers) {

        Metrics.reportGatewayRequestStats();

        Map<String, String> params = headers.entrySet().stream()
                .filter(header -> (StringUtils.isNotBlank(header.getValue().get(0))))
                .collect(Collectors.toMap(header -> header.getKey(), header -> header.getValue().get(0)));

        if (requestParams != null && !requestParams.isEmpty()) {
            params.putAll(requestParams);
        }

        return getRedirectResult(params, appName);
    }

    private RedirectLocations getRedirectResult(Map<String, String> params, String serviceName) {
        if (redirectorGateway.isAppRegistered(serviceName)) {
            InstanceInfo info = redirectorGateway.getRedirector(serviceName)
                .redirect(params);

            return convert(info);
        } else {
            Metrics.reportGatewayFailedResponseStats();
            return null;
        }
    }

    private RedirectLocations convert(InstanceInfo instanceInfo) {
        RedirectLocations locations = new RedirectLocations();

        if (instanceInfo == null) {
            return locations;
        }

        if (instanceInfo.isServerGroup()) {
            for (Server server : instanceInfo.getServerGroup().getServers()) {
                Location location = new Location(server.getURL(), server.getPath(), "");
                location.setContext(server.getQuery());
                locations.addLocation(location);
            }
        } else if (instanceInfo.getIsAdvancedRule()) {
            Location location = new Location(getUrlWithoutPort(instanceInfo.getUrl()), instanceInfo.getRuleName(), getProtocol(instanceInfo.getUrl()), getPortFromUrl(instanceInfo.getUrl()));
            location.setContext(instanceInfo.getServer().getQuery());
            locations.addLocation(location);
        } else {
            Location location = new Location(instanceInfo.getAddress(), instanceInfo.getStack(), instanceInfo.getProtocol(), String.valueOf(instanceInfo.getPort()));
            location.setContext(instanceInfo.getServer().getQuery());
            locations.addLocation(location);
        }

        return locations;
    }

    private String getUrlWithoutPort(String fullUrl) {
        String url = "";
        if (StringUtils.isNotBlank(fullUrl)) {
            try {
                String urlWithPort = fullUrl.split("/")[2];
                url = urlWithPort.lastIndexOf(":") == -1 ? urlWithPort : urlWithPort.substring(0, urlWithPort.lastIndexOf(":"));
            } catch (IndexOutOfBoundsException ex) {
                log.error("Failed to get url without port from " + fullUrl, ex);
            }
        }
        return url;
    }

    private String getPortFromUrl(String fullUrl) {
        String port = "";
        if (StringUtils.isNotBlank(fullUrl)) {
            try {
                int portStart = fullUrl.lastIndexOf(":") + 1;
                if (portStart != 0) {
                    String partStartingFromPort = fullUrl.substring(portStart, fullUrl.length());
                    int portEnd = (partStartingFromPort.contains("/")) ? partStartingFromPort.indexOf("/") : partStartingFromPort.indexOf("?");
                    port = partStartingFromPort.substring(0, portEnd);
                }
            } catch (IndexOutOfBoundsException ex) {
                log.error("Failed to get port from " + fullUrl, ex);
            }
        }
        return port;
    }

    private String getProtocol(String fullUrl) {
        if (StringUtils.isNotBlank(fullUrl)) {
            return fullUrl.substring(0, fullUrl.lastIndexOf("://"));
        }
        return "";
    }
}
