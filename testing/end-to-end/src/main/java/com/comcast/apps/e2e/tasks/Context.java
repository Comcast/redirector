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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.tasks;

import com.comcast.apps.e2e.ZookeeperModelReloadDispatcher;
import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.apps.e2e.helpers.ServicePathHelper;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;

import java.util.Collection;

public class Context {
    private static final String REPORT_PROTOCOL = "http:";
    private RedirectorConfig redirectorConfig ;
    private NamespacedListsBatch namespaces;
    private StackBackup stackBackup;
    private Whitelisted whitelisted;
    private SelectServer selectServer;
    private URLRules urlRules;
    private RedirectorTestCaseList redirectorTestCaseList;
    private ZookeeperModelReloadDispatcher zookeeperModelReloadDispatcher;
    private ServicePathHelper servicePathHelper;

    private String serviceName;
    private String baseUrl;

    public Context(String baseUrl) {
        this.baseUrl = baseUrl;
        this.servicePathHelper = new ServicePathHelper();
    }

    public Context(String serviceName, String baseUrl) {
        this.serviceName = serviceName;
        this.baseUrl = baseUrl;
        this.servicePathHelper = new ServicePathHelper(serviceName);
    }

    public void reset() {
        serviceName = null;
        baseUrl = null;
        redirectorConfig = null;
        namespaces = null;
        stackBackup = null;
        whitelisted = null;
        selectServer = null;
        urlRules = null;
        servicePathHelper = null;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    String getBaseUrlForReport() {
        return E2EConfigLoader.getDefaultInstance().getReportBaseUrl();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RedirectorConfig getRedirectorConfig() {
        return redirectorConfig;
    }

    public void setRedirectorConfig(RedirectorConfig redirectorConfig) {
        this.redirectorConfig = redirectorConfig;
    }

    public NamespacedListsBatch getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(NamespacedListsBatch namespaces) {
        this.namespaces = namespaces;
    }

    public StackBackup getStackBackup() {
        return stackBackup;
    }

    public void setStackBackup(StackBackup stackBackup) {
        this.stackBackup = stackBackup;
    }

    public Whitelisted getWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(Whitelisted whitelisted) {
        this.whitelisted = whitelisted;
    }

    public SelectServer getSelectServer() {
        return selectServer;
    }

    public Distribution getDistribution() {
        return selectServer.getDistribution();
    }

    public Server getDefaultServer() {
        return selectServer.getDistribution().getDefaultServer();
    }

    public Collection<IfExpression> getFlavorRules() {
        return selectServer.getItems();
    }

    public UrlRule getDefaultUrlRule() {
        return urlRules.getDefaultStatement().getUrlRule();
    }

    public void setSelectServer(SelectServer selectServer) {
        this.selectServer = selectServer;
    }

    public URLRules getUrlRules() {
        return urlRules;
    }

    public void setUrlRules(URLRules urlRules) {
        this.urlRules = urlRules;
    }

    public RedirectorTestCaseList getRedirectorTestCaseList() {
        return redirectorTestCaseList;
    }

    public void setRedirectorTestCaseList(RedirectorTestCaseList redirectorTestCaseList) {
        this.redirectorTestCaseList = redirectorTestCaseList;
    }

    public ZookeeperModelReloadDispatcher getZookeeperModelReloadDispatcher() {
        return zookeeperModelReloadDispatcher;
    }

    public void setZookeeperModelReloadDispatcher(ZookeeperModelReloadDispatcher zookeeperModelReloadDispatcher) {
        this.zookeeperModelReloadDispatcher = zookeeperModelReloadDispatcher;
    }

    public ServicePathHelper getServicePathHelper() {
        return servicePathHelper;
    }

    public void setServicePathHelper(ServicePathHelper servicePathHelper) {
        this.servicePathHelper = servicePathHelper;
    }
}
