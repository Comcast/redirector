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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.model.xrestack;

import javax.xml.bind.annotation.*;
import java.util.Objects;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PathItem {
    @XmlValue()
    private String value;

    @XmlAttribute(name = "nodes")
    private int activeNodesCount;

    @XmlAttribute(name = "nodesWhitelisted")
    private int whitelistedNodesCount;

    public PathItem() {
        this("", 0, 0);
    }

    public PathItem(String value, int activeNodesCount, int whitelistedNodesCount) {
        this.value = value;
        this.activeNodesCount = activeNodesCount;
        this.whitelistedNodesCount = whitelistedNodesCount;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getActiveNodesCount() {
        return activeNodesCount;
    }

    public void setActiveNodesCount(int activeNodesCount) {
        this.activeNodesCount = activeNodesCount;
    }

    public int getWhitelistedNodesCount() {
        return whitelistedNodesCount;
    }

    public void setWhitelistedNodesCount(int whitelistedNodesCount) {
        this.whitelistedNodesCount = whitelistedNodesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathItem pathItem = (PathItem) o;
        return Objects.equals(activeNodesCount, pathItem.activeNodesCount) &&
                Objects.equals(whitelistedNodesCount, pathItem.whitelistedNodesCount) &&
                Objects.equals(value, pathItem.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, activeNodesCount, whitelistedNodesCount);
    }
}
