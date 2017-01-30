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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;

public class GetWhitelistedStacksTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(GetWhitelistedStacksTask.class);

    private boolean fromDataStore;
    private IModelHolder<Whitelisted> whiteListHolder;
    private ModelTranslationService transformService;

    public GetWhitelistedStacksTask(boolean fromDataStore,
                                    IModelHolder<Whitelisted> whiteListHolder,
                                    ModelTranslationService transformService) {
        this.fromDataStore = fromDataStore;
        this.whiteListHolder = whiteListHolder;
        this.transformService = transformService;
    }

    @Override
    public Result handle(ModelContext context) {
        Whitelisted whitelistedStacks = whiteListHolder.load(fromDataStore);
        context.setWhitelistedStacks(whitelistedStacks); // TODO: this line is only needed for BackupNewModelTask. Find a way to get rid of this setter

        WhiteList whiteList = transformService.translateWhitelistedStacks(whitelistedStacks);

        if (whiteList != null) {
            context.setWhiteListModel(whiteList);
            return Result.success(context);
        } else {
            log.error("Failed to convert from UrlRules to URLRuleModel for {} model", (fromDataStore ? "Data Storage" : "Backup"));

            return Result.failure(context);
        }
    }
}
