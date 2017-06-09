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

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;

public class FlavorRulesHolder extends BaseModelHolder<SelectServer> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(FlavorRulesHolder.class);

    private IAppModelFacade modelFacade;

    public FlavorRulesHolder(IAppModelFacade modelFacade, Serializer serializer, IBackupManagerFactory backupManagerFactory) {
        super(SelectServer.class, serializer, backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.FLAVOR_RULES));
        this.modelFacade = modelFacade;
    }

    @Override
    protected SelectServer loadFromDataStore() {
        SelectServer selectServer = new SelectServer();
        Distribution distribution;
        try {
            long startMillis = System.currentTimeMillis();
            log.info("Start getting data from zk - startTime=" + startMillis);
            distribution = modelFacade.getDistribution();
            if (distribution == null) distribution = new Distribution();

            selectServer.setItems(modelFacade.getFlavorRules());
            distribution.setDefaultServer(modelFacade.getServer(RedirectorConstants.DEFAULT_SERVER_NAME));
            selectServer.setDistribution(distribution);
            long endMillis = System.currentTimeMillis();
            log.info("End getting data from zk - endTime=" + endMillis + ", total duration=" + (endMillis - startMillis) + " millis");

            return selectServer;
        } catch (Exception e) {
            log.error("Failed to build SelectServer from zkCache: {}", e);
            return null;
        }
    }
}
