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

package com.comcast.redirector.dataaccess.client;

import com.comcast.redirector.dataaccess.cache.IDataListener;
import com.comcast.redirector.dataaccess.cache.IStacksCache;
import com.comcast.redirector.dataaccess.cache.factory.INodeCacheFactory;
import com.comcast.redirector.dataaccess.cache.factory.IPathChildrenCacheFactory;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;

import java.nio.charset.Charset;
import java.util.List;

public interface IDataSourceConnector extends INodeCacheFactory, IPathChildrenCacheFactory {
    Charset UTF8_CHARSET = Charset.forName("UTF-8");

    List<String> getChildren(String path) throws DataSourceConnectorException;

    void save(String data, String path) throws DataSourceConnectorException;
    void saveCompressed(String data, String path) throws DataSourceConnectorException;

    byte[] getData(String path) throws DataSourceConnectorException;
    byte[] getDataDecompressed(String path) throws DataSourceConnectorException;

    void delete(String path) throws DataSourceConnectorException;
    void deleteWithChildren(String path) throws DataSourceConnectorException;

    void create(String path) throws DataSourceConnectorException;
    void createEphemeral(String path) throws DataSourceConnectorException;
    boolean isPathExists(String path) throws DataSourceConnectorException;

    int getNodeVersion(String path) throws DataSourceConnectorException;

    Transaction createTransaction();

    String getBasePath();
    boolean isCacheHosts();

    void connect();
    void disconnect();
    boolean isConnected();
    boolean blockUntilConnectedOrTimedOut() throws InterruptedException;
    void addConnectionListener(ConnectionListener listener);

    IStacksCache getStacksCache();

    void addNodeDataChangeListener(String path, IDataListener listener);
    void addPathChildrenChangeListener(String path, IDataListener listener);

    SharedInterProcessLock createLock(String path);

    interface ConnectionListener {
        void stateChanged(ConnectorState newState);
    }

    interface Transaction {
        void save(String data, String path) throws DataSourceConnectorException;
        void delete(String path) throws DataSourceConnectorException;
        void commit() throws DataSourceConnectorException;
    }

    enum ConnectorState {
        CONNECTED, DISCONNECTED, RECONNECTED, UNKNOWN
    }
}
