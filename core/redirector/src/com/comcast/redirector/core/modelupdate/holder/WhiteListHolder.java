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

import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.backup.IBackupManagerFactory;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;

import java.util.ArrayList;

public class WhiteListHolder extends BaseModelHolder<Whitelisted> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(WhiteListHolder.class);

    private IAppModelFacade modelFacade;

    public WhiteListHolder(IAppModelFacade modelFacade, Serializer serializer, IBackupManagerFactory backupManagerFactory) {
        super(Whitelisted.class, serializer, backupManagerFactory.getBackupManager(IBackupManagerFactory.BackupEntity.WHITE_LIST));
        this.modelFacade = modelFacade;
    }

    @Override
    protected Whitelisted loadFromDataStore() {
        long startMillis = System.currentTimeMillis();
        log.info("Start getting data from zk - startTime=" + startMillis);

        Whitelisted whitelisted = modelFacade.getWhitelist();
        if (whitelisted == null) {
            whitelisted = new Whitelisted();
            whitelisted.setPaths(new ArrayList<>());
        }
        long endMillis = System.currentTimeMillis();
        log.info("End getting data from zk - endTime=" + endMillis  + ", total duration=" + (endMillis - startMillis) + " millis");

        return whitelisted;
    }
}
