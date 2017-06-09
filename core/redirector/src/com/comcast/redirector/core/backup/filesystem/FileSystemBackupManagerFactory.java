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

import com.comcast.redirector.common.thread.ThreadUtils;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.backup.AbstractBackupManagerFactory;
import com.comcast.redirector.core.backup.IBackupManager;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO: unit test
public class FileSystemBackupManagerFactory extends AbstractBackupManagerFactory {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(FileSystemBackupManagerFactory.class);

    private IFileSystemBackupFiles fileSystemBackupFiles;

    // TODO: task queue
    private ExecutorService fileWriter = Executors.newSingleThreadExecutor(ThreadUtils.newThreadFactory("FileSystemBackupManagerFactory"));

    public FileSystemBackupManagerFactory(IFileSystemBackupFiles fileSystemBackupFiles) {
        this.fileSystemBackupFiles = fileSystemBackupFiles;
    }

    @Override
    protected IBackupManager createBackupManager(BackupEntity backupEntity) {
        return new FileSystemBackupManager(backupEntity);
    }

    private class FileSystemBackupManager implements IBackupManager {
        private String fileName;
        private BackupEntity backupEntity;
        private boolean logSuccessBackup = true;

        private ReadWriteLock lock = new ReentrantReadWriteLock();

        private FileSystemBackupManager(BackupEntity entity) {
            this.fileName = fileSystemBackupFiles.getFilename(entity);
            backupEntity = entity;
            if (StringUtils.isBlank(fileName)) {
                log.warn("Failed to create backup file for: {}", entity);
            } else {
                log.info("Backup manager created for {}. File: {}", entity, fileName);
                if (backupEntity == BackupEntity.STACKS_AUTOMATIC) {
                    logSuccessBackup = false;
                }
            }
        }

        @Override
        public Future<Boolean> backup(final String data) {
            return fileWriter.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    if (StringUtils.isBlank(fileName)) {
                        log.warn("Failed to backup {} : backup file name is not defined", backupEntity);
                        return false;
                    }

                    lock.writeLock().lock();
                    try {
                        Path file = Paths.get(fileName);
                        if(file == null) {
                            log.warn("Failed to backup {} : backup file is not defined", backupEntity);
                            return false;
                        } else {
                            Path parentPath = file.getParent();
                            if( parentPath == null) {
                                log.warn("Failed to backup {} : backup file is not defined", backupEntity);
                                return false;
                            }
                            Files.createDirectories(parentPath);
                            Files.write(file, data.getBytes("UTF-8"));
                        }
                        if (logSuccessBackup) {
                            log.info("{} was backup successfully into {}", backupEntity, fileName);
                        }

                        return true;
                    } catch (IOException e) {
                        log.error("Failed to write data to backup: {}", e.getMessage());
                        return false;
                    } finally {
                        lock.writeLock().unlock();
                    }
                }
            });
        }

        @Override
        public String load() {
            String result = null;
            lock.readLock().lock();
            try {
                if (StringUtils.isNotBlank(fileName)) {
                    Path path = Paths.get(fileName);
                    if (Files.exists(path)) {
                        byte[] b = Files.readAllBytes(path);
                        result = new String(b, "UTF-8");
                        if (StringUtils.isBlank(result)) {
                            log.warn("Failed to read backup {}: data is absent", backupEntity);
                        }
                    } else {
                        log.warn("Failed to read backup {}: file is absent", backupEntity);
                    }
                } else {
                    log.warn("Failed to read backup {}: backupFileName is not defined in config", backupEntity);
                }
            } catch (IOException e) {
                log.error("Failed ro read data from backup: {}", e.getMessage());
            } finally {
                lock.readLock().unlock();
            }

            return result;
        }
    }
}
