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

package com.comcast.redirector.dataaccess.cache.newzkstackscache;

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class StreamDiscoveryCycle implements IDiscoveryCycle {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(StreamDiscoveryCycle.class);

    private Set<XreStackPath> allStackPaths;
    private IDataSourceConnector connector;
    private String stackPathPrefix;
    private int version;

    private ForkJoinPool forkJoinPool;

    public StreamDiscoveryCycle(IDataSourceConnector connector, ForkJoinPool forkJoinPool, int version) {
        this.connector = connector;
        this.stackPathPrefix = PathHelper.getPathHelper(EntityType.STACK, connector.getBasePath()).getPath();
        this.forkJoinPool = forkJoinPool;
        this.version = version;
    }

    @Override
    public void execute() throws Exception {
        if (!connector.isConnected()) {
            log.error("Failed to update hosts for version={} due to absent connection to data source", version);
            throw new DataSourceConnectorException();
        }

        long start = System.currentTimeMillis();
        forkJoinPool.submit(() -> {
            return doExecute();
        }).get();

        log.info("discoveryTime={}ms", System.currentTimeMillis() - start);
    }

    private Collection<XreStackPath> doExecute() throws DataSourceConnectorException {
        /**
         * TODO investigate what caches we will get in case when Zookeeper becomes unavailable during this method execution
         * shouldn't we catch NoNodeInZookeeperException instead of DataSourceConnectorException?
         */
        allStackPaths = connector.getChildren(stackPathPrefix).stream().parallel()
                .flatMap(dc -> {
                    try {
                        return connector.getChildren(stackPathPrefix + "/" + dc).stream()
                                .map(item -> stackPathPrefix + "/" + dc + "/" + item);
                    } catch (DataSourceConnectorException e) {
                        log.error("failed", e);
                        return null;
                    }
                })
                .flatMap(stackPath -> {
                    try {
                        return connector.getChildren(stackPath).stream()
                                .map((stackPath + "/")::concat);
                    } catch (DataSourceConnectorException e) {
                        log.error("failed", e);
                        return null;
                    }})
                .flatMap(versionPath -> {
                    try {
                        return connector.getChildren(versionPath).stream()
                                .map((versionPath + "/")::concat)
                                .map(xreStackPath -> xreStackPath.replaceFirst(stackPathPrefix, ""));
                    } catch (DataSourceConnectorException e) {
                        log.error("failed", e);
                        return null;
                    }})
                .map(XreStackPath::new)
                .collect(Collectors.toSet());
        return allStackPaths;
    }

    @Override
    public Set<XreStackPath> getAllStacks() {
        return allStackPaths;
    }
}
