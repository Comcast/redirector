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

package com.comcast.redirector.core.balancer.serviceprovider;

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.thread.ThreadUtils;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.tvx.cloud.MetaData;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

class DynamicServiceProviderManager extends AbstractServiceProviderManager {
    private static final Logger log = LoggerFactory.getLogger(DynamicServiceProviderManager.class);

    interface Initializer {
        boolean isInitializedInDisconnectedMode(Consumer<Set<StackData>> stacksApplier);
    }

    private Initializer backupInitializer;

    private IStacksChangeObserver stacksChangeObserver;
    private DynamicServiceProviderFactory dynamicServiceProviderFactory;
    private int updatePeriodInSeconds;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
        ThreadUtils.newThreadFactory("ZookeeperServiceDiscovery"));

    private boolean usingBackupProviders = true;

    DynamicServiceProviderManager(ZKConfig config,
                                  ProviderStrategy<MetaData> providerStrategy,
                                  Supplier<Set<XreStackPath>> availableStacksSupplier,
                                  Initializer backupInitializer,
                                  DynamicServiceProviderFactory dynamicServiceProviderFactory) {
        super(providerStrategy, config);

        this.stacksChangeObserver = new StacksChangeObserver(availableStacksSupplier);
        this.backupInitializer = backupInitializer;
        this.dynamicServiceProviderFactory = dynamicServiceProviderFactory;

        this.updatePeriodInSeconds = config.getDiscoveryUpdatePeriodInSeconds();

        start();
    }

    private void start() {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                checkStacks();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 0, updatePeriodInSeconds, TimeUnit.SECONDS);
    }

    private void checkStacks() {
        try {
            if (getAllStackServiceProviders().isEmpty()) {
                if (backupInitializer != null &&
                    backupInitializer.isInitializedInDisconnectedMode(this::applyDynamicStacksChangesFromBackup)) {
                    return;
                }
            }

            StacksChange stacksChange = stacksChangeObserver.getStacksChange();
            if (stacksChange.isNotEmpty()) {
                log.info("Stacks change registered: {}", stacksChange);
                if (usingBackupProviders) {
                    log.info("Backup providers become unavailable because connection to datasource is established and latest data is obtained from datasource");
                    reset();
                    switchToDynamicMode(dynamicServiceProviderFactory);
                    usingBackupProviders = false;
                }
                applyStacksChanges(stacksChange.getAdded(), stacksChange.getRemoved());
            }
        } catch (Exception e) {
            log.error("Failed to obtain stacks", e);
        }
    }

    private void applyDynamicStacksChangesFromBackup(Set<StackData> stacks) {
        Set<String> excludedApps = config.getExcludedAppsFromStackAutoDiscovery();
        IStaticServiceProviderFactory serviceProviderFactory = new StaticServiceProviderFactory(providerStrategy, config);

        StaticProvidersUpdateService updateService = new StaticProvidersUpdateService(serviceProviderFactory, excludedApps);
        ServiceProviders providers = updateService.apply(stacks);
        applyServiceProviders(providers);
    }

    private interface IStacksChangeObserver {
        StacksChange getStacksChange();
    }

    private static class StacksChange {
        private Set<XreStackPath> addedStacks;
        private Set<XreStackPath> removedStacks;

        StacksChange(Set<XreStackPath> added, Set<XreStackPath> removed) {
            addedStacks = added;
            removedStacks = removed;
        }

        public Set<XreStackPath> getAdded() {
            return addedStacks;
        }

        public Set<XreStackPath> getRemoved() {
            return removedStacks;
        }

        @Override
        public String toString() {
            return "StacksChange{" +
                "added=" + addedStacks +
                ", removed=" + removedStacks +
                '}';
        }

        public boolean isEmpty() {
            return addedStacks.isEmpty() && removedStacks.isEmpty();
        }

        public boolean isNotEmpty() {
            return !isEmpty();
        }
    }

    private static class StacksChangeObserver implements IStacksChangeObserver {
        private Supplier<Set<XreStackPath>> availableStacksSupplier;

        private Set<XreStackPath> stacksCache = Collections.emptySet();

        public StacksChangeObserver(Supplier<Set<XreStackPath>> availableStacksSupplier) {
            this.availableStacksSupplier = availableStacksSupplier;
        }

        public StacksChange getStacksChange() {
            Set<XreStackPath> allStacks = availableStacksSupplier.get();
            allStacks.remove(null); //in case when we have empty item

            Set<XreStackPath> oldStacks = stacksCache;
            stacksCache = allStacks;

            Set<XreStackPath> added = getAdded(allStacks, oldStacks);
            Set<XreStackPath> removed = getRemoved(allStacks, oldStacks);

            return new StacksChange(added, removed);
        }

        private Set<XreStackPath> getAdded(Set<XreStackPath> newSet, Set<XreStackPath> oldSet) {
            Set<XreStackPath> result = new HashSet<>(newSet);
            result.removeAll(oldSet);
            return result;
        }

        private Set<XreStackPath> getRemoved(Set<XreStackPath> newSet, Set<XreStackPath> oldSet) {
            Set<XreStackPath> result = new HashSet<>(oldSet);
            result.removeAll(newSet);
            return result;
        }
    }
}
