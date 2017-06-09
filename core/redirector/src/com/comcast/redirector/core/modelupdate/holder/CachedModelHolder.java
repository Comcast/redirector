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

abstract class CachedModelHolder<T> extends BaseModelHolder<T> implements ICachedModelHolder<T> {
    private T cache;
    private T backupCache;

    CachedModelHolder(Class<T> classType, Serializer serializer, IBackupManager backupManager) {
        super(classType, serializer, backupManager);
    }

    @Override
    public T load(boolean fromDataStore) {
        T data;

        if (fromDataStore) {
            if (cache != null) {
                data = cache;
            } else {
                data = super.load(fromDataStore);
                cache = data;
            }
        } else {
            if (backupCache != null) {
                data = backupCache;
            } else {
                data = super.load(fromDataStore);
                backupCache = data;
            }
        }

        return data;
    }

    @Override
    public boolean backup(T model) {
        boolean result = super.backup(model);
        backupCache = model;

        return result;
    }

    @Override
    public void resetCache() {
        cache = null;
        backupCache = null;
    }
}
