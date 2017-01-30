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

package com.comcast.redirector.api.model;

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import org.apache.commons.collections.CollectionUtils;

import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "snapshot")
@XmlSeeAlso({SelectServer.class, URLRules.class, Default.class, URLRules.class, Distribution.class, Whitelisted.class, PendingChangesStatus.class, ServicePaths.class, Server.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Snapshot implements Serializable{

    @XmlElement(name = "version", required = true)
    private int version = 0;

    @XmlElement(name = "application", required = true)
    private String application;

    @XmlElement(name = "pathRules", required = true)
    private SelectServer flavorRules = new SelectServer();

    @XmlElement(name = "urlRules", required = true)
    private URLRules urlRules = new URLRules();

    @XmlElement(name = "urlParams", required = true)
    private Default defaultUrlParams;

    @XmlElement(name = "templatePathRules", required = true)
    private SelectServer templatePathRules = new SelectServer();

    @XmlElement(name = "templateUrlPathRules", required = true)
    private URLRules templateUrlRules = new URLRules();

    @XmlElement(name = "distributions", required = true)
    private Distribution distribution = new Distribution();

    @XmlElement(name = "whitelisted", required = true)
    private Whitelisted whitelist = new Whitelisted();

    @XmlElement(name = "whitelistedUpdates", required = true)
    private WhitelistedStackUpdates whitelistedStackUpdates = new WhitelistedStackUpdates();

    @XmlElement(name = "pendingChanges", required = true)
    private PendingChangesStatus pendingChanges;

    @XmlElement(name = "stacks", required = true)
    private ServicePaths servicePaths;

    @XmlElement(name = "servers", required = true)
    private Server servers;

    @XmlElement(name="stackBackup")
    private String stackBackup;

    @XmlElementWrapper(name = "entityToSave")
    @XmlElements({
            @XmlElement(name="distribution", type = Distribution.class),
            @XmlElement(name="whitelisted", type = Whitelisted.class),
            @XmlElement(name="default", type = Default.class),
            @XmlElement(name="server", type = Server.class),
            @XmlElement(name="if", type = IfExpression.class)
    })
    private List<Expressions> entityToSave = new ArrayList<>();

    public Snapshot(){}

    public Snapshot(String application) {
        this.application = application;
    }

    public SelectServer getFlavorRules() {
        return flavorRules;
    }

    public void setFlavorRules(SelectServer flavorRules) {
        this.flavorRules = flavorRules;
    }

    public URLRules getUrlRules() {
        return urlRules;
    }

    public void setUrlRules(URLRules urlRules) {
        this.urlRules = urlRules;
    }

    public Default getDefaultUrlParams() {
        return defaultUrlParams;
    }

    public void setDefaultUrlParams(Default defaultUrlParams) {
        this.defaultUrlParams = defaultUrlParams;
    }

    public SelectServer getTemplatePathRules() {
        return templatePathRules;
    }

    public void setTemplatePathRules(SelectServer templatePathRules) {
        this.templatePathRules = templatePathRules;
    }

    public URLRules getTemplateUrlRules() {
        return templateUrlRules;
    }

    public void setTemplateUrlRules(URLRules templateUrlRules) {
        this.templateUrlRules = templateUrlRules;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public Whitelisted getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(Whitelisted whitelist) {
        this.whitelist = whitelist;
    }

    public WhitelistedStackUpdates getWhitelistedStackUpdates() {
        return whitelistedStackUpdates;
    }

    public void setWhitelistedStackUpdates(WhitelistedStackUpdates whitelistedStackUpdates) {
        this.whitelistedStackUpdates = whitelistedStackUpdates;
    }

    public PendingChangesStatus getPendingChanges() {
        return pendingChanges;
    }

    public void setPendingChanges(PendingChangesStatus pendingChanges) {
        this.pendingChanges = pendingChanges;
    }

    public ServicePaths getServicePaths() {
        return servicePaths;
    }

    public void setServicePaths(ServicePaths servicePaths) {
        this.servicePaths = servicePaths;
    }

    public Server getServers() {
        return servers;
    }

    public void setServers(Server servers) {
        this.servers = servers;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getStackBackup() {
        return stackBackup;
    }

    public void setStackBackup(String stackBackup) {
        this.stackBackup = stackBackup;
    }

    public Expressions getEntityToSave() {
        if (CollectionUtils.isNotEmpty(entityToSave)) {
            return entityToSave.get(0); //todo this is really unclear.
        }
        return null;
    }

    public List<Expressions> getEntitiesToSave() {
        return entityToSave; //todo this is only further decreasing code readability.
    }

    public void setEntityToSave(Expressions entityToSave) {
        this.entityToSave.add(entityToSave);  //todo this is really unclear. Do we lose all other elements? We don't have getter.
    }

    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
