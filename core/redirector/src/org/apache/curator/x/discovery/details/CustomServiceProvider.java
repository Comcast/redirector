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

package org.apache.curator.x.discovery.details;

import com.comcast.redirector.core.balancer.serviceprovider.weight.IInstanceWeigher;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.IDiscoveryBackupManager;
import com.google.common.collect.Lists;
import org.apache.curator.utils.ThreadUtils;
import org.apache.curator.x.discovery.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

 /**
  * Note: this class was placed to org.apache.curator.x.discovery.details package, because it needs to access package-private
  * methods of {@link ServiceDiscoveryImpl} and {@link org.apache.curator.x.discovery.details.DownInstanceManager}. That is not a robust solution, but we have no other option.
  *
  * In addition to {@link ServiceProvider} functionality also logs cache changes. Unfortunately we can't use {@link ServiceCache#addListener(Object)},
  * because it's listener gives no information regarding what was changed ({@link org.apache.curator.x.discovery.details.ServiceCacheListener#cacheChanged()}.
  * That's why {@link CustomServiceCache} was introduced.
  *
  * {@link ServiceDiscovery#serviceProviderBuilder()} already provides implementation of
  * {@link ServiceProvider}. But that implementation doesn't allow to log cache changes.
  */
 public class CustomServiceProvider<T> implements ServiceProvider<T> {
     private final static Logger log = LoggerFactory.getLogger(CustomServiceProvider.class);

     private final String serviceName;
     private final ServiceCache<T> cache;
     private final InstanceProvider<T> instanceProvider;
     private final ServiceDiscoveryImpl<T> discovery;
     private final ProviderStrategy<T> providerStrategy;

     public CustomServiceProvider(ServiceDiscoveryImpl<T> discovery, String basePath, String serviceName,
                                  ProviderStrategy<T> providerStrategy, IDiscoveryBackupManager discoveryStacksBackupManager, IInstanceWeigher<T> weighter) {
         this(discovery, basePath, serviceName, providerStrategy, ThreadUtils.newThreadFactory("CustomServiceProvider: " + basePath + serviceName),
                 new ArrayList<InstanceFilter<T>>(), discoveryStacksBackupManager, weighter);
     }

     public CustomServiceProvider(ServiceDiscoveryImpl<T> discovery, String basePath, String serviceName,
                                  ProviderStrategy<T> providerStrategy,
                                  ThreadFactory threadFactory,
                                  List<InstanceFilter<T>> filters, IDiscoveryBackupManager discoveryStacksBackupManager, IInstanceWeigher<T> weighter) {
         this.serviceName = serviceName;
         this.discovery = discovery;
         this.providerStrategy = providerStrategy;

         cache = new CustomServiceCache<>(discovery, basePath, serviceName, threadFactory, discoveryStacksBackupManager, weighter);

         ArrayList<InstanceFilter<T>> localFilters = Lists.newArrayList(filters);
         instanceProvider = new FilteredInstanceProvider<>(cache, localFilters);
     }

     /**
      * The provider must be started before use
      *
      * @throws Exception any errors
      */
     @Override
     public void start() throws Exception {
         log.info("Starting Service Provider for zkStackPath={}", serviceName);
         cache.start();
         discovery.providerOpened(this);
         log.info("Service Provider started for zkStackPath={}", serviceName);
     }

     /**
      * {@inheritDoc}
      */
     @Override
     public void close() throws IOException {
         log.info("Closing Service Provider for zkStackPath={}", serviceName);
         discovery.providerClosed(this);
         cache.close();
         log.info("Service Provider closed for zkStackPath={}", serviceName);
     }

     /**
      * Return the current available set of instances <b>IMPORTANT: </b> users
      * should not hold on to the instance returned. They should always get a fresh list.
      *
      * @return all known instances
      * @throws Exception any errors
      */
     @Override
     public Collection<ServiceInstance<T>> getAllInstances() throws Exception {
         return instanceProvider.getInstances();
     }

     /**
      * Return an instance for a single use. <b>IMPORTANT: </b> users
      * should not hold on to the instance returned. They should always get a fresh instance.
      *
      * @return the instance to use
      * @throws Exception any errors
      */
     @Override
     public ServiceInstance<T> getInstance() throws Exception {
         return providerStrategy.getInstance(instanceProvider);
     }

     @Override
     public void noteError(ServiceInstance<T> instance) {
         log.warn("host {} is down", instance);
     }
 }
