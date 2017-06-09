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

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.balancer.serviceprovider.weight.IInstanceWeigher;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.balancer.util.ServiceProviderUtils;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.IDiscoveryBackupManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadFactory;

/**
 * The goal of this class is to implement logging of cache changes. See {@link #childEvent(CuratorFramework, PathChildrenCacheEvent)}
 * Unfortunately we can't use {@link org.apache.curator.x.discovery.ServiceCache#addListener(Object)} for logging,
 * because it's listener gives no information regarding what was changed ({@link org.apache.curator.x.discovery.details.ServiceCacheListener#cacheChanged()}.
 * <p/>
 * To get detailed information we have to extend ServiceCacheImpl, as long as it implements {@link org.apache.curator.framework.recipes.cache.PathChildrenCacheListener}
 * and passes itself to underlying {@link org.apache.curator.framework.recipes.cache.PathChildrenCache} as listener.
 */
public class CustomServiceCache<T> extends ServiceCacheImplProxy<T> {

    private static Logger log = LoggerFactory.getLogger(CustomServiceCache.class);

    private static final String IP_PORT_SEPARATOR = ":"; // registered service node has name ip:port
    private IInstanceWeigher<T> weighter;

    enum Action {
        REGISTERED("registered"),
        UNREGISTERED("unregistered"),
        MODIFIED("modified"),
        INITIALIZED("initialized");

        private String id;

        Action(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private static Set<PathChildrenCacheEvent.Type> EVENTS_TO_LISTEN = new HashSet<PathChildrenCacheEvent.Type>() {{
        add(PathChildrenCacheEvent.Type.CHILD_ADDED);
        add(PathChildrenCacheEvent.Type.CHILD_UPDATED);
        add(PathChildrenCacheEvent.Type.CHILD_REMOVED);
        add(PathChildrenCacheEvent.Type.INITIALIZED);
    }};

    private String basePath;
    private String stackPath;

    private IDiscoveryBackupManager discoveryStacksBackupManager;

    CustomServiceCache(ServiceDiscoveryImpl<T> discovery, String basePath, String name, ThreadFactory threadFactory,
                       IDiscoveryBackupManager discoveryStacksBackupManager, IInstanceWeigher<T> weighter) {
        super(discovery, name, threadFactory);
        this.basePath = basePath;
        this.weighter = weighter;
        stackPath = discovery.pathForName(name);
        this.discoveryStacksBackupManager = discoveryStacksBackupManager;
    }

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        try {
            super.childEvent(client, event); // throws Exception
        } catch (Exception e) {
            log.error("Issue receiving ZkEventType={} for zkStackPath={}", event.getType(), stackPath, e);
        } finally {
            if (EVENTS_TO_LISTEN.contains(event.getType())) {
                switch (event.getType()) {
                    case CHILD_REMOVED:
                        eventFromClient(event.getData().getPath(), getInstances(), Action.UNREGISTERED);
                        break;
                    case INITIALIZED:
                        List<String> paths = new ArrayList<>();
                        List<ChildData> childData = getCache().getCurrentData();
                        if (CollectionUtils.isNotEmpty(childData)) {
                            for (ChildData data : childData) {
                                paths.add(data.getPath());
                            }
                        }
                        log.info("Dynamic Service Discovery cache for {} is initialized. Stacks.json will be written of FS", stackPath);
                        eventFromClient(paths, getInstances(), Action.INITIALIZED);
                        break;
                    default:
                        eventFromClient(event.getData().getPath(), getInstances(), Action.REGISTERED);
                }
            }
        }
    }

    @Override
    public List<ServiceInstance<T>> getInstances() {
        List<ServiceInstance<T>> instances = new ArrayList<>();
        for (ServiceInstance<T> si : super.getInstances()) {
            int weight = weighter.getWeight(si);
            for (int i = 0; i < weight; i++) {
                instances.add(si);
            }
        }

        Collections.shuffle(instances, new Random(System.currentTimeMillis()));
        return instances;
    }

    private void writeBackup(String path, final String addressIPV6, Action action, String weight) {
        int nameIndex = path.lastIndexOf(RedirectorConstants.DELIMETER);
        String stack = path.substring(basePath.length(), nameIndex);
        final String address = StringUtils.substringBeforeLast(StringUtils.substring(path, nameIndex + 1), IP_PORT_SEPARATOR);
        StackSnapshot currentSnapshot = createStackSnapshot(stack, address, addressIPV6, weight);
        if (action.getId().equals(Action.REGISTERED.getId())) {
            discoveryStacksBackupManager.addStackSnapshot(currentSnapshot);
        } else {
            discoveryStacksBackupManager.deleteStackSnapshot(currentSnapshot);
        }
    }

    private void syncBackup(List<String> paths) {

        List<StackSnapshot.Host> hosts = new ArrayList<>();
        for (String path : paths) {
            hosts.add(new StackSnapshot.Host(getIPv4(path), null));
        }
        StackSnapshot stackSnapshot = new StackSnapshot();
        stackSnapshot.setPath(StringUtils.substringAfter(stackPath, basePath));
        stackSnapshot.setHosts(hosts);

        discoveryStacksBackupManager.syncStackSnapshot(stackSnapshot);
    }

    private String getIPv4(String path) {
        int nameIndex = path.lastIndexOf(RedirectorConstants.DELIMETER);
        return StringUtils.substringBeforeLast(StringUtils.substring(path, nameIndex + 1), IP_PORT_SEPARATOR);
    }

    private StackSnapshot createStackSnapshot(String stack, final String address, final String addressIPV6, final String weight) {
        StackSnapshot currentSnapshot = new StackSnapshot();
        currentSnapshot.setPath(stack);
        currentSnapshot.setHosts(new ArrayList<StackSnapshot.Host>() {{
            add(new StackSnapshot.Host(address, addressIPV6, weight));
        }});
        return currentSnapshot;
    }

    private void eventFromClient(String path, List<ServiceInstance<T>> serviceInstanceList, Action type) {
        eventFromClient(Arrays.asList(path), serviceInstanceList, type);
    }

    private void eventFromClient(List<String> paths, List<ServiceInstance<T>> serviceInstanceList, Action type) {
        log.info("{} for {} ", type, paths);
        switch (type) {
            case INITIALIZED:
                syncBackup(paths);
                break;
            default:
                String path = paths.get(0);
                String IPv6 = null;
                String weight = null;
                int nameIndex = path.lastIndexOf(RedirectorConstants.DELIMETER);
                String id = StringUtils.substring(path, nameIndex + 1);
                for (ServiceInstance<T> instance : serviceInstanceList) {
                    if (instance.getId().equals(id)) {
                        IPv6 = gedIPV6FromInstance(instance);
                        weight = getWeightFromInstance(instance);
                        break;
                    }
                }
                writeBackup(path, IPv6, type, weight);
        }
    }

    private String gedIPV6FromInstance(ServiceInstance<T> instance) {
        MetaData metaData = (MetaData) instance.getPayload();
        String addressIPV6 = null;
        if (metaData.getParameters() != null) {
            addressIPV6 = metaData.getParameters().get(IpProtocolVersion.IPV6.getId());
        }
        return addressIPV6;
    }

    private String getWeightFromInstance(ServiceInstance<T> instance) {
        MetaData metaData = (MetaData) instance.getPayload();
        String weight = null;
        if (metaData.getParameters() != null) {
            weight = metaData.getParameters().get(ServiceProviderUtils.WEIGHT_PARAMETER);
        }
        return weight;
    }
}
