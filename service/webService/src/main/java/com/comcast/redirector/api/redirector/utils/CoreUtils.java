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

package com.comcast.redirector.api.redirector.utils;

import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.ServiceProviderManagerFactory;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

public class CoreUtils {
    public static IServiceProviderManagerFactory newServiceProviderManagerFactory() {
        ServiceProviderManagerFactory serviceProviderManagerFactory = new ServiceProviderManagerFactory();
        serviceProviderManagerFactory.setConfig(new Config());
        serviceProviderManagerFactory.setProviderStrategy(new RoundRobinStrategy<>());

        return serviceProviderManagerFactory;
    }
}
