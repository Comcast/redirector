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

package it.context;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Rule;

public class DistributionRule {
    String percent = "100.0", flavor;

    Rule distributionRule(int id) {
        Server server = new Server();
        server.setUrl(RedirectorConstants.URL_TEMPLATE);
        server.setPath(flavor);

        Rule rule = new Rule();
        rule.setId(id);
        rule.setPercent(Float.valueOf(percent));
        rule.setServer(server);

        return rule;
    }

    public String getFlavor() {
        return flavor;
    }
}
