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

package it.helper;

import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.applications.Applications;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.dataaccess.EntityCategory;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.tvx.cloud.TestServiceUtil;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import it.context.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static com.comcast.redirector.common.function.Wrappers.unchecked;

public class IntegrationTestHelper {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTestHelper.class);

    private Serializer serializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    private TestContext context;
    private ZKConfig config;
    private IDynamicAppsAwareRedirectorFactory redirectorFactory;
    private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
    private IntegrationTestChangeListener<String> integrationTestStacksReloadListener;
    private IntegrationTestChangeListener<String> modelFailsInitListener;
    private IBackupManagerFactory backupManagerFactory;
    private IBackupManagerFactory globalBackupManagerFactory;
    private IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;
    private DataStoreSupport dataStoreSupport;

    private RedirectorSupport redirectorSupport;
    private Backups backups;
    private DataStore dataStore;

    public void stopDataStore() throws IOException {
        dataStoreSupport.stopZookeeper();
    }

    private boolean isAppNameDefined() {
        return context.getAppName() != null;
    }

    public static class Builder {
        private TestContext context;
        private ZKConfig config;
        private IDynamicAppsAwareRedirectorFactory redirectorFactory;
        private IntegrationTestChangeListener<String> integrationTestModelRefreshListener;
        private IntegrationTestChangeListener<String> integrationTestStacksReloadListener;
        private IBackupManagerFactory backupManagerFactory;
        private IBackupManagerFactory globalBackupManagerFactory;
        private IntegrationTestChangeListener<String> modelFailsInitListener;
        private IntegrationTestChangeListener<StackSnapshot> integrationTestStacksChangeListener;
        private DataStoreSupport dataStoreSupport;

        public Builder context(TestContext input) {
            context = input;
            return this;
        }
        public Builder config(ZKConfig input) {
            config = input;
            return this;
        }
        public Builder redirectorEngine(IDynamicAppsAwareRedirectorFactory input) {
            redirectorFactory = input;
            return this;
        }
        public Builder integrationTestModelRefreshListener(IntegrationTestChangeListener<String> input) {
            integrationTestModelRefreshListener = input;
            return this;
        }
        public Builder backupManagerFactory(IBackupManagerFactory input) {
            backupManagerFactory = input;
            return this;
        }
        public Builder globalBackupManagerFactory(IBackupManagerFactory input) {
            globalBackupManagerFactory = input;
            return this;
        }

        public Builder modelFailsInitListener(IntegrationTestChangeListener<String> input) {
            modelFailsInitListener = input;
            return this;
        }

        public Builder integrationTestStacksChangeListener(IntegrationTestChangeListener<StackSnapshot> input) {
            integrationTestStacksChangeListener = input;
            return this;
        }

        public Builder integrationTestStacksReloadListener(IntegrationTestChangeListener<String> input) {
            integrationTestStacksReloadListener = input;
            return this;
        }

        public Builder dataStoreSupport(DataStoreSupport input) {
            dataStoreSupport = input;
            return this;
        }

        public IntegrationTestHelper build() {
            return new IntegrationTestHelper(this);
        }

        public DataSourceBasedIntegrationHelperBuilder buildDataSourceBasedIntegrationHelper() {
            return new DataSourceBasedIntegrationHelperBuilder(new IntegrationTestHelper(this));
        }

        public SimpleDataSourceBasedIntegrationHelperBuilder buildSimpleDataSourceBasedIntegrationHelper() {
            return new SimpleDataSourceBasedIntegrationHelperBuilder(new IntegrationTestHelper(this));
        }

        public AlreadyRunningDataSourceBasedHelperBuilder buildHelperForAlreadyRunningDataStore() {
            return new AlreadyRunningDataSourceBasedHelperBuilder(new IntegrationTestHelper(this));
        }

        public BackupBasedIntegrationHelperBuilder buildBackupBasedIntegrationHelper() {
            return new BackupBasedIntegrationHelperBuilder(new IntegrationTestHelper(this));
        }

        public BackupBasedFailingIntegrationHelperBuilder buildBackupBasedFailingIntegrationHelper() {
            return new BackupBasedFailingIntegrationHelperBuilder(new IntegrationTestHelper(this));
        }
    }

    public static class DataSourceBasedIntegrationHelperBuilder {
        IntegrationTestHelper delegate;

        DataSourceBasedIntegrationHelperBuilder(IntegrationTestHelper delegate) {
            this.delegate = delegate;
        }

        public IntegrationTestHelper startDataStoreAndSetupModel() {
            delegate.startDataSourceAndUpdateModel();
            return delegate;
        }
    }

    public static class SimpleDataSourceBasedIntegrationHelperBuilder {
        IntegrationTestHelper delegate;

        SimpleDataSourceBasedIntegrationHelperBuilder(IntegrationTestHelper delegate) {
            this.delegate = delegate;
        }

        public IntegrationTestHelper startDataStore() {
            delegate.startDataStore();
            return delegate;
        }
    }

    public static class AlreadyRunningDataSourceBasedHelperBuilder {
        IntegrationTestHelper delegate;

        AlreadyRunningDataSourceBasedHelperBuilder(IntegrationTestHelper delegate) {
            this.delegate = delegate;
        }

        public IntegrationTestHelper setupEnv() {
            return setupEnv(1100);
        }

        public IntegrationTestHelper setupEnv(int discoveryTimeout) {
            delegate.setupEnvForContext();
            return delegate;
        }
    }

    public static class BackupBasedIntegrationHelperBuilder {
        IntegrationTestHelper delegate;

        BackupBasedIntegrationHelperBuilder(IntegrationTestHelper delegate) {
            this.delegate = delegate;
        }

        public IntegrationTestHelper setupEnvironmentAndModel() {
            delegate.initEnvInBackupsAndInitModel();
            return delegate;
        }
    }

    public static class BackupBasedFailingIntegrationHelperBuilder {
        IntegrationTestHelper delegate;

        BackupBasedFailingIntegrationHelperBuilder(IntegrationTestHelper delegate) {
            this.delegate = delegate;
        }

        public IntegrationTestHelper initRedirectorWithFailure() {
            delegate.initRedirectorWithFailure();
            return delegate;
        }
    }

    public IntegrationTestHelper(Builder builder) {
        context = builder.context;
        config = builder.config;
        redirectorFactory = builder.redirectorFactory;
        integrationTestModelRefreshListener = builder.integrationTestModelRefreshListener;
        integrationTestStacksReloadListener = builder.integrationTestStacksReloadListener;
        backupManagerFactory = builder.backupManagerFactory;
        globalBackupManagerFactory = builder.globalBackupManagerFactory;
        modelFailsInitListener = builder.modelFailsInitListener;
        integrationTestStacksChangeListener = builder.integrationTestStacksChangeListener;
        dataStoreSupport = builder.dataStoreSupport;
        if (dataStoreSupport == null) {
            dataStoreSupport = new DataStoreSupport(config);
        }

        redirectorSupport = new RedirectorSupport();
        backups = new Backups();
        dataStore = new DataStore(dataStoreSupport);
    }

    private void startDataSourceAndUpdateModel() {
        try {
            dataStoreSupport.startZookeeper();
            dataStore.setupEnvForContext();
            redirectorSupport.triggerModelUpdate();
        } catch (Exception e) {
            log.error("Failed to start data source and update model", e);
        }
    }

    private void setupEnvForContext() {
        try {
            dataStore.setupEnvForContext();
        } catch (Exception e) {
            log.error("Failed to setup environment in data store", e);
        }
    }

    private void startDataStore() {
        try {
            dataStoreSupport.startZookeeper();
            setupEnvForContext();
            Thread.sleep(1100); // TODO: get rid of it. So far added since there are issues when reading namespaced list from ZK
        } catch (Exception e) {
            log.error("Failed to start data store", e);
        }
    }

    private void initEnvInBackupsAndInitModel() {
        try {
            backups.setupBackupEnvForContext();
            redirectorSupport.initRedirector();
        } catch (Exception e) {
            log.error("Failed to fill redirector backups and init model", e);
        }
    }

    private void initRedirectorWithFailure() {
        try {
            backups.setupBackupEnvForContext();
            redirectorSupport.initRedirectorFails(2);
        } catch (Exception e) {
            log.error("Failed to fill redirector backups and init model", e);
        }
    }

    public class RedirectorSupport {

        public void triggerModelUpdate() throws Exception {
            makeRedirectorStartLookingForApps();
            dataStore.simulateStacksChangedEventFromWS();
            CountDownLatch latch = new CountDownLatch(1);
            integrationTestModelRefreshListener.setCallback(event -> {
                log.info("TriggerModelUpdate event: type={}, data={}", event.getType(), event.getData());
                if (event.getData().startsWith("app=" + context.getAppName())) {
                    latch.countDown();
                    try {
                        TimeUnit.SECONDS.sleep(config.getModelPollIntervalSeconds());
                    } catch (InterruptedException e) {
                        log.info("TriggerModelUpdate event is interrupted in time processing");
                    }
                    log.info("TriggerModelUpdate event is processed");
                }
            });
            redirectorFactory.createRedirector(context.getAppName());
            dataStore.triggerModelUpdated();
            latch.await(30, TimeUnit.SECONDS);
            log.info("Redirector updated model for app " + context.getAppName() + " latch=" + latch.getCount());
        }

        public void triggerStacksReload() throws Exception {
            makeRedirectorStartLookingForApps();
            CountDownLatch latch = new CountDownLatch(1);
            integrationTestStacksReloadListener.setCallback(event -> {
                log.info("TriggerStacksReload event: type={}, data={}", event.getType(), event.getData());
                if (event.getData().startsWith("app=" + context.getAppName())) {
                    latch.countDown();
                    try {
                        TimeUnit.SECONDS.sleep(config.getModelPollIntervalSeconds());
                    } catch (InterruptedException e) {
                        log.info("TriggerStacksReload event is interrupted in time processing");
                    }
                    log.info("TriggerStacksReload event is processed");
                }
            });
            redirectorFactory.createRedirector(context.getAppName());
            dataStore.triggerStacksReload();
            latch.await(30, TimeUnit.SECONDS);
            log.info("Redirector reloaded stacks for app " + context.getAppName());
        }

        public void initRedirector() throws Exception {
            initRedirector(30);
        }

        public void initRedirector(int waitSeconds) throws Exception {
            makeRedirectorStartLookingForApps();
            CountDownLatch latch = new CountDownLatch(1);
            integrationTestModelRefreshListener.setCallback(event -> {
                log.info("initRedirector event: type={}, data={}", event.getType(), event.getData());
                if (event.getData().startsWith("app=" + context.getAppName())) {
                    latch.countDown();
                    log.info("initRedirector event is processed");
                }
            });
            redirectorFactory.createRedirector(context.getAppName());
            latch.await(waitSeconds, TimeUnit.SECONDS);
            log.info("Redirector initialized for app: " + context.getAppName());
        }

        private void makeRedirectorStartLookingForApps() {
            redirectorFactory.startLookingForAppsChanges();
        }

        void initRedirectorFails(int waitTimeInSeconds) throws Exception {
            makeRedirectorStartLookingForApps();
            CountDownLatch latch = new CountDownLatch(1);
            modelFailsInitListener.setCallback(event -> {
                log.info("Init fail event is coming {}", event.getData());
                if (event.getData().startsWith("app=" + context.getAppName())) {
                    latch.countDown();
                }
            });
            redirectorFactory.createRedirector(context.getAppName());
            latch.await(waitTimeInSeconds, TimeUnit.SECONDS);
            log.info("Redirector init failed as expected");
        }

        public InstanceInfo redirect() {
            return redirect(Collections.emptyMap());
        }

        public InstanceInfo redirect(Map<String, String> params) {
            return redirectorFactory.createRedirector(context.getAppName()).redirect(params);
        }
    }

    public RedirectorSupport getRedirectorSupport() {
        return redirectorSupport;
    }

    public TestContext getContext() {
        return context;
    }

    public class Backups {

        void backupHosts() throws SerializerException {
            StackBackup backup = getStackBackup();

            if (context.isDynamic()) {
                backupHostsDynamic(backup);
            } else {
                backupHostsStatic(backup);
            }
        }

        void forceBackupHostsStatic() throws SerializerException {
            backupHostsStatic(getStackBackup());
        }

        public void forceBackupHostsDynamic(Predicate<TestContext.Host> hostsPredicate) throws SerializerException {
            backupHostsDynamic(getStackBackup(hostsPredicate));
        }

        public void forceBackupHostsStatic(Predicate<TestContext.Host> hostsPredicate) throws SerializerException {
            backupHostsStatic(getStackBackup(hostsPredicate));
        }

        StackBackup getStackBackup() {
            return getStackBackup(host -> true);
        }

        private StackBackup getStackBackup(Predicate<TestContext.Host> hostsPredicate) {
            return new StackBackup(1,
                context.getHosts().stream()
                    .filter(hostsPredicate)
                    .map(host -> new StackSnapshot(host.getPath(), Collections.singletonList(new StackSnapshot.Host(host.getIpv4(), host.getIpv6()))))
                    .collect(Collectors.toList()));
        }

        void backupRules() throws SerializerException {
            SelectServer selectServer = new SelectServer();
            if (context.getFlavorRule() != null) {
                selectServer.setItems(Collections.singletonList(context.getFlavorRule().value()));
            }

            if (context.getDistribution() != null) {
                Distribution distribution = context.getDistribution().value();
                if (context.getDefaultServer() != null) {
                    distribution.setDefaultServer(context.getDefaultServer().value());
                }
                selectServer.setDistribution(distribution);
            }

            backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.FLAVOR_RULES)
                .backup(serializer.serialize(selectServer, true));
        }

        void backupUrlRules() throws SerializerException {
            URLRules urlRules = new URLRules();
            UrlRule defaultUrlParams = context.getDefaultUrlParams().value();
            Default defaultStatement = new Default();
            defaultStatement.setUrlRule(defaultUrlParams);
            if (context.getUrlRule() != null) {
                urlRules.setItems(Collections.singletonList(context.getUrlRule().value()));
            }
            urlRules.setDefaultStatement(defaultStatement);

            backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.URL_RULES)
                .backup(serializer.serialize(urlRules, true));
        }

        void backupWhitelist() throws SerializerException {
            if (context.getWhitelist() != null) {
                backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.WHITE_LIST)
                    .backup(serializer.serialize(context.getWhitelist().value(), true));
            }
        }

        private void backupVersion() throws SerializerException {
            ModelMetadata metadata = new ModelMetadata();
            metadata.setVersion(context.getVersion());
            backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.MODEL_METADATA)
                .backup(new JsonSerializer().serialize(metadata, true));

        }

        void backupNamespacedList() throws SerializerException {
            if (context.getNamespacedLists() == null)
                return;

            NamespacedListsBatch batch = new NamespacedListsBatch();
            for (TestNamespacedList list : context.getNamespacedLists()) {
                //using getRet (deprecated) because TestNamespacedList is marshalled directly to zooKeeper.
                batch.addValues(list.getName(), list.value().getRet().stream().map(Value::getValue).collect(Collectors.toList()));
            }
            backupNamespacedList(batch);
        }

        public void backupNamespacedList(NamespacedListsBatch batch) throws SerializerException {
            Serializer serializer = new JsonSerializer();
            globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS)
                .backup(serializer.serialize(batch, false));
        }

        void backupApplications() {
            globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.APPLICATIONS)
                .backup(new JsonSerializer().serialize(context.getApplications().value(), true));
        }

        public void setupBackupEnvForContext() throws SerializerException {
            setupBackupEnvForContext(false);
        }

        public void setupBackupEnvForContext(boolean forceBackupHostsStatic) throws SerializerException {
            if (forceBackupHostsStatic) {
                forceBackupHostsStatic();
            } else {
                backupHosts();
            }
            setupModelBackupForContext();
        }

        public void setupModelBackupForContext() throws SerializerException {
            backupRules();
            backupUrlRules();
            backupWhitelist();
            backupNamespacedList();
            backupApplications();
            backupVersion();
        }

        private void backupHostsDynamic(StackBackup backup) throws SerializerException {
            Serializer serializer = new JsonSerializer();
            globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.DISCOVERY)
                .backup(serializer.serialize(backup, false));
        }

        private void backupHostsStatic(StackBackup backup) throws SerializerException {
            Serializer serializer = new JsonSerializer();
            backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.STACKS_MANUAL)
                .backup(serializer.serialize(backup, false));
        }

        public StackBackup getDynamicHosts() throws SerializerException {
            String data = globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.DISCOVERY).load();
            return new JsonSerializer().deserialize(data, StackBackup.class);
        }

        public SelectServer getFlavorRules() throws SerializerException {
            String data = backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.FLAVOR_RULES).load();
            return serializer.deserialize(data, SelectServer.class);
        }

        public Whitelisted getWhitelist() throws SerializerException {
            String data = backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.WHITE_LIST).load();
            return serializer.deserialize(data, Whitelisted.class);
        }

        public NamespacedListsBatch getNamespacedLists() throws SerializerException {
            String data = globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS).load();
            return new JsonSerializer().deserialize(data, NamespacedListsBatch.class);
        }

        public Applications getApplications() {
            String data = globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.APPLICATIONS).load();
            return new JsonSerializer().deserialize(data, Applications.class);
        }
    }

    public Backups getBackups() {
        return backups;
    }

    public class DataStore {
        private DataStoreSupport dataStoreSupport;

        DataStore(DataStoreSupport dataStoreSupport) {
            this.dataStoreSupport = dataStoreSupport;
        }

        void registerHosts() throws Exception {
            context.getHosts().forEach(unchecked(this::registerHost));
        }

        private void registerHost(TestContext.Host host) throws Exception {
            log.info("Registering host {}", host);
            TestServiceUtil serviceUtil = new TestServiceUtil(dataStoreSupport.getStartedConnector(),
                PathHelper.getPathHelper(EntityCategory.GLOBAL, config.getZooKeeperBasePath()).getPath(EntityType.STACK));
            serviceUtil.registerServiceInstance(host.getDataCenter(), host.getStack(), host.getFlavor(), host.getAppName(), 0, host.getIpv4(), host.getIpv6(), host.getWeight());
        }

        public void deRegisterHost(TestContext.Host host) throws Exception {
            log.info("DE-Registering host {}", host);
            TestServiceUtil serviceUtil = new TestServiceUtil(dataStoreSupport.getStartedConnector(),
                PathHelper.getPathHelper(EntityCategory.GLOBAL, config.getZooKeeperBasePath()).getPath(EntityType.STACK));
            serviceUtil.deRegisterServiceInstance(host.getDataCenter(), host.getStack(), host.getFlavor(), host.getAppName(), 0, host.getIpv4());
        }

        private void putRules() throws Exception {
            log.info("Put rules for app={}", context.getAppName());

            IDataSourceConnector connector = dataStoreSupport.getStartedConnector();
            IPathHelper helper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZooKeeperBasePath());

            if (context.getFlavorRule() != null) {
                TestFlavorRule rule = context.getFlavorRule();
                String path = helper.getPathByService(context.getAppName(), EntityType.RULE, rule.getId());
                connector.save(rule.toString(), path);
            }

            if (context.getDistribution() != null) {
                connector.save(
                    context.getDistribution().toString(),
                    helper.getPathByService(context.getAppName(), EntityType.DISTRIBUTION));
            }

            if (context.getDefaultServer() != null) {
                connector.save(
                    context.getDefaultServer().toString(),
                    helper.getPathByService(context.getAppName(), EntityType.SERVER, RedirectorConstants.DEFAULT_SERVER_NAME));
            }
        }

        public void putNamespacedList() throws Exception {
            // TODO: this method adds or overwrites zNode in zookeeper but doesn't remove previously existing nodes. So cleanup actions should be added
            log.info("Put namespacedList for app={}", context.getAppName());

            if (context.getNamespacedLists() != null) {
                for (TestNamespacedList list : context.getNamespacedLists()) {
                    String path = PathHelper.getPathHelper(EntityCategory.GLOBAL, config.getZooKeeperBasePath()).getPath(EntityType.NAMESPACED_LIST, list.getName());
                    dataStoreSupport.getStartedConnector().saveCompressed(list.toString(), path);
                    bumpNamespacedListVersion();
                }
            }
        }

        public void bumpNamespacedListVersion() throws Exception {
            log.info("bump namespacedList version ");

            dataStoreSupport.getStartedConnector().save("", PathHelper.getPathHelper(EntityType.NAMESPACED_LIST, config.getZooKeeperBasePath()).getPath());
        }

        private void putUrlRules() throws Exception {
            log.info("Put Url rules for app={}", context.getAppName());

            IDataSourceConnector connector = dataStoreSupport.getStartedConnector();
            IPathHelper helper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZooKeeperBasePath());

            if (context.getUrlRule() != null) {
                TestUrlRule urlRule = context.getUrlRule();
                String path = helper.getPathByService(context.getAppName(), EntityType.URL_RULE, urlRule.getId());

                connector.save(urlRule.toString(), path);
            }

            if (context.getDefaultUrlParams() != null) {
                connector.save(
                    context.getDefaultUrlParams().toString(),
                    helper.getPathByService(context.getAppName(), EntityType.URL_PARAMS, RedirectorConstants.DEFAULT_URL_RULE));
            }
        }

        public void putWhitelist() throws Exception {
            log.info("Put whitelist for app={}", context.getAppName());

            if (context.getWhitelist() == null) {
                return;
            }
            IPathHelper helper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZooKeeperBasePath());
            dataStoreSupport.getStartedConnector().save(
                context.getWhitelist().toString(),
                helper.getPathByService(context.getAppName(), EntityType.WHITELIST));
        }

        public void setupEnvForContext() throws Exception {
            createBasePath();
            createRedirectorConfig();
            registerHosts();
            createEmptyStacks();
            createStacksWithData();
            simulateStacksChangedEventFromWS();
            createRedirectorInstances();
            putNamespacedList();

            if (isAppNameDefined()) {
                putRules();
                putUrlRules();
                putWhitelist();
            }
        }

        void createEmptyStacks() throws Exception {
            context.getEmptyStacks().forEach(unchecked(this::createEmptyStack));
        }

        private void createEmptyStack(EmptyStack emptyStack) throws Exception {
            log.info("Creating empty stack {}", emptyStack);
            final String path = getStackFullPath(emptyStack);

            if (! dataStoreSupport.getStartedConnector().isPathExists(path)) {
                dataStoreSupport.getStartedConnector().createEphemeral(path);
            } else {
                log.warn("Node " + path + " already exists");
            }
        }

        private String getStackFullPath(EmptyStack stack) {
            return PathHelper.getPathHelper(EntityType.STACK, config.getZooKeeperBasePath()).getPath() + stack.getPath();
        }

        public void createStacksWithData() throws Exception {
            context.getStackWithData().forEach(unchecked(this::createStackWithData));
        }

        public void createStackWithData(StackWithData stackWithData) throws Exception {
            log.info("Creating stack with data {}", stackWithData);
            final String path = getStackFullPath(stackWithData);
            if (!dataStoreSupport.getStartedConnector().isPathExists(path)) {
                dataStoreSupport.getStartedConnector().createEphemeral(path);
                dataStoreSupport.getStartedConnector().save(stackWithData.getData(), path);
            } else {
                log.warn("Node " + path + " already exists");
            }
        }

        private void createRedirectorInstances() throws Exception {
            context.getRedirectorInstances().forEach(unchecked(this::createRedirectorInstance));
        }

        private void createRedirectorInstance(RedirectorInstance redirectorInstance) throws Exception {
            log.info("Creating redirector instance {}", redirectorInstance);
            String rootPath = PathHelper.getPathHelper(EntityType.INSTANCES, config.getZooKeeperBasePath()).getPath();
            String path = rootPath + DELIMETER + redirectorInstance.getApp() + DELIMETER + redirectorInstance.getInstance();

            if (!dataStoreSupport.getStartedConnector().isPathExists(path)) {
                dataStoreSupport.getStartedConnector().createEphemeral(path);
                dataStoreSupport.getStartedConnector().save(redirectorInstance.getNodeData(), path);
            }
        }

        private void createBasePath() throws Exception {
            if ("".equals(config.getZooKeeperBasePath()))
                return;

            dataStoreSupport.getStartedConnector().save("", config.getZooKeeperBasePath());
        }

        private void createRedirectorConfig() throws Exception {
            IPathHelper helper = PathHelper.getPathHelper(EntityType.CONFIG, config.getZooKeeperBasePath());

            RedirectorConfig redirectorConfig = new RedirectorConfig();
            redirectorConfig.setMinHosts(1);
            redirectorConfig.setAppMinHosts(1);

            dataStoreSupport.getStartedConnector().save(serializer.serialize(redirectorConfig), helper.getPath());
        }

        public void registerNewStaticHost(TestContext.Host host) throws Exception {
            context.addHost(host);
            registerHost(host);
            simulateStacksChangedEventFromWS();
        }

        public void registerNewDynamicHost(TestContext.Host host) throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            integrationTestStacksChangeListener.setCallback(event -> {
                if (event.getData().getPath().startsWith("/" + host.getDataCenter() + "/" + host.getStack())) {
                    log.info("Stack changes applied for /{}/{}", host.getDataCenter(), host.getStack());
                    latch.countDown();
                }
            });
            context.addHost(host);
            registerHost(host);
            simulateStacksChangedEventFromWS();
            latch.await(config.getZooKeeperConnectionTimeout(), TimeUnit.SECONDS);
            // TODO: better control dynamic discovery mechanism and be able to call listener from appropriate page
            // So far we call listener from place very close to discovery cache update that's why sometimes listener
            // is called before new discovery info is actually available. So far adding 1 second wait to give cache time to update by 1 host
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void deRegisterDynamicHost(TestContext.Host host) throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            // TODO: this is not reliable since deleted host is absent in stacks snapshot. Need to come up with more reliable approach
            integrationTestStacksChangeListener.setCallback(event -> latch.countDown());
            deRegisterHost(host);
            latch.await();
        }

        public void triggerModelUpdated() throws Exception {
            log.info("Triggering model update for app={}", context.getAppName());
            IPathHelper helper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZooKeeperBasePath());
            String nodePath = helper.getPathByService(context.getAppName(), EntityType.MODEL_CHANGED);

            dataStoreSupport.getStartedConnector().save("", nodePath);
        }

        public void triggerStacksReload() throws Exception {
            log.info("Triggering stacks reload for app={}", context.getAppName());
            IPathHelper helper = PathHelper.getPathHelper(EntityCategory.REDIRECTOR, config.getZooKeeperBasePath());
            String nodePath = helper.getPathByService(context.getAppName(), EntityType.STACKS_RELOAD);

            dataStoreSupport.getStartedConnector().save("", nodePath);
        }

        public void simulateStacksChangedEventFromWS() throws Exception {
            log.info("Simulating stacks changed event from WS for app={}", context.getAppName());

            String nodePath = PathHelper.getPathHelper(EntityType.SERVICES_CHANGED, config.getZooKeeperBasePath())
                .getPath(EntityType.SERVICES_CHANGED);

            dataStoreSupport.getStartedConnector().save("", nodePath);
        }

        public List<String> getServiceAvailabilityInfo() throws Exception {
            String path = PathHelper.getPathHelper(EntityType.INSTANCES, config.getZooKeeperBasePath())
                .getPathByService(context.getAppName());
            return dataStoreSupport.getStartedConnector().getChildren(path);
        }
    }

    public DataStore getDataStore() {
        return dataStore;
    }

}
