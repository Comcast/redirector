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
package com.comcast.redirector.api.config;

import com.comcast.redirector.common.config.Config;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.stereotype.Component;

@Component
public class RedirectorConfig implements Config {
    private int connectionTimeoutMs = 10000;
    private String connectionUrl = "127.0.0.1:2181";
    private int retryCount = 10;
    private int sleepsBetweenRetryMs = 500;
    private int zooKeeperWaitTimeBeforeReconnect = 5 * 60 * 1000;
    private String zookeeperBasePath = "";
    private boolean cacheHosts = true;
    private String snapshotBasePath = "";
    private String metricsConfigUrl = "";
    private long stacksSnapshotRateInSeconds = 60;

    public RedirectorConfig() {}

    @ManagedAttribute
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    @ManagedAttribute
    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    @ManagedAttribute
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @ManagedAttribute
    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    @ManagedAttribute
    public int getRetryCount() {
        return retryCount;
    }

    @ManagedAttribute
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @ManagedAttribute
    public int getSleepsBetweenRetryMs() {
        return sleepsBetweenRetryMs;
    }

    @ManagedAttribute
    public void setSleepsBetweenRetryMs(int sleepsBetweenRetryMs) {
        this.sleepsBetweenRetryMs = sleepsBetweenRetryMs;
    }

    @ManagedAttribute
    public String getZookeeperBasePath() {
        return zookeeperBasePath;
    }

    @ManagedAttribute
    public void setZookeeperBasePath(String zookeeperBasePath) {
        this.zookeeperBasePath = zookeeperBasePath;
    }

    @ManagedAttribute
    public boolean isCacheHosts() {
        return cacheHosts;
    }

    @ManagedAttribute
    public void setCacheHosts(boolean cacheHosts) {
        this.cacheHosts = cacheHosts;
    }

    @ManagedAttribute
    public String getSnapshotBasePath() {
        return snapshotBasePath;
    }

    @ManagedAttribute
    public void setSnapshotBasePath(String snapshotBasePath) {
        this.snapshotBasePath = snapshotBasePath;
    }

    @ManagedAttribute
    public void setZooKeeperWaitTimeBeforeReconnect(int zooKeeperWaitTimeBeforeReconnect) {
        this.zooKeeperWaitTimeBeforeReconnect = zooKeeperWaitTimeBeforeReconnect;
    }

    @ManagedAttribute
    public int getZooKeeperWaitTimeBeforeReconnect() {
        return this.zooKeeperWaitTimeBeforeReconnect;
    }

    @ManagedAttribute
    public String getMetricsConfigUrl() {
        return metricsConfigUrl;
    }

    @ManagedAttribute
    public void setMetricsConfigUrl(String metricsConfigUrl) {
        this.metricsConfigUrl = metricsConfigUrl;
    }

    @ManagedAttribute
    public long getStacksSnapshotRateInSeconds() {
        return stacksSnapshotRateInSeconds;
    }

    @ManagedAttribute
    public void setStacksSnapshotRateInSeconds(long stacksSnapshotRateInSeconds) {
        this.stacksSnapshotRateInSeconds = stacksSnapshotRateInSeconds;
    }
}
