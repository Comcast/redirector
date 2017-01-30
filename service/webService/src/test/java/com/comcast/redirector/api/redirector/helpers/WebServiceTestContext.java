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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.ServicePaths;

public class WebServiceTestContext {

    private String serviceName;

    private Whitelisted whitelisted;

    private Distribution distribution;

    private ServicePaths servicePaths;

    private UrlRule defaultUrlParams;

    public WebServiceTestContext(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Whitelisted getWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(Whitelisted whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public ServicePaths getServicePaths() {
        return servicePaths;
    }

    public void setServicePaths(ServicePaths servicePaths) {
        this.servicePaths = servicePaths;
    }

    public UrlRule getDefaultUrlParams() {
        return defaultUrlParams;
    }

    public void setDefaultUrlParams(UrlRule defaultUrlParams) {
        this.defaultUrlParams = defaultUrlParams;
    }
}
