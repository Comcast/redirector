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
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;


public class  ApplyNewModelTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(ApplyNewModelTask.class);

    private final boolean fromDataStore;
    private final IRedirectorEngineFactory redirectorEngineFactory;

    public ApplyNewModelTask(boolean fromDataStore, IRedirectorEngineFactory redirectorEngineFactory) {
        this.fromDataStore = fromDataStore;
        this.redirectorEngineFactory = redirectorEngineFactory;
    }

    @Override
    public Result handle(ModelContext context) {
        String appName = context.getAppName();
        Model model = context.getFlavorRulesModel();
        URLRuleModel urlRuleModel = context.getUrlRulesModel();
        WhiteList whiteList = context.getWhiteListModel();
        StackBackup stacks = context.getMainStacksBackup();

        if (isModelNotEmpty(appName, model, urlRuleModel, whiteList, stacks)) {
            context.setRedirectorEngine(
                    redirectorEngineFactory.newRedirectorEngine(appName, model, urlRuleModel, whiteList, stacks.getAllStacks(), context.getModelVersion()));

            log.info("New model of version {} is successfully applied from {}", ((fromDataStore) ? "data store" : "backup"), context.getModelVersion());
            return Result.success(context);
        } else {
            log.warn("Failed to apply model of version={}", context.getModelVersion());
            return Result.failure(context);
        }
    }

    private static boolean isModelNotEmpty(String appName, Model model, URLRuleModel urlRuleModel, WhiteList whiteList, StackBackup stacks) {
        return appName != null &&
                model != null &&
                urlRuleModel != null &&
                whiteList != null &&
                stacks != null;
    }

    public boolean isFromDataStore() {
        return fromDataStore;
    }
}
