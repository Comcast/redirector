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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.spring.IntegrationTestChangeListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import com.comcast.redirector.ruleengine.IpAddressInitException;
import com.comcast.redirector.ruleengine.model.IpAddress;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NamespacedListsHolder implements IDataStoreAwareNamespacedListsHolder {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(NamespacedListsHolder.class);
    private NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
    private Map<String, Set<IpAddress>> ipAddressesCache = Collections.synchronizedMap(new HashMap<>());

    private NamespacedListToIpAddressListConverter toIpAddressListConverter = new NamespacedListToIpAddressListConverter(this::getNamespacedListValues);

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    @Qualifier("globalBackupManagerFactory")
    private IBackupManagerFactory globalBackupManagerFactory;

    @VisibleForTesting
    @Autowired(required = false)
    private IntegrationTestChangeListener<NamespacedListsBatch> newBatchAppliedListener;

    @Autowired
    private ICommonModelFacade commonModelFacade;

    @Autowired
    private Serializer xmlSerializer;

    @Autowired
    private IDataSourceConnector connector;

    @PostConstruct
    private void init() {
        commonModelFacade.initNamespacedListDataChangePolling(this::onNamespaceChanged);
    }

    @Override
    public boolean isNamespacedListsOutOfDate() {
        return getNamespacedListsBatch().getDataNodeVersion() < commonModelFacade.getNamespacedListsVersion();
    }

    @Override
    public void load(boolean fromDataStore) {
        if (fromDataStore) {
            log.info("started loading namespaced list from datastore");
            loadFromDataStore();
            createBackupNamespacedLists();

            // For testing
            log.info("Loaded namespaced list from datastore");
            notifyNewBatchApplied(getNamespacedListsBatch());
        } else {
            loadFromBackup();
        }
    }

    private void onNamespaceChanged(Integer newVersion, Consumer<Integer> updateCurrentVersion) {
        log.info("Namespacedlist list is changed");
        load(IModelHolder.GET_FROM_DATA_STORE);
        updateCurrentVersion.accept(newVersion);
    }

    @VisibleForTesting
    void loadFromDataStore() {
        try {
            Date date = new Date();
            int version = commonModelFacade.getNextNamespacedListsVersion();
            log.info("Start getting NamespacedList(listVersion={}) from WS - startTime={}", version, date.getTime());
            NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
            namespacedListsBatch.setDataNodeVersion(version);
            for (NamespacedList item : commonModelFacade.getAllNamespacedLists()) {
                namespacedListsBatch.addValues(item.getName(), Collections2.transform(item.getValueSet(),
                        item.getType().equals(NamespacedListType.ENCODED) ? NamespacedListValueForWS::getEncodedValue : NamespacedListValueForWS::getValue));
            }
            setNamespacedListsBatch(namespacedListsBatch);

            Long endTime = (new Date()).getTime();
            log.info("End getting NamespacedList from WS - endTime=" + endTime + ", total duration=" + (endTime - date.getTime()) + " millis");
        } catch (Exception e) {
            log.error("Failed to load NamespacedList from commonModelFacade: {}", e.getMessage());
            throw e;
        }
    }

    @VisibleForTesting
    Set<IpAddress> getIpAddressesFromCacheForNamespacedList(String listName) {
        Set<IpAddress> copyOfCache = ipAddressesCache.containsKey(listName) ?
            new HashSet<>(ipAddressesCache.get(listName)) : Collections.emptySet();
        return copyOfCache;
    }

    private void notifyNewBatchApplied(NamespacedListsBatch namespacedListsBatch) {
        if (newBatchAppliedListener != null) {
            newBatchAppliedListener.update(namespacedListsBatch);
        }
    }

    @Override
    public void createBackupNamespacedLists() {
        String data = null;
        NamespacedListsBatch namespacedListsBatch = getNamespacedListsBatch();
        if (! namespacedListsBatch.getNamespacedLists().isEmpty()) {
            try {
                data = new JsonSerializer().serialize(namespacedListsBatch, true);
            } catch (Exception e) {
                log.error("Failed to serialize NamespacedList ", e);
                throw e;
            }
        }

        if (data != null) {
            globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS).backup(data);
        }
    }

    @VisibleForTesting
    void loadFromBackup() {
        String data = globalBackupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.NAMESPACED_LISTS).load();
        if (data != null) {
            try {
                setNamespacedListsBatch(new JsonSerializer().deserialize(data, NamespacedListsBatch.class));
            } catch (Exception e) {
                log.error("Failed to load NamespacedList from backup ", e);
            }
        }
    }

    @Override
    public Set<String> getNamespacedListValues(String namespacedListName) {
        Set<String> namespacedListsValues = getNamespacedListsBatch().getNamespacedLists().get(namespacedListName);
        return namespacedListsValues == null ? new HashSet<>() : namespacedListsValues;
    }

    @Override
    public Set<IpAddress> getIpAddressesFromNamespacedList(String name) {
        lock.readLock().lock();
        try {
            Set<IpAddress> result = ipAddressesCache.computeIfAbsent(name, this::convertNamespacedListToIpAddressesList);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    private Set<IpAddress> convertNamespacedListToIpAddressesList(String name) {
        return toIpAddressListConverter.convert(name);
    }

    @Override
    public NamespacedListsBatch getNamespacedListsBatch() {
        lock.readLock().lock();
        try {
            return namespacedListsBatch;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setNamespacedListsBatch(NamespacedListsBatch namespacedListsBatch) {
        lock.writeLock().lock();
        try {
            this.namespacedListsBatch = namespacedListsBatch;
            ipAddressesCache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static class NamespacedListToIpAddressListConverter {
        private Function<String, Set<String>> getNamespacedListValues;

        public NamespacedListToIpAddressListConverter(Function<String, Set<String>> getNamespacedListValues) {
            this.getNamespacedListValues = getNamespacedListValues;
        }

        public Set<IpAddress> convert(String name) {
            return getNamespacedListValues.apply(name).stream()
                .map(ipAddressCandidate -> {
                    try {
                        return new IpAddress(ipAddressCandidate);
                    } catch (IpAddressInitException e) {
                        log.warn("Invalid ip address {} in list {}", ipAddressCandidate, name);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
    }
}
