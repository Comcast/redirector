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

package com.comcast.xre.redirector.test;

import com.comcast.redirector.core.config.ZKConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * ZKConfig for tests. Be careful changing this values, look where they are used before.
 */
public class TestZKConfig implements ZKConfig {

    private String zooKeeperConnection = "localhost:2181";
    private String zooKeeperBasePath = "";
    private int zooKeeperRetryAttempts = 2;
    private int zooKeeperRetryInterval = 500;       // milliseconds
    private int zooKeeperConnectionTimeout = 500;   // milliseconds
    private int zooKeeperSessionTimeout = 500;      // milliseconds
    private int zooKeeperWaitTimeBeforeReconnectMin = 0; // seconds
    private int zooKeeperWaitTimeBeforeReconnectMax = 0; // seconds
    private String zooKeeperProviderStrategy = "RoundRobin";
    private String serviceName = "xreGuide";
    private String monitoredInboundAttributes;
    private Boolean considerPercents = false;
    private Boolean useZooKeeperWaitTimePolicy = false;

    private Integer fallbackPort = 10004;
    private String fallbackProtocol = "xre";
    private String fallbackUrn = "shell";
    private Integer fallbackIPProtocolVersion = 4;
    private Integer maxWeightOfTheNode = 100;

    public String getZooKeeperConnection() {
        return zooKeeperConnection;
    }

    public void setZooKeeperConnection(String zooKeeperConnection) {
        this.zooKeeperConnection = zooKeeperConnection;
    }

    public String getZooKeeperBasePath() {
        return zooKeeperBasePath;
    }

    public void setZooKeeperBasePath(String zooKeeperBasePath) {
        this.zooKeeperBasePath = zooKeeperBasePath;
    }

    public int getZooKeeperRetryAttempts() {
        return zooKeeperRetryAttempts;
    }

    public void setZooKeeperRetryAttempts(int zooKeeperRetryAttempts) {
        this.zooKeeperRetryAttempts = zooKeeperRetryAttempts;
    }

    public int getZooKeeperRetryInterval() {
        return zooKeeperRetryInterval;
    }

    public void setZooKeeperRetryInterval(int zooKeeperRetryInterval) {
        this.zooKeeperRetryInterval = zooKeeperRetryInterval;
    }

    public String getZooKeeperProviderStrategy() {
        return zooKeeperProviderStrategy;
    }

    public void setZooKeeperProviderStrategy(String zooKeeperProviderStrategy) {
        this.zooKeeperProviderStrategy = zooKeeperProviderStrategy;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getZooKeeperConnectionTimeout() {
        return zooKeeperConnectionTimeout;
    }

    public void setZooKeeperConnectionTimeout(int zooKeeperConnectionTimeout) {
        this.zooKeeperConnectionTimeout = zooKeeperConnectionTimeout;
    }

    public int getZooKeeperSessionTimeout() {
        return zooKeeperSessionTimeout;
    }

    public void setZooKeeperSessionTimeout(int zooKeeperSessionTimeout) {
        this.zooKeeperSessionTimeout = zooKeeperSessionTimeout;
    }

    @Override
    public int getZooKeeperWaitTimeBeforeReconnectMin() {
        return zooKeeperWaitTimeBeforeReconnectMin;
    }

    public void setZooKeeperWaitTimeBeforeReconnectMin(int zooKeeperWaitTimeBeforeReconnectMin) {
        this.zooKeeperWaitTimeBeforeReconnectMin = zooKeeperWaitTimeBeforeReconnectMin;
    }

    @Override
    public int getZooKeeperWaitTimeBeforeReconnectMax() {
        return zooKeeperWaitTimeBeforeReconnectMax;
    }

    public void setZooKeeperWaitTimeBeforeReconnectMax(int zooKeeperWaitTimeBeforeReconnectMax) {
        this.zooKeeperWaitTimeBeforeReconnectMax = zooKeeperWaitTimeBeforeReconnectMax;
    }

    @Override
    public String getMonitoredInboundAttributes() {
        return monitoredInboundAttributes;
    }

    public void setMonitoredInboundAttributes(String monitoredInboundAttributes) {
        this.monitoredInboundAttributes = monitoredInboundAttributes;
    }

    @Override
    public Integer getFallbackPort() {
        return fallbackPort;
    }

    @Override
    public String getFallbackProtocol() {
        return fallbackProtocol;
    }

    @Override
    public String getFallbackUrn() {
        return fallbackUrn;
    }

    @Override
    public Integer getFallbackIPProtocolVersion() {
        return fallbackIPProtocolVersion;
    }

    @Override
    public String getBackupBasePath() {
        return null;
    }

    @Override
    public int getMinHosts() {
        return 0;
    }

    @Override
    public Boolean isConsiderPercents() {
        return considerPercents;
    }

    @Override
    public int getAppsRetrievingIntervalInSeconds() {
        return 0;
    }

    @Override
    public int getDiscoveryUpdatePeriodInSeconds() {
        return 30;
    }

    @Override
    public Set<String> getExcludedAppsFromStackAutoDiscovery() {
        return new HashSet<String>(){{add("xreGuide");}};
    }

    @Override
    public int getDefaultWeightOfTheNode() {
        return 0;
    }

    @Override
    public int getMaxWeightOfTheNode() {
        return maxWeightOfTheNode;
    }

    @Override
    public int getAppMinHosts() {
        return 0;
    }

    @Override
    public boolean isCacheHosts() {
        return false;
    }

    @Override
    public Boolean isWeightFromZookeeperApplied() {
        return false;
    }

    @Override
    public Boolean isEndToEndModeEnabled() {
        return false;
    }

    @Override
    public Boolean useZooKeeperWaitTimePolicy() {
        return useZooKeeperWaitTimePolicy;
    }

    @Override
    public void setUseZooKeeperWaitTimePolicy(Boolean useZooKeeperWaitTimePolicy) {
        this.useZooKeeperWaitTimePolicy = useZooKeeperWaitTimePolicy;
    }

    @Override
    public int getDiscoveryPullInterval() {
        return 0;
    }

    @Override
    public int getDiscoveryPullWorkersPoolSize() {
        return 0;
    }

    @Override
    public String getRestBasePath() {
        return "http://localhost";
    }

    @Override
    public Integer getStacksPollIntervalSeconds() {
        return 2;
    }

    @Override
    public Integer getModelPollIntervalSeconds() {
        return 2;
    }

    @Override
    public Integer getNsListsPollIntervalSeconds() {
        return 2;
    }

    @Override
    public Integer getStacksRefreshPollIntervalSeconds() {
        return 2;
    }

    @Override
    public Integer getPollingClientsMaxNumberOfConnectionsPerRoute() {
        return 2;
    }

    @Override
    public Integer getPollingClientsMaxNumberOfConnectionsTotal() {
        return 10;
    }
    
    public int getPollingRequestTimeoutMs() {
        return 1000;
    }
    
    public int getPollingSocketTimeoutMs() {
        return 1000;
    }
    
    public int getPollingConnectionTimeoutMs() {
        return 1000;
    }
    
    public int getDataRestSocketTimeoutMs() {
        return 10000;
    }
    
    public int getDataRestRequestTimeoutMs() {
        return 10000;
    }
    
    public int getDataRestConnectionTimeoutMs() {
        return 2000;
    }
    
    @Override
    public int getDataRestClientsMaxNumberOfConnectionsPerRoute() {
        return 500;
    }
    
    @Override
    public int getDataRestClientsMaxNumberOfConnectionsTotal() {
        return 1000;
    }

    @Override
    public boolean getRedirectWith302() {
        return false;
    }
}
