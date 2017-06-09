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
package com.comcast.redirector.core.modelupdate;

import com.comcast.redirector.common.logging.ExecutionFlow;
import com.comcast.redirector.common.logging.ExecutionStep;
import com.comcast.redirector.common.thread.ThreadUtils;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.common.util.UrlUtils;
import com.comcast.redirector.dataaccess.dao.NodeVersionDAO;
import com.comcast.redirector.webserviceclient.IWebServiceClient;
import org.glassfish.jersey.internal.util.Producer;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.comcast.redirector.common.RedirectorConstants.Logging.*;

public class DataChangePoller implements IDataChangePoller {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(DataChangePoller.class);
    private static final int CORE_POOL_SIZE = 1;
    private ScheduledExecutorService executor;
    private IWebServiceClient webServiceClient;
    private List<String> suspendedApps = new CopyOnWriteArrayList<>();
    
    public DataChangePoller(IWebServiceClient webServiceClient) {
        this.webServiceClient = webServiceClient;
    }
    
    @Override
    public void startDataChangePolling(String entityName,
                                       String url,
                                       int interval,
                                       NewVersionHandler<Integer> action,
                                       Producer<Integer> getCurrentVersion,
                                       Consumer<Integer> setCurrentVersion,
                                       Consumer<Integer> setNextVersion,
                                       String appName) {
        
        String threadFactoryName = "DataChangePolling" + "-" + entityName + (appName != null ? "-" + appName : "");
        
        executor = Executors.newScheduledThreadPool(CORE_POOL_SIZE, ThreadUtils.newThreadFactory(threadFactoryName));
        
        executor.scheduleWithFixedDelay(
                () -> {
                    try {
                        if (appName == null || !suspendedApps.contains(appName)) {
                            doDataChangePolling(entityName, url, action, getCurrentVersion, setCurrentVersion, setNextVersion, appName);
                        }
                    } catch (Exception e) {
                        log.error(EXECUTION_STEP_PREFIX + ExecutionStep.responseFromWS + " " + OPERATION_RESULT + "RequestToWsFailed ", e.getMessage());
                    }
                    
                    ThreadLocalLogger.clear();
                },
                0,
                interval,
                TimeUnit.SECONDS);
    }
    
    @Override
    public void suspendPolling(String appName) {
        if (suspendedApps.add(appName)) {
            ThreadLocalLogger.setExecutionFlow("ChangePollingBehavior");
            log.info(OPERATION_RESULT + "PollingIsSuspended " + APP_NAME_PREFIX + appName);
        }
    }
    
    @Override
    public void restartPollingIfSuspended(String appName) {
        if (suspendedApps.remove(appName)) {
            ThreadLocalLogger.setExecutionFlow("ChangePollingBehavior");
            log.info(OPERATION_RESULT + "PollingIsRelaunched " + APP_NAME_PREFIX + appName);
        }
    }
    
    @PreDestroy
    public void shutdown () {
        executor.shutdown();
    }

    private void doDataChangePolling(String entityName, String url,
                                     NewVersionHandler<Integer> newVersionHandler,
                                     Producer<Integer> getCurrentVersion,
                                     Consumer<Integer> setCurrentVersion,
                                     Consumer<Integer> setNextVersion,
                                     String appName) {
        ThreadLocalLogger.setExecutionFlow(ExecutionFlow.dataChangePolling);
        ThreadLocalLogger.setCustomMessage((appName == null ? "" : APP_NAME_PREFIX + appName) + ", " + ENTITY_NAME_PREFIX + entityName);
        Integer version;
        try {
            version = webServiceClient.getRequestAsInteger(UrlUtils.buildUrl(url, (appName == null ? "" : appName)));
            if (version != null) {

                if (setNextVersion != null) {
                    setNextVersion.accept(version);
                }
                ThreadLocalLogger.setExecutionFlow(ExecutionStep.responseFromWS.toString());
                log.info("flagVersionFromWS=" + version + ", currentFlagVersion=" + getCurrentVersion.call());

                if (version > getCurrentVersion.call()) {
                    log.info(OPERATION_RESULT + "FlagChanged");
                    if (newVersionHandler != null) {
                        newVersionHandler.handleNewVersion(version, setCurrentVersion);
                    }
                }
                if (version == NodeVersionDAO.NO_VERSION) {
                    log.info(OPERATION_RESULT + "NoVersionInDataStore, url=" + url);
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
