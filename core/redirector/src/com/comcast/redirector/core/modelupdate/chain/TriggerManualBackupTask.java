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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.balancer.serviceprovider.backup.IStacksBackupManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TriggerManualBackupTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(TriggerManualBackupTask.class);

    private IStacksBackupManager backupManager;

    public TriggerManualBackupTask(IStacksBackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @Override
    public Result handle(ModelContext context) {
        log.info("Starting backup stacks data");
        try {
            if (! backupManager.backup(context.getMainStacksBackup()).get(2, TimeUnit.MINUTES)) {
                log.error("Failed to backup stacks");

                return Result.failure(context);
            }
        } catch (InterruptedException|ExecutionException |TimeoutException e) {
            log.error("Failed to backup stacks", e);

            return Result.failure(context);
        }
        return Result.success(context);
    }
}
