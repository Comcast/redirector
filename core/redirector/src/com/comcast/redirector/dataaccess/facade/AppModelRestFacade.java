/*
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
 * @author Alexander Ievstratiev (oievstratiev@productengine.com)
 */
package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.modelupdate.IDataChangePoller;
import com.comcast.redirector.core.modelupdate.NewVersionHandler;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.webserviceclient.IWebServiceClient;

import java.util.Collection;

import static com.comcast.redirector.common.RedirectorConstants.*;

public class AppModelRestFacade extends AbstractModelFacade implements IAppModelFacade {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(AppModelRestFacade.class);
    private final String appName;
    private IWebServiceClient webServiceClient;
    private IDataChangePoller dataChangePoller;

    private Integer currentModelVersion = NO_MODEL_NODE_VERSION;
    private Integer nextModelVersion = null;
    private int currentModelVersionUpdateIntervalSeconds = 60;

    private Integer currentStacksVersion = 0;
    private int stacksVersionUpdateIntervalSeconds = 60;
    
    public AppModelRestFacade(IDataSourceConnector connector, String appName, IDataChangePoller dataChangePoller, IWebServiceClient webServiceClient, ZKConfig zkConfig) {
        super(connector);
        this.webServiceClient = webServiceClient;
        this.dataChangePoller = dataChangePoller;
        this.appName = appName;

        stacksVersionUpdateIntervalSeconds = zkConfig.getStacksPollIntervalSeconds();
        currentModelVersionUpdateIntervalSeconds = zkConfig.getModelPollIntervalSeconds();

        doStart();
    }
    
    public static class Builder {
        private IDataSourceConnector connector;
        private IWebServiceClient webServiceClient;
        private String appName;
        private ZKConfig zkConfig;
        private IDataChangePoller dataChangePoller;

        public AppModelRestFacade.Builder withConnector(IDataSourceConnector connector) {
            this.connector = connector;
            return this;
        }
        
        public AppModelRestFacade.Builder withWebServiceClient(IWebServiceClient webServiceClient) {
            this.webServiceClient = webServiceClient;
            return this;
        }
        
        public AppModelRestFacade.Builder forApplication(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder withZkConfig(ZKConfig zkConfig) {
            this.zkConfig = zkConfig;
            return this;
        }

        public AppModelRestFacade.Builder withDataChangePoller(IDataChangePoller dataChangePoller) {
            this.dataChangePoller = dataChangePoller;
            return this;
        }

        public AppModelRestFacade build() {
            return new AppModelRestFacade(connector, appName, dataChangePoller, webServiceClient, zkConfig);
        }
    }
    
    @Override
    public Server getServer(String serverName) {
        ThreadLocalLogger.setExecutionFlow("requestingServer");
        try {
            return webServiceClient.getRequest(Server.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.SERVERS_CONTROLLER_PATH, appName, serverName));
        } catch (Exception e) {
            log.error("failed to get server", e);
            return null;
        }
    }
    
    @Override
    public Distribution getDistribution() {
        ThreadLocalLogger.setExecutionFlow("requestingDistribution");
        try {
            return webServiceClient.getRequest(Distribution.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.DISTRIBUTION_CONTROLLER_PATH, appName));
        } catch (Exception e) {
            log.error("failed to get distribution", e);
            return null;
        }
    }
    
    @Override
    public Whitelisted getWhitelist() {
        ThreadLocalLogger.setExecutionFlow("requestingWhitelist");
        try {
            return webServiceClient.getRequest(Whitelisted.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.WHITELISTED_CONTROLLER_PATH, appName));
        } catch (Exception e) {
            log.error("failed to get whiteList", e);
            return null;
        }
    }
    
    @Override
    public Collection<IfExpression> getFlavorRules() {
        ThreadLocalLogger.setExecutionFlow("requestingFlavorRules");
        try {
            SelectServer selectServer = webServiceClient.getRequest(SelectServer.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.RULES_CONTROLLER_PATH, appName));
            if (selectServer != null) {
                log.info("got FlavorRules: " + selectServer.getItems());
                return selectServer.getItems();
            }
        } catch (Exception e) {
            log.warn("failed to get flavor rules: {}", e);
        }
        return null;
    }
    
    @Override
    public UrlRule getUrlParams(String urlParamsRuleName) {
        ThreadLocalLogger.setExecutionFlow("requestingDefaultRule");
        try {
            if (urlParamsRuleName.equals("default")) {
                Default defaultRule = webServiceClient.getRequest(Default.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.URL_RULES_CONTROLLER_PATH, appName, "defaultUrlParams"));
                if (defaultRule != null) {
                    return defaultRule.getUrlRule();
                } else {
                    return null;
                }
            } else {
                return webServiceClient.getRequest(UrlRule.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.URL_RULES_CONTROLLER_PATH, appName, urlParamsRuleName));
            }
        } catch (Exception e) {
            log.error("failed to get url params", e);
            return null;
        }
    }
    
    @Override
    public Collection<IfExpression> getUrlRules() {
        ThreadLocalLogger.setExecutionFlow("requestingUrlRules");
        try {
            URLRules urlRules = webServiceClient.getRequest(URLRules.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.URL_RULES_CONTROLLER_PATH, appName));
            if (urlRules != null) {
                return urlRules.getItems();
            }
        } catch (Exception e) {
            log.warn("failed to get url rules: ", e);
        }
        return null;
    }
    
    @Override
    public IfExpression getUrlRule(String ruleId) {
        ThreadLocalLogger.setExecutionFlow("requestingUrlRule");
        try {
            return webServiceClient.getRequest(IfExpression.class, concatEndpointAndVersion(nextModelVersion, RedirectorConstants.URL_RULES_CONTROLLER_PATH, appName, ruleId));
        } catch (Exception e) {
            log.error("failed to get url rules: ", e.getMessage());
            return null;
        }
    }


    @Override
    public void initModelChangedPolling(NewVersionHandler<Integer> refreshModel) {
        dataChangePoller.startDataChangePolling("model", MODEL_RELOAD_PATH +"/getVersion/",
                currentModelVersionUpdateIntervalSeconds,
                refreshModel,
                this::getCurrentModelVersion,
                this::setCurrentModelVersion,
                this::setNextModelVersion,
                appName);
    }

    @Override
    public void initStacksReloadPolling(NewVersionHandler<Integer> refreshStacks) {
        dataChangePoller.startDataChangePolling("stacks", STACKS_RELOAD_PATH + "/getVersion/",
                stacksVersionUpdateIntervalSeconds,
                refreshStacks,
                this::getCurrentStacksVersion,
                this::setCurrentStacksVersion,
                null /* next Stack Version */,
                appName );
    }

    @Override
    public void suspendPolling() {
        dataChangePoller.suspendPolling(appName);
    }

    @Override
    public int getModelVersion(String serviceName) {
        try {
            return connector.getNodeVersion(PathHelper.getPathHelper(EntityType.MODEL_CHANGED, connector.getBasePath()).getPathByService(serviceName));
        } catch (DataSourceConnectorException e) {
            log.warn("Failed to get model version for " + serviceName);
            return NO_MODEL_NODE_VERSION;
        }
    }
    
    @Override
    public int getModelVersion() {
        return currentModelVersion;
    }
    
    @Override
    public boolean isModelExists() {
        Whitelisted whitelisted = getWhitelist();
        Server server = getServer("default");
        UrlRule urlParams = getUrlParams("default");
        return whitelisted != null && server != null && urlParams != null;
    }
    
    @Override
    public void notifyModelRefreshCompleted(int version) {
        //toDo: it was for e2e, we should decide what to do
        //simplePlainDAO.notifyStacksReloadExecutedForApp(serviceName, version);
    }
    
    @Override
    public void notifyStacksReloadCompleted(int version) {
        //toDo: it was for e2e, we should decide what to do
        //simplePlainDAO.notifyModelRefreshExecutedForApp(serviceName, version);
    }

    public Integer getCurrentStacksVersion() {
        return currentStacksVersion;
    }

    public void setCurrentStacksVersion(Integer currentStacksVersion) {
        this.currentStacksVersion = currentStacksVersion;
    }

    public Integer getCurrentModelVersion() {
        return currentModelVersion;
    }

    public void setCurrentModelVersion(Integer currentModelVersion) {
        this.currentModelVersion = currentModelVersion;
    }

    @Override
    public void setNextModelVersion(Integer nextModelVersion) {
        this.nextModelVersion = nextModelVersion;
    }

    @Override
    public Integer getNextModelVersion() {
        return nextModelVersion;
    }
    
    @Override
    public void restartPollingIfSuspended() {
        dataChangePoller.restartPollingIfSuspended(appName);
    }
}
