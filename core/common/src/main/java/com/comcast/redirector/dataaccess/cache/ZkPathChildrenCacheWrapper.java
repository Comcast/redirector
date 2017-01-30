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

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

public class ZkPathChildrenCacheWrapper extends BaseCacheWrapper implements IPathChildrenCacheWrapper {
    private static final Logger log = LoggerFactory.getLogger(ZkPathChildrenCacheWrapper.class);

    private PathChildrenCache cache;
    private String path;
    private boolean dataIsCompressed = false;

    public ZkPathChildrenCacheWrapper(IDataSourceConnector connector,
                                      String path,
                                      boolean dataIsCompressed,
                                      PathChildrenCache cache) {
        super(connector);
        this.useCache = cache != null;
        this.path = path;
        this.dataIsCompressed = dataIsCompressed;
        this.cache = cache;
    }

    @Override
    public void start(final PathChildrenCache.StartMode startMode) throws DataSourceConnectorException {
        if (useCache || useCacheWhenNotConnectedToDataSource) {
            if (! connector.isConnected()) {
                throw new DataSourceConnectorException("Failed to start cache for path=" + path + " due to no connection");
            }
            try {
                cache.start(startMode);
                allowUseCache();

                log.debug("Successfully started cache for path={}", path);
            } catch (Exception e) {
                log.error("Failed to start cache for path={}", path, e);
            }
        }
    }

    @Override
    public void addListener(PathChildrenCacheListener listener) {
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
                log.debug("Successfully rebuilt cache for path={}", path);
            } catch (Exception e) {
                log.error("Failed to rebuild cache for path={}", path, e);
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
    public void rebuildNode(final String path) {
        if (useCache || useCacheWhenNotConnectedToDataSource) {
            try {
                cache.rebuildNode(path);
                log.debug("Successfully rebuilt node cache for path={}", path);
            } catch (Exception e) {
                log.error("Failed to rebuild node cache for path={}", path, e);
            }
        }
    }

    @Override
    public Map<String, byte[]> getNodeIdToDataMap() throws DataSourceConnectorException {
        Map<String, byte[]> result = new HashMap<>();
        if (isCacheUsageAllowed()) {
            for (ChildData childData : cache.getCurrentData()) {
                String nodeId = getRuleId(childData.getPath());
                result.put(nodeId, childData.getData());
            }
        } else {
            List<String> children = connector.getChildren(path);
            for ( String child : children ) {
                String fullPath = ZKPaths.makePath(path, child);
                String nodeId = getRuleId(fullPath);
                byte[] bytes = getBytesForPath(fullPath);
                if (bytes != null) {
                    result.put(nodeId, bytes);
                } else {
                    log.info("ignoring null data for zkPath={}", fullPath);
                }
            }
        }

        return result;
    }

    @Override
    public byte[] getCurrentData(String fullPath) throws DataSourceConnectorException {
        byte[] result = null;
        if (isCacheUsageAllowed()) {
            ChildData childData = cache.getCurrentData(fullPath);
            if (childData != null) {
                result = childData.getData();
            }
        } else {
            result = getBytesForPath(fullPath);
        }
        return result;
    }

    private byte[] getBytesForPath(String fullPath) {
        try {
            return dataIsCompressed ? connector.getDataDecompressed(fullPath) : connector.getData(fullPath);
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get data for zkPath={}", fullPath, e);
            return null;
        }
    }

    public boolean isUseCache() {
        return useCache;
    }

    private String getRuleId(String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf(DELIMETER) + 1);
    }
}
