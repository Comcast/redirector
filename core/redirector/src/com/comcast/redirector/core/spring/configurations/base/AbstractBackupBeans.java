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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.core.spring.configurations.base;

import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.core.backup.InMemoryBackupManagerFactory;
import com.comcast.redirector.core.backup.filesystem.FileSystemBackupFiles;
import com.comcast.redirector.core.backup.filesystem.FileSystemBackupManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.backup.IAppBackupManagerFactories;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.RedirectorEngineFactory;
import com.comcast.redirector.core.spring.AppScope;
import com.comcast.redirector.core.spring.AppsContextHolder;
import com.comcast.redirector.core.spring.configurations.common.BackupBeans;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public abstract class AbstractBackupBeans {
    private static final Logger log = LoggerFactory.getLogger(BackupBeans.class);

    @Bean
    public RedirectorEngineFactory.BackupMode backupMode(ZKConfig config) {
        if (config.isEndToEndModeEnabled()) {
            return RedirectorEngineFactory.BackupMode.IN_MEMORY;
        }
        return RedirectorEngineFactory.BackupMode.FILE_SYSTEM;
    }

    public IBackupManagerFactory createBackupManagerFactory(String appName, ZKConfig config) {
        String basePath = config.getBackupBasePath();
        if (backupMode(config) == RedirectorEngineFactory.BackupMode.IN_MEMORY) {
            return new InMemoryBackupManagerFactory();
        } else if (backupMode(config) == RedirectorEngineFactory.BackupMode.FILE_SYSTEM) {
            if (StringUtils.isBlank(basePath)) {
                log.error("MISSING CONFIGURATION PARAMETER: backupBasePath");
            }
            return new FileSystemBackupManagerFactory(new FileSystemBackupFiles(appName, basePath));
        }

        return null;
    }

    @Bean
    @Scope(AppScope.APP_SCOPE)
    public IBackupManagerFactory backupManagerFactory(String appName, ZKConfig config) {
        return createBackupManagerFactory(appName, config);
    }

    @Bean
    public IBackupManagerFactory globalBackupManagerFactory(ZKConfig config) {
        return createBackupManagerFactory(AppsContextHolder.GLOBAL_APP, config);
    }

    @Bean
    public IAppBackupManagerFactories appBackupManagerFactories(ZKConfig config) {
        return appName -> {
            String currentApp = AppsContextHolder.getCurrentApp();
            AppsContextHolder.setCurrentApp(appName);

            IBackupManagerFactory result = backupManagerFactory(appName, config);

            AppsContextHolder.setCurrentApp(currentApp);

            return result;
        };
    }
}
