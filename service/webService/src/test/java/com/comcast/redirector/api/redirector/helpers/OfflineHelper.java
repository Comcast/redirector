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

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.redirector.service.pending.entityview.ServerHelper;

import java.util.*;

import static com.comcast.redirector.api.redirector.service.pending.entityview.PendingChangesHelper.putDistributionChange;

@Deprecated // use model builders like IfExpressionBuilder, SeverBuilder and etc..
public class OfflineHelper {

    public static final String defaultServiceName = "serviceName1";
    public static final String RULE = "rule1";
    public static final String RULE_URL = "ruleUrl1";

    @Deprecated //left for backward compatibility
    public static SnapshotList getSnapshotList() {
        return getSnapshotList(defaultServiceName);
    }

    public static SnapshotList getSnapshotList(String serviceName) {
        Snapshot snapshot = getSnapshotWithoutPendingChangesStatus(serviceName);
        snapshot.setPendingChanges(getPendingChangesStatus());
        SnapshotList snapshotList = createSnapshotList(snapshot);
        return snapshotList;
    }

    @Deprecated //left for backward compatibility
    public static SnapshotList getSnapshotListForDefaultServer() {
        return getSnapshotListForDefaultServer(defaultServiceName);
    }

    public static SnapshotList getSnapshotListForDefaultServer(String serviceName) {
        Snapshot snapshot = getSnapshotWithoutPendingChangesStatus(serviceName);
        snapshot.setPendingChanges(getPendingChangesStatusDefaultServer());
        SnapshotList snapshotList = createSnapshotList(snapshot);
        return snapshotList;
    }

    private static SnapshotList createSnapshotList(Snapshot snapshot) {
        List<Snapshot> snapshots = new ArrayList<>();
        snapshots.add(snapshot);

        RedirectorConfig redirectorConfig = new RedirectorConfig();
        redirectorConfig.setMinHosts(1);
        redirectorConfig.setAppMinHosts(1);

        SnapshotList snapshotList = new SnapshotList();
        snapshotList.setItems(snapshots);
        snapshotList.setConfig(redirectorConfig);
        return snapshotList;
    }

    private static Snapshot getSnapshotWithoutPendingChangesStatus(String serviceName) {
        Server defaultServer = getDefaultServer();
        Collection<IfExpression> rules = new ArrayList<>();
        rules.add(getRule(RULE));

        Distribution distribution = new Distribution();
        distribution.setDefaultServer(defaultServer);
        SelectServer currentSelectServer = new SelectServer();
        currentSelectServer.setItems(rules);
        currentSelectServer.setDistribution(distribution);

        Snapshot snapshot = new Snapshot();
        snapshot.setServicePaths(getServicePaths(serviceName));
        snapshot.setWhitelist(getWhitelisted());
        snapshot.setStackBackup(getStackBackupJson(serviceName));
        snapshot.setEntityToSave(getRule(RULE));
        snapshot.setFlavorRules(currentSelectServer);
        snapshot.setServers(defaultServer);
        return snapshot;
    }

    public static Server getDefaultServer() {
        return ServerHelper.prepareServer("Default Server", "flavoursPathValue1");
    }

    public static IfExpression getRule(String ruleId) {
        Server server = new Server();
        server.setName(ruleId);
        server.setUrl("{protocol}://{host}:{port}/{urn}");
        server.setPath("flavoursPathValue1");
        server.setDescription("Description of server route");

        Equals equals = new Equals();
        equals.setParam("abc");
        equals.setValue("1");

        IfExpression rule = new IfExpression();
        rule.setId(ruleId);
        rule.setItems(Arrays.<Expressions>asList(equals));
        rule.setReturn(server);

        return rule;
    }

    public static Whitelisted getWhitelisted() {
        String w1 = "/BR/BRC9";
        String w2 = "flavoursPathValue1";
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(Arrays.asList(w1, w2));
        return whitelisted;
    }

    public static PendingChangesStatus getPendingChangesStatus() {
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        pendingChangesStatus.setWhitelisted(getPendingWhitelist());
        pendingChangesStatus.setDistributions(getPendingDistribution());
        pendingChangesStatus.setPathRules(getPendingRules());
        pendingChangesStatus.setUrlRules(getPendingUrlRules());
        return pendingChangesStatus;
    }

    public static PendingChangesStatus getPendingChangesStatusDefaultServer() {
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        pendingChangesStatus.setServers(getPendingDefaultServer());
        return pendingChangesStatus;
    }

    private static String getStackBackupJson(String serviceName) {
        return "{\"version\":0,\"snapshotList\":[{\"path\":\"/BR/BRC9/flavoursPathValue1/" + serviceName + "\",\"hosts\":[{\"ipv4\":\"ccpapp-po-c534-p.po.ccp.cable3ff33.comcast.com\"}]},{\"path\":\"/BR/BRC9/flavoursPathValue1/" + serviceName + "\",\"hosts\":[{\"ipv4\":\"ccpapp-po-c534-p.po.ccp.cable3ff34.comcast.com\"}]}]}";
    }

    private static Map<String, PendingChange> getPendingRules() {
        PendingChange pendingChange = new PendingChange();
        pendingChange.setChangedExpression(getRule(RULE));
        pendingChange.setChangeType(ActionType.ADD);

        Map<String, PendingChange> pendingChangesStatusHashMap = new HashMap<>();
        pendingChangesStatusHashMap.put(RULE, pendingChange);

        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        pendingChangesStatus.setPathRules(pendingChangesStatusHashMap);
        return pendingChangesStatusHashMap;
    }

    private static Map<String, PendingChange> getPendingWhitelist() {
        Map<String, PendingChange> whitelistChanges = new HashMap<>();

        PendingChange pendingChangeADD = new PendingChange();
        pendingChangeADD.setChangeType(ActionType.ADD);
        pendingChangeADD.setChangedExpression(new Value("flavoursPathValue1_pending"));
        whitelistChanges.put("lavoursPathValue1_pending", pendingChangeADD);

        PendingChange pendingChangeDELETE = new PendingChange();
        pendingChangeDELETE.setChangeType(ActionType.DELETE);
        pendingChangeDELETE.setCurrentExpression(new Value("stackPathValue2"));
        whitelistChanges.put("stackPathValue2", pendingChangeDELETE);

        return whitelistChanges;
    }

    private static Map<String, PendingChange> getPendingUrlRules() {
        PendingChange pendingChange = new PendingChange();
        pendingChange.setChangedExpression(getRule(RULE_URL));
        pendingChange.setChangeType(ActionType.ADD);
        Map<String, PendingChange> pendingChangesStatusHashMap = new HashMap<>();
        pendingChangesStatusHashMap.put(RULE_URL, pendingChange);
        return pendingChangesStatusHashMap;
    }

    private static Map<String, PendingChange> getPendingDefaultServer() {
        PendingChange pendingChange = new PendingChange();
        pendingChange.setChangedExpression(getServer("flavoursPathValue1"));
        pendingChange.setCurrentExpression(getDefaultServer());
        pendingChange.setChangeType(ActionType.UPDATE);
        Map<String, PendingChange> pendingChangesStatusHashMap = new HashMap<>();
        pendingChangesStatusHashMap.put(RedirectorConstants.DEFAULT_SERVER_NAME, pendingChange);
        return pendingChangesStatusHashMap;
    }

    private static Map<String, PendingChange> getPendingDistribution() {
        Map<String, PendingChange> distributionChanges = new HashMap<>();
        putDistributionChange(distributionChanges, 1, 20.0f, "flavoursPathValue2", ActionType.UPDATE);
        putDistributionChange(distributionChanges, 2, 30.0f, "flavoursPathValue3", ActionType.DELETE);
        putDistributionChange(distributionChanges, 3, 10.0f, "flavoursPathValue1", ActionType.ADD);
        return distributionChanges;
    }

    public static Server getServer(String path) {
        Server server = new Server();
        server.setName("default");
        server.setUrl("{protocol}://{host}:{port}/{urn}");
        server.setPath(path);
        server.setDescription("Default server route");
        return server;
    }

    @Deprecated //left for backward compatibility
    public static ServicePaths getServicePaths() {
        return getServicePaths(defaultServiceName);
    }

    public static ServicePaths getServicePaths(String serviceName) {
        ServicePaths servicePaths = new ServicePaths();
        List<Paths> pathsList = new ArrayList<>();
        List<PathItem> stacks = new ArrayList<>();
        List<PathItem> flavours = new ArrayList<>();
        for (int j = 1; j <= 3; j++) {
            PathItem stacksPathItem = new PathItem();
            stacksPathItem.setActiveNodesCount(j);
            stacksPathItem.setValue("/BR/BRC9/flavoursPathValue" + j);
            stacks.add(stacksPathItem);

            PathItem flavoursPathItem = new PathItem();
            flavoursPathItem.setActiveNodesCount(j);
            flavoursPathItem.setValue("flavoursPathValue" + j);
            flavours.add(flavoursPathItem);
        }
        Paths newPaths = new Paths(serviceName, stacks, flavours);
        pathsList.add(newPaths);
        servicePaths.setPaths(pathsList);
        return servicePaths;
    }
}
