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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model.distribution;


import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.VisitableExpression;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "distribution")

@XmlSeeAlso({Rule.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Distribution extends VisitableExpression implements java.io.Serializable, Expressions {

    @XmlAnyElement(lax = true)
    private List<Rule> rules = new ArrayList<>();

    @XmlElement(required = false)
    private Server server;

    public Distribution() {
    }

    public static Distribution newInstance(Distribution distribution) {

        if (distribution == null) {
            return null;
        }

        Distribution distributionCopy = new Distribution();

        // copy rules
        List<Rule> rulesCopy = new ArrayList<>(distribution.getRules().size());
        for (Rule rule : distribution.getRules()) {
            rulesCopy.add(Rule.newInstance(rule));
        }


        Server defaultServerCopy = Server.newInstance(distribution.getDefaultServer());

        distributionCopy.setRules(rulesCopy);
        distributionCopy.setDefaultServer(defaultServerCopy);

        return distributionCopy;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public Server getDefaultServer() {
        return server;
    }

    public void setDefaultServer(Server server) {
        this.server = server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Distribution that = (Distribution) o;
        return Objects.equals(rules, that.rules) &&
                Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rules, server);
    }
}
