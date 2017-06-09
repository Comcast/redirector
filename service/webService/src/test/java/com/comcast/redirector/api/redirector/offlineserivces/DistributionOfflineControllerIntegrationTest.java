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

package com.comcast.redirector.api.redirector.offlineserivces;

import com.comcast.redirector.api.OfflineRestApiFacade;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.builders.PendingChangesBuilder;
import com.comcast.redirector.api.model.builders.ServerBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.factory.UrlRuleFactory;
import com.comcast.redirector.api.model.factory.WhitelistFactory;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.offlinemode.PendingChangesBatchOperationResult;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.OfflineHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.service.pending.entityview.DistributionHelper.prepareDistribution;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class DistributionOfflineControllerIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Ignore
    @Test
    public void testSaveDistribution_JsonObject() {
        String serviceName = getServiceNameForTest();
        new DistributionOfflineServiceSteps(serviceName)
                .createDistribution()
                .createSnapshotForSavingDistribution()
                .saveDistribution()
                .verifyDistributionAddingIsPending();
    }

    @Test
    public void testModifyDisributionByRemovingRule() {
        String serviceName = getServiceNameForTest();
        new DistributionOfflineServiceSteps(serviceName)
                .createSnapshotListForDistributionDeletionApproval()
                .approveDistributionWithDeletedRule()
                .verityDistributionRemovedProperly();
    }

    private class DistributionOfflineServiceSteps {
        private String serviceName;
        private Snapshot snapshot;
        private OperationResult responseEntityObject;
        private Distribution distribution;

        private SnapshotList snapshotListToApprove;
        private PendingChangesBatchOperationResult responseDistributionApproval;

        DistributionOfflineServiceSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        public DistributionOfflineServiceSteps createDistribution() {
            distribution = getDistribution();
            return this;
        }

        public DistributionOfflineServiceSteps createSnapshotForSavingDistribution() {
            Snapshot result = new Snapshot();
            result.setPendingChanges(new PendingChangesStatus());
            result.setServicePaths(OfflineHelper.getServicePaths(serviceName));
            result.setEntityToSave(distribution);
            snapshot = result;

            return this;
        }

        public DistributionOfflineServiceSteps saveDistribution() {
            responseEntityObject = apiFacade.postDistributionForServiceOffline(serviceName, snapshot);
            return this;
        }

        public DistributionOfflineServiceSteps approveDistributionWithDeletedRule() {
            responseDistributionApproval = apiFacade.approveDistributionPendingChangesForServiceOffline(serviceName, snapshotListToApprove);
            return this;
        }

        public DistributionOfflineServiceSteps verifyDistributionAddingIsPending() {
            Assert.assertNotNull(responseEntityObject.getApprovedEntity());
            Assert.assertNotNull(responseEntityObject.getPendingChanges());
            Assert.assertNotNull(responseEntityObject.getPendingChanges().getDistributions());

            Assert.assertEquals(responseEntityObject.getApprovedEntity(), distribution);
            Assert.assertEquals(2, responseEntityObject.getPendingChanges().getDistributions().size());
            Assert.assertEquals(responseEntityObject.getPendingChanges().getDistributions().get("1").getChangeType(), ActionType.ADD);
            Assert.assertTrue(distribution.getRules().contains(responseEntityObject.getPendingChanges().getDistributions().get("1").getChangedExpression()));
            Assert.assertTrue(distribution.getRules().contains(responseEntityObject.getPendingChanges().getDistributions().get("2").getChangedExpression()));

            return this;
        }

        public DistributionOfflineServiceSteps verityDistributionRemovedProperly() {
            Assert.assertNotNull(responseDistributionApproval);
            Assert.assertEquals(responseDistributionApproval.getDistribution().getEntitiesToSave().size(), 1);

            Assert.assertTrue(((Distribution)responseDistributionApproval.getDistribution().getEntitiesToSave().get(0)).getRules().get(0).getPercent() == 20);
            Assert.assertEquals(((Distribution)responseDistributionApproval.getDistribution().getEntitiesToSave().get(0)).getRules().get(0).getServer(), new ServerBuilder().withFlavor("2.0").build());

            return this;
        }

        private Distribution getDistribution() {
            Distribution distribution = new Distribution();
            prepareDistribution(distribution, 1, 30.0f, "flavoursPathValue2");
            prepareDistribution(distribution, 2, 10.0f, "flavoursPathValue3");
            return distribution;
        }

        private DistributionOfflineServiceSteps createSnapshotListForDistributionDeletionApproval() {
            Snapshot snapshot = getSnapshot();
            snapshotListToApprove = getSnapshotList(snapshot);
            return this;
        }

        private Snapshot getSnapshot() {

            Whitelisted whitelisted = WhitelistFactory.createWhitelisted("/PO/POC1", "/PO/POC2");
            Server defaultServer = new ServerBuilder().withFlavor("1.0").withName("default server").build();
            UrlRule defaultUrlParams = UrlRuleFactory.newUrlParams("shell", "xre", "8080", "4");

            Distribution distribution = new DistributionBuilder()
                    .withDefaultServer(defaultServer)
                    .withRule(new Rule(1, 10, new ServerBuilder().withFlavor("1.0").build()))
                    .withRule(new Rule(2, 20, new ServerBuilder().withFlavor("2.0").build()))
                    .build();

            PendingChangesBuilder pendingChangesBuilder = new PendingChangesBuilder();
            pendingChangesBuilder.withDistributionPendingChange(null, new Rule(1, 20, new ServerBuilder().withFlavor("2.0").build()), ActionType.DELETE);
            PendingChangesStatus pendingChangesStatus = pendingChangesBuilder.build();

            Snapshot snapshot = new Snapshot(serviceName);
            snapshot.setWhitelist(whitelisted);
            snapshot.setServers(defaultServer);
            snapshot.setDefaultUrlParams(new Default(defaultUrlParams));
            snapshot.setDistribution(distribution);
            snapshot.setPendingChanges(pendingChangesStatus);
            snapshot.setStackBackup("{\"version\":0,\"snapshotList\":[" +
                    "{\"path\":\"/PO/POC1/1.0/" + serviceName + "\",\"hosts\":[{\"ipv4\":\"host1.comcast.com\"}]}," +
                    "{\"path\":\"/PO/POC1/2.0/" + serviceName + "\",\"hosts\":[{\"ipv4\":\"host2.comcast.com\"}]}]}");

            return snapshot;
        }

        private SnapshotList getSnapshotList(Snapshot snapshot) {

            SnapshotList snapshotList = new SnapshotList();
            snapshotList.setItems(Arrays.asList(snapshot));
            snapshotList.setConfig(new RedirectorConfig(1, 1));

            return snapshotList;
        }
    }
}
