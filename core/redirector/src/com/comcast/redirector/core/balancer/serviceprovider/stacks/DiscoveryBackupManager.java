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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.core.balancer.serviceprovider.stacks;

import com.comcast.redirector.common.util.PathUtils;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.applications.ApplicationsBackup;
import com.comcast.redirector.core.applications.IApplicationsBackup;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.backup.IBackupManager;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.*;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.core.applications.Applications;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.comcast.redirector.core.backup.IBackupManagerFactory.BackupEntity.*;

public class DiscoveryBackupManager implements IDiscoveryBackupManager {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(DiscoveryBackupManager.class);

    private final IAppBackupManagerFactories appBackupManagerFactories;
    private IStacksBackupManager discoveryStacksBackupManager;
    private StackBackup discoveryStacksBackup;
    private Set<String> excludedApplications;
    private IBackupManagerFactory backupManagerFactory;

    @VisibleForTesting
    DiscoveryBackupManager(IBackupManagerFactory backupManagerFactory, Set<String> excludedApplications, IAppBackupManagerFactories appBackupManagerFactories) {
        this.excludedApplications = excludedApplications;
        this.backupManagerFactory = backupManagerFactory;
        this.appBackupManagerFactories = appBackupManagerFactories;
        createBackupManager();
        discoveryStacksBackup = getBackupData();
    }

    @VisibleForTesting
    private IntegrationTestChangeListener<StackSnapshot> integrationTestChangeListener;

    private void createBackupManager() {
        discoveryStacksBackupManager = new StacksBackupManager(backupManagerFactory.getBackupManager(DISCOVERY), new JsonSerializer());
    }

    private IApplicationsBackup getApplicationsBackup() {
        IBackupManager backupManager = backupManagerFactory.getBackupManager(APPLICATIONS);
        return new ApplicationsBackup(backupManager, new JsonSerializer());
    }

    private IStacksBackupManager getStacksManualBackupManagerForApp(String app) {
        return new StacksBackupManager(
            appBackupManagerFactories.get(app).getBackupManager(STACKS_MANUAL), new JsonSerializer());
    }

    private StackBackup getBackupData() {

        final StackBackup discoveryStacksBackup = discoveryStacksBackupManager.load();

        if (discoveryStacksBackup != null && !discoveryStacksBackup.getSnapshotList().isEmpty()) {
            // TODO: use isStaticDiscoveryNeededForApp predicate
            if (excludedApplications.stream().anyMatch(a ->
                    discoveryStacksBackup.getSnapshotList().removeIf(s ->
                            PathUtils.getAppFromPath(s.getPath()).equals(a)))) {

                log.info("Remove dynamic stack from backup");
                discoveryStacksBackupManager.backup(discoveryStacksBackup);
                return  discoveryStacksBackup;
            }
        }

        if (discoveryStacksBackup == null || discoveryStacksBackup.getSnapshotList().isEmpty()) {
            log.info("Merge stacks from applications");
            Applications apps = getApplicationsBackup().load();

            if (apps != null) {
                StackBackup newDiscoveryStackBackup = new StackBackup();

                apps.getApps().stream()
                    .peek(a -> log.info("trying to get backup for app={}", a))
                    .filter(a -> !excludedApplications.contains(a)) // TODO: use isStaticDiscoveryNeededForApp predicate
                    .forEach(a -> {
                            log.info("loading manual backup for app={}", a);
                            StackBackup backup = getStacksManualBackupManagerForApp(a).load();
                            if (backup != null) {
                                log.info("adding backup info into dynamic discovery backup for app={} from manual backup={}", a, backup);
                                backup.getSnapshotList().forEach(b->newDiscoveryStackBackup.addSnapshot(b));
                            }
                        }
                );
                discoveryStacksBackupManager.backup(newDiscoveryStackBackup);
                return newDiscoveryStackBackup;
            }
        }

        if (discoveryStacksBackup == null) {
            return new StackBackup();
        }

        return discoveryStacksBackup;
    }

    @Override
    public synchronized void deleteStackSnapshot(StackSnapshot stackSnapshot) {
        discoveryStacksBackup.deleteSnapshotIgnoringIPv6(stackSnapshot);
        doBackup(discoveryStacksBackup);
    }

    @Override
    public synchronized void syncStackSnapshot(StackSnapshot snapshotToSyncWith) {
        discoveryStacksBackup.syncStackSnapshot(snapshotToSyncWith);
        doBackup(discoveryStacksBackup);
    }

    private void notifyIntegrationTests(StackSnapshot snapshotToSyncWith) {
        if (integrationTestChangeListener != null) integrationTestChangeListener.update(snapshotToSyncWith);
    }

    private synchronized void doBackup(StackBackup stackBackup) {
        List<StackSnapshot> snapshotList = new ArrayList<>();
        for (StackSnapshot stackSnapshot : stackBackup.getSnapshotList()) {
            if (stackSnapshot != null) {
                snapshotList.add(stackSnapshot);
                notifyIntegrationTests(stackSnapshot);
            }
        }

        discoveryStacksBackup.setSnapshotList(snapshotList);
        discoveryStacksBackupManager.backup(discoveryStacksBackup);
    }


    @Override
    public synchronized void addStackSnapshot(StackSnapshot stackSnapshot) {
       if (!discoveryStacksBackup.getSnapshotList().contains(stackSnapshot)) {
          discoveryStacksBackup.addSnapshot(stackSnapshot);
           doBackup(discoveryStacksBackup);
       }
    }

    @Override
    public StackBackup getCurrentSnapshot() {
        return discoveryStacksBackup;
    }

    public static class Builder {
        private Set<String> excludedApplications;
        private IBackupManagerFactory backupManagerFactory;
        private IAppBackupManagerFactories appBackupManagerFactories;
        private IntegrationTestChangeListener<StackSnapshot> integrationTestChangeListener;

        public Builder excludedApplications(Set<String> apps) {
            this.excludedApplications = apps;
            return this;
        }

        public Builder withGlobalBackupManagerFactory(IBackupManagerFactory backupManagerFactory) {
            this.backupManagerFactory = backupManagerFactory;
            return this;
        }

        public Builder withAppBackupManagerFactories(IAppBackupManagerFactories appBackupManagerFactories) {
            this.appBackupManagerFactories = appBackupManagerFactories;
            return this;
        }

        public Builder withIntegrationTestChangeListener(IntegrationTestChangeListener<StackSnapshot> integrationTestChangeListener) {
            this.integrationTestChangeListener = integrationTestChangeListener;
            return this;
        }

        public DiscoveryBackupManager build() {
            DiscoveryBackupManager discoveryBackupManager =
                new DiscoveryBackupManager(backupManagerFactory, excludedApplications, appBackupManagerFactories);
            // this is visible for testing
            discoveryBackupManager.integrationTestChangeListener = integrationTestChangeListener;

            return discoveryBackupManager;
        }
    }
}
