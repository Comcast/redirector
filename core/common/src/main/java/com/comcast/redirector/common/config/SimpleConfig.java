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

package com.comcast.redirector.common.config;

public class SimpleConfig implements Config {
    private int connectionTimeoutMs;
    private String connectionUrl;
    private String zookeeperBasePath;
    private int retryCount;
    private int sleepsBetweenRetryMs;
    private boolean cacheHosts;
    private int discoveryPullInterval;
    private int discoveryPullWorkersPoolSize;
    private int zooKeeperWaitTimeBeforeReconnect;

    private String connectionType;
    private String backupConnectionUrl;
    private String backupConnectionType;

    public SimpleConfig(int connectionTimeoutMs, String connectionUrl, String zookeeperBasePath, int retryCount, int sleepsBetweenRetryMs, boolean cacheHosts,
                        int discoveryPullInterval,
                        int zooKeeperWaitTimeBeforeReconnect) {
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.connectionUrl = connectionUrl;
        this.zookeeperBasePath = zookeeperBasePath;
        this.retryCount = retryCount;
        this.sleepsBetweenRetryMs = sleepsBetweenRetryMs;
        this.cacheHosts = cacheHosts;
        this.discoveryPullInterval = discoveryPullInterval;
        this.zooKeeperWaitTimeBeforeReconnect = zooKeeperWaitTimeBeforeReconnect;
    }

    @Override
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String getZookeeperBasePath() {
        return zookeeperBasePath;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public int getSleepsBetweenRetryMs() {
        return sleepsBetweenRetryMs;
    }

    @Override
    public boolean isCacheHosts() {
        return cacheHosts;
    }

    @Override
    public int getZooKeeperWaitTimeBeforeReconnect() {
        return zooKeeperWaitTimeBeforeReconnect;
    }

}
