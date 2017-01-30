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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DistributionHelper {
    public static Distribution prepareDistribution(Map<Float, String> percentToStackMap) {
        Distribution distribution = new Distribution();
        List<Rule> rules = new ArrayList<>();

        int id = 0;
        for (Map.Entry<Float, String> percentToStack : percentToStackMap.entrySet()) {
            rules.add(getRule(++id, percentToStack.getKey(), percentToStack.getValue()));
        }

        distribution.setRules(rules);
        return distribution;
    }

    public static void prepareDistribution(Distribution distribution, int id, float percent, String path) {
        distribution.addRule(getRule(id, percent, path));
    }

    static Rule getRule(int id, float percent, String path) {
        Rule rule = new Rule();
        rule.setId(id);
        rule.setPercent(percent);

        Server server = new Server();
        server.setPath(path);
        server.setUrl("{protocol}://{host}:{port}/{urn}");
        rule.setServer(server);

        return rule;
    }
}
