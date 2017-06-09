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

package com.comcast.redirector.api.model.search;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.ServerGroup;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "result")
@XmlSeeAlso({NamespacedListEntity.class, RuleEntity.class, Server.class, ServerGroup.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class NamespacedListSearchResult {
    private String searchItem;

    @XmlElement(type = NamespacedListEntity.class)
    private List<NamespacedListEntity> namespacedLists;

    public String getSearchItem() {
        return searchItem;
    }

    public void setSearchItem(String searchItem) {
        this.searchItem = searchItem;
    }

    public List<NamespacedListEntity> getNamespacedLists() {
        return namespacedLists;
    }

    public void setNamespacedLists(List<NamespacedListEntity> namespacedLists) {
        this.namespacedLists = namespacedLists;
    }
}
