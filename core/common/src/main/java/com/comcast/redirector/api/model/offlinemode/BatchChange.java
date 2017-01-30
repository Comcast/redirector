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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model.offlinemode;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="batchChange")
@XmlSeeAlso({SelectServer.class, URLRules.class, Default.class, UrlRule.class, Distribution.class, Whitelisted.class, PendingChangesStatus.class, ServicePaths.class, Server.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class BatchChange {

    @XmlElementWrapper(name = "entitiesToSave")
    @XmlElements({
            @XmlElement(name="distribution", type = Distribution.class),
            @XmlElement(name="whitelisted", type = Whitelisted.class),
            @XmlElement(name="urlRule", type = UrlRule.class),
            @XmlElement(name="if", type = IfExpression.class),
            @XmlElement(name="server", type = Server.class)
    })
    private List<Expressions> entitiesToSave;

    // ids of entities to be deleted
    @XmlElement(name = "entitiesToDelete")
    private List<String> entitiesToDelete = new ArrayList<>();

    public List<Expressions> getEntitiesToSave() {
        return entitiesToSave;
    }

    public void setEntitiesToSave(List<Expressions> entitiesToSave) {
        this.entitiesToSave = entitiesToSave;
    }

    public void addEntityToSave(Expressions entityToSave) {
        if (CollectionUtils.isEmpty(entitiesToSave)) {
            entitiesToSave = new ArrayList<Expressions>();
        }
        entitiesToSave.add(entityToSave);
    }

    public List<String> getEntitiesToDelete() {
        return entitiesToDelete;
    }

    public void setEntitiesToDelete(List<String> entitiesToDelete) {
        this.entitiesToDelete = entitiesToDelete;
    }

    public void addEntityToDelete(String id) {
        if (StringUtils.isNotBlank(id)) {
            entitiesToDelete.add(id);
        }
    }
}
