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

package com.comcast.redirector.core.modelupdate.chain.validator;

import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.engine.IRedirectorEngine;

public class CheckIfAbleToRedirect {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(CheckIfAbleToRedirect.class);
    private String appName;

    private IServiceProviderManager serviceProviderManager;
    private IRedirectorEngineFactory factory;

    private Model flavorRulesModel;
    private WhiteList whiteList;
    private int minHosts;

    private CheckIfAbleToRedirect(Builder builder) {
        appName = builder.appName;
        flavorRulesModel = builder.flavorRulesModel;
        whiteList = builder.whiteList;
        serviceProviderManager = builder.serviceProviderManager;
        factory = builder.redirectorEngineFactory;

        minHosts = builder.minHosts;
    }

    public ValidationReport validate() {
        IRedirectorEngine.IHostSelector hostSelector = factory.newHostSelector(appName, serviceProviderManager, whiteList, flavorRulesModel);
        ValidationReport validationReport = new ValidationReport(Validator.ValidationResultType.SUCCESS);

        if (hostSelector.getDefaultHost() == null) {
            validationReport.setValidationResultType(Validator.ValidationResultType.CANT_REDIRECT_TO_DEFAULT);
            validationReport.setMessage("Can't redirect to Default server");
        } else {
            int defaultAndDistributionHosts;
            String actualHosts;

            int defaultHostsCount = hostSelector.getCountOfHostsForDefaultServer();
            int distributionHostsCount = hostSelector.getCountOfHostsForDistribution();
            defaultAndDistributionHosts = defaultHostsCount + distributionHostsCount;
            actualHosts = "default=" + defaultHostsCount + ", distribution=" + distributionHostsCount + ", total=" + defaultAndDistributionHosts;
            if (defaultAndDistributionHosts < minHosts) {
                validationReport.setValidationResultType(Validator.ValidationResultType.NOT_ENOUGH_HOSTS_FOR_DEFAULT);
                validationReport.setMessage(minHosts + " hosts are required for default + distribution paths, actual: " + actualHosts);
            } else {
                log.info("Default + distribution hosts count is good enough: {}", defaultAndDistributionHosts);
            }
        }

        return validationReport;
    }

    public static class Builder {
        private String appName;

        private IServiceProviderManager serviceProviderManager;
        private Model flavorRulesModel;
        private WhiteList whiteList;
        private IRedirectorEngineFactory redirectorEngineFactory;

        private int minHosts = 100;

        public Builder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder setFlavorRulesModel(Model flavorRulesModel) {
            this.flavorRulesModel = flavorRulesModel;
            return this;
        }

        public Builder setServiceProviderManager(IServiceProviderManager serviceProviderManager) {
            this.serviceProviderManager = serviceProviderManager;
            return this;
        }

        public Builder setRedirectorEngineFactory(IRedirectorEngineFactory factory) {
            this.redirectorEngineFactory = factory;
            return this;
        }

        public Builder setMinHosts(int minHosts) {
            this.minHosts = minHosts;
            return this;
        }

        public Builder setWhiteList(WhiteList whiteList) {
            this.whiteList = whiteList;
            return this;
        }

        public CheckIfAbleToRedirect build() {
            return new CheckIfAbleToRedirect(this);
        }
    }

    public String getAppName() {
        return appName;
    }

    public IRedirectorEngineFactory getFactory() {
        return factory;
    }

    public int getMinHosts() {
        return minHosts;
    }
}
