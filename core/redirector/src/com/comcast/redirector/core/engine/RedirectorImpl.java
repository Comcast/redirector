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

package com.comcast.redirector.core.engine;

import com.comcast.redirector.common.logging.ExecutionFlow;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.executor.LoggingExecutor;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.applications.IModelRefreshManager;
import com.comcast.redirector.core.modelupdate.IRedirectorEngineProvider;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;
import com.comcast.redirector.metrics.Metrics;
import com.comcast.redirector.ruleengine.model.ServerGroup;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RedirectorImpl implements IRedirector {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(RedirectorImpl.class);

    @Autowired
    private String appName;

    @Autowired
    private IRedirectorEngineProvider redirectorEngineProvider;

    @Autowired
    private IModelRefreshManager modelRefreshManager;

    @Autowired
    private Predicate<String> isStaticDiscoveryNeededForApp;

    @Autowired
    private IAppModelFacade modelFacade;

    @VisibleForTesting
    @Autowired(required = false)
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
    @VisibleForTesting
    @Autowired(required = false)
    private IntegrationTestChangeListener<String> integrationTestModelInitListener;
    @VisibleForTesting
    @Autowired(required = false)
    private IntegrationTestChangeListener<String> integrationTestStacksReloadListener;

    private ExecutorService taskQueue;
    private IRedirectorEngine redirectorEngine;

    public RedirectorImpl() {
        this.redirectorEngine = new EmptyRedirectorEngine();
    }

    @PostConstruct
    private void init() {
        log.info("Starting redirector engine for " + appName);
        taskQueue = LoggingExecutor.Factory.newSingleThreadExecutor(new RedirectorModelLoadThreadFactory(appName));

        initModel();
    }

    @Override
    public void close() throws IOException {
        log.info("Shutting down redirector for " + appName);
        taskQueue.shutdownNow();
    }

    @Override
    public InstanceInfo redirect(Map<String, String> context) {
        return redirectorEngine.redirect(context);
    }

    @Override
    public ServerGroup redirectServerGroup(ServerGroup serverGroup, Map<String, String> context) {
        return redirectorEngine.redirectServerGroup(serverGroup, context);
    }

    public ILoggable getLog() {
        if (redirectorEngine instanceof ILoggable) {
            return (ILoggable) redirectorEngine;
        } else {
            return null;
        }
    }

    private void setRedirectorEngine(IRedirectorEngine newRedirectorEngine) {
        if (newRedirectorEngine != null) {
            this.redirectorEngine = newRedirectorEngine;
        }
    }

    public void refreshModel(int newVersion, Consumer<Integer> upgradeCurrentVersion) throws Exception {
        ThreadLocalLogger.setExecutionFlow(ExecutionFlow.modelRefresh);
        try {
            if (modelRefreshManager.isModelExists()) {
                setRedirectorEngine(redirectorEngineProvider.refreshModel(newVersion));
                upgradeCurrentVersion.accept(newVersion);
                log.info("REFRESHED MODEL");

                Metrics.reportGatewayModelRefreshStats(appName, newVersion, true);
                notifyRefreshListener(newVersion);
            }
            modelRefreshManager.notifyModelRefreshCompleted(newVersion);
        } catch (Exception ex) {
            log.error("Failed to refresh model appName={} updateVersion={}", appName, newVersion);

            Metrics.reportGatewayModelRefreshStats(appName, newVersion, false);
            notifyRefreshListener(newVersion);
            modelRefreshManager.notifyModelRefreshCompleted(RedirectorConstants.NO_MODEL_NODE_VERSION);
            throw ex;
        }
    }

    public void reloadStacks(int newVersion, Consumer<Integer> upgradeCurrentVersion) throws Exception {
        ThreadLocalLogger.setExecutionFlow(ExecutionFlow.reloadStacks);
        try {
            if (modelRefreshManager.isModelExists()) {
                setRedirectorEngine(redirectorEngineProvider.reloadStacks(newVersion));
                upgradeCurrentVersion.accept(newVersion);
                log.info("RELOADED STACKS OF MODEL for app={}", appName);
                notifyStacksReloadListener(newVersion);
            }
            modelRefreshManager.notifyStacksReloadCompleted(newVersion);
        } catch (Exception e) {
            log.error("failed to refresh model stacks", e);
            notifyStacksReloadListener(newVersion);
            modelRefreshManager.notifyStacksReloadCompleted(RedirectorConstants.NO_MODEL_NODE_VERSION);
            throw e;
        }
    }

    public void initModel() {
        taskQueue.submit(() -> {
            ThreadLocalLogger.setExecutionFlow(ExecutionFlow.initModel);
            try {
                if (modelRefreshManager.isModelExists()) {
                    setRedirectorEngine(redirectorEngineProvider.initModel());
                    log.info("INITIATED MODEL");
                    notifyInitListener(0);
                }
                modelRefreshManager.notifyModelRefreshCompleted(0);
                modelFacade.initModelChangedPolling(this::refreshModel);
                if (isStaticDiscoveryNeededForApp.test(appName)) {
                    modelFacade.initStacksReloadPolling(this::reloadStacks);
                }
            } catch (Exception e) {
                log.error("failed to init model", e);
                modelRefreshManager.notifyModelRefreshCompleted(RedirectorConstants.NO_MODEL_NODE_VERSION); modelFacade.initModelChangedPolling(this::refreshModel);
                if (isStaticDiscoveryNeededForApp.test(appName)) {
                    modelFacade.initStacksReloadPolling(this::reloadStacks);
                }
            }
        });
    }

    @Override
    public void suspendPolling() {
        modelFacade.suspendPolling();
    }
    
    @Override
    public void restartPollingIfSuspended() {
        modelFacade.restartPollingIfSuspended();
    }

    @VisibleForTesting
    private void notifyInitListener(int version) {
        if (integrationTestModelInitListener != null) {
            integrationTestModelInitListener.update("app=" + appName + ".version=" + version);
        }
    }

    @VisibleForTesting
    private void notifyRefreshListener(int version) {
        if (integrationTestModelRefreshListener != null) {
            integrationTestModelRefreshListener.update("app=" + appName + ".version=" + version);
        }
    }

    @VisibleForTesting
    private void notifyStacksReloadListener(int version) {
        if (integrationTestStacksReloadListener != null) {
            integrationTestStacksReloadListener.update("app=" + appName + ".version=" + version);
        }
    }
}
