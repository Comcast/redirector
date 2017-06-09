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

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.io.Closeable;
import java.util.Map;

public interface IPathChildrenCacheWrapper extends Closeable {
    void start(PathChildrenCache.StartMode startMode) throws DataSourceConnectorException;
    void addListener(PathChildrenCacheListener listener);
    void rebuild();
    void rebuildNode(String path);
    Map<String, byte[]> getNodeIdToDataMap() throws DataSourceConnectorException;
    byte[] getCurrentData(String fullPath) throws DataSourceConnectorException;
}
