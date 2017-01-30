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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.ruleengine.TemplateFlavorRulesService;
import com.comcast.redirector.api.redirector.lock.LockHelper;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import java.util.*;

import static com.comcast.redirector.api.redirector.service.pending.entityview.FlavorRuleExpressionsHelper.getFlavorIfExpression;
import static com.comcast.redirector.api.redirector.service.pending.entityview.FlavorRuleExpressionsHelper.getRuleIdByServerPath;
import static com.comcast.redirector.api.redirector.service.pending.entityview.PendingChangesHelper.putPendingChange;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TemplateFlavorRulesServiceTest {
    @Mock
    private IStacksService stacksService;

    @Mock
    private IListServiceDAO<IfExpression> flavorRulesDAO;

    @Mock
    private IChangesStatusService pendingChangesService;

    @Mock
    private IPendingEntityWriteService<IfExpression> pendingFlavorRuleWriteService;

    @Mock
    private IListServiceDAO<IfExpression> rulesDAOEntity;

    @Mock
    private LockHelper lockHelper;

    @Mock
    private SharedInterProcessLock lock;

    @InjectMocks
    private TemplateFlavorRulesService templateFlavorRulesService = spy(new TemplateFlavorRulesService(stacksService, flavorRulesDAO, pendingChangesService, pendingFlavorRuleWriteService, rulesDAOEntity));

    @Before
    public void setUp() {
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        Map<String, PendingChange> rulesChanges = new HashMap<>();
        putPendingChange(rulesChanges, getRuleIdByServerPath("rule2-flavor"),
                getFlavorIfExpression(new Equals("mac", "receiverMacAddress1"), "rule2-flavor"),
                getFlavorIfExpression(new Equals("mac", "receiverMacAddress"), "rule2-flavor"), ActionType.UPDATE);
        pendingChangesStatus.setPathRules(rulesChanges);
        when(pendingChangesService.getPendingChangesStatus(anyString())).thenReturn(pendingChangesStatus);
        when(lockHelper.getLock(anyString(), Matchers.<EntityType>anyObject())).thenReturn(lock);
        when(lock.acquire()).thenReturn(true);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @Ignore
    public void testDeleteRuleIfRulesUsingTemplate() {
        when(flavorRulesDAO.getById(anyString(), anyString())).thenReturn(getTemplateRule());
        when(rulesDAOEntity.getAll(anyString())).thenReturn(getRules("serviceName", "template1"));
        thrown.expect(WebApplicationException.class);
        thrown.expectMessage("Failed to delete template 'template1' for serviceName application due to validation error(s). Validation failed. Next errors has been found:\n" +
                "1. Template is used in rule(s): serviceName ");
        templateFlavorRulesService.deleteRule("serviceName", "template1");
    }

    @Test
    @Ignore
    public void testDeleteRuleIfRulesDoesNotUsingTemplate() {
        when(flavorRulesDAO.getById(anyString(), anyString())).thenReturn(getTemplateRule());
        when(rulesDAOEntity.getAll(anyString())).thenReturn(getRules("serviceName", null));
        templateFlavorRulesService.deleteRule("serviceName", "template1");
        verify(templateFlavorRulesService, times(1)).deleteRule("serviceName", "template1");
    }

    private IfExpression getTemplateRule() {
        IfExpression rule = new IfExpression();
        rule.setId("template1");
        Equals equals = new Equals();
        equals.setParam("ab3c");
        equals.setValue("3");
        rule.setItems(Arrays.<Expressions>asList(equals));
        return rule;
    }

    private List<IfExpression> getRules(String ruleId, String templateDependencyName) {
        Server server = new Server();
        server.setName(ruleId);
        server.setIsNonWhitelisted("false");
        server.setUrl("{protocol}://{host}:{port}/{urn}");
        server.setPath("1.44");
        server.setDescription("dfsfsf server route");
        Equals equals = new Equals();
        equals.setParam("abc");
        equals.setValue("1");
        IfExpression rule = new IfExpression();
        rule.setId(ruleId);
        rule.setItems(Arrays.<Expressions>asList(equals));
        rule.setReturn(server);
        rule.setTemplateDependencyName(templateDependencyName);
        List<IfExpression> rules = new ArrayList<>();
        rules.add(rule);
        return rules;
    }
}
