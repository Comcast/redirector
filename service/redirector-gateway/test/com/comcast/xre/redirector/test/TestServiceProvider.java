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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.xre.redirector.test;

import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.redirector.core.balancer.serviceprovider.IFilteredServiceProvider;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.IWhitelistPredicate;
import com.comcast.redirector.core.engine.FilterMode;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class TestServiceProvider implements IFilteredServiceProvider {
    private ProviderStrategy providerStrategy;
    private List<ServiceInstance<MetaData>> serviceInstances;
    private XreStackPath stack;

    public TestServiceProvider(ProviderStrategy providerStrategy, List<ServiceInstance<MetaData>> serviceInstances, XreStackPath stack) {
        this.providerStrategy = providerStrategy;
        this.serviceInstances = serviceInstances;
        this.stack = stack;
    }

    @Override
    public ServiceInstance<MetaData> getInstance() {
        try {
            return providerStrategy.getInstance(new InstanceProvider() {
                @Override
                public List<ServiceInstance<MetaData>> getInstances() throws Exception {
                    return serviceInstances;
                }
            });
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllInstances() {
        return serviceInstances;
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllUniqueInstances() {
        return new HashSet<>(getAllInstances());
    }

    @Override
    public void close() throws IOException {}


    @Override
    public ServiceInstance getFilteredInstance(FilterMode filterMode, IWhitelistPredicate whitelist) {
        return null;
    }

    @Override
    public Collection<ServiceInstance<MetaData>> getAllFilteredInstances(FilterMode filterMode, IWhitelistPredicate whitelist) {
        return null;
    }

    @Override
    public XreStackPath getStack() {
        return stack;
    }
}
