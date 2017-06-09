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
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.util.RulesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CurrentUrlRulesEntityViewService implements IEntityViewService<URLRules> {
    @Autowired
    private IUrlRulesService urlRulesService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Override
    public URLRules getEntity(String serviceName) {
        Collection<IfExpression> rules = urlRulesService.getUrlRules(serviceName);

        Default defaultStatement = new Default();
        UrlRule urlParams = urlParamsService.getUrlParams(serviceName);
        if (urlParams == null) {
            defaultStatement.setUrlRule(new UrlRule());
        } else {
            defaultStatement.setUrlRule(urlParams);
        }

        return RulesUtils.buildURLRules(rules, defaultStatement);
    }

    @Override
    public URLRules getEntity(PendingChangesStatus pendingChangesStatus, URLRules currentEntity) {
        throw new UnsupportedOperationException();
    }
}
