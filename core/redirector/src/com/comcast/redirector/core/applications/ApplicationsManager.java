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

package com.comcast.redirector.core.applications;

import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.common.thread.ThreadUtils;
import com.comcast.redirector.core.IRedirectorFactory;
import com.comcast.redirector.core.backup.IBackupManager;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.backup.filesystem.FileSystemBackupFiles;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import com.comcast.xre.redirector.utils.XreGuideAppNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApplicationsManager implements IApplicationsManager {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsManager.class);

    private ScheduledExecutorService appsObserverExecutor = Executors.newSingleThreadScheduledExecutor(
        ThreadUtils.newThreadFactory("ApplicationsObserver"));

    private boolean started = false;
    private Lock lock = new ReentrantLock();
    private int appsRetrievingIntervalInSeconds;
    private Applications apps = new Applications();

    @Autowired
    private ICommonModelFacade commonModelFacade;

    @Autowired(required = false)
    private IApplicationRegistry applicationRegistry;

    @Autowired
    @Qualifier("globalBackupManagerFactory")
    private IBackupManagerFactory globalBackupManagerFactory;

    @Autowired
    private IRedirectorFactory redirectorFactory;

    private IApplicationsBackup applicationsBackup;
    
    private String backupBasePath;

    public ApplicationsManager(int appsRetrievingIntervalInSeconds, String backupBasePath) {
        this.appsRetrievingIntervalInSeconds = appsRetrievingIntervalInSeconds;
        this.backupBasePath = backupBasePath;
    }

    @PostConstruct
    private void init() {
        applicationsBackup = initApplicationsBackup();
    }

    private IApplicationsBackup initApplicationsBackup() {
        log.info("Applications backup init started");

        IBackupManager backupManager = globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.APPLICATIONS);
        IApplicationsBackup applicationsBackup = new ApplicationsBackup(backupManager, new JsonSerializer());

        log.info("Applications backup init finished");
        return applicationsBackup;
    }

    @Override
    public void start() {
        if (lock.tryLock()) {
            try {
                startInternal();
            } finally {
                lock.unlock();
            }
        } else {
            log.info("Trying to start ApplicationsManager in another thread");
        }
    }

    private void startInternal() {
        if ( !started ) {
            started = true;
            initBackupWithAppNamesAndStartApps();
            startObservingApps();
        } else {
            log.info("ApplicationsManager has already been started");
        }
    }

    private void initBackupWithAppNamesAndStartApps() {
        apps = getApplicationNamesFromBackup();
        
        if (apps == null || apps.getApps().isEmpty()) {
            apps = getApplicationNamesFromDataSource();
            apps = getAppNamesWithValidModel(apps, isValidModeExistsInDataSource());
            backupAppNames(apps);
        } else {
            apps = getAppNamesWithValidModel(apps, isValidModeExistsInBackup());
        }
    
        // TODO: avoid this line since it's about implementation details of concrete XRE Redirector
        apps.addApp(XreGuideAppNames.xreGuide.toString());

        registerApplications();
        log.info("Obtaining applications finished successfully");
    }

    private void startObservingApps() {
        appsObserverExecutor.scheduleWithFixedDelay((Runnable) () -> {
            try {
                Applications appNames = getApplicationNamesFromDataSource();
                if (appNames.getApps().size() > 0) {

                    Applications validAppNames = getAppNamesWithValidModel(appNames, isValidModeExistsInDataSource());
                    if (!validAppNames.getApps().isEmpty()) {
                        backupAppNames(validAppNames);
                        registerApplications(validAppNames);
                        Applications appsToSuspend = apps.diff(validAppNames);
                        
                        appsToSuspend.getApps().forEach(app -> {
                            redirectorFactory.createRedirector(app).suspendPolling();
                        });
    
                        validAppNames.getApps().forEach(app -> {
                            redirectorFactory.createRedirector(app).restartPollingIfSuspended();
                        });
    
                        apps = validAppNames;
                    }

                }
            } catch (Exception e) {
                log.error("Failed to get applications ", e);
            }
        }, appsRetrievingIntervalInSeconds, appsRetrievingIntervalInSeconds, TimeUnit.SECONDS);
    }

    private Applications getApplicationNamesFromBackup() {
        return applicationsBackup.load();
    }
    
    private Applications getApplicationNamesFromDataSource() {
        return new Applications(commonModelFacade.getAllRegisteredApps());
    }
    
    private Applications getAppNamesWithValidModel(Applications appNames, Predicate<String> predicate) {
        Set<String> applicationNames = appNames.getApps();
        if (applicationNames == null || applicationNames.size() == 0) {
            return new Applications();
        }
        
        return  new Applications(applicationNames.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
    
    private Predicate<String> isValidModeExistsInDataSource() {
        return commonModelFacade::isValidModelForAppExists;
    }
    
    private Predicate<String> isValidModeExistsInBackup() {
        return this::isBackupFilesExistForDynamicApplication;
    }
    
    private void backupAppNames(Applications apps) {
        applicationsBackup.backup(apps);
    }

    private void registerApplications() {
        registerApplications(apps);
    }

    private void registerApplications(Applications applications) {
        // TODO: in parallel
        applications.getApps().forEach( appName -> {
            if (applicationRegistry != null) {
                applicationRegistry.registerApplication(appName);
            }
            redirectorFactory.createRedirector(appName);
        });
    }

    @Override
    public void close() throws IOException {
        stopObservingApps();
    }

    private void stopObservingApps() {
        appsObserverExecutor.shutdownNow();
    }

    @Override
    public Applications getApplications() {
        return apps;
    }

    @Override
    public boolean isAppRegistered(String appName) {
        return getApplications().getApps().contains(appName);
    }
    
    private Boolean isBackupFilesExistForDynamicApplication(String appName) {
        return Files.exists(Paths.get(backupBasePath + File.separator + appName + File.separator + FileSystemBackupFiles.filenames.get(IBackupManagerFactory.BackupEntity.FLAVOR_RULES))) &&
                Files.exists(Paths.get(backupBasePath + File.separator + appName + File.separator + FileSystemBackupFiles.filenames.get(IBackupManagerFactory.BackupEntity.URL_RULES))) &&
                Files.exists(Paths.get(backupBasePath + File.separator + appName + File.separator + FileSystemBackupFiles.filenames.get(IBackupManagerFactory.BackupEntity.WHITE_LIST))) &&
                Files.exists(Paths.get(backupBasePath + File.separator + appName + File.separator + FileSystemBackupFiles.filenames.get(IBackupManagerFactory.BackupEntity.MODEL_METADATA)));
    }
    
}
