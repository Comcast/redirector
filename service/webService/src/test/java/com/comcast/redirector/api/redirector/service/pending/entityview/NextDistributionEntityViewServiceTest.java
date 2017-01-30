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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.redirector.service.pending.entityview.DistributionHelper.getRule;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NextDistributionEntityViewServiceTest {
    private IDistributionService distributionService;
    private IChangesStatusService changesStatusService;
    private NextDistributionEntityViewService testee;

    @Before
    public void setUp() throws Exception {
        distributionService = mock(IDistributionService.class);
        changesStatusService = mock(IChangesStatusService.class);
        testee = new NextDistributionEntityViewService();
        testee.setChangesStatusService(changesStatusService);
        testee.setDistributionService(distributionService);
    }

    @Test
    public void testGetEntity() throws Exception {
        String serviceName = "xreGuide";
        DistributionBuilder builder = new DistributionBuilder();
        Distribution currentDistribution = builder.
                withRule(newDistributionRule(0, 10f, newSimpleServerForFlavor("1.40"))).
                withRule(newDistributionRule(1, 20f, newSimpleServerForFlavor("1.41"))).
                withRule(newDistributionRule(2, 30f, newSimpleServerForFlavor("1.42"))).
                build();

        when(distributionService.getDistribution(serviceName)).thenReturn(currentDistribution);

        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        Map<String, PendingChange> distributionChanges = new HashMap<>();
        PendingChangesHelper.putDistributionChange(distributionChanges, 0, 10.0f, "1.40", ActionType.DELETE);
        PendingChangesHelper.putDistributionChange(distributionChanges, 1, 15.0f, "1.40-SNAPSHOT", ActionType.UPDATE);
        PendingChangesHelper.putDistributionChange(distributionChanges, 3, 35.0f, "1.44", ActionType.ADD);
        pendingChangesStatus.setDistributions(distributionChanges);
        when(changesStatusService.getPendingChangesStatus(serviceName)).thenReturn(pendingChangesStatus);

        Distribution distribution = testee.getEntity(serviceName);

        verifyRulesAreEqual(getRule(0, 15.0f, "1.40-SNAPSHOT"), distribution.getRules().get(0));
        verifyRulesAreEqual(getRule(1, 30.0f, "1.42"), distribution.getRules().get(1));
        verifyRulesAreEqual(getRule(2, 35.0f, "1.44"), distribution.getRules().get(2));
    }

    private void verifyRulesAreEqual(Rule expected, Rule actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getPercent(), actual.getPercent());
        Assert.assertEquals(expected.getServer().getPath(), actual.getServer().getPath());
    }
}
