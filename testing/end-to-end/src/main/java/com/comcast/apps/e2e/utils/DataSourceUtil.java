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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.utils;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.redirector.common.config.Config;
import com.comcast.redirector.common.config.SimpleConfig;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.ZookeeperConnector;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityCategory;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.PathHelper;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataSourceUtil {
    private static final Logger log = LoggerFactory.getLogger(DataSourceUtil.class);

    private static final String ZOOKEEPER_PATH = "zookeeper";
    private static final int CONNECTION_TIMEOUT_MS = 10000;
    private static final int RETRY_COUNT = 10;
    private static final int SLEEPS_BETWEEN_RETRY_MS = 500;
    private static final int WAIT_TIME_BEFORE_RECONNECT = 0;

    private static IDataSourceConnector connector;

    public static void init() {
        String zookeeperPath = E2EConfigLoader.getDefaultInstance().getZooKeeperBasePath();
        String zookeeperConnectionString = E2EConfigLoader.getDefaultInstance().getZooKeeperConnection();

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(SLEEPS_BETWEEN_RETRY_MS, RETRY_COUNT);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);

        Config config = new SimpleConfig(CONNECTION_TIMEOUT_MS, zookeeperConnectionString, zookeeperPath, RETRY_COUNT, SLEEPS_BETWEEN_RETRY_MS, false, 5000, WAIT_TIME_BEFORE_RECONNECT);

        connector = new ZookeeperConnector(curatorFramework, config.getZookeeperBasePath(), config.isCacheHosts());
        connector.connect();
    }

    public static void cleanUpDataSource() throws DataSourceConnectorException {
        String basePath = connector.getBasePath();
        log.info("Cleanup for basePath: {} has been started", basePath);
        List<String> children = connector.getChildren(basePath);
        for (String child : children) {
            if (!child.equals(ZOOKEEPER_PATH)) {
                connector.deleteWithChildren(basePath + RedirectorConstants.DELIMETER + child);
            }
        }
        log.info("Cleanup for basePath: {} has been finished", basePath);
    }

    public static void createRulesAndUrlRulesNode(String appName) {
        IPathHelper helper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, E2EConfigLoader.getDefaultInstance().getZooKeeperBasePath());
        String pathRules = helper.getPathByService(appName, EntityType.RULE);
        String pathUrlRules = helper.getPathByService(appName, EntityType.URL_RULE);
        try {
            connector.create(pathRules);
            connector.create(pathUrlRules);
        } catch (DataSourceConnectorException e) {
            log.error("Couldn't create rules node");
        }
    }

    public static IDataSourceConnector getConnector() {
        return connector;
    }
}
