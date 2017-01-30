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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirectorOffline;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;

import java.util.ArrayList;
import java.util.Collection;

public class ModelFactory {

    public static SelectServer newSelectServer(Collection<IfExpression> flavorRules, Distribution distribution, Server defaultServer) {

        SelectServer selectServer = new SelectServer();
        if (distribution == null) {
            distribution = new Distribution();
        }
        distribution.setDefaultServer(defaultServer);
        selectServer.setDistribution(distribution);
        selectServer.setItems(flavorRules);

        return selectServer;
    }

    public static URLRules newUrlRules(Collection<IfExpression> rules, UrlRule defaultUrlParams) {

        Default defaultStatement = new Default();
        if (defaultUrlParams != null) {
            defaultStatement.setUrlRule(defaultUrlParams);
        } else {
            defaultStatement.setUrlRule(new UrlRule());
        }

        URLRules urlRules = new URLRules();
        urlRules.setItems(rules);
        urlRules.setDefaultStatement(defaultStatement);

        return urlRules;
    }

    public static Whitelisted newWhitelisted(Whitelisted whitelisted) {

        if (whitelisted == null) {
            whitelisted = new Whitelisted();
            whitelisted.setPaths(new ArrayList<>());
        }

        return whitelisted;
    }

    public static WhitelistedStackUpdates newWhitelistedStackUpdates(WhitelistedStackUpdates whitelistedStackUpdates) {

        if (whitelistedStackUpdates == null) {
            whitelistedStackUpdates = new WhitelistedStackUpdates();
        }

        return whitelistedStackUpdates;
    }
}
