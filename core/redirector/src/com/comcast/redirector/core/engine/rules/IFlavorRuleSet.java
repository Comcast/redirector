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

package com.comcast.redirector.core.engine.rules;

import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.DistributionServer;

import java.util.List;
import java.util.Map;

public interface IFlavorRuleSet extends IRuleSet<Model> {
    Object getResult(Map<String, String> context);

    Object getDefault();

    List<DistributionServer> getDistributionServers();
}
