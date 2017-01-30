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

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;

public class BackupNewModelTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(BackupNewModelTask.class);

    private IModelHolder<SelectServer> flavorRulesHolder;
    private IModelHolder<Whitelisted> whiteListHolder;
    private IModelHolder<URLRules> urlRulesHolder;
    private IModelHolder<ModelMetadata> modelMetadataHolder;

    public BackupNewModelTask(IModelHolder<SelectServer> flavorRulesHolder,
                              IModelHolder<Whitelisted> whiteListHolder,
                              IModelHolder<URLRules> urlRulesHolder,
                              IModelHolder<ModelMetadata> modelMetadataHolder) {
        this.flavorRulesHolder = flavorRulesHolder;
        this.whiteListHolder = whiteListHolder;
        this.urlRulesHolder = urlRulesHolder;
        this.modelMetadataHolder = modelMetadataHolder;
    }

    @Override
    public Result handle(ModelContext context) {
        SelectServer flavorRules = context.getFlavorRules();
        URLRules urlRules = context.getUrlRules();
        Whitelisted whitelistedStacks = context.getWhitelistedStacks();

        boolean success = false;

        if (flavorRules != null && urlRules != null && whitelistedStacks != null) {
            log.info("Starting backup new model (Flavor Rules, URL Rules, Namespaced lists, Whitelists)");

            boolean flavorRulesSaved = backupFlavorRules(flavorRules);
            boolean urlRulesSaved = backupUrlRules(urlRules);
            boolean whitelistSaved = backupWhitelistedStacks(whitelistedStacks);
            boolean modelMetaDataSaved = backupModelMetadata(context.getModelVersion());

            success = flavorRulesSaved && urlRulesSaved && whitelistSaved && modelMetaDataSaved;
        } else {
            log.warn("Did not backup the model");
        }

        return (success) ? Result.success(context) : Result.failure(context);
    }

    private boolean backupFlavorRules(SelectServer flavorRules) {
        return flavorRulesHolder.backup(flavorRules);
    }

    private boolean backupUrlRules(URLRules urlRules) {
        return urlRulesHolder.backup(urlRules);
    }

    private boolean backupWhitelistedStacks(Whitelisted whitelistedStacks) {
        return whiteListHolder.backup(whitelistedStacks);
    }

    private boolean backupModelMetadata(int versionFromDataStore) {
        ModelMetadata modelMetadata = modelMetadataHolder.load(IModelHolder.GET_FROM_DATA_STORE);
        modelMetadata.setVersion(versionFromDataStore);
        return modelMetadataHolder.backup(modelMetadata);
    }
}
