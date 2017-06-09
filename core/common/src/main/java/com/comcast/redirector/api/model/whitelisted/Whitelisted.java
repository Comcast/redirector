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
 * @author {authorPlaceHolder}
 */

package com.comcast.redirector.api.model.whitelisted;


import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.VisitableExpression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "whitelisted")
@XmlAccessorType(XmlAccessType.FIELD)
public class Whitelisted extends VisitableExpression implements java.io.Serializable, Expressions {

    public Whitelisted() {
    }

    public Whitelisted(List<String> paths) {
        this.paths = paths;
    }

    @XmlElement(name = "paths")
    private List<String> paths = new ArrayList<>();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Whitelisted that = (Whitelisted) o;
        return Objects.equals(paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths);
    }
}
