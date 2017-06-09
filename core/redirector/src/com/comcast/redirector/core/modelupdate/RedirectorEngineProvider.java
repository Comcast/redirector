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

package com.comcast.redirector.core.modelupdate;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.logging.ExecutionFlow;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.backup.IBackupManager;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.IStacksBackupManager;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StacksBackupManager;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.DataSourceStacksSupplier;
import com.comcast.redirector.core.balancer.util.ServiceProviderUtils;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.core.modelupdate.chain.*;
import com.comcast.redirector.core.modelupdate.chain.validator.CheckIfAbleToRedirect;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.core.modelupdate.holder.*;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import com.comcast.xre.redirector.utils.XreGuideAppNames;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.comcast.redirector.core.engine.RedirectorModelLoadThreadFactory.getModelUpdateThreadName;

public class RedirectorEngineProvider implements IRedirectorEngineProvider {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(RedirectorEngineProvider.class);

    @Autowired
    private String appName;
    @Autowired
    private ZKConfig config;
    @Autowired
    private Predicate<String> isStaticDiscoveryNeededForApp;

    @Autowired
    private IAppModelFacade modelFacade;
    @Autowired
    private ICommonModelFacade commonDaoFacade;
    @Autowired
    private IRedirectorEngineFactory redirectorEngineFactory;
    @Autowired
    private IBackupManagerFactory backupManagerFactory;

    @Autowired
    private Serializer xmlSerializer;
    @Autowired
    private IBackupManagerFactory globalBackupManagerFactory;

    @Autowired
    private IServiceProviderManagerFactory serviceProviderManagerFactory;

    @Autowired
    private IDataStoreAwareNamespacedListsHolder namespacedListsHolder;

    @VisibleForTesting
    @Autowired(required = false)
    private IntegrationTestChangeListener<String> modelFailsInitListener;

    private ICachedModelHolder<ModelMetadata> modelMetadataHolder;
    private IModelHolder<SelectServer> flavorRulesHolder;
    private IModelHolder<Whitelisted> whiteListHolder;
    private IModelHolder<URLRules> urlRulesHolder;

    private IStacksBackupManager stacksBackupManager;

    private TaskChain refreshModelChain;
    private TaskChain serviceDiscoveryReloadChain;
    private TaskChain initModelChain;
    private TaskChain syncModelChain;
    private ModelContext lastKnownGoodModelContext;


    @PostConstruct
    private void init() {
        flavorRulesHolder = new FlavorRulesHolder(modelFacade, xmlSerializer, backupManagerFactory);
        whiteListHolder = new WhiteListHolder(modelFacade, xmlSerializer, backupManagerFactory);
        urlRulesHolder = new UrlRulesHolder(modelFacade, xmlSerializer, backupManagerFactory);
        modelMetadataHolder = new ModelMetadataHolder(modelFacade, new JsonSerializer(), backupManagerFactory);

        IBackupManager backupManager = isStaticDiscoveryNeededForApp.test(appName)
            ? backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.STACKS_MANUAL)
            : globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.DISCOVERY);

        stacksBackupManager = new StacksBackupManager(backupManager, new JsonSerializer());

        buildInitModelChain();
        buildRefreshModelChain();
        buildServiceDiscoveryReloadChain();
        buildSyncModelChain();
    }

    private void buildInitModelChain() {
        Supplier<Set<StackData>> stacksSupplier =
            () -> Optional.ofNullable(stacksBackupManager.load()).orElse(new StackBackup()).getAllStacks();

        TaskFactory taskFactory = new TaskFactory(DataSource.BACKUP);

        initModelChain = new TaskChain( taskFactory.newGetNamespacedLists())
            .and(taskFactory.newGetFlavorRules())
            .and(taskFactory.newGetUrlRules())
            .and(taskFactory.newGetWhitelistedStacks())
            .and(taskFactory.newGetStacksWithHostsTask(stacksSupplier))
            .and(taskFactory.newBackupStacksInMemory())
            .and(taskFactory.newValidateAbleToRedirectTask())
            .and(taskFactory.newApplyNewModelTask());
    }

    private void buildRefreshModelChain() {
        Supplier<Set<StackData>> stacksSupplier = new DataSourceStacksSupplier(commonDaoFacade, appName);
        TaskFactory taskFactory = new TaskFactory(DataSource.DATA_STORE);

        refreshModelChain = new TaskChain( new InitDataStoreTask(modelFacade))
            .and(taskFactory.newGetFlavorRules())
            .and(taskFactory.newGetUrlRules())
            .and(taskFactory.newGetWhitelistedStacks())
            .and(taskFactory.newGetNamespacedLists())
            .and(taskFactory.newGetStacksWithHostsTask(stacksSupplier))
            .and(taskFactory.newBackupStacksInMemory())
            .and(taskFactory.newValidateAbleToRedirectTask())
            .and(taskFactory.newApplyNewModelTask());

        if (isStaticDiscoveryNeededForApp.test(appName)) {
            refreshModelChain.and(new TriggerManualBackupTask(stacksBackupManager));
        }

        refreshModelChain.and(new BackupNewModelTask(flavorRulesHolder, whiteListHolder, urlRulesHolder, modelMetadataHolder));
    }

    private void buildServiceDiscoveryReloadChain() {
        Supplier<Set<StackData>> stacksSupplier = new DataSourceStacksSupplier(commonDaoFacade, appName);
        TaskFactory taskFactory = new TaskFactory(DataSource.DATA_STORE);

        serviceDiscoveryReloadChain = new TaskChain(new InitDataStoreTask(modelFacade))
                .and(taskFactory.newGetStacksWithHostsTask(stacksSupplier))
                .and(taskFactory.newBackupStacksInMemory())
                .and(taskFactory.newValidateAbleToRedirectTask())
                .and(taskFactory.newApplyNewModelTask());

        if (isStaticDiscoveryNeededForApp.test(appName)) {
            serviceDiscoveryReloadChain.and(new TriggerManualBackupTask(stacksBackupManager));
        }
    }

    private void buildSyncModelChain() {
        syncModelChain =  new TaskChain(new InitDataStoreTask(modelFacade)).and(new SyncModelsTask(modelMetadataHolder));
    }

    private enum DataSource {
        BACKUP(false, "Backup"),
        DATA_STORE(true, "DataStore");

        private boolean fromDataStore;
        private String label;

        DataSource(boolean fromDataStore, String label) {
            this.fromDataStore = fromDataStore;
            this.label = label;
        }

        public boolean isFromDataStore() {
            return fromDataStore;
        }

        public String getLabel() {
            return label;
        }
    }

    private class TaskFactory {
        private DataSource dataSource;
        private ModelTranslationService modelTranslationService;

        public TaskFactory(DataSource dataSource) {
            this.dataSource = dataSource;
            this.modelTranslationService = new ModelTranslationService(xmlSerializer);
        }

        GetFlavorRulesTask newGetFlavorRules() {
            return new GetFlavorRulesTask(dataSource.isFromDataStore(), flavorRulesHolder, modelTranslationService, namespacedListsHolder);
        }

        GetUrlRulesTask newGetUrlRules() {
            return new GetUrlRulesTask(dataSource.isFromDataStore(), urlRulesHolder, modelTranslationService, namespacedListsHolder);
        }

        GetWhitelistedStacksTask newGetWhitelistedStacks() {
            return new GetWhitelistedStacksTask(dataSource.isFromDataStore(), whiteListHolder, modelTranslationService);
        }

        GetNamespacedListsTask newGetNamespacedLists() {
            return new GetNamespacedListsTask(namespacedListsHolder, dataSource.isFromDataStore());
        }

        ApplyNewModelTask newApplyNewModelTask() {
            return new ApplyNewModelTask(dataSource.isFromDataStore(), redirectorEngineFactory);
        }

        IProcessTask newGetStacksWithHostsTask(Supplier<Set<StackData>> stacksSupplier) {
           return new GetStacksWithHostsTask(stacksSupplier);
        }
        
        IProcessTask newBackupStacksInMemory() {
            return context -> {
                IServiceProviderManager serviceProviderManager = serviceProviderManagerFactory.newStaticServiceProviderManager(context.getStackData());
                StackBackup stackBackup = ServiceProviderUtils.getStackBackup(serviceProviderManager.getAllStackServiceProviders());
                context.setMainStacksBackup(stackBackup);
                return Result.success(context);
            };
        }

        ValidationTask newValidateAbleToRedirectTask() {
            return new ValidationTask("New model can't redirect",
                context -> new CheckIfAbleToRedirect.Builder()
                    .setAppName(appName)
                    .setFlavorRulesModel(context.getFlavorRulesModel())
                    .setWhiteList(context.getWhiteListModel())
                    .setServiceProviderManager(
                        serviceProviderManagerFactory.newStaticServiceProviderManager( context.getStackData() ))
                    .setRedirectorEngineFactory(redirectorEngineFactory)
                    .setMinHosts(appName.equals(XreGuideAppNames.xreGuide.toString()) ? config.getMinHosts() : config.getAppMinHosts()) // TODO: make minHosts property be individual for each app
                    .build().validate());
        }
    }

    @Override
    public IRedirectorEngine refreshModel(int updateVersion) throws Exception { // TODO: put logging and stats out of main logic
        ThreadLocalLogger.setCustomMessage(getMessageForAppending(updateVersion));

        IRedirectorEngine engine = null;
        long startMillis = System.currentTimeMillis();
        log.info("Start updating model from zk - startTime=" + startMillis);
        ValidationReport validationReport = null;
        Result refreshModelResult = null;
        modelMetadataHolder.resetCache(); // TODO: put inside task

        Result syncModelResult = processModelSync(syncModelChain, updateVersion);
        if (syncModelResult.isSuccessful()) {

            refreshModelResult = processModel(refreshModelChain, updateVersion);
            if (refreshModelResult.isSuccessful()) {
                log.info("MODEL UPDATED SUCCESSFULLY");

                engine = refreshModelResult.getRedirectorEngine();
            } else {
                validationReport = refreshModelResult.getValidationReport();
                String errorReason = buildErrorReason("Not updated from Zk. Details: ", validationReport);
                log.error("FAILED TO UPDATE MODEL. REDIRECTOR WILL USE LAST KNOWN GOOD UNTIL NEW GOOD MODEL COMES. Reason: {}", errorReason);

                Validator.ValidationResultType errorType = validationReport == null ? Validator.ValidationResultType.FAILURE : validationReport.getValidationResultType();
                log.error("Failed to refresh model for app={} errorType={} currentVersion={}", appName, errorType, updateVersion);
                throw new Exception("Failed to refresh model for app="+ appName +" currentVersion=" + updateVersion);
            }
        }

        long endMillis = System.currentTimeMillis();
        log.info("End updating model from zk - endTime=" + endMillis + ", total duration=" + (endMillis - startMillis) + " millis");

        return engine;
    }

    @Override
    public IRedirectorEngine reloadStacks(int updateVersion) throws Exception {
        ThreadLocalLogger.setCustomMessage(getMessageForAppending(updateVersion));

        IRedirectorEngine engine = null;
        long startMillis = System.currentTimeMillis();
        log.info("Start updating stacks from zk - startTime=" + startMillis);
        ValidationReport validationReport = null;

        //how do we know that the context is there?
        Result reloadDiscoveryResult = processServiceDiscoveryReload(serviceDiscoveryReloadChain, lastKnownGoodModelContext);
        if (reloadDiscoveryResult.isSuccessful()) {
            log.info("SUCCESSFULLY COMPLETED PROCESS OF SERVICE DISCOVERY RELOAD FOR \"{}\" APP", appName);
            engine = reloadDiscoveryResult.getRedirectorEngine();
        } else {
            validationReport = reloadDiscoveryResult.getValidationReport();
            String errorReason = buildErrorReason("Not updated from Zk. Details: ", validationReport);
            log.error("FAILED RELOAD SERVICE DISCOVERY FOR \"{}\" APP. REDIRECTOR WILL USE LAST KNOWN GOOD UNTIL NEW GOOD STACKS COME. Reason: {}", appName, errorReason);
        }

        if (!reloadDiscoveryResult.isSuccessful()) {
            Validator.ValidationResultType errorType = validationReport == null ? Validator.ValidationResultType.FAILURE : validationReport.getValidationResultType();
            log.error("Failed to refresh model for app={} errorType={} currentVersion={}", appName, errorType, updateVersion);
            throw new Exception("Failed to refresh model for app="+ appName +" currentVersion=" + updateVersion);
        }

        long endMillis = System.currentTimeMillis();
        log.info("Completed service discovery reloading for app: " + appName + " from zk - endTime=" + endMillis + ", total duration=" + (endMillis - startMillis) + " millis");

        return engine;
    }

    @Override
    public IRedirectorEngine initModel() {
        int initialVersion = getModelVersion(IModelHolder.GET_FROM_BACKUP);

        ThreadLocalLogger.setExecutionFlow(ExecutionFlow.initFromBackup);
        ThreadLocalLogger.setCustomMessage(getMessageForAppending(initialVersion));

        modelFacade.setNextModelVersion(initialVersion);
        IRedirectorEngine engine = null;

        Result initModelResult = processModel(initModelChain, initialVersion);
        if (initModelResult.isSuccessful()) {
            log.info("MODEL INITIALIZED SUCCESSFULLY FROM BACKUP");

            engine = initModelResult.getRedirectorEngine();
        } else {
            String errorReason = buildErrorReason("Not initialized from backup. Details: ", initModelResult.getValidationReport());
            log.error("FAILED INIT MODEL FROM BACKUP. Reason: {}. TRY UPDATE FROM ZOOKEEPER", errorReason);

            notifyInitFailListener(errorReason);

            int initialVersionFromDS = getModelVersion(IModelHolder.GET_FROM_DATA_STORE);
            ThreadLocalLogger.setExecutionFlow(ExecutionFlow.initFromDS);
            ThreadLocalLogger.setCustomMessage(getMessageForAppending(initialVersionFromDS));

            Result refreshModelResult = processModel(refreshModelChain, initialVersionFromDS);
            if (refreshModelResult.isSuccessful()) {
                log.info("MODEL INITIALIZED AND UPDATED SUCCESSFULLY");

                engine = refreshModelResult.getRedirectorEngine();
            } else {
                errorReason = buildErrorReason(
                        "Not initialized from Zk. Details: ",
                    refreshModelResult.getValidationReport());
                log.error("FAILED TO INITIALIZE MODEL. REDIRECTOR WON'T BE ABLE TO REDIRECT UNTIL GOOD MODEL COMES. Reason: {}", errorReason);
            }
        }

        Result syncModelResult = processModelSync(syncModelChain);
        if (syncModelResult.isSuccessful()) {
            int initialVersionFromDS = getModelVersion(IModelHolder.GET_FROM_DATA_STORE);

            modelFacade.setNextModelVersion(initialVersionFromDS);
            ThreadLocalLogger.setExecutionFlow(ExecutionFlow.initFromZkToLatestModel);
            ThreadLocalLogger.setCustomMessage(getMessageForAppending(initialVersionFromDS));

            Result refreshModelResult = processModel(refreshModelChain, initialVersionFromDS);
            if (refreshModelResult.isSuccessful()) {
                log.info("INIT MODELS SYNC LEAD TO SUCCESSFUL UPDATE OF MODELS");

                engine = refreshModelResult.getRedirectorEngine();
            } else {
                String errorReason = buildErrorReason(
                        "Not updated on init model sync. Details: ",
                    refreshModelResult.getValidationReport());

                log.info("INIT MODELS SYNC LEAD TO FAILED MODEL UPDATE. Reason: {}", errorReason);
            }
        }

        return engine;
    }

    private int getModelVersion(boolean fromDataStore) {
        return Optional.ofNullable(modelMetadataHolder.load(fromDataStore)).orElse(new ModelMetadata()).getVersion();
    }

    private static String buildErrorReason(String prefix, ValidationReport validationReport) {
        return prefix + (validationReport != null ? validationReport.getMessage() : "");
    }

    private Result processServiceDiscoveryReload(TaskChain taskChain, ModelContext modelContext) {
        if (modelContext == null) {
            return processModelSync(taskChain);
        }
        return taskChain.execute(modelContext);
    }

    private Result processModelSync(TaskChain taskChain) {
        return processModelSync(taskChain, RedirectorConstants.NO_MODEL_NODE_VERSION);
    }

    private Result processModelSync(TaskChain taskChain, int versionToSyncWith) {
        ModelContext modelContext = new ModelContext();
        modelContext.setModelVersion(versionToSyncWith);
        modelContext.setAppName(appName);

        return taskChain.execute(modelContext);
    }

    private Result processModel(TaskChain taskChain, int version) {
        ModelContext modelContext = new ModelContext();
        modelContext.setModelVersion(version);
        modelContext.setAppName(appName);

        Result result = taskChain.execute(modelContext);
        if (result.isSuccessful()) {
            lastKnownGoodModelContext = result.getContext();
        }
        return result;
    }

    @VisibleForTesting
    private void notifyInitFailListener(String message) {
        if (modelFailsInitListener != null) {
            modelFailsInitListener.update("app=" + appName + " " + message);
        }
    }

    private String getMessageForAppending(int modelVersion) {
        return getModelUpdateThreadName(appName) + " rmv=" + modelVersion + " app=" + appName + ".version=" + modelVersion;
    }

}
