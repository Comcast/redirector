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

package com.comcast.redirector.api.redirectorOffline;

import com.comcast.redirector.api.config.RedirectorConfig;
import com.comcast.redirector.api.model.AppNames;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.api.redirector.service.IAppsService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JSONSerializer;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.common.thread.ThreadUtils;
import com.comcast.redirector.core.modelupdate.NewVersionHandler;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.stacks.DataSourceStacksSupplier;
import com.comcast.redirector.core.balancer.util.ServiceProviderUtils;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.cache.ICacheListener;
import com.comcast.redirector.dataaccess.cache.IDataListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.dao.IListDAO;
import com.comcast.redirector.dataaccess.dao.IRegisteringDAOFactory;
import com.comcast.redirector.dataaccess.dao.ISimpleDAO;
import com.comcast.redirector.dataaccess.dao.IStacksDAO;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.comcast.redirector.api.redirectorOffline.SnapshotFilesPathHelper.SnapshotEntity.*;

@Component
public class SnapshotManager implements IDataSourceConnector.ConnectionListener {

    private static Logger log = LoggerFactory.getLogger(SnapshotManager.class);

    private Executor executor = Executors.newSingleThreadExecutor(ThreadUtils.newThreadFactory("SnapshotManager"));

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(ThreadUtils.newThreadFactory("SnapshotManagerStackSaver"));

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    private IListDAO<NamespacedList> namespacedListDAO;

    @Autowired
    ISimpleDAO<com.comcast.redirector.api.model.RedirectorConfig> redirectorConfigDAO; // TODO: avoid fully-qualified names

    @Autowired
    private IStacksDAO stacksDAO;

    @Autowired
    private IRegisteringDAOFactory daoFactory;

    @Autowired
    private IAppsService applicationsService;

    @Autowired
    private OfflineRedirectorHelper offlineRedirectorHelper;

    @Autowired
    private RedirectorConfig redirectorConfig;

    @Autowired
    private IFileSystemHelper fileSystemHelper;

    private DataListenersFacade dataListenersFacade;
    
    @Autowired
    private Serializer jsonSerializer;

    private Map<String, SnapshotFileManager> appToFileManagerMap = new HashMap<>();
    private Map<String, ICacheListener> appToListenerMap = new HashMap<>();

    private ICommonModelFacade stacksDaoToCommonDaoFacadeAdapter;

    @PostConstruct
    public void init() throws IOException, SerializerException {
        dataListenersFacade = new DataListenersFacade(connector);
        initModelFacade();
        initFileManagers();
        initListeners();
        connector.addConnectionListener(this);
    }

    private void initModelFacade() {
        stacksDaoToCommonDaoFacadeAdapter = new ICommonModelFacade() {
            @Override
            public Set<String> getAllRegisteredApps() {
                return stacksDAO.getAllAppNamesRegisteredInStacks();
            }

            @Override
            public Set<XreStackPath> getAllStackPaths() {
                return stacksDAO.getAllStackPaths();
            }

            @Override
            public Set<XreStackPath> getAllStackPaths(Set<String> excludedApps) {
                return stacksDAO.getAllStackPaths(excludedApps);
            }

            @Override
            public Collection<HostIPs> getHosts(XreStackPath path) {
                return stacksDAO.getHosts(path);
            }

            @Override
            public Collection<NamespacedList> getAllNamespacedLists() {
                return null;
            }

            @Override
            public NamespacedList getNamespacedList(String namespacedListName) {
                return null;
            }

            @Override
            public int getNamespacedListsVersion() {
                return 0;
            }

            @Override
            public Integer getNextNamespacedListsVersion() {
                return 0;
            }

            @Override
            public void initNamespacedListDataChangePolling(NewVersionHandler<Integer> refreshNamespacedLists) {
                //why do we need it?
            }

            @Override
            public com.comcast.redirector.api.model.RedirectorConfig getRedirectorConfig() {
                return null;
            }

            @Override
            public void saveRedirectorConfig(com.comcast.redirector.api.model.RedirectorConfig config) {
            }

            @Override
            public void start() throws Exception {}

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public List<String> getApplications() {
                return null;
            }
    
            @Override
            public Boolean isValidModelForAppExists(String appName) {
                return true;
            }
        };
    }

    private void initListeners() {
        if (connector.isConnected()) {
            for (String serviceName : applicationsService.getAppNames().getAppNames()) {
                registerAppModelChangesListener(serviceName);
            }
            registerNamespaceChangeListener();
            registerNewApplicationsObserver();
            registerSettingsListener();
        }
    }

    private void initFileManagers() throws IOException, SerializerException {
        Set<String> applicationNames = Collections.emptySet();

        if (connector.isConnected()) {
            applicationNames = applicationsService.getAppNames().getAppNames();
        }
        else {
            log.info("Zookeeper currently is unavailable. Loading list of application from snapshot.");
            Path appNamesFile = Paths.get(redirectorConfig.getSnapshotBasePath()).resolve("applications.json");
            if (Files.exists(appNamesFile)) {
                byte[] apps = Files.readAllBytes(appNamesFile);
                AppNames appNames = jsonSerializer.deserialize(apps, AppNames.class);
                applicationNames = appNames.getAppNames();
            }
        }

        for (String serviceName: applicationNames) {
            initApplictionFileManager(serviceName);
        }

        initNamespaceFileManager();
        initAppNamesFileManager();
        initConfigFileManager();

    }

    private SnapshotFileManager initApplictionFileManager(String serviceName) {
        if(!appToFileManagerMap.containsKey(serviceName)) {
            SnapshotFileManager fileManager = new SnapshotFileManager(new SnapshotFilesPathHelper(serviceName, redirectorConfig.getSnapshotBasePath()), MODEL, fileSystemHelper);
            appToFileManagerMap.put(serviceName, fileManager);
        }
        return appToFileManagerMap.get(serviceName);
    }

    private SnapshotFileManager initAppNamesFileManager() {
        if(!appToFileManagerMap.containsKey(APPLICATIONS.toString())) {
            SnapshotFileManager fileManager = new SnapshotFileManager(new SnapshotFilesPathHelper(redirectorConfig.getSnapshotBasePath()), APPLICATIONS, fileSystemHelper);
            appToFileManagerMap.put(APPLICATIONS.toString(), fileManager);
        }
        return appToFileManagerMap.get(APPLICATIONS.toString());
    }

    private SnapshotFileManager initNamespaceFileManager() {
        if(!appToFileManagerMap.containsKey(NAMESPACED_LISTS.toString())) {
            SnapshotFileManager fileManager = new SnapshotFileManager(new SnapshotFilesPathHelper(redirectorConfig.getSnapshotBasePath()), NAMESPACED_LISTS, fileSystemHelper);
            appToFileManagerMap.put(NAMESPACED_LISTS.toString(), fileManager);
        }
        return appToFileManagerMap.get(NAMESPACED_LISTS.toString());
    }

    private SnapshotFileManager initConfigFileManager() {
        if(!appToFileManagerMap.containsKey(CONFIG.toString())) {
            SnapshotFileManager fileManager = new SnapshotFileManager(new SnapshotFilesPathHelper(redirectorConfig.getSnapshotBasePath()), CONFIG, fileSystemHelper);
            appToFileManagerMap.put(CONFIG.toString(), fileManager);
        }
        return appToFileManagerMap.get(CONFIG.toString());
    }

    private void registerAppModelChangesListener(String serviceName) {
        if (!appToListenerMap.containsKey(serviceName)) {
            log.info("Registering model change listener for {} application", serviceName);

            DataSourceStacksSupplier stacksObserver = new DataSourceStacksSupplier(stacksDaoToCommonDaoFacadeAdapter, serviceName);
            ApplicationModelReloadListener listener = new ApplicationModelReloadListener(serviceName, initApplictionFileManager(serviceName), stacksObserver);

            dataListenersFacade.addModelChangedListener(listener, serviceName);
            stacksDAO.addCacheListener(listener);

            appToListenerMap.put(serviceName, listener);
            log.info("Creating initial snapshot for {} application", serviceName);
            listener.createSnapshot();
        }
    }

    private void registerNamespaceChangeListener() {
        if (!appToListenerMap.containsKey(NAMESPACED_LISTS.toString())) {
            log.info("Registering listener for Namespaced List changes");
            NamespaceChangeListener namespaceChangeListener = new NamespaceChangeListener(initNamespaceFileManager());
            namespacedListDAO.addCacheListener(namespaceChangeListener);
            appToListenerMap.put(NAMESPACED_LISTS.toString(), namespaceChangeListener);
            log.info("Creating initial snapshot of Namespaced List");
            // create initial namespace model snapshot
            namespaceChangeListener.createSnapshot();
        }
    }

    private void registerSettingsListener() {
        if (!appToListenerMap.containsKey(CONFIG.toString())) {
            log.info("Registering listener for application settings changes");
            ConfigChangeListener configChangeListener = new ConfigChangeListener(initConfigFileManager());
            redirectorConfigDAO.addCacheListener(configChangeListener);
            appToListenerMap.put(CONFIG.toString(), configChangeListener);
            log.info("Creating initial snapshot of application settings");
            // create initial application settings snapshot
            configChangeListener.createSnapshot();
        }
    }

    private void registerNewApplicationsObserver() {
        if (!appToListenerMap.containsKey(APPLICATIONS.toString())) {
            log.info("Registering listener for Stacks & Flavor changes");
            ApplicationsListener applicationsObserver = new ApplicationsListener(initAppNamesFileManager());
            stacksDAO.addCacheListener(applicationsObserver);
            appToListenerMap.put(APPLICATIONS.toString(), applicationsObserver);
            log.info("Creating initial snapshot of Application Names");
            applicationsObserver.backupApplicationsList();
        }
    }

    public byte[] getSnapshot (String key) throws IOException {

        byte[] snapshotByteArray = new byte[0];
        if (appToFileManagerMap.containsKey(key)) {
            snapshotByteArray = appToFileManagerMap.get(key).getSnapshot();
        }
        return snapshotByteArray;
    }

    @Override
    public void stateChanged(IDataSourceConnector.ConnectorState newState) {
        log.info("Zookeeper connection state changed to: {}", newState.toString());
        if (connector.isConnected()) {
            try {
                initListeners();
            } catch (RedirectorDataSourceException e) {
                log.error("Failed to initialize ZK model change listeners after ZK recovered: {}", e.getMessage());
            }
        }
    }

    private class ApplicationModelReloadListener implements IDataListener, ICacheListener {

        private String applicationName;
        private SnapshotFileManager fileManager;
        private Supplier<Set<StackData>> stacksSupplier;
        private Serializer stackBackupSerializer;

        private volatile boolean snapshotNeeded = false;

        private ApplicationModelReloadListener(String applicationName, SnapshotFileManager fileManager, Supplier<Set<StackData>> stacksSupplier) {
            this.applicationName = applicationName;
            this.fileManager = fileManager;
            this.stacksSupplier = stacksSupplier;
            this.stackBackupSerializer = new JsonSerializer();

            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    if (snapshotNeeded) {
                        log.info("About to create snapshot for app={}", applicationName);

                        createSnapshot();
                        snapshotNeeded = false;
                    }
                } catch (Exception e) {
                    log.error("Failed to create snapshot for app=" + applicationName, e);
                }
            }, 0, redirectorConfig.getStacksSnapshotRateInSeconds(), TimeUnit.SECONDS);
        }

        @Override
        public void onEvent(EventType eventType, String path, byte[] data, int updatedVersion) {
            createSnapshot();
        }

        @Override
        public void onChanged() {
        }

        @Override
        public void onChanged(TreeCacheEvent event) {
            ChildData data = event.getData();
            if (data == null) {
                return;
            }

            String path = data.getPath();
            switch (event.getType()) {
                case NODE_ADDED:
                case NODE_REMOVED:
                case NODE_UPDATED: {
                    boolean isFullHostPath = StringUtils.isNotBlank(path) && path.split(RedirectorConstants.DELIMETER).length == 7;
                    if (isFullHostPath && path.contains(RedirectorConstants.DELIMETER + applicationName + RedirectorConstants.DELIMETER)) {
                        snapshotNeeded = true;
                    }
                    break;
                }
            }
        }

        void createSnapshot() {
            if (! connector.isConnected())
                return;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!connector.isConnected()) {
                            return;
                        }
                        daoFactory.getRegisteredCacheableDAO(EntityType.RULE).rebuildCache();
                        StackBackup stackBackup = ServiceProviderUtils.getStackBackup(stacksSupplier.get());
                        byte[] snapshot = offlineRedirectorHelper.getJsonSnapshot(applicationName, stackBackupSerializer.serialize(stackBackup, false));
                        fileManager.createSnapshot(snapshot, applicationName);
                    } catch (Exception e) {
                        log.error("Failed to create snapshot for app: {}. Error: {}", applicationName, e);
                    }
                }
            });
        }
    }

    private class NamespaceChangeListener implements ICacheListener {

        private SnapshotFileManager fileManager;
        private ExecutorService executor = Executors.newSingleThreadExecutor(ThreadUtils.newThreadFactory("SnapshotManager.NamespaceChangeListener"));

        private NamespaceChangeListener(SnapshotFileManager fileManager) {
            this.fileManager = fileManager;
        }

        @Override
        public void onChanged() {
            createSnapshot();
        }

        void createSnapshot() {
            if (! connector.isConnected())
                return;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] namespacedLists = new byte[0];
                    try {
                        namespacedLists = offlineRedirectorHelper.getNamespacedListJsonSnapshot();
                    } catch (RedirectorDataSourceException | SerializerException e) {
                        log.error("Failed to create snapshot of Namespaces. Error: {}", e);
                    }
                    fileManager.createSnapshot(namespacedLists, NAMESPACED_LISTS.toString());
                }
            });

        }
    }

    private class ConfigChangeListener implements ICacheListener {

        private SnapshotFileManager fileManager;
        private ExecutorService executor = Executors.newSingleThreadExecutor(ThreadUtils.newThreadFactory("SnapshotManager.ConfigChangeListener"));

        private ConfigChangeListener(SnapshotFileManager fileManager) {
            this.fileManager = fileManager;
        }

        @Override
        public void onChanged() {
            createSnapshot();
        }

        void createSnapshot() {
            if (! connector.isConnected())
                return;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] redirectorConfig = new byte[0];
                    try {
                        redirectorConfig = offlineRedirectorHelper.getSettingsJsonSnapshot();
                    } catch (RedirectorDataSourceException | SerializerException e) {
                        log.error("Failed to create snapshot of Redirector Settings. Error: {}", e);
                    }
                    fileManager.createSnapshot(redirectorConfig, CONFIG.toString());
                }
            });
        }
    }

    private class ApplicationsListener implements ICacheListener {
        SnapshotFileManager fileManager;
        private ExecutorService executor = Executors.newSingleThreadExecutor(ThreadUtils.newThreadFactory("SnapshotManager.ApplicationsListener"));
        private DateTime currentTimeInUTC = new DateTime(DateTimeZone.UTC);

        public ApplicationsListener(SnapshotFileManager fileManager) {
            this.fileManager = fileManager;
        }

        @Override
        public void onChanged() {
            if (! connector.isConnected())
                return;

            try {
                for (String serviceName : applicationsService.getAppNames().getAppNames()) {
                    registerAppModelChangesListener(serviceName);
                }
            } catch (RedirectorDataSourceException e) {
                log.error("Failed to register listener of new applications registration");
            }
            backupApplicationsList();
        }

        void backupApplicationsList() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] applications = new byte[0];
                    try {
                        AppNames appNames = new AppNames();
                        appNames.setAppNames(applicationsService.getAppNames().getAppNames());
                        appNames.setVersion(currentTimeInUTC.getMillis());
                        applications = jsonSerializer.serializeToByteArray(appNames);
                    } catch (RedirectorDataSourceException | SerializerException e) {
                        log.error("Failed to create snapshot of applications list. Error: {}", e);
                    }
                    fileManager.createSnapshot(applications, APPLICATIONS.toString());
                }
            });
        }
    }

}
