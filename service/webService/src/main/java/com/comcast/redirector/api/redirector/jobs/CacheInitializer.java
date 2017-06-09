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

package com.comcast.redirector.api.redirector.jobs;

import com.comcast.redirector.api.redirector.service.*;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.redirectortestsuite.IRedirectorTestSuiteService;
import com.comcast.redirector.api.redirector.service.ruleengine.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.dao.IStacksDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.comcast.redirector.api.redirectorOffline.SnapshotFilesPathHelper.SnapshotEntity.APPLICATIONS;

@Component
public class CacheInitializer implements IDataSourceConnector.ConnectionListener {
    private static final Logger log = LoggerFactory.getLogger(CacheInitializer.class);

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    private IAppsService applicationsService;

    @Autowired
    @Qualifier("flavorRulesService")
    private IFlavorRulesService flavorRulesService;

    @Autowired
    @Qualifier("urlRulesService")
    private IUrlRulesService urlRulesService;

    @Autowired
    @Qualifier("templateFlavorRulesService")
    private IFlavorRulesService templateFlavorRulesService;

    @Autowired
    @Qualifier("templateUrlRulesService")
    private IUrlRulesService templateUrlRulesService;

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdateService;

    @Autowired
    private IWhiteListService whiteListService;

    @Autowired
    @Qualifier("changesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IStacksDAO stacksDAO;

    @Autowired
    private INamespacedListsService namespacedListsService;

    @Autowired
    private IRedirectorTestSuiteService testSuiteService;

    @Autowired
    IRedirectorConfigService redirectorConfigService;

    @Autowired
    private IStackCommentsService stackCommentsService;

    private Set<String> appRegistry = new HashSet<>();

    @PostConstruct
    public void init() throws IOException, SerializerException {
        initCache();
        connector.addConnectionListener(this);
    }

    @Override
    public void stateChanged(IDataSourceConnector.ConnectorState newState) {
        if ( connector.isConnected()) {
            try {
                initCache();
            } catch (RedirectorDataSourceException e) {
                log.error("Failed to initialize ZK model change listeners after ZK recovered: {}", e.getMessage());
            }
        }
    }

    private void initCache() {
        if (connector.isConnected()) {
            namespacedListsService.getAllNamespacedLists();
            redirectorConfigService.getRedirectorConfig();
            applicationsService.getAppNames().getAppNames().forEach(this::registerAppModelChangesListener);
            registerNewApplicationsObserver();
        }
    }

    private void registerAppModelChangesListener(String serviceName) {
        if (! appRegistry.contains(serviceName)) {
            log.info("Initializing cache for application={} application", serviceName);
            cacheModelForService(serviceName);
            appRegistry.add(serviceName);
        }
    }

    private void registerNewApplicationsObserver() {
        if (! appRegistry.contains(APPLICATIONS.toString())) {
            log.info("Registering listener for Stacks & Flavor changes");
            stacksDAO.addCacheListener(new ApplicationsListener());
            appRegistry.add(APPLICATIONS.toString());
        }
    }

    private class ApplicationsListener implements ICacheListener {
        @Override
        public void onChanged() {
            applicationsService.getAppNames().getAppNames().forEach(CacheInitializer.this::registerAppModelChangesListener);
        }
    }

    private void cacheModelForService(String serviceName) {
        flavorRulesService.getAllRules(serviceName);
        urlRulesService.getAllRules(serviceName);
        urlParamsService.getDefaultUrlParams(serviceName);
        templateFlavorRulesService.getAllRules(serviceName);
        templateUrlRulesService.getAllRules(serviceName);
        distributionService.getDistribution(serviceName);
        whiteListService.getWhitelistedStacks(serviceName);
        whiteListStackUpdateService.getWhitelistedStacksUpdates(serviceName);
        pendingChangesService.getPendingChangesStatus(serviceName);
        testSuiteService.getAllTestCasesByServiceName(serviceName);
        serverService.getServer(serviceName);
        stackCommentsService.getAllComments(serviceName);
    }
}
