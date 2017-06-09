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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class InMemoryBackupManagerFactory extends AbstractBackupManagerFactory {
    private static final Logger log = LoggerFactory.getLogger(InMemoryBackupManagerFactory.class);

    @Override
    protected IBackupManager createBackupManager(BackupEntity backupEntity) {
        return new InMemoryBackupManager(backupEntity);
    }

    private static class InMemoryBackupManager implements IBackupManager {
        private BackupEntity entity;
        private String data;

        InMemoryBackupManager(BackupEntity entity) {
            this.entity = entity;
        }

        @Override
        public Future<Boolean> backup(String data) {
            log.info("Saving backup of {}. Data: {}", entity, data);
            this.data = data;

            return CompletableFuture.completedFuture(true);
        }

        @Override
        public String load() {
            log.info("Loading backup of {}", entity);
            return data;
        }
    }
}
