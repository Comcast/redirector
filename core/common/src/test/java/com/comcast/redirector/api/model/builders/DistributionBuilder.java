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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.api.model.builders;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;

import java.util.ArrayList;
import java.util.List;

public class DistributionBuilder {
    private List<Rule> rules = new ArrayList<>();
    private Server defaultServer;

    public DistributionBuilder withRule(Rule rule) {
        rules.add(rule);
        return this;
    }

    public DistributionBuilder withRules(List<Rule> rules) {
        this.rules = rules;
        return this;
    }

    public DistributionBuilder withDefaultServer(Server defaultServer) {
        this.defaultServer = defaultServer;
        return this;
    }

    public Distribution build() {
        Distribution distribution = new Distribution();
        distribution.setDefaultServer(defaultServer);
        distribution.setRules(rules);
        return distribution;
    }
}
