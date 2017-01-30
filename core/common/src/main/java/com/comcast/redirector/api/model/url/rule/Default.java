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

import com.comcast.redirector.api.model.Expressions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.Objects;

@XmlRootElement(name = "default")

@XmlSeeAlso({UrlRule.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Default implements Expressions{

    private UrlRule urlRule;

    public Default() {
    }

    public Default(UrlRule urlRule) {
        this.urlRule = urlRule;
    }

    public UrlRule getUrlRule() {
        return urlRule;
    }

    public void setUrlRule(UrlRule urlRule) {
        this.urlRule = urlRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Default aDefault = (Default) o;
        return Objects.equals(urlRule, aDefault.urlRule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlRule);
    }
}
