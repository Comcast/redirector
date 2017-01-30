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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.model.pending;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.Server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.comcast.redirector.common.RedirectorConstants.DEFAULT_SERVER_NAME;

@XmlRootElement(name = "pending")
@XmlSeeAlso({PendingChange.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class PendingChangesStatus implements Expressions {
    private Integer version = 0;
    private Map<String, PendingChange> pathRules = new LinkedHashMap<>();
    private Map<String, PendingChange> templatePathRules = new LinkedHashMap<>();
    private Map<String, PendingChange> templateUrlPathRules = new LinkedHashMap<>();
    private Map<String, PendingChange> urlRules = new LinkedHashMap<>();
    private Map<String, PendingChange> urlParams = new LinkedHashMap<>();
    private Map<String, PendingChange> servers = new LinkedHashMap<>();
    private Map<String, PendingChange> distributions = new LinkedHashMap<>();
    private Map<String, PendingChange> whitelisted = new LinkedHashMap<>();

    public PendingChangesStatus () {
    }

    public Map<String, PendingChange> getPathRules() {
        return pathRules;
    }

    public void setPathRules(Map<String, PendingChange> pathRules) {
        this.pathRules = pathRules;
    }

    public Map<String, PendingChange> getTemplatePathRules() {
        return templatePathRules;
    }

    public void setTemplatePathRules(Map<String, PendingChange> templatePathRules) {
        this.templatePathRules = templatePathRules;
    }

    public Map<String, PendingChange> getTemplateUrlPathRules() {
        return templateUrlPathRules;
    }

    public void setTemplateUrlPathRules(Map<String, PendingChange> templateUrlPathRules) {
        this.templateUrlPathRules = templateUrlPathRules;
    }

    public Map<String, PendingChange> getUrlRules() {
        return urlRules;
    }

    public void setUrlRules(Map<String, PendingChange> urlRules) {
        this.urlRules = urlRules;
    }

    public Map<String, PendingChange> getServers() {
        return servers;
    }

    public void setServers(Map<String, PendingChange> servers) {
        this.servers = servers;
    }

    public Map<String, PendingChange> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<String, PendingChange> distributions) {
        this.distributions = distributions;
    }

    public Map<String, PendingChange> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(Map<String, PendingChange> urlParams) {
        this.urlParams = urlParams;
    }

    public Map<String, PendingChange> getWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(Map<String, PendingChange> whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isPendingChangesEmpty() {
        return pathRules.isEmpty() && urlRules.isEmpty() && urlParams.isEmpty() &&  templatePathRules.isEmpty()
                && templateUrlPathRules.isEmpty() && servers.isEmpty() && distributions.isEmpty() && whitelisted.isEmpty();
    }

    public boolean onlyWhitelistedOrDefaultUrlParamsNotEmpty () {
        return ( pathRules.isEmpty()
                && templatePathRules.isEmpty()
                && templateUrlPathRules.isEmpty()
                && urlRules.isEmpty()
                && servers.isEmpty()
                && distributions.isEmpty())
                && (whitelisted.size() > 0 || urlParams.size() == 1) ;
    }

    public void clear() {
        getPathRules().clear();
        getTemplatePathRules().clear();
        getTemplateUrlPathRules().clear();
        getDistributions().clear();
        getServers().clear();
        getUrlParams().clear();
        getUrlRules().clear();
        getWhitelisted().clear();
    }

    public Server getPendingDefaultServer() {
        return (Server) getServers().getOrDefault(DEFAULT_SERVER_NAME, new PendingChange()).getChangedExpression();
    }
}
