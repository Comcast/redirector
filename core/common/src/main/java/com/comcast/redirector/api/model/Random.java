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

package com.comcast.redirector.api.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "random")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Random extends VisitableExpression implements java.io.Serializable, Expressions, UnaryExpression {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        try {
            // this is needed to filter out values like: "00100" by converting to "100"
            int intValue = Integer.parseInt(value);
            value = String.valueOf(intValue);
        } catch (Exception ex) {
            // silently ignoring an exception here because this expression will be validated later
        }
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Random random = (Random) o;
        return Objects.equals(value, random.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
