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

package com.comcast.redirector.api.redirectorOffline;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import com.comcast.redirector.api.redirector.service.IWhiteListStackUpdateService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.comcast.redirector.common.RedirectorConstants.NO_MODEL_NODE_VERSION;

@Component
public class OfflineRedirectorHelper {
    private static Logger log = LoggerFactory.getLogger(OfflineRedirectorHelper.class);

    @Autowired
    @Qualifier("flavorRulesService")
    private IFlavorRulesService flavorRulesService;

    @Autowired
    @Qualifier("urlRulesService")
    private IUrlRulesService urlRulesService;

    @Autowired
    @Qualifier("templateFlavorRulesService")
    private IFlavorRulesService templateFlavorRulesService;

    @Autowired
    @Qualifier("templateUrlRulesService")
    private IUrlRulesService templateUrlRulesService;

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdateService;

    @Autowired
    private IWhiteListService whiteListService;

    @Autowired
    @Qualifier("changesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    private INamespacedListsService namespacedListsService;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    IRedirectorConfigService redirectorConfigService;
    
    @Autowired
    private Serializer jsonSerializer;

    byte[] getJsonSnapshot(String serviceName, String stackBackup) throws SerializerException {

        Snapshot modelSnapshot = new Snapshot();
        modelSnapshot.setFlavorRules(flavorRulesService.getAllRules(serviceName));
        modelSnapshot.setUrlRules(urlRulesService.getAllRules(serviceName));
        modelSnapshot.setDefaultUrlParams(urlParamsService.getDefaultUrlParams(serviceName));
        modelSnapshot.setTemplatePathRules(templateFlavorRulesService.getAllRules(serviceName));
        modelSnapshot.setTemplateUrlRules(templateUrlRulesService.getAllRules(serviceName));
        modelSnapshot.setDistribution(distributionService.getDistribution(serviceName));
        modelSnapshot.setWhitelist(whiteListService.getWhitelistedStacks(serviceName));
        modelSnapshot.setWhitelistedStackUpdates(whiteListStackUpdateService.getWhitelistedStacksUpdates(serviceName));
        modelSnapshot.setPendingChanges(pendingChangesService.getPendingChangesStatus(serviceName));
        modelSnapshot.setServicePaths(stacksService.getStacksForService(serviceName));
        modelSnapshot.setServers(serverService.getServer(serviceName));
        modelSnapshot.setVersion(getModelVersion(serviceName));
        modelSnapshot.setApplication(serviceName);
        modelSnapshot.setStackBackup(stackBackup);

        return jsonSerializer.serializeToByteArray(modelSnapshot);
    }


    public byte[] getNamespacedListJsonSnapshot() throws SerializerException {
        DateTime currentTimeInUTC = new DateTime(DateTimeZone.UTC);
        Namespaces namespaces = namespacedListsService.getAllNamespacedLists();

        if (namespaces != null) {
            namespaces.setDataNodeVersion(getNamespacedListDataNodeVersion());
            namespaces.setVersion(currentTimeInUTC.getMillis());
            return jsonSerializer.serializeToByteArray(namespaces);
        }

        log.info("Could NOT find namespaces in a storage");
        return new byte[0];
    }

    private long getNamespacedListDataNodeVersion() {
        try {
            return connector.getNodeVersion(PathHelper.getPathHelper(EntityType.NAMESPACED_LIST, connector.getBasePath()).getPath());
        } catch (DataSourceConnectorException e) {
            log.error("Failed to get namespacedList version", e);
            return NO_MODEL_NODE_VERSION;
        }
    }

    private int getModelVersion(String serviceName) {
        try {
            return connector.getNodeVersion(PathHelper.getPathHelper(EntityType.MODEL_CHANGED, connector.getBasePath()).getPathByService(serviceName));
        } catch (DataSourceConnectorException e) {
            log.warn("Failed to get model version for " + serviceName);
            return NO_MODEL_NODE_VERSION;
        }
    }

    public byte[] getSettingsJsonSnapshot() throws SerializerException {
        DateTime currentTimeInUTC = new DateTime(DateTimeZone.UTC);
        RedirectorConfig redirectorConfig = redirectorConfigService.getRedirectorConfig();

        if (redirectorConfig != null) {
            redirectorConfig.setVersion(currentTimeInUTC.getMillis());
            return jsonSerializer.serializeToByteArray(redirectorConfig);
        }

        log.info("Could NOT read redirectorConfig from ZK");
        return new byte[0];
    }

    public static Map<String, PendingChange> getRulesPendingChange(Map<String, IfExpression> current, Collection<IfExpression> changed) {

        Map<String, PendingChange> pendingChangeMap = new LinkedHashMap<>();

        if (current == null || changed == null) {
            return pendingChangeMap;
        }

        for(IfExpression expression : changed) {
            String id = expression.getId();
            if (current.containsKey(id) && !current.get(id).equals(expression)) {
                PendingChange pendingChange = new PendingChange(id, ActionType.UPDATE);
                pendingChange.setChangedExpression(expression);
                pendingChange.setCurrentExpression(current.get(id));
                pendingChangeMap.put(id, pendingChange);
            }
            else if (!current.containsKey(id)) {
                PendingChange pendingChange = new PendingChange(id, ActionType.ADD);
                pendingChange.setChangedExpression(expression);
                pendingChangeMap.put(id, pendingChange);
            }
            current.remove(id);
        }

        for (Map.Entry<String, IfExpression> entry: current.entrySet()) {
            PendingChange pendingChange = new PendingChange(entry.getKey(), ActionType.DELETE);
            pendingChange.setCurrentExpression(entry.getValue());
            pendingChangeMap.put(entry.getKey(), pendingChange);
        }

        return pendingChangeMap;
    }

    public static Map<String, PendingChange> getServerPendingChange(Server current, Server pending) {

        Map<String, PendingChange> changeMap;
        changeMap = new LinkedHashMap<>();
        if ((current != null && pending != null && !current.equals(pending)) || (current == null && pending != null)) {
            ActionType changeType = PendingChangeStatusHelper.getActionType(EntityType.SERVER, pending, current);
            changeMap.put(RedirectorConstants.DEFAULT_SERVER_NAME, new PendingChange(RedirectorConstants.DEFAULT_SERVER_NAME, changeType, pending, current));
        }

        return changeMap;
    }

    public static Map<String, PendingChange> getDefaultUrlParams(Default current, Default pending) {

        Map<String, PendingChange> changeMap;
        changeMap = new LinkedHashMap<>();
        if ((current != null && pending != null && !current.equals(pending)) || (current == null && pending != null)) {
            ActionType changeType = PendingChangeStatusHelper.getActionType(EntityType.URL_PARAMS, pending, current);
            changeMap.put(RedirectorConstants.DEFAULT_URL_RULE, new PendingChange(RedirectorConstants.DEFAULT_URL_RULE, changeType, pending.getUrlRule(), current.getUrlRule()));
        }

        return changeMap;
    }

    public static Map<NamespacedList, ActionType> getNamespaceChanges(Map<String, NamespacedList> currentNamespaces, List<NamespacedList> fromBackup) {
        Map<NamespacedList, ActionType> namespaceChangesMap = new LinkedHashMap<>();

        if (fromBackup == null || currentNamespaces == null) {
            return namespaceChangesMap;
        }

        for (NamespacedList namespacedList : fromBackup) {
            String name = namespacedList.getName();
            if (currentNamespaces.containsKey(name) && currentNamespaces.get(name).getVersion() < namespacedList.getVersion()) {
                namespaceChangesMap.put(namespacedList, ActionType.UPDATE);
            } else if (!currentNamespaces.containsKey(name)) {
                namespaceChangesMap.put(namespacedList, ActionType.ADD);
            }
            currentNamespaces.remove(name);
        }

        for (Map.Entry<String, NamespacedList> entry : currentNamespaces.entrySet()) {
            namespaceChangesMap.put(entry.getValue(), ActionType.DELETE);
        }

        return namespaceChangesMap;
    }
}
