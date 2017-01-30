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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.redirector.api;

import com.comcast.redirector.api.model.Snapshot;
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.model.namespaced.NamespaceDuplicates;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedValuesToDeleteByName;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.common.RedirectorConstants;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class OfflineRestApiFacade {

    public OfflineRestApiFacade() {
    }

    public OfflineRestApiFacade(String baseUrl) {
        HttpTestServerHelper.initWebTarget(baseUrl);
    }

    public PendingChangesBatchOperationResult approveDistributionPendingChangesForServiceOffline(String serviceName, SnapshotList snapshotList) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("distribution/approve");
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, PendingChangesBatchOperationResult.class);
    }

    public OperationResult approveDefaultServerForService(final String serviceName,
                                                          final SnapshotList snapshotList) {

        final WebTarget target = HttpTestServerHelper.target()
                .path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH)
                .path(serviceName)
                .path("server")
                .path("default")
                .path("approve");
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult cancelDistributionPendingChangesForServiceOffline(String serviceName, Snapshot snapshot) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("distribution/cancel");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult cancelDefaultServerForService(final String serviceName, final Snapshot snapshot) {
        final WebTarget target = HttpTestServerHelper.target()
                .path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH)
                .path(serviceName)
                .path("server")
                .path("default")
                .path("cancel");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult postFlavorRuleForServiceOffline(Snapshot snapshot, String ruleId, String serviceName) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.RULES_OFFLINE_CONTROLLER_PATH).path(serviceName).path(ruleId).path("add");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult postDefaultServerForService(Snapshot snapshot, String serviceName) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.SERVERS_OFFLINE_CONTROLLER_PATH).path(serviceName).path(RedirectorConstants.DEFAULT_SERVER_NAME);
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult deleteFlavorRuleForServiceOffline(Snapshot snapshot, String ruleId, String serviceName) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.RULES_OFFLINE_CONTROLLER_PATH).path(serviceName).path(ruleId).path("delete");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult approveFlavorRulePendingChangesForServiceOffline(String serviceName, String ruleId, SnapshotList snapshotList) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("flavorrule").path(ruleId).path("approve");
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult cancelFlavorRulePendingChangesForServiceOffline(String serviceName, String ruleId, Snapshot snapshot) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("flavorrule").path(ruleId).path("cancel");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult approveWhitelistPendingChangesForServiceOffline(String serviceName, SnapshotList snapshotList) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("stackmanagement/approve");
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult approveUrlRulesPendingChangesForServiceOffline(String serviceName, String ruleId, SnapshotList snapshotList) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("urlrule").path(ruleId).path("approve");
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult cancelUrlRulesPendingChangesForServiceOffline(String serviceName, String ruleId, Snapshot snapshot) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.PENDING_OFFLINE_CONTROLLER_PATH).path(serviceName).path("urlrule").path(ruleId).path("cancel");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult postUrlRuleForServiceOffline(String serviceName, String ruleId, Snapshot snapshot) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.URL_RULES_OFFLINE_CONTROLLER_PATH).path(serviceName).path(ruleId).path("add");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public OperationResult deleteUrlRuleForServiceOffline(String serviceName, String ruleId, Snapshot snapshot) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.URL_RULES_OFFLINE_CONTROLLER_PATH).path(serviceName).path(ruleId).path("delete");
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public  OperationResult postDistributionForServiceOffline(String serviceName, Snapshot snapshot) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.DISTRIBUTION_OFFLINE_CONTROLLER_PATH).path(serviceName);
        return ServiceHelper.post(target, snapshot, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public NamespacedList postNamespacedListOffline(SnapshotList snapshotList) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH).path("validate");
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, NamespacedList.class);
    }

    public NamespacedListSearchResult searchForNamespacedListOffline(SnapshotList snapshotList, String searchValue) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH).path("search").path(searchValue);
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, NamespacedListSearchResult.class);
    }

    public NamespaceDuplicates searchForNamespaceDuplicates(Namespaces namespaces) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH).path("duplicates");
        return ServiceHelper.post(target, namespaces, MediaType.APPLICATION_JSON, NamespaceDuplicates.class);
    }

    public Response deleteNamespacedList(SnapshotList snapshotList, String name) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH).path(name);
        return ServiceHelper.postAndGetRawResponse(target, snapshotList, MediaType.APPLICATION_JSON);
    }

    public OperationResult deleteNamespacedListValues(SnapshotList snapshotList, String name) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH).path("deleteNamespacedEntities").path(name);
        return ServiceHelper.post(target, snapshotList, MediaType.APPLICATION_JSON, OperationResult.class);
    }

    public Namespaces deleteValuesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> valuesToDeleteByNames) {
        WebTarget target = HttpTestServerHelper.target().path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH).path("deleteEntitiesFromNamespacedLists");
        return ServiceHelper.post(target, valuesToDeleteByNames.toArray(new NamespacedValuesToDeleteByName[valuesToDeleteByNames.size()]), MediaType.APPLICATION_JSON, Namespaces.class);
    }
}
