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

import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.modelupdate.chain.validator.UrlRulesValidator;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUrlRulesTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(GetUrlRulesTask.class);

    private boolean fromDataStore;
    private IModelHolder<URLRules> urlRulesHolder;
    private ModelTranslationService transformService;
    private NamespacedListRepository namespacedLists;

    public GetUrlRulesTask(boolean fromDataStore, IModelHolder<URLRules> urlRulesHolder, ModelTranslationService transformService, NamespacedListRepository namespacedLists) {
        this.fromDataStore = fromDataStore;
        this.urlRulesHolder = urlRulesHolder;
        this.transformService = transformService;
        this.namespacedLists = namespacedLists;
    }

    @Override
    public Result handle(ModelContext context) {
        URLRules urlRules = load();
        context.setUrlRules(urlRules); // TODO: this line is only needed for BackupNewModelTask. Find a way to get rid of this setter

        ValidationReport validationReport = validate(urlRules);

        if (validationReport.isSuccessValidation()) {
            URLRuleModel urlRulesModel = translate(urlRules);

            if (urlRulesModel != null) {
                context.setUrlRulesModel(urlRulesModel);
                return Result.success(context);
            } else {
                log.error("Failed to convert from UrlRules to URLRuleModel for {} model", (fromDataStore ? "Data Storage" : "Backup"));

                return Result.failure(context);
            }
        } else {
            return Result.failure(context, validationReport);
        }
    }

    private URLRules load() {
        return urlRulesHolder.load(fromDataStore);
    }

    private ValidationReport validate(URLRules urlRules) {
        ValidationReport report = new UrlRulesValidator().validate(urlRules);

        if (report == null) {
            log.error("Validation failed. Reason: unknown. Got invalid Url Rules from " + (fromDataStore ? "Data Storage" : "Backup"));
            report = new ValidationReport(Validator.ValidationResultType.FAILURE);
        } else if (!report.isSuccessValidation()) {
            report.setMessage("Validation failed: " + report.getMessage() + ". Got invalid Url Rules from" + (fromDataStore ? "Data Storage" : "Backup"));
        }

        return report;
    }

    private URLRuleModel translate(URLRules urlRules) {
        return transformService.translateUrlRules(urlRules, namespacedLists);
    }
}
