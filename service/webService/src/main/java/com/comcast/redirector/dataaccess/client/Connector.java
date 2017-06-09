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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.dataaccess.client;

import com.comcast.redirector.common.config.Config;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostJsonSerializer;
import com.comcast.redirector.dataaccess.cache.ZKStacksCache;
import com.comcast.redirector.dataaccess.cache.factory.ZkNodeCacheFactory;
import com.comcast.redirector.dataaccess.cache.factory.ZkPathChildrenCacheFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Connector extends ZookeeperConnector {
    @Autowired
    private Config specificConfig;

    public Connector() {
        super();
    }

    @PostConstruct
    private void init() {
        createConnector();
        connect();
    }
    
    private void createConnector() {
        CuratorFramework curator = CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(specificConfig.getConnectionTimeoutMs())
                .retryPolicy(new RetryNTimes(specificConfig.getRetryCount(), specificConfig.getSleepsBetweenRetryMs()))
                .connectString(specificConfig.getConnectionUrl())
                .compressionProvider(new GzipCompressionCustomProvider())
                .build();
        
        setClient(curator);
        setBasePath(specificConfig.getZookeeperBasePath());
        setCacheHosts(specificConfig.isCacheHosts());
        setStacksCacheFactory(() -> new ZKStacksCache(curator, this, new ServiceDiscoveryHostJsonSerializer(),
                specificConfig.isCacheHosts(), specificConfig.getZookeeperBasePath()));
        
        setNodeCacheFactory(new ZkNodeCacheFactory(this, curator));
        setPathChildrenCacheFactory(new ZkPathChildrenCacheFactory(this, curator));
    }
}
