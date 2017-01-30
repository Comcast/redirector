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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonEntityLoader implements IEntityLoader {
    private static final Logger log = LoggerFactory.getLogger(CommonEntityLoader.class);

    private final FileUtil fileUtil;
    private final FilesPathHelper filesPathHelper;
    private final Context context;

    public CommonEntityLoader(FileUtil fileUtil, FilesPathHelper filesPathHelper, Context context) {
        this.fileUtil = fileUtil;
        this.filesPathHelper = filesPathHelper;
        this.context = context;
    }

    @Override
    public boolean load() {
        log.info("Starting load all data from backup files");
        StackBackup stackBackup = loadStackBackup();
        RedirectorConfig redirectorConfig = loadRedirectorConfig();
        NamespacedListsBatch namespaces = loadNamespacedListsBatch();
        context.setStackBackup(stackBackup);
        context.setRedirectorConfig(redirectorConfig);
        context.setNamespaces(namespaces);

        if (isValidData(context)) {
            log.info("Successfully loaded common data from backup files");
            return true;
        } else {
            log.error("Failed load common data from backup files");
            throw new IllegalArgumentException("Failed load common data from backup files");
        }
    }

    public boolean isValidData(Context context) {
        return (context.getRedirectorConfig() != null) && (context.getNamespaces() != null);
    }

    private NamespacedListsBatch loadNamespacedListsBatch() {
        return fileUtil.readJson(filesPathHelper.getFilename(FilesPathHelper.TestEntity.NAMESPACED_LISTS), NamespacedListsBatch.class);
    }

    private RedirectorConfig loadRedirectorConfig() {
        return fileUtil.readJson(filesPathHelper.getFilename(FilesPathHelper.TestEntity.REDIRECTOR_CONFIG), RedirectorConfig.class);
    }

    private StackBackup loadStackBackup() {
        return fileUtil.readJson(filesPathHelper.getFilename(FilesPathHelper.TestEntity.DYNAMIC_STACKS), StackBackup.class);
    }
}
