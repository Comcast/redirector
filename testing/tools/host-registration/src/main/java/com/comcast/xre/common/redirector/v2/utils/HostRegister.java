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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.xre.common.redirector.v2.utils;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.ServiceDiscoveryHostJsonSerializer;
import com.comcast.tvx.cloud.MetaData;
import com.google.common.base.Throwables;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class HostRegister {
    private static final Logger log = LoggerFactory.getLogger(HostRegister.class);

    private CuratorFramework curatorFramework;
    private String zookeeperBasePath;

    private List<HostRegistry> hostRegistries = new ArrayList<>();

    public HostRegister(String zooKeeperConnectionString, String zookeeperBasePath) {
        curatorFramework = CuratorFrameworkFactory.builder()
            .connectionTimeoutMs(10 * 1000)
            .retryPolicy(new ExponentialBackoffRetry(10, 20))
            .connectString(zooKeeperConnectionString).build();

        curatorFramework.start();
        this.zookeeperBasePath = zookeeperBasePath;

        try {
            curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    public void registerHosts(String stackName, final String flavor, final String serviceName, final List<Host> hosts) {
        registerHosts(stackName, flavor, serviceName, hosts, null);
    }

    public void registerHosts(String stackName, final String flavor, final String serviceName, final List<Host> hosts, final String weight) {
        HostRegistry registry = new HostRegistry(serviceName, stackName, flavor, weight);
        registry.registerHosts(hosts);
        log.info("Hosts are registered for {}/{}/{}", stackName, flavor, serviceName);

        hostRegistries.add(registry);
    }

    public void deRegisterAllHosts() {
        hostRegistries.forEach(HostRegistry::shutdown);
    }

    private class HostRegistry {
        private String weight;
        private String appName;

        private String basePath;
        private ServiceDiscovery<MetaData> discovery;
        private CountDownLatch registrationDone;

        private ExecutorService executorService =
            new ThreadPoolExecutor(0, 10, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
        private Thread aliveHostsSimulator;

        public HostRegistry(String appName, String stackName, String flavor, String weight) {
            this.weight = weight;
            this.appName = appName;

            initBasePathWithStackAndFlavor(stackName, flavor);
            initServiceDiscovery();
        }

        private void initBasePathWithStackAndFlavor(String stackName, String flavor) {
            basePath = zookeeperBasePath + "/" + RedirectorConstants.SERVICES_PATH + stackName + "/" + flavor;
        }

        private void initServiceDiscovery() {
            JsonInstanceSerializer<MetaData> serializer = new JsonInstanceSerializer<>(MetaData.class);
            discovery = ServiceDiscoveryBuilder.builder(MetaData.class).client(curatorFramework).basePath(basePath).serializer(serializer).build();
            try {
                discovery.start();
                new EnsurePath(basePath).ensure(curatorFramework.getZookeeperClient());
            } catch (Exception e) {
                log.error("failed to start discovery for {}", basePath, e);
                throw new RuntimeException("failed to start discovery for " + basePath, e);
            }
        }

        public void registerHosts(List<Host> hosts) {
            registrationDone = new CountDownLatch(hosts.size());

            aliveHostsSimulator = new Thread(() -> {
                hosts.forEach(host -> executorService.submit(() -> register(host)));
                keepHostsAliveUntilInterrupted();
            });
            aliveHostsSimulator.start();

            try {
                registrationDone.await();
            } catch (InterruptedException e) {
                log.error("Failed to register hosts for " + appName, e);
            }
        }

        private void register(Host host) {
            Map<String, String> params = getParamsForHost(host);
            try {
                registerServiceForHostWithParams(host, params);
            } catch (Exception e) {
                log.error("failed to register service {}/{}", basePath, host.getIpv4(), e);
            }
        }

        private Map<String, String> getParamsForHost(Host host) {
            Map<String, String> params = new HashMap<>();
            String addressIpv4 = host.getIpv4();
            String addressIpv6 = host.getIpv6();
            params.put(ServiceDiscoveryHostJsonSerializer.IPV4_ADDRESS_FIELD_NAME, addressIpv4);
            if (addressIpv6 != null) {
                params.put(ServiceDiscoveryHostJsonSerializer.IPV6_ADDRESS_FIELD_NAME, addressIpv6);
            }
            if (weight != null) {
                params.put(ServiceDiscoveryHostJsonSerializer.WEIGHT_FIELD_NAME, weight);
            }

            return params;
        }

        private void registerServiceForHostWithParams(Host host, Map<String, String> params) throws Exception {
            ServiceInstance<MetaData> service = getServiceInstance(appName, host.getIpv4(), params);
            discovery.registerService(service);
            log.info("Registered host={}(weight={}) for path {}", host.getIpv4(), params.get(ServiceDiscoveryHostJsonSerializer.WEIGHT_FIELD_NAME), basePath);

            countHostRegistered();
        }

        private void countHostRegistered() {
            registrationDone.countDown();
        }

        private void keepHostsAliveUntilInterrupted() {
            while (! Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    log.warn("Stop simulating alive hosts connection: {}", e.getMessage());
                    break;
                }
            }
        }

        public void shutdown() {
            try {
                discovery.close();
            } catch (IOException e) {
                log.error("Failed to close discovery");
            }
            aliveHostsSimulator.interrupt();
        }
    }

    static ServiceInstance<MetaData> getServiceInstance(
            String serviceName,
            String serviceAddress,
            Map<String, String> parameters) throws Exception {

        int servicePort = 0;
        ServiceInstanceBuilder<MetaData> builder = ServiceInstance.builder();

        // Address is optional.  The Curator library will automatically use the IP from the first
        // ethernet device
        String registerAddress = (serviceAddress == null) ? builder.build().getAddress() : serviceAddress;

        MetaData metadata = new MetaData(UUID.randomUUID(), registerAddress, servicePort, serviceName);
        metadata.setParameters(parameters);

        builder.name(serviceName).payload(metadata).id(registerAddress + ":" +
                String.valueOf(servicePort)).serviceType(ServiceType.DYNAMIC).address(registerAddress).port(servicePort);

        return builder.build();
    }
}
