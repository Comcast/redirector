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

package com.comcast.redirector.core.balancer.serviceprovider.backup;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.core.backup.IBackupManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class StacksBackupManager implements IStacksBackupManager {
    private static final Logger log = LoggerFactory.getLogger(StacksBackupManager.class);

    private Serializer serializer;

    private IBackupManager manualBackupManager;
    private StackBackup cache;

    public StacksBackupManager(IBackupManager manualBackupManager,
                               Serializer serializer) {
        this.manualBackupManager = manualBackupManager;
        this.serializer = serializer;
    }

    @Override
    public Future<Boolean> backup(StackBackup backup) {
        return backupInternal(backup);
    }

    @Override
    public StackBackup load() {
        if (cache == null) {
            fillCache();
        }

        return cache;
    }

    private void fillCache() {
        try {
            String data = manualBackupManager.load();
            if (StringUtils.isNotBlank(data)) {
                cache = serializer.deserialize(data, StackBackup.class);
            }
        } catch (Exception e) {
            log.error("failed to de-serialize manual active nodes snapshot", e);
        }
    }

    private Future<Boolean> backupInternal(StackBackup snapshot) {
        cache = snapshot;
        String data = null;
        try {
            data = serializer.serialize(snapshot, false);
        } catch (SerializerException e) {
            log.error("failed to serialize manual active nodes snapshot", e);
        }
        return manualBackupManager.backup(data);
    }
}
