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

package com.comcast.redirector.dataaccess.client;

import com.comcast.redirector.common.serializers.ServiceDiscoveryHostJsonSerializer;
import com.comcast.redirector.dataaccess.cache.*;
import com.comcast.redirector.dataaccess.cache.factory.INodeCacheFactory;
import com.comcast.redirector.dataaccess.cache.factory.IPathChildrenCacheFactory;
import com.comcast.redirector.dataaccess.cache.factory.ZkNodeCacheFactory;
import com.comcast.redirector.dataaccess.cache.factory.ZkPathChildrenCacheFactory;
import com.comcast.redirector.dataaccess.cache.newzkstackscache.IServiceDiscovery;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import com.comcast.redirector.dataaccess.lock.ZookeeperSharedInterProcessLock;
import com.comcast.redirector.metrics.Metrics;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZookeeperConnector implements IDataSourceConnector, ConnectionStateListener {
    private static Logger log = LoggerFactory.getLogger(ZookeeperConnector.class);

    private Supplier<IStacksCache> stacksCacheFactory;

    private CuratorFramework client;
    private IPathCreator pathCreator;
    private ConnectionState currentConnectionState;
    private final Lock lock = new ReentrantLock();
    private List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());

    private String basePath;
    private boolean cacheHosts;
    private IStacksCache stackCache;

    private ExecutorService listenerParallelRunner = Executors.newCachedThreadPool();

    private INodeCacheFactory nodeCacheFactory;
    private IPathChildrenCacheFactory pathChildrenCacheFactory;

    private Map<String, INodeCacheWrapper> nodeCacheWrapperMap = new HashMap<>();
    private Map<String, IPathChildrenCacheWrapper> pathChildrenCacheWrapperMap = new HashMap<>();

    @VisibleForTesting
    public ZookeeperConnector() {
        this.pathCreator = new PathCreator();
    }

    @VisibleForTesting
    ZookeeperConnector(CuratorFramework client, String basePath, boolean cacheHosts, IPathCreator pathCreator,
                       IStacksCache stackCache, INodeCacheFactory nodeCacheFactory, IPathChildrenCacheFactory pathChildrenCacheFactory) {
        if (client == null || basePath == null || pathCreator == null) {
            throw new IllegalStateException("Curator, basePath and pathCreator should not be null");
        }
        this.client = client;
        this.basePath = basePath;
        this.cacheHosts = cacheHosts;
        this.pathCreator = pathCreator;
        this.stackCache = stackCache;
        this.nodeCacheFactory = nodeCacheFactory;
        this.pathChildrenCacheFactory = pathChildrenCacheFactory;
    }

    public ZookeeperConnector(CuratorFramework client, String basePath, boolean cacheHosts) {
        this(client, basePath, cacheHosts, null, null);
    }

    public ZookeeperConnector(CuratorFramework client, String basePath, boolean cacheHosts, Function<IServiceDiscovery, Boolean> stacksRefreshListener) {
        this(client, basePath, cacheHosts, null, stacksRefreshListener);
    }

    public ZookeeperConnector(CuratorFramework client, String basePath, boolean cacheHosts, Supplier<IStacksCache> stacksCacheFactory) {
        this(client, basePath, cacheHosts, stacksCacheFactory, null);
    }

    public ZookeeperConnector(CuratorFramework client, String basePath, boolean cacheHosts,
                              Supplier<IStacksCache> stacksCacheFactory, Function<IServiceDiscovery, Boolean> stacksRefreshListener) {
        this();
        if (client == null) {
            throw new IllegalStateException("Curator and config should not be null");
        }
        this.client = client;
        this.basePath = basePath;
        this.cacheHosts = cacheHosts;
        this.stacksCacheFactory = stacksCacheFactory;

        init(stacksRefreshListener);
    }

    private void init(Function<IServiceDiscovery, Boolean> stacksRefreshListener) {
        if (this.stacksCacheFactory == null) {
            this.stacksCacheFactory = () -> new NewZkStacksCache(client, this, new ServiceDiscoveryHostJsonSerializer(),
                cacheHosts, basePath, stacksRefreshListener);
        }
        this.nodeCacheFactory = new ZkNodeCacheFactory(this, client);
        this.pathChildrenCacheFactory = new ZkPathChildrenCacheFactory(this, client);
    }
    
    void setClient(CuratorFramework client) {
        this.client = client;
    }
    
    void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    void setCacheHosts(boolean cacheHosts) {
        this.cacheHosts = cacheHosts;
    }
    
    void setStacksCacheFactory(Supplier<IStacksCache> stacksCacheFactory) {
        this.stacksCacheFactory = stacksCacheFactory;
    }
    
    void setNodeCacheFactory(ZkNodeCacheFactory nodeCacheFactory) {
        this.nodeCacheFactory = nodeCacheFactory;
    }
    
    void setPathChildrenCacheFactory(ZkPathChildrenCacheFactory pathChildrenCacheFactory) {
        this.pathChildrenCacheFactory = pathChildrenCacheFactory;
    }
    
    
    @Override
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public boolean blockUntilConnectedOrTimedOut() throws InterruptedException {
        return client.getZookeeperClient().blockUntilConnectedOrTimedOut();
    }

    @Override
    public boolean isPathExists(String path) throws DataSourceConnectorException {
        String error = "Can't check if path: " + path + " exists";
        return executeOrFailWithError(() -> isNodeExistsInternal(path),  error);
    }

    private boolean isNodeExistsInternal(String path) throws Exception {
        return getConnectedClientOrFailFast().checkExists().forPath(path) != null;
    }

    @Override
    public List<String> getChildren(final String path) throws DataSourceConnectorException {
        String error = "Can't get children for path: " + path;
        return executeOrFailWithError(() -> getConnectedClientOrFailFast().getChildren().forPath(path) , error );
    }

    @Override
    public void saveCompressed(final String data, final String path) throws DataSourceConnectorException {
        save(data, path, true);
    }

    private void save(final String data, final String path, final boolean compressed) throws DataSourceConnectorException {
        String error = "Can't save data for path: " + path;
        executeOrFailWithError(
            () -> {
                pathCreator.createPath(path);
                setValue(path, data, compressed);
            },
            error);
    }

    @Override
    public void save(String data, String path) throws DataSourceConnectorException {
        save(data, path, false);
    }

    @Override
    public void deleteWithChildren(final String path) throws DataSourceConnectorException {
        String error = "Can't delete item for path: " + path;
        executeOrFailWithError(
            () -> getConnectedClientOrFailFast().delete().deletingChildrenIfNeeded().forPath(path),
            error);
    }

    @Override
    public void delete(String path) throws DataSourceConnectorException {
        String error = "Can't delete item for path: " + path;
        executeOrFailWithError(
            () -> getConnectedClientOrFailFast().delete().forPath(path),
            error);
    }

    @Override
    public void create(final String path) throws DataSourceConnectorException {
        String error = "Can't save data for path: " + path;
        executeOrFailWithError(() -> pathCreator.createPath(path), error);
    }

    @Override
    public void createEphemeral(final String path) throws DataSourceConnectorException {
        String error = "Can't save data for ephemeral path: " + path;
        executeOrFailWithError(() -> pathCreator.createEphemeralPath(path), error);
    }

    @Override
    public Transaction createTransaction() {
        return new ZookeeperTransaction();
    }

    class ZookeeperTransaction implements Transaction {
        private CuratorTransaction transaction;

        @Override
        public void save(String data, String path) throws DataSourceConnectorException {
            String error = "Can't save data for path: " + path + " in transaction";
            transaction = executeOrFailWithError(() -> saveInTransaction(transaction, data.getBytes(UTF8_CHARSET), path), error);
        }

        @Override
        public void delete(String path) throws DataSourceConnectorException {
            String error = "Can't delete data for path: " + path + " in transaction";
            transaction = executeOrFailWithError(
                () -> {
                    CuratorTransaction resultingTransaction = (transaction == null) ? getConnectedClientOrFailFast().inTransaction() : transaction;
                    return resultingTransaction.delete().forPath(path).and();
                },
                error);
        }

        @Override
        public void commit() throws DataSourceConnectorException {
            String error = "Can't commit transaction";
            executeOrFailWithError(
                () -> {
                    if (transaction instanceof CuratorTransactionFinal) {
                        Collection<CuratorTransactionResult> result = ((CuratorTransactionFinal)transaction).commit();
                        log.info("Transaction is committed. Result: {}", result);
                    } else {
                        log.info("Transaction cancelled: nothing to save");
                    }
                    return null;
                }, error);
        }

        private CuratorTransaction saveInTransaction(CuratorTransaction transaction, byte[] data, String path) throws Exception {
            CuratorTransaction resultingTransaction = (transaction == null) ? getConnectedClientOrFailFast().inTransaction() : transaction;
            if (isNodeExistsInternal(path)) {
                resultingTransaction = resultingTransaction.setData().forPath(path, data).and();
            } else {
                String parentPath = StringUtils.substringBeforeLast(path, "/");
                if (!isNodeExistsInternal(parentPath)) {
                    resultingTransaction = resultingTransaction.create().forPath(parentPath).and();
                }
                resultingTransaction = resultingTransaction.create().forPath(path, data).and();
            }
            return resultingTransaction;
        }

        @VisibleForTesting
        CuratorTransaction getTransaction() {
            return transaction;
        }
    }

    @Override
    public byte[] getData(final String path) throws DataSourceConnectorException {
        String error = "Failed to get data for path " + path;
        return executeOrFailWithError(() -> getConnectedClientOrFailFast().getData().forPath(path), error);
    }

    @Override
    public byte[] getDataDecompressed(final String path) throws DataSourceConnectorException {
        String error = "Failed to get compressed data for path " + path;
        return executeOrFailWithError(() -> getConnectedClientOrFailFast().getData().decompressed().forPath(path), error);
    }

    @Override
    public int getNodeVersion(String path) throws DataSourceConnectorException {
        if (isPathExists(path)) {
            return getNodeMetadata(path).getVersion();
        } else {
            throw new DataSourceConnectorException("node " + path + " doesn't exist");
        }
    }

    private Stat getNodeMetadata(final String path) throws DataSourceConnectorException {
        String error = "Failed to get node metadata for path " + path;
        return executeOrFailWithError(() -> getConnectedClientOrFailFast().checkExists().forPath(path), error);
    }

    @Override
    public String getBasePath() {
        return Optional.ofNullable(basePath).orElse("");
    }

    @Override
    public boolean isCacheHosts() {
        return cacheHosts;
    }

    @Override
    public void connect() {
        client.getConnectionStateListenable().addListener(this);
        client.start();
    }

    @Override
    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public IStacksCache getStacksCache() {
        return lazyInitStacksCache();
    }

    private synchronized IStacksCache lazyInitStacksCache() {
        if (stackCache == null) {
            stackCache = stacksCacheFactory.get();
        }

        return stackCache;
    }

    @Override
    public void addNodeDataChangeListener(String path, IDataListener listener) {
        INodeCacheWrapper nodeCache = nodeCacheWrapperMap.computeIfAbsent(path, this::createAndStartNodeCache);
        nodeCache.addListener(new NodeListenerWrapper(path, listener, nodeCache));
    }

    private INodeCacheWrapper createAndStartNodeCache(String path) {
        INodeCacheWrapper nodeCache = nodeCacheFactory.newNodeCacheWrapper(path, true);

        if (isConnected()) {
            startCache(nodeCache);
        } else {
            addConnectionListener(newState -> {
                if (newState == ConnectorState.CONNECTED || newState == ConnectorState.RECONNECTED) {
                    startCache(nodeCache);
                }
            });
        }

        return nodeCache;
    }

    private static void startCache(INodeCacheWrapper cache) {
        try {
            cache.start(true);
        } catch (DataSourceConnectorException e) {
            log.error("Failed to start cache for path=" + cache.getPath(), e);
        }
    }

    private static void startCache(IPathChildrenCacheWrapper cache, String path) {
        try {
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (DataSourceConnectorException e) {
            log.error("Failed to start path children cache for path=" + path, e);
        }
    }

    @Override
    public void addPathChildrenChangeListener(String path, IDataListener listener) {
        IPathChildrenCacheWrapper pathChildrenCache = pathChildrenCacheWrapperMap.computeIfAbsent(
            path, this::createAndStartPathChildrenCache
        );
        pathChildrenCache.addListener(new PathChildrenCacheListenerWrapper(listener));
    }

    private IPathChildrenCacheWrapper createAndStartPathChildrenCache(String path) {
        IPathChildrenCacheWrapper cache = pathChildrenCacheFactory.newPathChildrenCacheWrapper(path, true);
        if (isConnected()) {
            startCache(cache, path);
        } else {
            addConnectionListener(newState -> {
                if (newState == ConnectorState.CONNECTED || newState == ConnectorState.RECONNECTED) {
                    startCache(cache, path);
                }
            });
        }
        return cache;
    }

    @Override
    public INodeCacheWrapper newNodeCacheWrapper(String path, boolean useCache) {
        return nodeCacheFactory.newNodeCacheWrapper(path, useCache);
    }

    @Override
    public INodeCacheWrapper newNodeCacheWrapper(String path, boolean useCache, boolean useCacheWhenNotConnectedToDataSource) {
        return nodeCacheFactory.newNodeCacheWrapper(path, useCache, useCacheWhenNotConnectedToDataSource);
    }

    @Override
    public INodeCacheWrapper newNodeCacheWrapper(String path, boolean useCache, boolean useCacheWhenNotConnectedToDataSource, boolean isCompressed) {
        return nodeCacheFactory.newNodeCacheWrapper(path, useCache, useCacheWhenNotConnectedToDataSource, isCompressed);
    }

    @Override
    public IPathChildrenCacheWrapper newPathChildrenCacheWrapper(String path, boolean useCache) {
        return pathChildrenCacheFactory.newPathChildrenCacheWrapper(path, useCache);
    }

    @Override
    public IPathChildrenCacheWrapper newPathChildrenCacheWrapper(String path, boolean dataIsCompressed, ThreadFactory threadFactory, boolean useCache) {
        return pathChildrenCacheFactory.newPathChildrenCacheWrapper(path, dataIsCompressed, threadFactory, useCache);
    }

    /**
     * This implementation is done in parallel because some listeners can hang the thread for a few minutes
     * while waiting for the others.
     * @param client
     * @param newState
     */
    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (this.client instanceof  RedirectorCuratorFramework) {
            if (((RedirectorCuratorFramework) this.client).getConnection().equals(client)) {
                currentConnectionState = newState;
            }
        } else  {
            if (this.client.equals(client)) {
                currentConnectionState = newState;
            }
        }

        Metrics.reportConnectionState(newState.name());

        connectionListeners.forEach(listener ->
                listenerParallelRunner.submit(
                        (Runnable) () -> listener.stateChanged(getConnectorState(newState))));
    }
    private static ConnectorState getConnectorState(ConnectionState state) {
        switch (state) {
            case CONNECTED:
                return ConnectorState.CONNECTED;
            case SUSPENDED:
            case LOST:
                return ConnectorState.DISCONNECTED;
            case RECONNECTED:
                return ConnectorState.RECONNECTED;
            default:
                return ConnectorState.UNKNOWN;
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isStarted() &&
                currentConnectionState != null &&
                (currentConnectionState == ConnectionState.CONNECTED ||
                currentConnectionState == ConnectionState.RECONNECTED);
    }

    @Override
    public SharedInterProcessLock createLock(String path) {
        return new ZookeeperSharedInterProcessLock(client, path);
    }

    private void setValue(String nodePath, String data, boolean isCompressed) throws Exception {
        if (isNodeExistsInternal(nodePath)) {
            if (isCompressed) {
                getConnectedClientOrFailFast().setData().compressed().forPath(nodePath, data.getBytes(UTF8_CHARSET));
            } else {
                getConnectedClientOrFailFast().setData().forPath(nodePath, data.getBytes(UTF8_CHARSET));
            }
        } else {
            if (isCompressed) {
                getConnectedClientOrFailFast().create().compressed().forPath(nodePath, data.getBytes(UTF8_CHARSET));
            } else {
                getConnectedClientOrFailFast().create().forPath(nodePath, data.getBytes(UTF8_CHARSET));
            }
        }
    }

    private CuratorFramework getConnectedClientOrFailFast() throws KeeperException.ConnectionLossException {
        checkConnection();
        return obtainClient();
    }

    private void checkConnection() throws KeeperException.ConnectionLossException {
        if (client != null &&
                currentConnectionState != null &&
                currentConnectionState != ConnectionState.CONNECTED &&
                currentConnectionState != ConnectionState.RECONNECTED) {

            KeeperException.ConnectionLossException exception = new KeeperException.ConnectionLossException();
            Metrics.reportZookeeperConnectionIssue(exception);
            throw exception;
        }
    }

    private CuratorFramework obtainClient() {
        lock.lock();
        try {
            if (client == null) {
                connect();
            }
            return client;
        } finally {
            lock.unlock();
        }
    }

    private interface Operation<T> {
        T execute() throws Exception;
    }

    private interface VoidOperation {
        void execute() throws Exception;
    }

    interface IPathCreator {
        void createPath(String path) throws Exception;
        void createEphemeralPath(String path) throws Exception;
    }

    private class PathCreator implements IPathCreator {

        @Override
        public void createPath(String path) throws Exception {
            EnsurePath ensurePath = new EnsurePath(path);
            ensurePath.ensure(getConnectedClientOrFailFast().getZookeeperClient());
        }

        @Override
        public void createEphemeralPath(String path) throws Exception {
            client
                .create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, "".getBytes());
        }
    }

    private void executeOrFailWithError(VoidOperation operation, String error) throws DataSourceConnectorException {
        executeOrFailWithError(
            () -> {
                operation.execute();
                return null;
            }, error);
    }

    private <T> T executeOrFailWithError(Operation<T> operation, String error) throws DataSourceConnectorException {
        try {
            return operation.execute();
        } catch (KeeperException.NoNodeException e) {
            NoNodeInZookeeperException exception = new NoNodeInZookeeperException("No node is zookeeper. " + error, e);

            Metrics.reportZookeeperConnectionIssue(exception);
            throw exception;
        } catch (KeeperException e) {
            if (e.code() == KeeperException.Code.CONNECTIONLOSS) {
                NoConnectionToDataSourceException exception = new NoConnectionToDataSourceException("No connection to zookeeper. " + error, e);

                Metrics.reportZookeeperConnectionIssue(exception);
                throw exception;
            } else {
                DataSourceConnectorException exception = new DataSourceConnectorException(error, e);

                Metrics.reportZookeeperConnectionIssue(exception);
                throw exception;
            }
        } catch (Exception e) {
            DataSourceConnectorException exception = new DataSourceConnectorException(error, e);

            Metrics.reportZookeeperConnectionIssue(exception);
            throw exception;
        }
    }

    private static class NodeListenerWrapper implements NodeCacheListener {
        private String name; // for making logs meaningful
        private IDataListener listener;
        private INodeCacheWrapper nodeCache;

        private NodeListenerWrapper(String name, IDataListener listener, INodeCacheWrapper nodeCache) {
            this.name = name;
            this.listener = listener;
            this.nodeCache = nodeCache;
        }

        @Override
        public void nodeChanged() throws Exception {
            log.info("Node changed event came for modelChanged node of app={}", name);
            if (nodeCache.getCurrentData() != null) {
                nodeCache.rebuild();
                if (nodeCache.getCurrentDataVersion() == 0) {
                    log.info("Skipping notification about creation of modelChanged node");
                    return;
                }
                listener.onEvent(
                    IDataListener.EventType.NODE_UPDATED,
                    nodeCache.getPath(),
                    nodeCache.getCurrentData(),
                    nodeCache.getCurrentDataVersion());
            } else {
                log.error("Null data is coming to zkNode cache - {}", name);
            }
        }

        public IDataListener getListener() {
            return listener;
        }
    }

    private static class PathChildrenCacheListenerWrapper implements PathChildrenCacheListener {
        private IDataListener listener;

        private PathChildrenCacheListenerWrapper(IDataListener listener) {
            this.listener = listener;
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            if (event.getData() != null) {
                listener.onEvent(
                    IDataListener.EventType.NODE_UPDATED,
                    event.getData().getPath(),
                    event.getData().getData(),
                    event.getData().getStat().getVersion()); // TODO: coupled to Zookeeper. Need to decouple
            } else if (event.getType() == PathChildrenCacheEvent.Type.INITIALIZED) {
                listener.onEvent(IDataListener.EventType.INITIALIZED, null, null);
            } else {
                log.error("Null data is coming to zkNode cache - {}", "Change NamespacedList");
            }
        }

        @VisibleForTesting
        public IDataListener getListener() {
            return listener;
        }
    }
}
