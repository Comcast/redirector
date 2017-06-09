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

package com.comcast.redirector.dataaccess.client;

import com.comcast.redirector.dataaccess.cache.IStacksCache;
import com.comcast.redirector.dataaccess.cache.factory.INodeCacheFactory;
import com.comcast.redirector.dataaccess.cache.factory.IPathChildrenCacheFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.data.Stat;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZookeeperConnectorTestBase {
    protected ZookeeperConnector.IPathCreator pathCreator;
    protected IStacksCache stackCache;
    protected INodeCacheFactory nodeCacheFactory;
    protected IPathChildrenCacheFactory pathChildrenCacheFactory;
    protected CuratorFramework client;
    private ExistsBuilder existsBuilder;
    protected ZookeeperConnector testee;

    protected void setupClient() {
        client = mock(CuratorFramework.class);
        existsBuilder = mock(ExistsBuilder.class);
        pathCreator = mock(ZookeeperConnector.IPathCreator.class);
        stackCache = mock(IStacksCache.class);
        nodeCacheFactory = mock(INodeCacheFactory.class);
        pathChildrenCacheFactory = mock(IPathChildrenCacheFactory.class);

        when(client.checkExists()).thenReturn(existsBuilder);
        when(client.getState()).thenReturn(CuratorFrameworkState.STARTED);
    }

    protected void initZookeeperConnector() throws NoSuchFieldException, IllegalAccessException {

        testee = new ZookeeperConnector(client, "anyBasePath", true, pathCreator, stackCache, nodeCacheFactory, pathChildrenCacheFactory);

        testee.stateChanged(client, ConnectionState.CONNECTED);
    }

    protected void setupCheckExists(String inputPath, boolean expectedResult) throws Exception {
        when(existsBuilder.forPath(eq(inputPath))).thenReturn(expectedResult ? mock(Stat.class) : null);
    }
}
