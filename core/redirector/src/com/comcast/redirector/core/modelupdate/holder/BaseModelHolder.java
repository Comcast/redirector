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

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.backup.IBackupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class BaseModelHolder<T> implements IModelHolder<T> {
    private static final Logger log = LoggerFactory.getLogger(BaseModelHolder.class);

    private Class<T> classType;
    protected final Serializer serializer;
    private IBackupManager backupManager;

    BaseModelHolder(Class<T> classType,
                           Serializer serializer,
                           IBackupManager backupManager) {
        this.serializer = serializer;
        this.backupManager = backupManager;
        this.classType = classType;
    }

    @Override
    public T load(boolean fromDataStore) {
        return (fromDataStore) ? loadFromDataStore() : loadFromBackup();
    }

    protected abstract T loadFromDataStore();

    private T loadFromBackup() {
        String data = backupManager.load();

        if (data != null) {
            try {
                return serializer.deserialize(data, classType);
            } catch (Exception e) {
                log.error("Failed to build SelectServer from backup ", e);
            }
        }

        return null;
    }

    @Override
    public boolean backup(T model) {
        String data = null;
        if (model != null) {
            try {
                data = serializer.serialize(model);
            } catch (Exception e) {
                log.error("Failed to serialize SelectServer ", e);
            }
        }

        if (data != null) {
            try {
                if (backupManager.backup(data).get(2, TimeUnit.MINUTES)) {
                    return true;
                } else {
                    logBackupFailure(data);
                }
            } catch (InterruptedException|ExecutionException|TimeoutException e) {
                logBackupFailureWithException(data, e);
            }
        }

        return false;
    }

    private void logBackupFailure(String data) {
        log.error("Failed to backup " + classType.getSimpleName() + " : " + data);
    }

    private void logBackupFailureWithException(String data, Exception e) {
        log.error("Failed to backup " + classType.getSimpleName() + " : " + data, e);
    }
}
