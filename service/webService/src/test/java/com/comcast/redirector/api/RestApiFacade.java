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

package com.comcast.redirector.api;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.summary.Summary;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.api.model.traffic.Traffic;
import com.comcast.redirector.api.model.traffic.TrafficInputParams;
import com.comcast.redirector.api.model.whitelisted.WhitelistUpdate;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.HostIPsListWrapper;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.comcast.redirector.api.redirector.helpers.NamespacedListsHelper.*;
import static com.comcast.redirector.common.RedirectorConstants.REDIRECTOR_CONTROLLER_PATH;

public class RestApiFacade {

    private boolean validModelExists;

    public RestApiFacade() {
    }

    public RestApiFacade(String baseUrl) {
        HttpTestServerHelper.initWebTarget(baseUrl);
    }

    public Namespaces getAllNamespacedLists() {
        return ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Namespaces.class);
    }

    public Whitelisted getWhiteListForService(String service) {
        return WhitelistedHelper.get(service, MediaType.APPLICATION_JSON);
    }

    public ServicePaths getAllStackPaths() {
        return StacksHelper
            .getAllStacks(MediaType.APPLICATION_JSON);
    }

    public PendingChangesStatus getPendingChangesForService(String service) {
        return PendingChangesHelper
            .getAllPendingChanges(service, MediaType.APPLICATION_JSON);
    }

    public SelectServer getFlavorRulesForService(String service) {
        return RulesHelper.getAllRules(service);
    }

    public IfExpression getFlavorRuleByIdForService(String service, String id) {
        return RulesHelper.getRule(HttpTestServerHelper.target(), service, id);
    }

    public void cancelFlavorRulePendingChangesForService(String service, String id) {
        RulesHelper.cancelPendingChanges(HttpTestServerHelper.target(), service, id);
    }

    public RuleIdsWrapper getFlavorRulesIdsForService(String serviceName) {
        return RulesHelper.getRulesIds(serviceName);
    }

    public IfExpression exportFlavorRulesForService (String serviceName) {
        return RulesHelper.exportRules(serviceName);
    }

    public IfExpression exportFlavorRuleForServiceById (String serviceName, String ruleId) {
        return RulesHelper.exportRule(serviceName, ruleId);
    }

    public URLRules getUrlRulesForService(String service) {
        return UrlRulesHelper.getAllUrlRules(service);
    }

    public RuleIdsWrapper getUrlRulesIdsForService(String serviceName) {
        return UrlRulesHelper.getUrlRulesIds(serviceName);
    }

    public IfExpression getUrlRuleByIdForService(String service, String id) {
        return UrlRulesHelper.getUrlRuleExpression(service, id, MediaType.APPLICATION_JSON);
    }

    public IfExpression exportUrlRulesForService(String serviceName) {
        return UrlRulesHelper.exportUrlRules(serviceName);
    }

    public IfExpression exportUrlRuleForServiceById(String serviceName, String urlRuleId) {
        return UrlRulesHelper.exportUrlRule(serviceName, urlRuleId);
    }

    public Distribution getDistributionForService(String service) {
        return DistributionHelper.get(service);
    }

    public PendingChangesStatus getAllPendingChangesForService(String service) {
        return PendingChangesHelper.getAllPendingChanges(service, MediaType.APPLICATION_JSON);
    }

    public void cancelDistributionPendingChangesForService(String service) {
        DistributionHelper.cancelPendingChanges(HttpTestServerHelper.target(), service);
    }

    public Server getServerForService(String service) {
        return ServersHelper.get(service, RedirectorConstants.DEFAULT_SERVER_NAME);
    }

    public void approveServerPendingChangesForService(String service, String serverName) {
        ServersHelper.approvePendingChanges(HttpTestServerHelper.target(), service, serverName);
    }

    public Default getUrlParamsForService(String service) {
        return UrlRulesHelper.getUrlParamsRule(service, MediaType.APPLICATION_JSON);
    }

    public Server postDefaultServerForService(Server defaultServer, String serviceName) {
        return ServersHelper.post(serviceName, defaultServer);
    }

    public void cancelServerPendingChangesForService(String service) {
        ServersHelper.cancelPendingChanges(HttpTestServerHelper.target(), service, RedirectorConstants.DEFAULT_SERVER_NAME);
    }

    public IfExpression postFlavorRuleForService(IfExpression flavorRule, String serviceName) {
        return RulesHelper.postRule(serviceName, flavorRule);
    }

    public IfExpression postUrlRuleForService(IfExpression urlRule, String serviceName) {
        return UrlRulesHelper.postUrlRule(serviceName, urlRule.getId(), urlRule, MediaType.APPLICATION_JSON);
    }

    public void updateUrlRuleForService(IfExpression urlRule, IfExpression updateUrlRule,  String serviceName) {
        UrlRulesHelper.makeUrlRuleWithUpdatedStatus(HttpTestServerHelper.target(), serviceName, urlRule, updateUrlRule);
    }

    public void cancelUrlRuleForService(IfExpression urlRule, String serviceName) {
        UrlRulesHelper.cancelUrlRulePendingChanges(HttpTestServerHelper.target(), serviceName, urlRule.getId());
    }

    public UrlRule postDefaultUrlParamsForService(UrlRule urlRule, String serviceName) {
        return UrlRulesHelper.postUrlParams(serviceName, urlRule);
    }

    public Distribution postDistributionForService(Distribution distribution, String serviceName) {
        return DistributionHelper.post(serviceName, distribution);
    }

    public void approveDistributionForService(String serviceName) {
        DistributionHelper.approvePendingChanges(HttpTestServerHelper.target(), serviceName);
    }

    public Whitelisted postWhitelistForService(Whitelisted whitelisted, String serviceName) {
        return WhitelistedHelper.post(serviceName, whitelisted);
    }

    public void cancelWhitelisedPendingChangesForService(String service) {
        WhitelistedHelper.cancelPendingChanges(service);
    }

    public void approvePendingChangesForService(String serviceName) {
        PendingChangesHelper.approveAllPendingChanges(HttpTestServerHelper.target(), serviceName);
    }

    public void cancelPendingChangesForService(String serviceName) {
        PendingChangesHelper.cancelAllPendingChanges(HttpTestServerHelper.target(), serviceName);
    }

    public void approveUrlRulePendingChanges(String serviceName, IfExpression urlRule) {
        UrlRulesHelper.approveUrlRulePendingChanges(HttpTestServerHelper.target(), serviceName, urlRule.getId());
    }

    public URLRules getAllUrlRules (String sericeName) {
        return UrlRulesHelper.getAllUrlRules(sericeName);
    }

    public void approveUrlRulePendingChanges(String serviceName) {
        UrlRulesHelper.approveUrlParamsPendingChanges(HttpTestServerHelper.target(), serviceName);
    }

    public void postNamespacedLists (Namespaces namespaces) {
        postNamespaces(namespaces);
    }

    public NamespacedList postOneNamespacedList (NamespacedList list) {
        return ServiceHelper.post(getWebTarget_Post(list.getName()), list, MediaType.APPLICATION_JSON);
    }

    public NamespacedList getOneNamespacedList (String name) {
        return ServiceHelper.get(getWebTarget_Get(name),
                MediaType.APPLICATION_JSON, NamespacedList.class);
    }

    public void deleteNamespacedListValues (NamespacedList list, String valueForDelete) {
        ServiceHelper.delete(getWebTarget_DeleteValues(list.getName(), valueForDelete));
    }

    public NamespacedList addValuesToNamespacedList (NamespacedList namespacedListWithAddedValues) {
        return ServiceHelper.put(getWebTarget_AddValues(namespacedListWithAddedValues.getName()),
                namespacedListWithAddedValues, MediaType.APPLICATION_JSON);
    }

    public void deleteNamespacedList(NamespacedList list) {
        ServiceHelper.delete(getWebTarget_Delete(list.getName()));
    }

    public NamespacedListSearchResult searchInNamespacedLists (String value) {
        return ServiceHelper.get(getWebTarget_Search(value),
                MediaType.APPLICATION_JSON, NamespacedListSearchResult.class);
    }

    public NamespaceDuplicates searchNamespaceDuplicates (NamespacedList namespacedList) {
        return ServiceHelper.post(getWebTarget_SearchDuplicated(), namespacedList, MediaType.APPLICATION_JSON,
                         NamespaceDuplicates.class);
    }

    public NamespacedEntities bulkDeleveValuesFromNamespacedList(NamespacedList list, NamespacedEntities entries) {
        return ServiceHelper.post(getWebTarget_Delete_multiple(list.getName()), entries,
                MediaType.APPLICATION_JSON, NamespacedEntities.class);
    }

    public Namespaces deleteMultipleValuesFromMultipleNSLists (List<NamespacedValuesToDeleteByName> toDelete) {
        return ServiceHelper.post(getWebTargetDeleteEntitiesFromMultipleNamespacedLists(),
                toDelete.toArray(new NamespacedValuesToDeleteByName[toDelete.size()]),
                MediaType.APPLICATION_JSON, Namespaces.class);
    }

    public void deleteUrlRule(String serviceName, IfExpression urlRule) {
        UrlRulesHelper.deleteUrlRule(serviceName, urlRule.getId());
    }

    public void deleteFlavorRule(String serviceName, IfExpression rule) {
        RulesHelper.deleteRule(HttpTestServerHelper.target(), serviceName, rule.getId());
    }

    public void deleteWhitelisted(String serviceName, Whitelisted whitelisted) throws UnsupportedEncodingException {
        String whitelistedStacksToDelete = StringUtils.join(whitelisted.getPaths(), ",");
        WhitelistedHelper.deleteStacks(HttpTestServerHelper.target(), serviceName,
                URLEncoder.encode(whitelistedStacksToDelete, "UTF-8"));
    }

    public void approveFlavorRulePendingChanges(String serviceName, IfExpression rule) {
        RulesHelper.approvePendingChanges(HttpTestServerHelper.target(), serviceName, rule.getId());
    }

    public void approveWhitelistedPendingChanges(String serviceName) {
        WhitelistedHelper.approvePendingChanges(serviceName);
    }

    public Whitelisted addStacks(String serviceName, Whitelisted whitelistedToAdd) {
        return WhitelistedHelper.addStacks(HttpTestServerHelper.target(), serviceName, whitelistedToAdd, MediaType.APPLICATION_JSON);
    }

    public StackComment postStackCommentForPath(StackComment comment, String serviceName, String path) {
        WebTarget webTarget = HttpTestServerHelper.target()
                .path(RedirectorConstants.STACK_COMMENTS_CONTROLLER_PATH)
                .path("post")
                .path(serviceName)
                .queryParam("path", path);
        return ServiceHelper.post(webTarget, comment, MediaType.APPLICATION_JSON, StackComment.class);
    }

    public StackComment getStackCommentForPath(String serviceName, String path) {
        WebTarget webTarget = HttpTestServerHelper.target()
                .path(RedirectorConstants.STACK_COMMENTS_CONTROLLER_PATH)
                .path("getOne")
                .path(serviceName)
                .queryParam("path", path);
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, StackComment.class);
    }

    public void deleteStacks(String serviceName, String stacksToDelete) throws UnsupportedEncodingException {
        WhitelistedHelper.deleteStacks(HttpTestServerHelper.target(), serviceName,
                URLEncoder.encode(stacksToDelete, "UTF-8"));
    }

    public Response postTestCase(RedirectorTestCase testCase, String serviceName) {
        return TestCaseHelper.post(HttpTestServerHelper.target(), testCase, serviceName);
    }

    public Response getTestCase(String testCaseName, String serviceName)  {
        return TestCaseHelper.get(HttpTestServerHelper.target(), testCaseName, serviceName);
    }

    public Response getTestCase(String serviceName) {
        return TestCaseHelper.getAll(HttpTestServerHelper.target(), serviceName);
    }

    public void delete(String testCaseName, String serviceName) {
        TestCaseHelper.delete(HttpTestServerHelper.target(), testCaseName, serviceName);
    }

    public RedirectorTestCaseList getRedirectorTestCaseList (Response response) {
        return response.readEntity(RedirectorTestCaseList.class);
    }

    public RedirectorConfig getRedirectorConfig() {
        return ServiceHelper.get(HttpTestServerHelper.target(), MediaType.APPLICATION_JSON, RedirectorConfig.class);
    }

    public RedirectorConfig postRedirectorConfig(RedirectorConfig config) {
        return ServiceHelper.post(HttpTestServerHelper.target(), config, MediaType.APPLICATION_JSON);
    }

    public Traffic calculatedTraffic(TrafficInputParams trafficInputparams, String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RedirectorConstants.TRAFFIC_PATH).path(serviceName);
        return ServiceHelper.post(webTarget, trafficInputparams, MediaType.APPLICATION_JSON, Traffic.class);
    }

    public ServicePaths getServicePathsForService(String serviceName) {
        return StacksHelper.getServicePaths(serviceName, MediaType.APPLICATION_JSON);
    }

    public HostIPsListWrapper getRandomAddressByFlavorForService(String serviceName, String flavor) {
        return StacksHelper.getRandomAddressByFlavor(serviceName, flavor, MediaType.APPLICATION_JSON);
    }

    public HostIPsListWrapper getRandomAddressByStackForService(String serviceName, String stackName) {
        return StacksHelper.getRandomAddressByStack(serviceName, stackName, MediaType.APPLICATION_JSON);
    }

    public ServicePaths getAllServicePaths() {
        return StacksHelper.getAllStacks(MediaType.APPLICATION_JSON);
    }

    public HostIPsListWrapper getAddressByStackForService(String serviceName, String stack) {
        return StacksHelper.getAddressByStack(serviceName, stack, MediaType.APPLICATION_JSON);
    }

    public HostIPsListWrapper getAddressByFlavorForService(String serviceName, String flavor) {
        return StacksHelper.getAddressByFlavor(serviceName, flavor, MediaType.APPLICATION_JSON);
    }

    public void deleteStacksForService(String serviceName, String... pathsItemForDelete) {
        StacksHelper.deleteStacks(StacksHelper.createDeletePathsObject(serviceName, pathsItemForDelete), MediaType.APPLICATION_JSON);
    }

    public Traffic getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThresholdReturnJSON(String serviceName, int totalNumberConnections, int connectionThreshold) {
        return getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThreshold(serviceName, totalNumberConnections, connectionThreshold, MediaType.APPLICATION_JSON);
    }

    public Traffic getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThresholdReturnXML(String serviceName, int totalNumberConnections, int connectionThreshold) {
        return getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThreshold(serviceName, totalNumberConnections, connectionThreshold, MediaType.APPLICATION_XML);
    }

    public Traffic getCurrentTrafficForServiceWithTotalNumberConnectionAndConnectionThreshold(String serviceName, int totalNumberConnections, int connectionThreshold, String mediaType) {
        boolean isActive = true;
        String path = serviceName + "/" + totalNumberConnections + "/" + connectionThreshold + "/" + isActive;
        WebTarget webTarget = HttpTestServerHelper.target().path(RedirectorConstants.TRAFFIC_PATH).path("current").path(path);
        return ServiceHelper.get(webTarget, mediaType, Traffic.class);
    }

    public ServiceInstances getRedirectorInstancesForService(String service) {
        WebTarget webTarget = HttpTestServerHelper.target().path(REDIRECTOR_CONTROLLER_PATH).path(service).path("instances");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, ServiceInstances.class);
    }

    public String getRedirectorAppNames() {
        WebTarget webTarget = HttpTestServerHelper.target().path(REDIRECTOR_CONTROLLER_PATH).path("applicationNames");
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, String.class);
    }

    public Summary getSummaryByNamespacedListForService(String serviceName, String namespacedListName) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.SUMMARY_PATH).path(serviceName).path(namespacedListName);
        return ServiceHelper.get(target, MediaType.APPLICATION_JSON, Summary.class);
    }

    public List<WhitelistUpdate> getWhitelistUpdatesForService(String serviceName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RedirectorConstants.WHITELISTED_UPDATES_CONTROLLER_PATH).path(serviceName);
        WhitelistedStackUpdates stackUpdates = webTarget.request(MediaType.APPLICATION_JSON).get().readEntity(WhitelistedStackUpdates.class);

        return new ArrayList<>(stackUpdates.getWhitelistedUpdates().values());
    }

    public RuleIdsWrapper getNewRuleIds(String serviceName, String objectType) {
        return PendingChangesHelper.getNewRuleIds(serviceName, objectType);
    }

    public Distribution getDistributionPendingChangesPreview(String serviceName) {
        return PendingChangesHelper.getDistributionPendingChangesPreview(serviceName);
    }

    public Whitelisted getWhitelistedPendingChangesPreview(String serviceName) {
        return PendingChangesHelper.getWhitelistedPendingChangesPreview(serviceName);
    }

    public ModelStatesWrapper getAllExistingApplications() {
        WebTarget webTarget = HttpTestServerHelper.target().path(RedirectorConstants.INITIALIZER_CONTROLLER_PATH);
        return ServiceHelper.get(webTarget, MediaType.APPLICATION_JSON, ModelStatesWrapper.class);
    }

    public Boolean activateModelForService(String appName) {
        WebTarget webTarget = HttpTestServerHelper.target().path(RedirectorConstants.INITIALIZER_CONTROLLER_PATH).path(appName);
        return ServiceHelper.post(webTarget, appName, MediaType.TEXT_PLAIN, Boolean.class);
    }

    public Boolean isValidModelExists() {
        WebTarget webTarget = HttpTestServerHelper.target().path(RedirectorConstants.INITIALIZER_CONTROLLER_PATH).path("validModelExists");
        return ServiceHelper.get(webTarget, MediaType.TEXT_PLAIN, Boolean.class);
    }
}
