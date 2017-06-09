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

package com.comcast.redirector.core.spring;

import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.RedirectorEngineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

@Configuration
@Import(IntegrationTestBackupBeans.class)
public class FileSystemIntegrationTestBackupBeans {

    @Bean
    public RedirectorEngineFactory.BackupMode backupMode(ZKConfig zkConfig) {
        return RedirectorEngineFactory.BackupMode.FILE_SYSTEM;
    }

    @Bean
    public InjectPathBackupBeanPostProcessor injectPathBackupAwareBeanPostProcessor() throws URISyntaxException {
        InjectPathBackupBeanPostProcessor injectPathBackupBeanPostProcessor = new InjectPathBackupBeanPostProcessor();
        injectPathBackupBeanPostProcessor.setBackupBasePath(determineBackupBasePath());
        return injectPathBackupBeanPostProcessor;
    }

    public String determineBackupBasePath () throws URISyntaxException {
        URL currentDir = FileSystemIntegrationTestBackupBeans.class.getResource(".");
        return Paths.get(currentDir.toURI()).toFile().getAbsolutePath() + File.separator + "tmp";
    }
}
