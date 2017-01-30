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

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.common.config.Config;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.ZookeeperConnector;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityCategory;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.dataaccess.dao.TestConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

public class RedirectorWSIntegrationTestsHelper {

    private static final String PROPERTIES_FILE_NAME = "service.properties";
    private Serializer serializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    private IDataSourceConnector connector;
    private Config config;
    private WebServiceTestContext context;

    public RedirectorWSIntegrationTestsHelper(CuratorFramework curatorFramework, Config config, WebServiceTestContext context) {
        this.connector = new ZookeeperConnector(curatorFramework, config.getZookeeperBasePath(), config.isCacheHosts());
        this.config = config;
        this.context = context;
    }

    public RedirectorWSIntegrationTestsHelper() {

        // load test properties of data source (zookeeper)
        Properties properties = loadProperties();
        String connectionUrl = properties.getProperty("redirector.connectionUrl");
        String zookeeperBasePath = properties.getProperty("redirector.zookeeperBasePath");

        Config config = getConfig(properties);

        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                config.getConnectionUrl(), new RetryNTimes(config.getRetryCount(), config.getSleepsBetweenRetryMs()));

        this.connector = new ZookeeperConnector(curatorFramework, config.getZookeeperBasePath(), config.isCacheHosts());
        this.config = config;
    }

    public void startDataSourceClient() {
        if (! connector.isConnected()) {
            connector.connect();
        }
    }

    public void stopDataSourceClient() {
        if (connector.isConnected()) {
            connector.disconnect();
        }
    }

    public void cleanupContextFromDataSource() throws DataSourceConnectorException {
        cleanupData();
    }

    public void cleanupContextFromDataSource(WebServiceTestContext context) throws DataSourceConnectorException {
        cleanupData(context);
    }

    private void cleanupData() throws DataSourceConnectorException {
        cleanupContextFromDataSource(context);
    }

    private void cleanupData(WebServiceTestContext context) throws DataSourceConnectorException {
        if (connector.isConnected()) {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZookeeperBasePath());
            String path = pathHelper.getPathByService(context.getServiceName());
            if (connector.isPathExists(path)) {
                connector.deleteWithChildren(path);
            }
        }
    }

    public void setUpContext(WebServiceTestContext context) throws SerializerException, DataSourceConnectorException {
        startDataSourceClient();

        setUpServicePaths(context);
        setUpWhitelisted(context);
        setUpDistribution(context);
        setUpDefaultUrlParams(context);
    }

    private void setUpServicePaths(WebServiceTestContext context) {
        if (context.getServicePaths() != null) {
            try {
                IPathHelper pathHelper = PathHelper.getPathHelper(EntityType.STACK, connector.getBasePath());
                String baseStacksPath = pathHelper.getPath();

                for (Paths paths : context.getServicePaths().getPaths()) {
                    String serviceName = paths.getServiceName();

                    for (PathItem stacks : paths.getStacks()) {
                        String stacksPath = stacks.getValue();
                        int activeNodesCount = stacks.getActiveNodesCount();
                        String fullStacksPath = baseStacksPath + stacksPath + DELIMETER + serviceName;
                        connector.save("", fullStacksPath);

                        for (int i = 0; i < activeNodesCount; i++) {
                            String hostNode = "Host" + i;
                            String zkNodeData = "{\"payload\" : {\"parameters\" : {\"ipv4Address\":\"testActiveNode" + i + "-ccpapp-po-c534-p.po.ccp.cable.comcast.com\", \"ipv6Address\":\"testActiveNode" + i + "-fe80:0:0:0:200:f8ff:fe21:67cf\"}}}";
                            connector.save(zkNodeData, fullStacksPath + DELIMETER + hostNode);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }

    private void setUpWhitelisted(WebServiceTestContext context) throws SerializerException, DataSourceConnectorException {

        Whitelisted whitelisted = context.getWhitelisted();

        if (whitelisted != null) {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZookeeperBasePath());
            String path = pathHelper.getPathByService(context.getServiceName(), EntityType.WHITELIST);
            connector.save(serializer.serialize(whitelisted), path);
        }
    }

    public void cleanUpWhitelisted(WebServiceTestContext context) {

        Whitelisted whitelisted = new Whitelisted();

        IPathHelper pathHelper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZookeeperBasePath());
        String path = pathHelper.getPathByService(context.getServiceName(), EntityType.WHITELIST);
        try {
            connector.save(serializer.serialize(whitelisted), path);
        } catch (DataSourceConnectorException e) {
            e.printStackTrace();
        } catch (SerializerException e) {
            e.printStackTrace();
        }
    }

    private void setUpDistribution(WebServiceTestContext context) throws SerializerException, DataSourceConnectorException {
        Distribution distribution = context.getDistribution();

        if (distribution != null) {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZookeeperBasePath());
            String path = pathHelper.getPathByService(context.getServiceName(), EntityType.DISTRIBUTION);
            connector.save(serializer.serialize(distribution), path);

            String defaultServerPath = pathHelper.getPathByService(context.getServiceName(), EntityType.SERVER, RedirectorConstants.DEFAULT_SERVER_NAME);
            connector.save(serializer.serialize(distribution.getDefaultServer()), defaultServerPath);
        }
    }

    private void setUpDefaultUrlParams(WebServiceTestContext context) throws SerializerException, DataSourceConnectorException {
        UrlRule defaultUrlParams = context.getDefaultUrlParams();

        if (defaultUrlParams != null) {
            IPathHelper pathHelper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZookeeperBasePath());
            String path = pathHelper.getPathByService(context.getServiceName(), EntityType.URL_PARAMS);
            connector.save(serializer.serialize(defaultUrlParams), path);
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        InputStream input = RedirectorWSIntegrationTestsHelper.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private Config getConfig(Properties properties) {
        TestConfig config = new TestConfig();

        config.setConnectionUrl(properties.getProperty("redirector.connectionUrl", "localhost:2182"));
        config.setZookeeperBasePath(properties.getProperty("redirector.zookeeperBasePath", "/testcases"));
        config.setRetryCount(Integer.parseInt(properties.getProperty("redirector.retryCount", "10")));
        config.setConnectionTimeoutMs(Integer.parseInt(properties.getProperty("redirector.connectionTimeoutMs", "10000")));
        config.setSleepsBetweenRetryMs(Integer.parseInt(properties.getProperty("redirector.sleepsBetweenRetryMs", "500")));

        return config;
    }
}
