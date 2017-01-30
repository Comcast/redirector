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

package com.comcast.redirector.core.balancer.util;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.core.balancer.serviceprovider.IFilteredServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceProviderUtils {
    private static final Logger log = LoggerFactory.getLogger(ServiceProviderUtils.class);

    private static final String STACK_NAME_PARAMETER = "stackName";
    public static final String WEIGHT_PARAMETER = "weight";

    public static String getAddress(ServiceInstance<MetaData> serviceInstance, IpProtocolVersion version) {
        String result = null;
        if (serviceInstance != null && serviceInstance.getPayload() != null && serviceInstance.getPayload().getParameters() != null) {
            result = serviceInstance.getPayload().getParameters().get(version.getId());
        }
        if (StringUtils.isBlank(result) && version == IpProtocolVersion.IPV4) {
            result = serviceInstance.getAddress();
        }
        return result;
    }

    // TODO: if we had abstraction above ServiceInstance<MetaData> we won't need all those dances
    public static MetaData getMetaDataFromHost(HostIPs host) {
        MetaData metaData = new MetaData();
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put(IpProtocolVersion.IPV4.getId(), host.getIpV4Address());
        parameters.put(IpProtocolVersion.IPV6.getId(), host.getIpV6Address());
        parameters.put(WEIGHT_PARAMETER, host.getWeight());
        metaData.setParameters(parameters);

        return metaData;
    }

    // TODO: if we had abstraction above ServiceInstance<MetaData> we won't need all those dances
    public static void setStackNameForServiceInstance(ServiceInstance<MetaData> instance, String stackName) {
        if (instance.getPayload() != null) {
            if (instance.getPayload().getParameters() == null) {
                // this is very edge case. Should happen almost never
                log.warn("Service instance without parameters in payload: {}", instance);
                instance.getPayload().setParameters(new HashMap<String, String>());
            }
            instance.getPayload().getParameters().put(STACK_NAME_PARAMETER, stackName);
        } else {
            log.warn("Can't set stackName to service instance without payload: {}", instance);
        }
    }

    public static String getStackNameFromServiceInstance(ServiceInstance<MetaData> instance) {
        String stackName = null;
        if (instance.getPayload() != null) {
            if (instance.getPayload().getParameters() != null) {
                stackName = instance.getPayload().getParameters().get(STACK_NAME_PARAMETER);
            }
        } else {
            log.warn("Can't get stackName from service instance without payload: {}", instance);
        }

        return stackName;
    }

    /**
     * format given string to /[DC]/[Stack] template
     */
    // TODO: util method, to be moved to utils
    public static String formatStackPath(String input) {
        List<String> parts = Splitter.on(RedirectorConstants.DELIMETER).omitEmptyStrings().splitToList(input);
        return RedirectorConstants.DELIMETER + Joiner.on(RedirectorConstants.DELIMETER).join(parts.subList(0, Math.min(2, parts.size())));
    }

    public static StackBackup getStackBackup(List<IFilteredServiceProvider> allProviders, int version) {
        List<StackSnapshot> snapshot = new ArrayList<>(allProviders.size());

        for (IFilteredServiceProvider provider : allProviders) {
            List<StackSnapshot.Host> hosts = new ArrayList<>();
            for (ServiceInstance<MetaData> instance : provider.getAllUniqueInstances()) {
                StackSnapshot.Host host = new StackSnapshot.Host(
                        getAddress(instance, IpProtocolVersion.IPV4),
                        getAddress(instance, IpProtocolVersion.IPV6),
                        getWeightFromPayload(instance));
                hosts.add(host);
            }

            snapshot.add(new StackSnapshot(provider.getStack().getPath(), hosts));
        }

        return new StackBackup(version, snapshot);
    }

    public static String getWeightFromPayload(ServiceInstance<MetaData> instance) {
        String weight = null;
        if (instance.getPayload() != null && instance.getPayload().getParameters() != null) {
            weight = instance.getPayload().getParameters().get(ServiceProviderUtils.WEIGHT_PARAMETER);
        }

        return weight;
    }

    public static StackBackup getStackBackup(List<IFilteredServiceProvider> allProviders) {
        return getStackBackup(allProviders, 0);
    }

    public static StackBackup getStackBackup(Set<StackData> stacksData) {
        return getStackBackup(stacksData, 0);
    }
    public static StackBackup getStackBackup(Set<StackData> stacksData, int version) {
        List<StackSnapshot> snapshot = new ArrayList<>();

        for (StackData stackData: stacksData) {
            List<StackSnapshot.Host> hosts = stackData.getHosts().orElse(new ArrayList<>()).stream()
                .map(hostIPs -> new StackSnapshot.Host(hostIPs.getIpV4Address(), hostIPs.getIpV6Address(), hostIPs.getWeight()))
                .collect(Collectors.toList());

            snapshot.add(new StackSnapshot(stackData.getPath(), hosts));
        }

        return new StackBackup(version, snapshot);
    }
}
