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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.commons.lang3.StringUtils;

import static com.comcast.redirector.dataaccess.client.IDataSourceConnector.UTF8_CHARSET;

public class BaseDAO<T> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(BaseDAO.class);

    public static final boolean COMPRESSED = true;
    public static final boolean NOT_COMPRESSED = false;

    protected final boolean compressed;
    protected final boolean useCache;
    protected final boolean useCacheWhenNotConnectedToDataSource;
    protected final Class<T> clazz;
    protected final IDataSourceConnector connector;

    private BaseOperations operations;

    BaseDAO(Class<T> clazz,
                   Serializer marshalling,
                   IDataSourceConnector connector,
                   boolean compressed,
                   boolean useCache) {
        this(clazz, marshalling, connector, compressed, useCache, false);
    }

    public BaseDAO(Class<T> clazz,
                   Serializer marshalling,
                   IDataSourceConnector connector,
                   boolean compressed,
                   boolean useCache,
                   boolean useCacheWhenNotConnectedToDataSource) {
        this.clazz = clazz;
        this.connector = connector;
        this.compressed = compressed;
        this.useCache = useCache;
        this.useCacheWhenNotConnectedToDataSource = useCacheWhenNotConnectedToDataSource;

        operations = new BaseOperations(marshalling, connector);
    }

    T deserializeOrReturnNull(byte[] data) {
        return operations.deserializeOrReturnNull(data, clazz);
    }

    protected String serialize(T data) throws SerializerException {
        return operations.serialize(data);
    }

    protected void save(String data, String path) throws DataSourceConnectorException {
        if (compressed) {
            operations.saveCompressed(data, path);
        } else {
            operations.save(data, path);
        }
    }

    void deleteByPath(String path) throws DataSourceConnectorException {
        operations.delete(path);
    }

    static class BaseOperations {
        private Serializer marshalling;
        private IDataSourceConnector aConnector;

        BaseOperations(Serializer marshalling, IDataSourceConnector aConnector) {
            this.marshalling = marshalling;
            this.aConnector = aConnector;
        }

        <T> T deserializeOrReturnNull(byte[] data, Class<T> clazz) {
            if (data != null) {
                String value = decodeUTF8(data);
                if (StringUtils.isNotBlank(value)) {
                    try {
                        return marshalling.deserialize(value, clazz);
                    } catch (SerializerException e) {
                        log.error("Failed to deserialize " + value + " to " + clazz.getSimpleName(), e);
                    }
                }
            }
            return null;
        }

        <T> String serialize(T data) throws SerializerException {
            try {
                return marshalling.serialize(data, false);
            } catch (SerializerException e) {
                log.error("Failed to serialize ", e);
                throw e;
            }
        }

        void save(String data, String path) throws DataSourceConnectorException {
            if (data == null) {
                throw new DataSourceConnectorException("data is null for path=" + path + " , compressed=false");
            }
            aConnector.save(data, path);
            log.info("Successfully saved data for path={}. Compressed=false", path);
        }

        void saveCompressed(String data, String path) throws DataSourceConnectorException {
            if (data == null) {
                throw new DataSourceConnectorException("data is null for path=" + path + " , compressed=true");
            }
            aConnector.saveCompressed(data, path);
            log.info("Successfully saved data for path={}. Compressed=true", path);
        }

        int getVersion (String path) throws DataSourceConnectorException {
            return aConnector.getNodeVersion(path);
        }

        void delete(String path) throws DataSourceConnectorException {
            aConnector.delete(path);
            log.info("Successfully deleted data for path={}", path);
        }

        private String decodeUTF8(byte[] bytes) {
            return new String(bytes, UTF8_CHARSET);
        }
    }
}
