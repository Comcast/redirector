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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.core.config;

import java.util.Set;

/**
 * Configuration of ZooKeeper Curator and Curator-X-Discovery frameworks.
 * See http://curator.apache.org/index.html, http://curator.apache.org/curator-x-discovery
 */
public interface ZKConfig {

    /**
     * @return comma separated host:port pairs, e.g. "localhost:3000,127.0.0.1:3001,127.0.0.1:3002"
     */
    String getZooKeeperConnection();

    /**
     * @return ZooKeeper base path
     */
    String getZooKeeperBasePath();

    /**
     * @return ZooKeeper maximum retry attempts
     */
    int getZooKeeperRetryAttempts();

    /**
     * @return ZooKeeper retry interval in milliseconds
     */
    int getZooKeeperRetryInterval();

    int getZooKeeperConnectionTimeout();   // milliseconds

    int getZooKeeperSessionTimeout();      // milliseconds

    int getZooKeeperWaitTimeBeforeReconnectMin(); // seconds

    int getZooKeeperWaitTimeBeforeReconnectMax(); // seconds

    boolean isCacheHosts();

    /**
     * Provider strategy can be considered as balancing algorithm
     * @see org.apache.curator.x.discovery.ProviderStrategy
     *
     * @return "Random"     for {@link org.apache.curator.x.discovery.strategies.RandomStrategy}
     *         "RoundRobin" for {@link org.apache.curator.x.discovery.strategies.RoundRobinStrategy}
     */
    String getZooKeeperProviderStrategy();

    String getServiceName();

    /**
     * @return comma-separated list of inbound attributes which statistics will be counted for
     */
    String getMonitoredInboundAttributes();

    /**
     * @return fallback port that XRE Redirector will use in redirect URL
     */
    Integer getFallbackPort();

    /**
     * @return fallback protocol that XRE Redirector will use in redirect URL
     */
    String getFallbackProtocol();

    /**
     * @return fallback urn that XRE Redirector will use in redirect URL
     */
    String getFallbackUrn();

    /**
     * @return fallback ipProtocolVersion that XRE Redirector will use in redirect URL
     */
    Integer getFallbackIPProtocolVersion();

    String getBackupBasePath();

    int getMinHosts();

    int getAppMinHosts();

    Boolean isConsiderPercents();

    int getAppsRetrievingIntervalInSeconds();

    /**
     * @return period of seconds for {@link: ZookeeperServiceDiscovery}
     */
    int getDiscoveryUpdatePeriodInSeconds();

    /**
     * @return list of excluded apps. Excluded apps use StaticStackDiscovery
     */
    Set<String> getExcludedAppsFromStackAutoDiscovery();

    int getDefaultWeightOfTheNode();

    int getMaxWeightOfTheNode();

    Boolean isWeightFromZookeeperApplied();

    Boolean isEndToEndModeEnabled();

    Boolean useZooKeeperWaitTimePolicy();

    void setUseZooKeeperWaitTimePolicy(Boolean useZooKeeperWaitTimePolicy);

    int getDiscoveryPullInterval();

    int getDiscoveryPullWorkersPoolSize();

    String getRestBasePath();

    Integer getStacksPollIntervalSeconds();

    Integer getModelPollIntervalSeconds();

    Integer getNsListsPollIntervalSeconds();

    Integer getStacksRefreshPollIntervalSeconds();

    Integer getPollingClientsMaxNumberOfConnectionsPerRoute();

    Integer getPollingClientsMaxNumberOfConnectionsTotal();
    
    int getPollingRequestTimeoutMs();
    
    int getPollingSocketTimeoutMs();
    
    int getPollingConnectionTimeoutMs();
    
    int getDataRestSocketTimeoutMs();
    
    int getDataRestRequestTimeoutMs();
    
    int getDataRestConnectionTimeoutMs();
    
    int getDataRestClientsMaxNumberOfConnectionsPerRoute();
    
    int getDataRestClientsMaxNumberOfConnectionsTotal();

    boolean getRedirectWith302();
}
