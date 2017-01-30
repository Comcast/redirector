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

package com.comcast.redirector.api.model.url.rule;

import com.comcast.redirector.api.model.IfExpression;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement(name = "urlRules")
@XmlType(propOrder = {"items", "defaultStatement"})

@XmlSeeAlso({IfExpression.class, UrlRule.class, Default.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class URLRules implements java.io.Serializable {

    @XmlAnyElement(lax = true)
    private Collection<IfExpression> items = new ArrayList<>();

    @XmlElement(name = "default")
    private Default defaultStatement;

    public Default getDefaultStatement() {
        return defaultStatement;
    }

    public void setDefaultStatement(Default defaultStatement) {
        this.defaultStatement = defaultStatement;
    }

    public Collection<IfExpression> getItems() {
        return items;
    }

    public void setItems(Collection<IfExpression> items) {
        this.items = items;
    }

    public IfExpression getUrlRule(final String ruleId) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        return (IfExpression)CollectionUtils.find(items, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((IfExpression)object).getId().equals(ruleId);
            }
        });
    }

    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URLRules urlRules = (URLRules) o;

        if (defaultStatement != null ? !defaultStatement.equals(urlRules.defaultStatement) : urlRules.defaultStatement != null)
            return false;
        if (items != null ? !items.equals(urlRules.items) : urlRules.items != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = items != null ? items.hashCode() : 0;
        result = 31 * result + (defaultStatement != null ? defaultStatement.hashCode() : 0);
        return result;
    }
}
