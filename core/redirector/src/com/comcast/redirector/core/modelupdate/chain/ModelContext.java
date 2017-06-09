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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;

import java.util.HashSet;
import java.util.Set;

public class ModelContext implements Cloneable {
    private String appName;
    private StackBackup mainStacksBackup;
    private IRedirectorEngine redirectorEngine;
    private int modelVersion;

    private SelectServer flavorRules;
    private URLRules urlRules;
    private Whitelisted whitelistedStacks;

    private Model flavorRulesModel;
    private URLRuleModel urlRulesModel;
    private WhiteList whiteListModel;
    
    private Set<StackData> stackData = new HashSet<>();

    StackBackup getMainStacksBackup() {
        return mainStacksBackup;
    }

    public void setMainStacksBackup(StackBackup mainStacksBackup) {
        this.mainStacksBackup = mainStacksBackup;
    }

    public static ModelContext from(ModelContext sourceContext) {
        try {
            if (sourceContext == null) {
                return null;
            }
            return (ModelContext) sourceContext.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRedirectorEngine(IRedirectorEngine redirectorEngine) {
        this.redirectorEngine = redirectorEngine;
    }

    IRedirectorEngine getRedirectorEngine() {
        return redirectorEngine;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(int modelVersion) {
        this.modelVersion = modelVersion;
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

    public Whitelisted getWhitelistedStacks() {
        return whitelistedStacks;
    }

    public void setWhitelistedStacks(Whitelisted whitelistedStacks) {
        this.whitelistedStacks = whitelistedStacks;
    }

    public Model getFlavorRulesModel() {
        return flavorRulesModel;
    }

    void setFlavorRulesModel(Model flavorRulesModel) {
        this.flavorRulesModel = flavorRulesModel;
    }

    public URLRuleModel getUrlRulesModel() {
        return urlRulesModel;
    }

    void setUrlRulesModel(URLRuleModel urlRulesModel) {
        this.urlRulesModel = urlRulesModel;
    }

    public WhiteList getWhiteListModel() {
        return whiteListModel;
    }

    void setWhiteListModel(WhiteList whiteListModel) {
        this.whiteListModel = whiteListModel;
    }
    
    public Set<StackData> getStackData() {
        return stackData;
    }
    
    public void setStackData(Set<StackData> stackData) {
        this.stackData = stackData;
    }
}
