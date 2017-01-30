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

package com.comcast.redirector.core.applications;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelRefreshManager implements IModelRefreshManager {
    private static Logger log = LoggerFactory.getLogger(ModelRefreshManager.class);

    @Autowired
    private IBackupManagerFactory backupManagerFactory;
    @Autowired
    private IAppModelFacade appModelFacade;
    @Autowired
    private IDataSourceConnector connector;
    @Autowired
    private Serializer xmlSerializer;

    @Override
    public boolean isModelExists() {
        if (appModelFacade.isModelExists()) {
            return true;
        } else {
            Whitelisted whitelist = getWhitelist();
            SelectServer flavorRules = getSelectServer();
            URLRules urlRules = getUrlRules();

            return whitelist != null && whitelist.getPaths().size() > 0
                && flavorRules != null && flavorRules.getDistribution() != null && flavorRules.getDistribution().getDefaultServer() != null
                && urlRules != null && urlRules.getDefaultStatement().getUrlRule() != null;
        }
    }

    @Override
    public void notifyModelRefreshCompleted(int version) {
        appModelFacade.notifyModelRefreshCompleted(version);
    }

    @Override
    public void notifyStacksReloadCompleted(int version) {
        appModelFacade.notifyStacksReloadCompleted(version);
    }

    private Whitelisted getWhitelist() {
        String data = backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.WHITE_LIST).load();
        try {
            if (data != null)
                return xmlSerializer.deserialize(data, Whitelisted.class);
        } catch (SerializerException e) {
            log.error("Can't deserialize object of type {}", Whitelisted.class.getSimpleName());
        }
        return null;
    }

    private SelectServer getSelectServer() {
        String data = backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.FLAVOR_RULES).load();
        try {
            if (data != null)
                return xmlSerializer.deserialize(data, SelectServer.class);
        } catch (SerializerException e) {
            log.error("Can't deserialize object of type {}", SelectServer.class.getSimpleName());
        }
        return null;
    }

    private URLRules getUrlRules() {
        String data = backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.URL_RULES).load();
        try {
            if (data != null)
                return xmlSerializer.deserialize(data, URLRules.class);
        } catch (SerializerException e) {
            log.error("Can't deserialize object of type {}", URLRules.class.getSimpleName());
        }
        return null;
    }
}
