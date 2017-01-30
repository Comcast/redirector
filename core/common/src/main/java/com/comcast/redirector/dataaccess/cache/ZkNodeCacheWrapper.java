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

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.common.RedirectorConstants;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ZkNodeCacheWrapper extends BaseCacheWrapper implements INodeCacheWrapper {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(ZkNodeCacheWrapper.class);

    private NodeCache cache;
    private String path;
    boolean dataIsCompressed = false;

    ZkNodeCacheWrapper(IDataSourceConnector connector, String path, NodeCache cache) {
        this(connector, path, cache, cache != null, false);
    }

    public ZkNodeCacheWrapper(IDataSourceConnector connector, String path, NodeCache cache, boolean dataIsCompressed, boolean useCache, boolean useCacheWhenNotConnectedToDataSource) {
        super(connector);
        this.useCache = useCache;
        this.dataIsCompressed = dataIsCompressed;
        this.useCacheWhenNotConnectedToDataSource = useCacheWhenNotConnectedToDataSource;
        this.path = path;
        this.cache = cache;
    }

    public ZkNodeCacheWrapper(IDataSourceConnector connector, String path, NodeCache cache, boolean useCache, boolean useCacheWhenNotConnectedToDataSource) {
        this(connector, path, cache, false, useCache, useCacheWhenNotConnectedToDataSource);
    }

    @Override
    public void start(final boolean buildInitial) throws DataSourceConnectorException {
        if (useCache || useCacheWhenNotConnectedToDataSource) {
            if (! connector.isConnected()) {
                throw new DataSourceConnectorException("Failed to start cache for path=" + path + " due to no connection");
            }
            try {
                cache.start(buildInitial);
                allowUseCache();

                log.debug("Successfully started cache for path={}", path);
            } catch (Exception e) {
                log.error("Failed to start cache for path={} {}", path, e);
            }
        }
    }

    @Override
    public void addListener(NodeCacheListener listener) {
        if (useCache || useCacheWhenNotConnectedToDataSource) {
            cache.getListenable().addListener(listener);
        }
    }

    @Override
    public void rebuild() {
        if ((useCache || useCacheWhenNotConnectedToDataSource) && connector.isConnected()) {
            try {
                cache.rebuild();
                allowUseCache();
                log.debug("Successfully rebuild cache for path={}", path);
            } catch (Exception e) {
                log.error("Failed to rebuild cache for path={} {}", path, e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (cache != null) {
            cache.close();
        }
    }

    @Override
    public byte[] getCurrentData() {
        byte[] result = null;
        if (isCacheUsageAllowed()) {
            ChildData data = cache.getCurrentData();
            if (data != null) {
                result = data.getData();
            }
        } else {
            try {
                result = dataIsCompressed ? connector.getDataDecompressed(path) : connector.getData(path);
            } catch (DataSourceConnectorException e) {
                log.warn("Failed to get data for zkPath={} {}", path);
            }
        }
        return result;
    }

    @Override
    public int getCurrentDataVersion() {
        int result = RedirectorConstants.NO_MODEL_NODE_VERSION;
        if (isCacheUsageAllowed()) {
            ChildData data = cache.getCurrentData();
            if (data != null && data.getStat() != null) {
                result = data.getStat().getVersion();
            }
        } else {
            try {
                if (connector.isPathExists(path)) {
                    result = connector.getNodeVersion(path);
                } else {
                    log.warn("Node {} does not exist", path);
                }
            } catch (DataSourceConnectorException e) {
                log.error("Failed to get data for zkPath={} {}", path, e);
            }
        }
        return result;
    }

    @Override
    public String getPath() {
        return path;
    }

    public boolean isUseCache() {
        return useCache;
    }
}
