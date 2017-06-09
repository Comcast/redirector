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

package com.comcast.redirector.core.backup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractBackupManagerFactory implements IBackupManagerFactory {
    private Map<BackupEntity, IBackupManager> backupManagers = new HashMap<>();
    private final Lock lock = new ReentrantLock(true);

    protected abstract IBackupManager createBackupManager(BackupEntity backupEntity);

    @Override
    public final IBackupManager getBackupManager(BackupEntity backupEntity) {
        lock.lock();
        try {
            if (!backupManagers.containsKey(backupEntity)) {
                backupManagers.put(backupEntity, createBackupManager(backupEntity));
            }

            return backupManagers.get(backupEntity);
        } finally {
            lock.unlock();
        }
    }
}
