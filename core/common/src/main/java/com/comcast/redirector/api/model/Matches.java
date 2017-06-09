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
import java.util.Objects;

@XmlRootElement(name = "matches")
@XmlAccessorType(XmlAccessType.FIELD)
public class Matches extends VisitableExpression implements java.io.Serializable, Expressions, NegationSupport, SingleParameterExpression {

    @XmlAttribute(name = "negation")
    protected boolean negation = false;

    @XmlElement(name = "param", required = true)
    private String paramVal;
    @XmlElement(name = "pattern", required = true)
    private String patternVal;

    public String getParam() {
        return paramVal;
    }

    public void setParam(String param) {
        this.paramVal = param;
    }

    public String getPatternVal() {
        return patternVal;
    }

    public void setPattern(String pattern) {
        this.patternVal = pattern;
    }

    public void setValue(String value) {
        this.patternVal = value;
    }

    @Override
    public String getValue() {
        return patternVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matches matches = (Matches) o;
        return Objects.equals(negation, matches.negation) &&
                Objects.equals(paramVal, matches.paramVal) &&
                Objects.equals(patternVal, matches.patternVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(negation, paramVal, patternVal);
    }

    @Override
    public boolean isNegated() {
        return negation;
    }
}
