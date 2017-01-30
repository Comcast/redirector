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
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelEntityLoader implements IEntityLoader {
    private static final Logger log = LoggerFactory.getLogger(ModelEntityLoader.class);

    private final FileUtil fileUtil;
    private final FilesPathHelper filesPathHelper;
    private final Context context;
    private final String appName;

    public ModelEntityLoader(FileUtil fileUtil, FilesPathHelper filesPathHelper, Context context) {
        this.fileUtil = fileUtil;
        this.filesPathHelper = filesPathHelper;
        this.context = context;
        this.appName = context.getServiceName();
    }

    @Override
    public boolean load() {
        log.info("Starting load data from backup files for application '{}'", appName);
        StackBackup stackBackup = loadStackBackup();
        Whitelisted whitelisted = loadWhitelisted();
        SelectServer selectServer = loadSelectServer();
        URLRules urlRules = loadUrlRules();

        context.setStackBackup(stackBackup);
        context.setWhitelisted(whitelisted);
        context.setSelectServer(selectServer);
        context.setUrlRules(urlRules);

        if (isValidData(context)) {
            log.info("Successfully loaded data from backup files for applications '{}'", appName);
            return true;
        } else {
            log.error("Failed load data from backup files for application '{}'", appName);
            return false;
        }
    }

    public boolean isValidData(Context context) {
        return (context.getUrlRules() != null) && (context.getSelectServer() != null) && (context.getWhitelisted() != null);
    }

    private Whitelisted loadWhitelisted() {
        return fileUtil.readXML(filesPathHelper.getFilename(FilesPathHelper.TestEntity.WHITELISTED, appName), Whitelisted.class);
    }

    private SelectServer loadSelectServer() {
        return fileUtil.readXML(filesPathHelper.getFilename(FilesPathHelper.TestEntity.SELECT_SERVER, appName), SelectServer.class);
    }

    private URLRules loadUrlRules() {
        return fileUtil.readXML(filesPathHelper.getFilename(FilesPathHelper.TestEntity.URL_RULES, appName), URLRules.class);
    }

    private StackBackup loadStackBackup() {
        return fileUtil.readJson(filesPathHelper.getFilename(FilesPathHelper.TestEntity.STATIC_STACKS, appName), StackBackup.class);
    }
}
