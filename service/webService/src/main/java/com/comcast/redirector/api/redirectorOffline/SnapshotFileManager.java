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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirectorOffline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class SnapshotFileManager {
    private static Logger log = LoggerFactory.getLogger(SnapshotFileManager.class);


    private SnapshotFilesPathHelper filesPathHelper;
    private SnapshotFilesPathHelper.SnapshotEntity snapshotEntityType;
    private IFileSystemHelper fileSystemHelper;

    public SnapshotFileManager(SnapshotFilesPathHelper filesPathHelper, SnapshotFilesPathHelper.SnapshotEntity snapshotEntityType, IFileSystemHelper fileSystemHelper) {
        this.filesPathHelper = filesPathHelper;
        this.snapshotEntityType = snapshotEntityType;
        this.fileSystemHelper = fileSystemHelper;
    }

    public synchronized void createSnapshot(byte[] utf8JsonSnapshot, String serviceName) {
        Path snapshotFile = fileSystemHelper.getPath(filesPathHelper.getFilename(snapshotEntityType));
        try {
            Path parentPath  = snapshotFile == null ? null : snapshotFile.getParent();
            if( snapshotFile == null) {
                log.error("Failed to create snapshot for {} : snapshot file is not defined", serviceName);
                return;
            }
            Files.createDirectories(parentPath);
            Files.write(snapshotFile, utf8JsonSnapshot);
        } catch (IOException e) {
            log.error("Failed to create snapshot for {}. {}", serviceName, e.getMessage());
        }
    }

    public synchronized byte[] getSnapshot() throws IOException {
        Path snapshotFile = fileSystemHelper.getPath(filesPathHelper.getFilename(snapshotEntityType));
        byte[] result;
        if (Files.exists(snapshotFile)) {
            try {
                result = Files.readAllBytes(snapshotFile);
            } catch (IOException e) {
                String errorMsg = String.format("Failed to read snapshot: %s", snapshotFile);
                log.error(errorMsg);
                throw new IOException(errorMsg, e);
            }
        }
        else {
            String errorMsg = String.format("Snapshot file not found: \"%s\"", snapshotFile);
            throw new FileNotFoundException(errorMsg);
        }
        return result;
    }
}
