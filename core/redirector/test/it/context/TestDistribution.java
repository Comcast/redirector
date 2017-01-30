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

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;

import java.util.List;
import java.util.Optional;

public class TestDistribution extends TestModelWrapper<Distribution> {
    private List<DistributionRule> rules;

    TestDistribution(List<DistributionRule> rules) {
        this.rules = rules;
    }

    @Override
    Serializer serializer() {
        return new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    }

    @Override
    public Distribution value() {
        Distribution builtDistribution = new Distribution();
        if (rules != null && !rules.isEmpty()) {
            for (int i = 0; i < rules.size(); i++) {
                builtDistribution.addRule(rules.get(i).distributionRule(i));
            }
        }

        return builtDistribution;
    }

    public String getFlavor(String distributionPercent) {
        Optional<DistributionRule> distributionRule = rules.stream()
            .filter(rule -> rule.percent.equals(distributionPercent)).findFirst();
        return distributionRule.isPresent() ? distributionRule.get().getFlavor() : null;
    }

    public String getFirstRuleFlavor() {
        return (rules.isEmpty()) ? null : rules.get(0).getFlavor();
    }
}
