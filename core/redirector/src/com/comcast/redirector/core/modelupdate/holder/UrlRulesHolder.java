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

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;

public class UrlRulesHolder extends BaseModelHolder<URLRules> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(UrlRulesHolder.class);

    private IAppModelFacade modelFacade;

    public UrlRulesHolder(IAppModelFacade modelFacade, Serializer serializer, IBackupManagerFactory backupManagerFactory) {
        super(URLRules.class, serializer, backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.URL_RULES));
        this.modelFacade = modelFacade;
    }

    @Override
    protected URLRules loadFromDataStore() {
        try {
            long startMillis = System.currentTimeMillis();
            log.info("Start getting data from zk - startTime=" + startMillis);
            URLRules urlRules = new URLRules();
            UrlRule defaultUrlParams = modelFacade.getUrlParams(RedirectorConstants.DEFAULT_URL_RULE);

            Default defaultStatement = new Default();
            if (defaultUrlParams != null) {
                defaultStatement.setUrlRule(defaultUrlParams);
            } else {
                defaultStatement.setUrlRule(new UrlRule());
            }

            urlRules.setItems(modelFacade.getUrlRules());
            urlRules.setDefaultStatement(defaultStatement);
            long endMillis = System.currentTimeMillis();
            log.info("End getting data from zk - endTime=" + endMillis + ", total duration=" + (endMillis - startMillis) + " millis");

            return urlRules;
        } catch (Exception e) {
            log.error("Failed to build URLRules from zkCache: {}", e.getMessage());
            return null;
        }
    }
}
