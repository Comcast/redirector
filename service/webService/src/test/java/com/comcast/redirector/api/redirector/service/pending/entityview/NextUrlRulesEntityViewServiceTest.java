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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import com.comcast.redirector.common.IpProtocolVersion;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.api.redirector.service.pending.entityview.CommonExpressionsHelper.verifyRuleExpressionsEqual;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NextUrlRulesEntityViewServiceTest {
    private IChangesStatusService changesStatusService;
    private IUrlRulesService urlRulesService;
    private IUrlParamsService urlParamsService;
    private NextUrlRulesEntityViewService testee;

    @Before
    public void setUp() throws Exception {
        changesStatusService = mock(IChangesStatusService.class);
        urlRulesService = mock(IUrlRulesService.class);
        urlParamsService = mock(IUrlParamsService.class);

        testee = new NextUrlRulesEntityViewService();
        testee.setChangesStatusService(changesStatusService);
        testee.setUrlRulesService(urlRulesService);
        testee.setUrlParamsService(urlParamsService);
    }

    @Test
    public void testGetEntity() throws Exception {
        String serviceName = "xreGuide";

        // Setup current data
        Collection<IfExpression> rules = new ArrayList<>();
        rules.add(UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("receiverType", "Native"), "protocol1", IpProtocolVersion.IPV4.getVersionString()));
        rules.add(UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("mac", "receiverMacAddress"), "protocol2", IpProtocolVersion.IPV6.getVersionString()));
        when(urlRulesService.getUrlRules(serviceName)).thenReturn(rules);
        when(urlParamsService.getUrlParams(serviceName))
                .thenReturn(UrlRuleExpressionsHelper.prepareUrlParams("DefaultProtocol", IpProtocolVersion.IPV4.getVersionString()));

        // Setup pending changes
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        Map<String, PendingChange> urlRulesChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(urlRulesChanges,
                UrlRuleExpressionsHelper.getRuleIdByProtocolAndIp("protocol1", IpProtocolVersion.IPV4.getVersionString()),
                UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("receiverType", "changed"), "protocol1", IpProtocolVersion.IPV4.getVersionString()),
                UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("receiverType", "Native"), "protocol1", IpProtocolVersion.IPV4.getVersionString()),
                ActionType.UPDATE);
        PendingChangesHelper.putPendingChange(urlRulesChanges,
                UrlRuleExpressionsHelper.getRuleIdByProtocolAndIp("protocol2", IpProtocolVersion.IPV6.getVersionString()),
                null,
                UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("mac", "receiverMacAddress"), "protocol2", IpProtocolVersion.IPV6.getVersionString()),
                ActionType.DELETE);
        PendingChangesHelper.putPendingChange(urlRulesChanges,
                UrlRuleExpressionsHelper.getRuleIdByProtocolAndIp("newProtocol", IpProtocolVersion.IPV6.getVersionString()),
                UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("newParameter", "newValue"), "newProtocol", IpProtocolVersion.IPV6.getVersionString()),
                null,
                ActionType.UPDATE);
        pendingChangesStatus.setUrlRules(urlRulesChanges);

        Map<String, PendingChange> urlParamsChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(urlParamsChanges,
                RedirectorConstants.DEFAULT_SERVER_NAME,
                UrlRuleExpressionsHelper.prepareUrlParams("changedDefaultProtocol", IpProtocolVersion.IPV6.getVersionString()),
                UrlRuleExpressionsHelper.prepareUrlParams("DefaultProtocol", IpProtocolVersion.IPV4.getVersionString()),
                ActionType.UPDATE);
        pendingChangesStatus.setUrlParams(urlParamsChanges);
        when(changesStatusService.getPendingChangesStatus(serviceName)).thenReturn(pendingChangesStatus);

        // Setup expected results
        URLRules expectedUrlRules = new URLRules();
        Collection<IfExpression> expectedRules = new ArrayList<>();
        expectedRules.add(UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("receiverType", "changed"), "protocol1", IpProtocolVersion.IPV4.getVersionString()));
        expectedRules.add(UrlRuleExpressionsHelper.getUrlRuleIfExpression(new Equals("newParameter", "newValue"), "newProtocol", IpProtocolVersion.IPV6.getVersionString()));
        expectedUrlRules.setItems(expectedRules);
        Default expectedDefaultUrlRule = new Default();
        expectedDefaultUrlRule.setUrlRule(UrlRuleExpressionsHelper.prepareUrlParams("changedDefaultProtocol", IpProtocolVersion.IPV6.getVersionString()));
        expectedUrlRules.setDefaultStatement(expectedDefaultUrlRule);

        URLRules result = testee.getEntity(serviceName);

        verifyRuleExpressionsEqual(expectedUrlRules.getItems(), result.getItems());
        Assert.assertEquals(expectedUrlRules.getDefaultStatement().getUrlRule(), result.getDefaultStatement().getUrlRule());
    }
}
