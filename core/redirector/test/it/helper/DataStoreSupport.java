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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package it.helper;

import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.ZookeeperConnector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.GzipCompressionProvider;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DataStoreSupport {
    private static final Logger log = LoggerFactory.getLogger(DataStoreSupport.class);

    private EmbeddedZookeeper zookeeperServer = new EmbeddedZookeeper();
    private IDataSourceConnector connector;

    private ZKConfig config;

    public DataStoreSupport(ZKConfig config) {
        this.config = config;
    }

    public void startZookeeper() throws Exception {
        try {
            log.info("Starting embedded zookeeper on port " + getDataStoreConnectionPort());
            zookeeperServer.start(getDataStoreConnectionPort(), 100, 0);
            TimeUnit.MILLISECONDS.sleep(400);
        } catch (Exception e) {
            log.error("Failed to start zookeeper on port " + getDataStoreConnectionPort(), e);
        }
    }

    public void stopZookeeper() throws IOException {
        log.info("Stopping embedded zookeeper on port " + getDataStoreConnectionPort());
        zookeeperServer.stop();
    }

    public void shutdown() {
        try {
            stopConnector();
            stopZookeeper();
        } catch (Exception e) {
            log.error("Failed to cleanup and stop zookeeper", e);
        }
    }

    private void startConnector() {
        if (connector == null || !connector.isConnected()) {
            log.info("Starting helper curator");
            connector = createConnector(config);
            connector.connect();
        }
    }

    private IDataSourceConnector createConnector(ZKConfig config) {
        CuratorFramework curator = createCurator(config);
        return new ZookeeperConnector(curator, config.getZooKeeperBasePath(), config.isCacheHosts());
    }

    private CuratorFramework createCurator(ZKConfig config) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
            .connectString(config.getZooKeeperConnection())
            .connectionTimeoutMs(config.getZooKeeperConnectionTimeout())
            .sessionTimeoutMs(config.getZooKeeperSessionTimeout())
            .retryPolicy(new RetryNTimes(config.getZooKeeperRetryAttempts(), config.getZooKeeperRetryInterval()));

        builder.compressionProvider(new GzipCompressionProvider());
        return builder.build();
    }

    /**
     * This method gets connector, ensuring that it is actually started.
     * So, this is a preferred one to get connectors that should actually do their job.
     * @return
     */
    public IDataSourceConnector getStartedConnector() {
        startConnector();
        return connector;
    }

    /**
     * This method is a standard getter.
     * It is intended to be used when one needs to perform checks on connector (e.g. null-check or isConnected check)
     * It DOES NOT GUARANTEE that connector will be actually connected.
     * @return
     */
    public IDataSourceConnector getConnector() {
        return connector;
    }

    public Boolean isConnectorConnected () {
        return connector != null && connector.isConnected();
    }

    private void stopConnector() {
        if (connector != null && connector.isConnected()) {
            log.info("Stopping helper curator");
            connector.disconnect();
        } else {
            log.warn("Helper curator is not started");
        }
    }

    private int getDataStoreConnectionPort() {
        String connectionString = config.getZooKeeperConnection();
        return Integer.parseInt(connectionString.split(":")[1]);
    }

    private static class EmbeddedZookeeper {
        private TestingServer zookeeperServer;

        public void start(int port, int tickTime, int maxConnections) throws Exception {
            InstanceSpec spec = new InstanceSpec(null, port, -1, -1, true, -1, tickTime, maxConnections);
            zookeeperServer = new TestingServer(spec, false);
            zookeeperServer.start();
        }

        void stop() throws IOException {
            if (zookeeperServer != null) {
                zookeeperServer.close();
            }
        }
    }
}
