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

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.common.util.RulesUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.api.redirector.service.pending.entityview.CommonExpressionsHelper.verifyRuleExpressionsEqual;
import static com.comcast.redirector.api.redirector.service.pending.entityview.FlavorRuleExpressionsHelper.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NextFlavorRulesEntityViewServiceTest {
    @Mock
    private IFlavorRulesService flavorRulesService;
    @Mock
    private IServerService serverService;
    @Mock
    private IChangesStatusService changesStatusService;
    @Mock
    private IEntityViewService<Distribution> nextDistributionEntityViewService;
    @Mock
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @InjectMocks
    private NextFlavorRulesEntityViewService testee;

    @Test
    public void testGetEntity() throws Exception {
        String serviceName = "xreGuide";
        // Setup expected select server
        Distribution distribution = new Distribution();
        distribution.setDefaultServer(ServerHelper.prepareServer(RedirectorConstants.DEFAULT_SERVER_NAME, "NewDefaultFlavor"));
        SelectServer expectedSelectServer = RulesUtils.buildSelectServer(
                prepareSimpleFlavorRules(new HashMap<Expressions, String>() {{
                    put(new Equals("mac", "receiverMacAddress1"), "rule2-flavor");
                }}),
                distribution
        );
        // Setup current data
        Collection<IfExpression> rules = prepareSimpleFlavorRules(new HashMap<Expressions, String>() {{
            put(new Equals("receiverType", "Native"), "rule1-flavor");
            put(new Equals("mac", "receiverMacAddress"), "rule2-flavor");
        }});
        SelectServer currentSelectServer = new SelectServer();
        PendingChangesStatus pendingChangesStatus = getPendingChangesStatus();
        currentSelectServer.setItems(rules);
        currentSelectServer.setDistribution(distribution);
        when(flavorRulesService.getAllRules(serviceName)).thenReturn(currentSelectServer);
        when(changesStatusService.getPendingChangesStatus(serviceName)).thenReturn(pendingChangesStatus);



        SelectServer selectServer = testee.getEntity(serviceName);

        verifyRuleExpressionsEqual(expectedSelectServer.getItems(), selectServer.getItems());
        assertEquals(expectedSelectServer.getDistribution(), selectServer.getDistribution());
    }

    private PendingChangesStatus getPendingChangesStatus() {
        // Setup pending changes
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        // Setup pending rules
        Map<String, PendingChange> rulesChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(rulesChanges, getRuleIdByServerPath("rule1-flavor"), null,
                getFlavorIfExpression(new Equals("receiverType", "Native"), "rule1-flavor"), ActionType.DELETE);
        PendingChangesHelper.putPendingChange(rulesChanges, getRuleIdByServerPath("rule2-flavor"),
                getFlavorIfExpression(new Equals("mac", "receiverMacAddress1"), "rule2-flavor"),
                getFlavorIfExpression(new Equals("mac", "receiverMacAddress"), "rule2-flavor"), ActionType.UPDATE);
        pendingChangesStatus.setPathRules(rulesChanges);

        // Setup pending server changes
        Map<String, PendingChange> serverChanges = new HashMap<>();
        PendingChangesHelper.putServerChange(serverChanges, RedirectorConstants.DEFAULT_SERVER_NAME, "NewDefaultFlavor", "DefaultFlavor");
        pendingChangesStatus.setServers(serverChanges);
        return pendingChangesStatus;
    }
}
