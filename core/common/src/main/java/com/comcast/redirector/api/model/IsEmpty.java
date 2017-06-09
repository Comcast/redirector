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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.*;
import java.util.Objects;

@XmlRootElement(name = "isEmpty")
@XmlAccessorType(XmlAccessType.FIELD)
public class IsEmpty extends VisitableExpression implements Expressions, NegationSupport {

    @XmlAttribute(name = "negation")
    private boolean negation = false;

    @XmlElement(name = "param", required = true)
    private String param;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsEmpty isEmpty = (IsEmpty) o;
        return Objects.equals(negation, isEmpty.negation) &&
                Objects.equals(param, isEmpty.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(negation, param);
    }

    public boolean isNegated() {
        return negation;
    }
}
