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

package com.comcast.redirector.core.applications;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.core.backup.IBackupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationsBackup implements IApplicationsBackup {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsBackup.class);

    private IBackupManager backupManager;
    private Serializer serializer;

    public ApplicationsBackup(IBackupManager backupManager, Serializer serializer) {
        this.backupManager = backupManager;
        this.serializer = serializer;
    }

    @Override
    public void backup(Applications applications) {
        try {
            backupManager.backup(serializer.serialize(applications, true));
        } catch (SerializerException e) {
            log.error("Failed to serialize apps list", e);
        }
    }

    @Override
    public Applications load() {
        String backup = backupManager.load();
        try {
            return (backup == null) ? null : serializer.deserialize(backupManager.load(), Applications.class);
        } catch (SerializerException e) {
            log.error("Failed to deserialize apps list", e);
            return null;
        }
    }
}
