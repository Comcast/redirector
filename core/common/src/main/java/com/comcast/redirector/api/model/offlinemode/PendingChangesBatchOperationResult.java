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

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.ServicePaths;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="PendingChangesBatchOperationResult")
@XmlSeeAlso({SelectServer.class, URLRules.class, Default.class, URLRules.class, Distribution.class, Whitelisted.class, PendingChangesStatus.class, ServicePaths.class, Server.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class PendingChangesBatchOperationResult implements GenericOperationResult {

    @XmlElement(name="pathRules")
    private BatchChange pathRules;

    @XmlElement(name="urlRules")
    private BatchChange urlRules;

    @XmlElement(name="templatePathRules")
    private BatchChange templatePathRules;

    @XmlElement(name="templateUrlPathRules")
    private BatchChange templateUrlRules;

    @XmlElement(name="whitelisted")
    private BatchChange whitelist;

    @XmlElement(name="servers")
    private BatchChange servers;

    @XmlElement(name="distributions")
    private BatchChange distribution;

    @XmlElement(name="urlParams")
    private BatchChange defaultUrlParams;

    @XmlElement(name="validationResult")
    private ErrorMessage errorMessage;

    @XmlElementWrapper(name = "entitiesToUpdate")
    @XmlElements({
            @XmlElement(name="servicePaths", type = ServicePaths.class),
            @XmlElement(name="whitelistedUpdates", type = WhitelistedStackUpdates.class)
    })
    private List<Object> entitiesToUpdate = new ArrayList<>();

    @XmlElement(name = "pendingChanges", required = true)
    private PendingChangesStatus pendingChangesStatus;

    public BatchChange getPathRules() {
        return pathRules;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setPathRules(BatchChange pathRules) {
        this.pathRules = pathRules;
    }

    public BatchChange getUrlRules() {
        return urlRules;
    }

    public void setUrlRules(BatchChange urlRules) {
        this.urlRules = urlRules;
    }

    public BatchChange getTemplatePathRules() {
        return templatePathRules;
    }

    public void setTemplatePathRules(BatchChange templatePathRules) {
        this.templatePathRules = templatePathRules;
    }

    public BatchChange getTemplateUrlRules() {
        return templateUrlRules;
    }

    public void setTemplateUrlRules(BatchChange templateUrlRules) {
        this.templateUrlRules = templateUrlRules;
    }

    public BatchChange getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(BatchChange whitelist) {
        this.whitelist = whitelist;
    }

    public BatchChange getServers() {
        return servers;
    }

    public void setServers(BatchChange servers) {
        this.servers = servers;
    }

    public BatchChange getDistribution() {
        return distribution;
    }

    public void setDistribution(BatchChange distribution) {
        this.distribution = distribution;
    }

    public BatchChange getDefaultUrlParams() {
        return defaultUrlParams;
    }

    public void setDefaultUrlParams(BatchChange defaultUrlParams) {
        this.defaultUrlParams = defaultUrlParams;
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

    public PendingChangesStatus getPendingChangesStatus() {
        return pendingChangesStatus;
    }

    public void setPendingChangesStatus(PendingChangesStatus pendingChangesStatus) {
        this.pendingChangesStatus = pendingChangesStatus;
    }
}
