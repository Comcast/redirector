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

package org.apache.curator.x.discovery.details;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.ListenerContainer;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceCacheImplProxy<T> implements ServiceCache<T>, PathChildrenCacheListener {

    private static Logger log = LoggerFactory.getLogger(ServiceCacheImplProxy.class);

    private ListenerContainer<ServiceCacheListener> listenerContainer;
    private ServiceDiscoveryImpl<T> discovery;
    private AtomicReference<State> state = new AtomicReference<State>(State.LATENT);
    private PathChildrenCache cache;
    private ConcurrentMap<String, ServiceInstance<T>> instances = Maps.newConcurrentMap();

    private ServiceCacheImpl<T> serviceCacheImpl;

    private enum State
    {
        LATENT,
        STARTED,
        STOPPED
    }

    public ServiceCacheImplProxy(ServiceDiscoveryImpl<T> discovery, String name, ThreadFactory threadFactory) {
        this.serviceCacheImpl = new ServiceCacheImpl<T>(discovery, name, threadFactory);

        try {
            Field privateListenerContainerField = ServiceCacheImpl.class.getDeclaredField("listenerContainer");
            privateListenerContainerField.setAccessible(true);
            this.listenerContainer = (ListenerContainer)privateListenerContainerField.get(serviceCacheImpl);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Failed to construct Service Cache. Container listeners is null.");
        }

        Preconditions.checkNotNull(discovery, "discovery cannot be null");
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(threadFactory, "threadFactory cannot be null");
        Preconditions.checkNotNull(this.listenerContainer, "container of listeners can not be null");


        this.discovery = discovery;
        this.cache = new PathChildrenCache(discovery.getClient(), discovery.pathForName(name), true, threadFactory);
        this.cache.getListenable().addListener(this);
    }

    public PathChildrenCache getCache() {
        return cache;
    }

    @Override
    public List<ServiceInstance<T>> getInstances() {
        return serviceCacheImpl.getInstances();
    }

    @Override
    public void start() throws Exception {
        Preconditions.checkState(state.compareAndSet(State.LATENT, State.STARTED), "Cannot be started more than once");

        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        for (ChildData childData : cache.getCurrentData()) {
            addInstance(childData, true);
        }
        discovery.cacheOpened(this);
    }

    @Override
    public void close() throws IOException {
        Preconditions.checkState(state.compareAndSet(State.STARTED, State.STOPPED), "Already closed or has not been started");

        listenerContainer.forEach
                (
                        new Function<ServiceCacheListener, Void>()
                        {
                            @Override
                            public Void apply(ServiceCacheListener listener)
                            {
                                discovery.getClient().getConnectionStateListenable().removeListener(listener);
                                return null;
                            }
                        }
                );
        listenerContainer.clear();

        CloseableUtils.closeQuietly(cache);

        discovery.cacheClosed(this);
    }

    @Override
    public void addListener(ServiceCacheListener listener) {
        serviceCacheImpl.addListener(listener);
    }

    @Override
    public void addListener(ServiceCacheListener listener, Executor executor) {
        serviceCacheImpl.addListener(listener, executor);
    }

    @Override
    public void removeListener(ServiceCacheListener listener) {
        serviceCacheImpl.removeListener(listener);
    }

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        serviceCacheImpl.childEvent(client, event);
    }

    private void addInstance(ChildData childData, boolean onlyIfAbsent) throws Exception {

        Method addInstanceMethod = serviceCacheImpl.getClass().getDeclaredMethod("addInstance");
        if (addInstanceMethod != null) {
            addInstanceMethod.invoke(serviceCacheImpl, childData, onlyIfAbsent);
        }
    }
}
