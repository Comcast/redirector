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
 */
package com.comcast.xre.common.redirector.selector.xmlimpl;

import com.comcast.redirector.dataaccess.cache.ZKPathHelperConstants;
import com.comcast.redirector.core.config.ZKConfig;
import com.google.common.collect.Sets;

import java.util.Set;

public class Config implements ZKConfig {
	private String _upArrowURL;
	private String _downArrowURL;

    private String zooKeeperConnection = "localhost:2181";
    private String zooKeeperBasePath = "";
    private int zooKeeperRetryAttempts = 3;
    private int zooKeeperRetryInterval = 500;              // milliseconds
    private int zooKeeperConnectionTimeout = 15 * 1000;    // milliseconds
    private int zooKeeperSessionTimeout = 60 * 1000;       // milliseconds
    private int zooKeeperWaitTimeBeforeReconnectMin = 1 * 60; // seconds
    private int zooKeeperWaitTimeBeforeReconnectMax = 10 * 60; // seconds
    private String zooKeeperProviderStrategy = "RoundRobin";
    private String serviceName = ZKPathHelperConstants.XRE_GUIDE;
    private int appsRetrievingIntervalInSeconds = 300;

    // statistics
    private String monitoredInboundAttributes = "";
    private Integer fallbackPort = 10004;
    private String fallbackProtocol = "xre";
    private String fallbackUrn = "shell";
    private Integer fallbackIPProtocolVersion = 4;
    private String backupBasePath;
    private int minHosts = 1;
    private Boolean considerPercents = false;
    private int appMinHosts = 1;
    private boolean cacheHosts = false;

    //discovery
    private int discoveryUpdatePeriod = 30; //seconds
    private int discoveryPullInterval = 5000;
    private int discoveryPullWorkersPoolSize = 20;
    private String excludedAppsForStackAutoDiscovery = "xreGuide";
    private int defaultWeightOfTheNode = 5;
    private int maxWeightOfTheNode = 100;
    private Boolean weightFromZookeeperApplied = true;

    // communication endpoint (HTTP, XMPP)
    private Boolean enableCommunicationEndpoint = true;
    private Integer communicationEndpointPort = 10601;

    private Boolean endToEndMode = false;

    private Boolean useZooKeeperWaitTimePolicy = false;
    private String metricsConfigUrl = "http://smartservice.do.xcal.tv:8080/smartConfigurationService";

    public void setUpArrowURL(String upArrowURL) {
		this._upArrowURL = upArrowURL;
	}
	public String getUpArrowURL() {
		return _upArrowURL;
	}
	public void setDownArrowURL(String downArrowURL) {
		this._downArrowURL = downArrowURL;
	}
	public String getDownArrowURL() {
		return _downArrowURL;
	}
    private String restBasePath = "http://localhost:10540/redirectorWebService/data";
    private Integer stacksPollIntervalSeconds = 60;
    private Integer modelPollIntervalSeconds = 60;
    private Integer stacksRefreshPollIntervalSeconds = 60;
    private Integer nsListsPollIntervalSeconds = 60;
    private Integer pollingClientsMaxNumberOfConnectionsPerRoute = 2;
    private Integer pollingClientsMaxNumberOfConnectionsTotal = 10;

    private Integer pollingRequestTimeoutMs = 1000;
    private Integer pollingSocketTimeoutMs = 1000;
    private Integer pollingConnectionTimeoutMs = 1000;
    
    private Integer dataRestSocketTimeoutMs = 10000;
    private Integer dataRestRequestTimeoutMs = 10000;
    private Integer dataRestConnectionTimeoutMs = 5000;
    
    private Integer dataRestClientsMaxNumberOfConnectionsPerRoute = 500;
    private Integer dataRestClientsMaxNumberOfConnectionsTotal = 1000;

    private boolean redirectWith302 = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getZooKeeperConnection() {
        return zooKeeperConnection;
    }

    public void setZooKeeperConnection(String zooKeeperConnection) {
        this.zooKeeperConnection = zooKeeperConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getZooKeeperBasePath() {
        return zooKeeperBasePath;
    }

    public void setZooKeeperBasePath(String zooKeeperBasePath) {
        this.zooKeeperBasePath = zooKeeperBasePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getZooKeeperRetryAttempts() {
        return zooKeeperRetryAttempts;
    }

    public void setZooKeeperRetryAttempts(int zooKeeperRetryAttempts) {
        this.zooKeeperRetryAttempts = zooKeeperRetryAttempts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getZooKeeperRetryInterval() {
        return zooKeeperRetryInterval;
    }

    public void setZooKeeperRetryInterval(int zooKeeperRetryInterval) {
        this.zooKeeperRetryInterval = zooKeeperRetryInterval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getZooKeeperProviderStrategy() {
        return zooKeeperProviderStrategy;
    }

    public void setZooKeeperProviderStrategy(String zooKeeperProviderStrategy) {
        this.zooKeeperProviderStrategy = zooKeeperProviderStrategy;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public int getZooKeeperConnectionTimeout() {
        return zooKeeperConnectionTimeout;
    }

    public void setZooKeeperConnectionTimeout(int zooKeeperConnectionTimeout) {
        this.zooKeeperConnectionTimeout = zooKeeperConnectionTimeout;
    }

    @Override
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

    public void setFallbackIPProtocolVersion(Integer fallbackIPProtocolVersion) {
        this.fallbackIPProtocolVersion = fallbackIPProtocolVersion;
    }

    public void setFallbackPort(Integer fallbackPort) {
        this.fallbackPort = fallbackPort;
    }

    public void setFallbackProtocol(String fallbackProtocol) {
        this.fallbackProtocol = fallbackProtocol;
    }

    public void setFallbackUrn(String fallbackUrn) {
        this.fallbackUrn = fallbackUrn;
    }

    @Override
    public String getBackupBasePath() {
        return backupBasePath;
    }

    public void setBackupBasePath(String backupBasePath) {
        this.backupBasePath = backupBasePath;
    }

    @Override
    public int getMinHosts() {
        return minHosts;
    }

    public void setMinHosts(int minHosts) {
        this.minHosts = minHosts;
    }

    @Override
    public int getAppMinHosts() {
        return appMinHosts;
    }

    public void setAppMinHosts(int appMinHosts) {
        this.appMinHosts = appMinHosts;
    }

    @Override
    public Boolean isConsiderPercents() {
        return considerPercents;
    }

    public void setConsiderPercents(Boolean considerPercents) {
        this.considerPercents = considerPercents;
    }

    @Override
    public int getAppsRetrievingIntervalInSeconds() {
        return appsRetrievingIntervalInSeconds;
    }

    public void setAppsRetrievingIntervalInSeconds(int appsRetrievingIntervalInSeconds) {
        this.appsRetrievingIntervalInSeconds = appsRetrievingIntervalInSeconds;
    }

    public void setDiscoveryUpdatePeriod(int discoveryUpdatePeriod) {
        this.discoveryUpdatePeriod = discoveryUpdatePeriod;
    }

    @Override
    public int getDiscoveryUpdatePeriodInSeconds() {
        return discoveryUpdatePeriod;
    }

    public void setExcludedAppsForStackAutoDiscovery(String excludedAppsForAutoDiscovery) {
        this.excludedAppsForStackAutoDiscovery = excludedAppsForAutoDiscovery;
    }

    @Override
    public Set<String> getExcludedAppsFromStackAutoDiscovery() {
        return Sets.newHashSet(excludedAppsForStackAutoDiscovery.split(","));
    }


    @Override
    public boolean isCacheHosts() {
        return cacheHosts;
    }

    public void setCacheHosts(boolean cacheHosts) {
        this.cacheHosts = cacheHosts;
    }

    @Override
    public int getDefaultWeightOfTheNode() {
        return defaultWeightOfTheNode;
    }

    public void setDefaultWeightOfTheNode(int defaultWeightOfTheNode) {
        this.defaultWeightOfTheNode = defaultWeightOfTheNode;
    }

    @Override
    public int getMaxWeightOfTheNode() {
        return this.maxWeightOfTheNode;
    }

    @Override
    public Boolean isWeightFromZookeeperApplied() {
        return weightFromZookeeperApplied;
    }

    public void setWeightFromZookeeperApplied(Boolean weightFromZookeeperApplied) {
        this.weightFromZookeeperApplied = weightFromZookeeperApplied;
    }

    public Boolean getEnableCommunicationEndpoint() {
        return enableCommunicationEndpoint;
    }

    public void setEnableCommunicationEndpoint(Boolean enableCommunicationEndpoint) {
        this.enableCommunicationEndpoint = enableCommunicationEndpoint;
    }

    public Integer getCommunicationEndpointPort() {
        return communicationEndpointPort;
    }

    public void setCommunicationEndpointPort(Integer communicationEndpointPort) {
        this.communicationEndpointPort = communicationEndpointPort;
    }

    @Override
    public Boolean isEndToEndModeEnabled() {
        return endToEndMode;
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
        return discoveryPullInterval;
    }

    @Override
    public int getDiscoveryPullWorkersPoolSize() {
        return discoveryPullWorkersPoolSize;
    }

    public void setDiscoveryPullInterval(int discoveryPullInterval) {
        this.discoveryPullInterval = discoveryPullInterval;
    }

    public void setDiscoveryPullWorkersPoolSize(int discoveryPullWorkersPoolSize) {
        this.discoveryPullWorkersPoolSize = discoveryPullWorkersPoolSize;
    }

    @Override
    public String getRestBasePath() {
        return this.restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
    }

    @Override
    public Integer getStacksPollIntervalSeconds() {
        return stacksPollIntervalSeconds;
    }

    public void setStacksPollIntervalSeconds(Integer stacksPollIntervalSeconds) {
        this.stacksPollIntervalSeconds = stacksPollIntervalSeconds;
    }

    @Override
    public Integer getModelPollIntervalSeconds() {
        return modelPollIntervalSeconds;
    }

    @Override
    public Integer getNsListsPollIntervalSeconds() {
        return nsListsPollIntervalSeconds;
    }

    public void setModelPollIntervalSeconds(Integer modelPollIntervalSeconds) {
        this.modelPollIntervalSeconds = modelPollIntervalSeconds;
    }

    public void setNsListsPollIntervalSeconds(Integer nsListsPollIntervalSeconds) {
        this.nsListsPollIntervalSeconds = nsListsPollIntervalSeconds;
    }

    @Override
    public Integer getStacksRefreshPollIntervalSeconds() {
        return stacksRefreshPollIntervalSeconds;
    }

    public void setStacksRefreshPollIntervalSeconds(Integer stacksRefreshPollIntervalSeconds) {
        this.stacksRefreshPollIntervalSeconds = stacksRefreshPollIntervalSeconds;
    }

    @Override
    public Integer getPollingClientsMaxNumberOfConnectionsPerRoute() {
        return pollingClientsMaxNumberOfConnectionsPerRoute;
    }

    public void setPollingClientsMaxNumberOfConnectionsPerRoute(Integer pollingClientsMaxNumberOfConnectionsPerRoute) {
        this.pollingClientsMaxNumberOfConnectionsPerRoute = pollingClientsMaxNumberOfConnectionsPerRoute;
    }

    @Override
    public Integer getPollingClientsMaxNumberOfConnectionsTotal() {
        return pollingClientsMaxNumberOfConnectionsTotal;
    }

    public void setPollingClientsMaxNumberOfConnectionsTotal(Integer pollingClientsMaxNumberOfConnectionsTotal) {
        this.pollingClientsMaxNumberOfConnectionsTotal = pollingClientsMaxNumberOfConnectionsTotal;
    }
    
    public int getPollingRequestTimeoutMs() {
        return this.pollingRequestTimeoutMs;
    }
    
    public void setPollingRequestTimeoutMs(int pollingRequestTimeoutMs) {
        this.pollingRequestTimeoutMs = pollingRequestTimeoutMs;
    }
    
    public int getPollingSocketTimeoutMs() {
        return this.pollingSocketTimeoutMs;
    }
    
    public void setPollingSocketTimeoutMs(int pollingSocketTimeoutMs) {
        this.pollingSocketTimeoutMs = pollingSocketTimeoutMs;
    }
    
    public int getPollingConnectionTimeoutMs() {
        return this.pollingConnectionTimeoutMs;
    }
    
    public void setPollingConnectionTimeoutMs(int pollingConnectionTimeoutMs) {
        this.pollingConnectionTimeoutMs = pollingConnectionTimeoutMs;
    }
    
    public int getDataRestSocketTimeoutMs() {
        return this.dataRestSocketTimeoutMs;
    }

    public void setDataRestSocketTimeoutMs(int dataRestSocketTimeoutMs) {
        this.dataRestSocketTimeoutMs = dataRestSocketTimeoutMs;
    }
    
    public int getDataRestRequestTimeoutMs() {
        return this.dataRestRequestTimeoutMs;
    }
    
    public void setDataRestRequestTimeoutMs(int dataRestRequestTimeoutMs) {
        this.dataRestRequestTimeoutMs = dataRestRequestTimeoutMs;
    }
    
    public int getDataRestConnectionTimeoutMs() {
        return this.dataRestConnectionTimeoutMs;
    }
    
    public void setDataRestConnectionTimeoutMs(int dataRestConnectionTimeoutMs) {
        this.dataRestConnectionTimeoutMs = dataRestConnectionTimeoutMs;
    }
    
    @Override
    public int getDataRestClientsMaxNumberOfConnectionsPerRoute() {
        return this.dataRestClientsMaxNumberOfConnectionsPerRoute;
    }
    
    public void setDataRestClientsMaxNumberOfConnectionsPerRoute(Integer dataRestClientsMaxNumberOfConnectionsPerRoute) {
        this.dataRestClientsMaxNumberOfConnectionsPerRoute = dataRestClientsMaxNumberOfConnectionsPerRoute;
    }
    
    @Override
    public int getDataRestClientsMaxNumberOfConnectionsTotal() {
        return this.dataRestClientsMaxNumberOfConnectionsTotal;
    }
    
    public void setDataRestClientsMaxNumberOfConnectionsTotal(Integer dataRestClientsMaxNumberOfConnectionsTotal) {
        this.dataRestClientsMaxNumberOfConnectionsTotal = dataRestClientsMaxNumberOfConnectionsTotal;
    }

    public String getMetricsConfigUrl() {
        return this.metricsConfigUrl;
    }

    public void setMetricsConfigUrl(String metricsConfigUrl) {
        this.metricsConfigUrl = metricsConfigUrl;
    }

    @Override
    public boolean getRedirectWith302() {
        return redirectWith302;
    }

    public void setRedirectWith302(boolean redirectWith302) {
        this.redirectWith302 = redirectWith302;
    }
}
