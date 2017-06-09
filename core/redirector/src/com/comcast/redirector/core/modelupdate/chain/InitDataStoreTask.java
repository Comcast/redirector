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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;

public class InitDataStoreTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(InitDataStoreTask.class);

    private IAppModelFacade modelFacade;

    public InitDataStoreTask(IAppModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public Result handle(ModelContext context) {
        if (!modelFacade.isAvailable()) {
            try {
                log.info("Zk Cache is not started yet, trying to start");
                modelFacade.start();
            } catch (Exception e) {
                log.error("Failed to start ZK Cache");

                return Result.failure(context);
            }
        }

        return Result.success(context);
    }
}
