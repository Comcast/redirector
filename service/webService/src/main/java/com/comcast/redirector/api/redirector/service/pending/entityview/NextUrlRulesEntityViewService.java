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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingRulesPreviewHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.util.RulesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class NextUrlRulesEntityViewService implements IEntityViewService<URLRules> {

    @Autowired
    private IChangesStatusService changesStatusService;

    @Autowired
    private IUrlRulesService urlRulesService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Override
    public URLRules getEntity(String serviceName) {
        PendingChangesStatus pendingChanges = changesStatusService.getPendingChangesStatus(serviceName);

        // 1. get all rules
        Collection<IfExpression> urlRules = PendingRulesPreviewHelper.mergeRules(
                RulesUtils.mapFromCollection(urlRulesService.getUrlRules(serviceName)), pendingChanges.getUrlRules());

        // 2. get default url params
        PendingChange defaultUrlParamChange = pendingChanges.getUrlParams().get(RedirectorConstants.DEFAULT_SERVER_NAME);
        Default defaultUrlParams = new Default();
        if (defaultUrlParamChange != null) {
            defaultUrlParams.setUrlRule((UrlRule) defaultUrlParamChange.getChangedExpression());
        } else {
            defaultUrlParams.setUrlRule(urlParamsService.getUrlParams(serviceName));
        }

        return RulesUtils.buildURLRules(urlRules, defaultUrlParams);
    }

    @Override
    public URLRules getEntity(PendingChangesStatus pendingChangesStatus, URLRules currentEntity) {
        throw new UnsupportedOperationException();
    }

    void setChangesStatusService(IChangesStatusService changesStatusService) {
        this.changesStatusService = changesStatusService;
    }

    void setUrlRulesService(IUrlRulesService urlRulesService) {
        this.urlRulesService = urlRulesService;
    }

    void setUrlParamsService(IUrlParamsService urlParamsService) {
        this.urlParamsService = urlParamsService;
    }
}
