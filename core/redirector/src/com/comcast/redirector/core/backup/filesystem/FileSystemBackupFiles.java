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

package com.comcast.redirector.core.backup.filesystem;

import com.comcast.redirector.core.backup.IBackupManagerFactory.BackupEntity;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileSystemBackupFiles implements IFileSystemBackupFiles {
    private String appName;
    private String basePath;

    // TODO: make format of backup be specified from prop file
    public static final Map<BackupEntity, String> filenames =
            new HashMap<BackupEntity, String>() {{
                put(BackupEntity.STACKS_AUTOMATIC, "automaticbackup.json");
                put(BackupEntity.STACKS_MANUAL, "manualbackup.json");
                put(BackupEntity.FLAVOR_RULES, "selectserver.xml");
                put(BackupEntity.URL_RULES, "urlrules.xml");
                put(BackupEntity.WHITE_LIST, "whitelist.xml");
                put(BackupEntity.MODEL_METADATA, "modelmetadata.json");
                put(BackupEntity.NAMESPACED_LISTS, "namespacedlists.json");
                put(BackupEntity.APPLICATIONS, "applications.json");
                put(BackupEntity.DISCOVERY, "stacks.json");
            }};

    public FileSystemBackupFiles(String appName, String basePath) {
        this.appName = appName;
        this.basePath = basePath;
    }

    public FileSystemBackupFiles(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String getFilename(BackupEntity backupEntity) {
        String fileName = null;
        if (!StringUtils.isBlank(basePath)) {
            fileName = createFileName(backupEntity);
        }
        return fileName;
    }

    private String createFileName(BackupEntity backupEntity) {
        return (filenames.containsKey(backupEntity)) ?
            Joiner.on(File.separator).skipNulls()
                .join(
                    basePath,
                    (isGlobal(backupEntity) ? null : appName),
                    filenames.get(backupEntity))
            : null;
    }

    private boolean isGlobal(BackupEntity backupEntity) {
        return Stream.of(BackupEntity.NAMESPACED_LISTS, BackupEntity.DISCOVERY, BackupEntity.APPLICATIONS)
            .anyMatch(backupEntity::equals);
    }
}
