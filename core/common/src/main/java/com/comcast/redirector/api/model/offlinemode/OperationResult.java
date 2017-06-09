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

package com.comcast.redirector.api.model.offlinemode;

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.NamespacedEntities;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.model.*;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="operationResult")
@XmlSeeAlso({ErrorMessage.class, SelectServer.class, URLRules.class, Default.class, URLRules.class, Distribution.class, Whitelisted.class, PendingChangesStatus.class, ServicePaths.class, Server.class, DistributionWithDefaultAndFallbackServers.class, NamespacedEntities.class, NamespacedList.class, Value.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationResult implements GenericOperationResult {

    @XmlElement(name = "pendingChanges", required = true)
    private PendingChangesStatus pendingChanges;

    @XmlAnyElement(lax=true)
    private Expressions approvedEntity;

    @XmlElement(name = "methodResponce")
    private Object methodResponce;

    @XmlElement(name="validationResult")
    private ErrorMessage errorMessage;

    @XmlElementWrapper(name = "entitiesToUpdate")
    @XmlElements({
            @XmlElement(name="servicePaths", type = ServicePaths.class),
            @XmlElement(name="whitelistedStackUpdates", type = WhitelistedStackUpdates.class),
    })
    private List<Object> entitiesToUpdate = new ArrayList<>();

    public OperationResult() {
    }

    public OperationResult(PendingChangesStatus pendingChanges, Expressions approvedEntity) {
        this.pendingChanges = pendingChanges;
        this.approvedEntity = approvedEntity;
    }

    public OperationResult(PendingChangesStatus pendingChanges) {
        this.pendingChanges = pendingChanges;
    }

    public PendingChangesStatus getPendingChanges() {
        return pendingChanges;
    }

    public void setPendingChanges(PendingChangesStatus pendingChanges) {
        this.pendingChanges = pendingChanges;
    }

    public Expressions getApprovedEntity() {
        return approvedEntity;
    }

    public void setApprovedEntity(Expressions approvedEntity) {
        this.approvedEntity = approvedEntity;
    }

    public Object getMethodResponce() {
        return methodResponce;
    }

    public void setMethodResponce(Object methodResponce) {
        this.methodResponce = methodResponce;
    }

    public List<Object> getEntitiesToUpdate() {
        return entitiesToUpdate;
    }

    public void setEntitiesToUpdate(List<Object> entitiesToUpdate) {
        this.entitiesToUpdate = entitiesToUpdate;
    }

    public void addEntityToUpdate(Object entityToUpdate) {
        this.entitiesToUpdate.add(entityToUpdate);
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }
}
