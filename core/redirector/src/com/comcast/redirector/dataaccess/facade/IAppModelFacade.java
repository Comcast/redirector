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

package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.core.modelupdate.NewVersionHandler;

import java.util.Collection;

public interface IAppModelFacade extends IModelFacade {

    int getModelVersion();

    Server getServer(String serverName);

    Distribution getDistribution();

    Whitelisted getWhitelist();

    Collection<IfExpression> getFlavorRules();

    UrlRule getUrlParams(String urlParamsRuleName);

    Collection<IfExpression> getUrlRules();

    IfExpression getUrlRule(String ruleId);

    boolean isModelExists();

    void notifyModelRefreshCompleted(int version);

    void notifyStacksReloadCompleted(int version);

    void initModelChangedPolling(NewVersionHandler<Integer> refreshModel);

    void initStacksReloadPolling(NewVersionHandler<Integer> refreshStacks);

    void suspendPolling();

    int getModelVersion(String serviceName);

    void setNextModelVersion(Integer nextModelVersion);

    Integer getNextModelVersion();
    
    void restartPollingIfSuspended();
}
