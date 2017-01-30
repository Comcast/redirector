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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.core.config;

import java.util.HashSet;
import java.util.Set;

public class RedirectorCoreConfigUtil {

    private static Set<String> providerStrategies = new HashSet<String>() {{
        add("random"); add("roundrobin");
    }};

    /**
     * @param config {@link ZKConfig}
     * @throws IllegalArgumentException if config is invalid
     */
    public static void validate(ZKConfig config) throws IllegalArgumentException {
        String error = validateConfig(config);
        if (error != null) {
            throw new IllegalArgumentException("misconfiguration: " + error);
        }
    }

    private static String validateConfig(ZKConfig config) {
        if (config.getZooKeeperRetryAttempts() <=0 ) {
            return "retryAttempts <= 0";
        }
        if (config.getZooKeeperRetryInterval() <=0 ) {
            return "retryInterval <= 0";
        }
        if (config.getZooKeeperConnectionTimeout() <=0) {
            return "zooKeeperConnectionTimeout <= 0";
        }
        if (config.getZooKeeperSessionTimeout() <=0) {
            return "zooKeeperSessionTimeout <= 0";
        }
        if (config.getZooKeeperBasePath() == null) {
            return "basePath is null";
        }
        if (config.getServiceName() == null || config.getServiceName().isEmpty()) {
            return "serviceName is null or empty";
        }
        if (config.getZooKeeperProviderStrategy() == null) {
            return "provider strategy is null";
        }
        if (config.getDefaultWeightOfTheNode() <= 0) {
            return "default value of node <= 0";
        }
        if (config.useZooKeeperWaitTimePolicy()) {
            if (config.getZooKeeperWaitTimeBeforeReconnectMin() <= 0) {
                return "min value of WaitTimeBeforeReconnect <= 0";
            }
            if (config.getZooKeeperWaitTimeBeforeReconnectMax() <= 0) {
                return "max value of WaitTimeBeforeReconnect <= 0";
            }
            if (config.getZooKeeperWaitTimeBeforeReconnectMax() < config.getZooKeeperWaitTimeBeforeReconnectMin()) {
                return "max < min in WaitTimeBeforeReconnect ";
            }
        }
        if (!providerStrategies.contains(config.getZooKeeperProviderStrategy().toLowerCase())) {
            return "unknown provider strategy: \"" + config.getZooKeeperProviderStrategy() + "\"";
        }
        return validateZooKeeperConnectionString(config.getZooKeeperConnection());
    }

    // comma separated host:port values, e.g. "127.0.0.1:2181,localhost:2182"
    public static String validateZooKeeperConnectionString(String zooKeeperConnectionString) {
        if (zooKeeperConnectionString == null || zooKeeperConnectionString.isEmpty()) {
            return "zooKeeperConnectionString is null or empty";
        }

        String[] parts = zooKeeperConnectionString.split(",");
        for (String hostAndPort : parts) {
            String error = validateHostAndPort(hostAndPort);
            if (error != null) {
                return "incorrect zooKeeperConnectionString: " + error;
            }
        }

        return null;
    }

    private static String validateHostAndPort(String hostAndPort) {
        String parts[] = hostAndPort.split(":");
        if (parts.length != 2) {
            return ("host and port is invalid: \"" + hostAndPort + "\"");
        }

        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            return ("port is not a numeric value: \"" + parts[1] + "\"");
        }
        if (port < 0 || port > 65535) {
            return ("port is out of bounds [0, 65535]: \"" + port + "\"");
        }

        return null;
    }
}
