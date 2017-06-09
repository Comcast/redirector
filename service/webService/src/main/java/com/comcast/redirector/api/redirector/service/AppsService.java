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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.AppNames;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.dataaccess.dao.IListDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AppsService implements IAppsService {
    private static final Logger log = LoggerFactory.getLogger(AppsService.class);

    @Autowired
    private IStacksService stacksService;
    
    @Autowired
    private IListDAO<String> applicationsDAO;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Override
    public AppNames getAppNames() {
        AppNames appNames = new AppNames();
        for (String serviceName : stacksService.getAllServiceNames()) {
            appNames.add(serviceName);
        }
        try {
            for (String serviceName : applicationsDAO.getAllIDs()) {
                try {
                    UrlRule defaultUrlRule = urlParamsService.getDefaultUrlParams(serviceName).getUrlRule();
                    if (serverService.getServer(serviceName) != null
                            && defaultUrlRule.getPort() != null
                            && defaultUrlRule.getProtocol() != null
                            && defaultUrlRule.getIpProtocolVersion() != null
                            && defaultUrlRule.getUrn() != null) {
                        appNames.add(serviceName);
                    }
                } catch (Exception e) {
                    log.error("Exception is caught while trying to get default params or default server", e);
                }
            }
        } catch (Exception e) {
            log.error("Exception is caught while trying to get requested applications", e);
        }

        return appNames;
    }

    @Override
    public boolean isApplicationExists(String serviceName) {
        final Set<String> appNames = getAppNames().getAppNames();
        return appNames.contains(serviceName);
    }
    
    @Override
    public AppNames getAllRegisteredApps() {
        AppNames appNames = new AppNames();
        for (String serviceName : stacksService.getAllServiceNames()) {
            appNames.add(serviceName);
        }
        return appNames;
    }
}
