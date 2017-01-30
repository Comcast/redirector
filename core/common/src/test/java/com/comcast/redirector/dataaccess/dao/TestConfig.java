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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.common.config.Config;

public class TestConfig implements Config {
    private int sleepsBetweenRetryMs;
    private int connectionTimeoutMs;
    private String connectionUrl;
    private String zookeeperBasePath;
    private int retryCount;
    private int zooKeeperWaitTimeBeforeReconnect;

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

    public void setSleepsBetweenRetryMs(int sleepsBetweenRetryMs) {
        this.sleepsBetweenRetryMs = sleepsBetweenRetryMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public void setZookeeperBasePath(String zookeeperBasePath) {
        this.zookeeperBasePath = zookeeperBasePath;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public boolean isCacheHosts() {
        return false;
    }

    @Override
    public int getZooKeeperWaitTimeBeforeReconnect() {
        return zooKeeperWaitTimeBeforeReconnect;
    }
}
