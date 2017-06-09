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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.dataaccess.cache.newzkstackscache;

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.logging.ExecutionFlow;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostDeserializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.cache.IDataListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public abstract class AbstractServiceDiscovery implements IServiceDiscovery {

    private static final ThreadLocalLogger log = new ThreadLocalLogger(AbstractServiceDiscovery.class);

    protected String stackPathPrefix;
    protected INotifier listenersNotifier;
    protected ServiceDiscoveryHostDeserializer hostsSerializer;
    protected IDataSourceConnector connector;
    private ForkJoinPool forkJoinPool = new ForkJoinPool();

    @FunctionalInterface
    public interface INotifier {
        void notifyListeners();
    }

    AbstractServiceDiscovery(IDataSourceConnector connector, ServiceDiscoveryHostDeserializer hostsSerializer, INotifier listenersNotifier) {
        this.hostsSerializer = hostsSerializer;
        this.connector = connector;
        this.stackPathPrefix = PathHelper.getPathHelper(EntityType.STACK, connector.getBasePath()).getPath();
        this.listenersNotifier = listenersNotifier;
    }

    abstract void applyChangesToHostCaches(Set<XreStackPath> allStackPaths);

    public void discoverAppsAndStacksChanges(int newVersion, Consumer<Integer> updateCurrentVersion) throws Exception {
        log.setExecutionFlow(ExecutionFlow.stacksCacheUpdate);
        log.info("Stacks changes detected. newStacksVersion=" + newVersion + " Triggering stacks discovery.");
        IDiscoveryCycle cycle = new StreamDiscoveryCycle(connector, forkJoinPool, newVersion);
        try {
            cycle.execute();
            Set<XreStackPath> stacks = cycle.getAllStacks();
            if (stacks != null) {
                applyChangesToHostCaches(stacks);
            }
        } catch (Exception e) {
            log.error("Failed to update stacks of version=" + newVersion, e);
            throw e;
        }
        updateCurrentVersion.accept(newVersion);
        log.setCustomMessage(null);

        listenersNotifier.notifyListeners();
    }
}
