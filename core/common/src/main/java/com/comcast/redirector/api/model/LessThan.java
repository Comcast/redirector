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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model;

import com.comcast.redirector.common.RedirectorConstants;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Objects;

@XmlRootElement(name = "lessThan")
@XmlAccessorType(XmlAccessType.FIELD)
public class LessThan extends VisitableExpression implements Serializable, Expressions, TypedSingleParameterExpression {

    @XmlAttribute(name = "type")
    String type = RedirectorConstants.VALUE_TYPE_NONE;

    @XmlElement(name = "param", required = true)
    private String param;
    @XmlElement(name = "value", required = true)
    private String value;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessThan lessThan = (LessThan) o;
        return Objects.equals(type, lessThan.type) &&
                Objects.equals(param, lessThan.param) &&
                Objects.equals(value, lessThan.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, param, value);
    }
}
