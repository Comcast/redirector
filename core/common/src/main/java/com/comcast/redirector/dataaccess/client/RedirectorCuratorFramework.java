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
 */
package com.comcast.redirector.dataaccess.client;

import com.comcast.redirector.core.config.ZKConfig;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.*;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.imps.GzipCompressionProvider;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedirectorCuratorFramework implements CuratorFramework, ConnectionStateListener {
    private static final Logger log = LoggerFactory.getLogger(RedirectorCuratorFramework.class);

    private ZKConfig config;
    private CuratorFramework curator;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private AtomicBoolean isFirstConnection = new AtomicBoolean(true);
    private ListenerStateProxy listenerStateProxy = new ListenerStateProxy(curator);
    private ListenerProxy listenerProxy = new ListenerProxy(curator);
    private int zooKeeperWaitTimeBeforeReconnect;

    public RedirectorCuratorFramework(ZKConfig config) {
        this.config = config;
        curator = buildCuratorFramework(config);
        curator.getConnectionStateListenable().addListener(this);
    }

    @PostConstruct
    public void init () {
        this.zooKeeperWaitTimeBeforeReconnect = getWaitTimeBeforeReconnect(config.getZooKeeperWaitTimeBeforeReconnectMin(), config.getZooKeeperWaitTimeBeforeReconnectMax());
    }

    public CuratorFramework getConnection () {
        return this.curator;
    }

    private synchronized CuratorFramework buildCuratorFramework(final ZKConfig config) {

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(config.getZooKeeperConnection())
                .connectionTimeoutMs(config.getZooKeeperConnectionTimeout())
                .sessionTimeoutMs(config.getZooKeeperSessionTimeout())
                .retryPolicy(new RetryNTimes(config.getZooKeeperRetryAttempts(), config.getZooKeeperRetryInterval()))
                .compressionProvider(new GzipCompressionProvider());

        CuratorFramework framework = builder.build();
        listenerStateProxy.updateCurator(framework);
        listenerProxy.updateCurator(framework);
        return framework;
    }

    private void retryConnection() {
        RetryConnection retryConnection = new RetryConnection();
        try {
            while (!isConnected.get()) {
                safeClose(curator);
                retryConnection.delay(zooKeeperWaitTimeBeforeReconnect, TimeUnit.SECONDS);

                curator = null;
                curator = buildCuratorFramework(config);
                curator.getConnectionStateListenable().addListener(this);
                curator.start();

                retryConnection.delay(config.getZooKeeperSessionTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.error("Cannot start curator framework", e.getMessage());
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED) {
            isConnected.set(true);
            if (!isFirstConnection.get()) {
                for (ConnectionStateListener listener : listenerStateProxy.getListeners()) {
                    listener.stateChanged(client, ConnectionState.RECONNECTED);
                }
            }
            return;
        }

        if (newState == ConnectionState.LOST) {
            isConnected.set(false);
            isFirstConnection.set(false);
            retryConnection();
        }
    }

    private void safeClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
            log.error("Error close connection");
        }
    }

    private int getWaitTimeBeforeReconnect(int min, int max) {
        int time = ThreadLocalRandom.current().nextInt(min, max + 1);
        log.info("WaitTimeBeforeReconnect set to [" + time +"] s");
        return time;
    }

    @Override
    public void start() {
        curator.start();
    }

    @Override
    public void close() {
        curator.close();
    }

    @Override
    public CuratorFrameworkState getState() {
        return curator.getState();
    }

    @Override
    public boolean isStarted() {
        return curator.isStarted();
    }

    @Override
    public CreateBuilder create() {
        return curator.create();
    }

    @Override
    public DeleteBuilder delete() {
        return curator.delete();
    }

    @Override
    public ExistsBuilder checkExists() {
        return curator.checkExists();
    }

    @Override
    public GetDataBuilder getData() {
        return curator.getData();
    }

    @Override
    public SetDataBuilder setData() {
        return curator.setData();
    }

    @Override
    public GetChildrenBuilder getChildren() {
        return curator.getChildren();
    }

    @Override
    public GetACLBuilder getACL() {
        return curator.getACL();
    }

    @Override
    public SetACLBuilder setACL() {
        return curator.setACL();
    }

    @Override
    public CuratorTransaction inTransaction() {
        return curator.inTransaction();
    }

    @Override
    public void sync(String path, Object backgroundContextObject) {
        curator.sync(path, backgroundContextObject);
    }

    @Override
    public void createContainers(String path) throws Exception {
        curator.createContainers(path);
    }

    @Override
    public SyncBuilder sync() {
        return curator.sync();
    }

    @Override
    public Listenable<ConnectionStateListener> getConnectionStateListenable() {
        return listenerStateProxy;
    }

    @Override
    public Listenable<CuratorListener> getCuratorListenable() {
        return listenerProxy;
    }

    @Override
    public Listenable<UnhandledErrorListener> getUnhandledErrorListenable() {
        return curator.getUnhandledErrorListenable();
    }

    @Override
    public CuratorFramework nonNamespaceView() {
        return curator.nonNamespaceView();
    }

    @Override
    public CuratorFramework usingNamespace(String newNamespace) {
        return curator.usingNamespace(newNamespace);
    }

    @Override
    public String getNamespace() {
        return curator.getNamespace();
    }

    @Override
    public CuratorZookeeperClient getZookeeperClient() {
        return curator.getZookeeperClient();
    }

    @Override
    public EnsurePath newNamespaceAwareEnsurePath(String path) {
        return curator.newNamespaceAwareEnsurePath(path);
    }

    @Override
    public void clearWatcherReferences(Watcher watcher) {
        curator.clearWatcherReferences(watcher);
    }

    @Override
    public boolean blockUntilConnected(int maxWaitTime, TimeUnit units) throws InterruptedException {
        return curator.blockUntilConnected(maxWaitTime, units);
    }

    @Override
    public void blockUntilConnected() throws InterruptedException {
        curator.blockUntilConnected();
    }

}
