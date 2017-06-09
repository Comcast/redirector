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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@XmlRootElement(name = "Rules")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Value.class})
public class Rules implements Expressions {

    @XmlElement(name = "value", type = Value.class)
    private Set<Value> ruleIds;

    public Set<String> getRuleIds() {

        if (ruleIds == null) {
            return null;
        }

        Set<String> result = new LinkedHashSet<String>();
        for (Value val : ruleIds) {
            result.add(val.getValue());
        }
        return result;
    }

    public void setRuleIds(final Set<Value> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public void addRule(final String id) {
        if (ruleIds == null) {
            ruleIds = new LinkedHashSet<Value>();
        }
        Value val = new Value(id);

        if (!ruleIds.contains(val)) {
            ruleIds.add(val);
        }
    }

    public void deleteRule(final String id){
        Value val = new Value(id);
        if (ruleIds != null && ruleIds.contains(val)){
            ruleIds.remove(val);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rules rules = (Rules) o;
        return Objects.equals(ruleIds, rules.ruleIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleIds);
    }
}
