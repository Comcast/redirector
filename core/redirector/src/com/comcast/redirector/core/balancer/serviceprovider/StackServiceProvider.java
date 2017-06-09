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

package com.comcast.redirector.core.balancer.serviceprovider;

import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.IWhitelistPredicate;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.engine.FilterMode;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class StackServiceProvider implements IStackServiceProvider, IFilteredServiceProvider {
    private static final Logger log = LoggerFactory.getLogger(StackServiceProvider.class);

    private final ServiceProvider<MetaData> provider;
    private final XreStackPath stack;

    public StackServiceProvider(ServiceProvider<MetaData> provider, XreStackPath stack) {
        this.provider = provider;
        this.stack = stack;
    }

    @Override
    public ServiceInstance<MetaData> getInstance() {
        try {
            return provider.getInstance();
        } catch (Exception e) {
            log.error("failed to get service instance", e);
            return null;
        }
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllInstances() {
        try {
            return provider.getAllInstances();
        } catch (Exception e) {
            log.error("failed to get all service instances", e);
            return null;
        }
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllUniqueInstances() {
        try {
            return UniqueInstancesExtractor.extract(provider.getAllInstances());
        } catch (Exception e) {
            log.error("failed to get all service instances", e);
            return null;
        }
    }

    private boolean apply(FilterMode filterMode, IWhitelistPredicate whitelist) {
        return (filterMode.isInverse()) != isWhiteListed(whitelist);
    }

    private boolean isWhiteListed(IWhitelistPredicate whitelist) {
        return whitelist.isWhiteListed(getStack().getStackOnlyPath());
    }

    @Override
    public ServiceInstance<MetaData> getFilteredInstance(FilterMode filterMode, IWhitelistPredicate whitelist) {
        return (apply(filterMode, whitelist)) ? getInstance() : null;
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllFilteredInstances(FilterMode filterMode, IWhitelistPredicate whitelist) {
        return (apply(filterMode, whitelist)) ? getAllInstances() : Collections.<ServiceInstance<MetaData>>emptyList();
    }

    public static class UniqueInstancesExtractor {

        public static Collection<ServiceInstance<MetaData>> extract(Collection<ServiceInstance<MetaData>> source) {
            // TODO: fix hashCode and Equals method of ServiceInstance/Metadata
            Map<String, ServiceInstance<MetaData>> result = new HashMap<>();
            for (ServiceInstance<MetaData> si : source) {
                result.put(getUniqueInstanceKey(si), si);
            }
            return result.values();
        }

        private static String getUniqueInstanceKey(ServiceInstance<MetaData> instance) {
            if (instance.getPayload() != null && instance.getPayload().getParameters() != null) {
                String ipv4 = instance.getPayload().getParameters().get(IpProtocolVersion.IPV4.getId());
                String ipv6 = instance.getPayload().getParameters().get(IpProtocolVersion.IPV6.getId());

                return ipv4 + ipv6;
            } else {
                return instance.getAddress();
            }
        }
    }

    @Override
    public XreStackPath getStack() {
        return stack;
    }

    @Override
    public void close() throws IOException {
        provider.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StackServiceProvider)) return false;
        StackServiceProvider that = (StackServiceProvider) o;
        return Objects.equals(getStack(), that.getStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, getStack());
    }
}
