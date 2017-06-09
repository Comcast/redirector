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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.ruleengine.TemplateUrlRulesService;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import java.util.*;

import static com.comcast.redirector.api.redirector.service.pending.entityview.PendingChangesHelper.putPendingChange;
import static com.comcast.redirector.api.redirector.service.pending.entityview.UrlRuleExpressionsHelper.getRuleIdByProtocolAndIp;
import static com.comcast.redirector.api.redirector.service.pending.entityview.UrlRuleExpressionsHelper.getUrlRuleIfExpression;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TemplateUrlRulesServiceTest {

    public static final String SERVICE_NAME = "serviceName";
    @Mock
    private IListServiceDAO<IfExpression> urlRulesDAO;

    @Mock
    private IChangesStatusService pendingChangesService;

    @Mock
    private IPendingEntityWriteService<IfExpression> pendingUrlRuleWriteService;

    @Mock
    private IListServiceDAO<IfExpression> urlRulesDAOEntity;

    @InjectMocks
    private TemplateUrlRulesService templateUrlRulesService = spy(new TemplateUrlRulesService(urlRulesDAO, pendingUrlRuleWriteService, pendingChangesService, urlRulesDAOEntity));

    @Before
    public void setUp() {
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        Map<String, PendingChange> urlRulesChanges = new HashMap<>();
        putPendingChange(urlRulesChanges,
                getRuleIdByProtocolAndIp("protocol1", IpProtocolVersion.IPV4.getVersionString()),
                getUrlRuleIfExpression(new Equals("receiverType", "changed"), "protocol1", IpProtocolVersion.IPV4.getVersionString()),
                getUrlRuleIfExpression(new Equals("receiverType", "Native"), "protocol1", IpProtocolVersion.IPV4.getVersionString()),
                ActionType.UPDATE);
        when(pendingChangesService.getPendingChangesStatus(anyString())).thenReturn(pendingChangesStatus);

    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @Ignore
    public void testDeleteUrlRuleIfUrlRulesUsingTemplate() {
        when(urlRulesDAO.getById(anyString(), anyString())).thenReturn(getTemplateUrlRule());
        when(urlRulesDAOEntity.getAll(anyString())).thenReturn(getUrlRules(SERVICE_NAME, "template1"));
        thrown.expect(WebApplicationException.class);
        thrown.expectMessage("Failed to delete template 'template1' for serviceName application due to validation error(s). Validation failed. Next errors has been found:\n" +
                        "1. Template is used in url rule(s): serviceName ");
        templateUrlRulesService.deleteUrlRule("serviceName", "template1");
    }

    @Test
    @Ignore
    public void testDeleteUrlRuleIfUrlRulesDoesNotUsingTemplate() {
        when(urlRulesDAO.getById(anyString(), anyString())).thenReturn(getTemplateUrlRule());
        when(urlRulesDAOEntity.getAll(anyString())).thenReturn(getUrlRules("serviceName", null));
        templateUrlRulesService.deleteUrlRule("serviceName", "template1");
        verify(templateUrlRulesService, times(1)).deleteUrlRule("serviceName", "template1");
    }

    private IfExpression getTemplateUrlRule() {
        IfExpression rule = new IfExpression();
        rule.setId("template1");
        Equals equals = new Equals();
        equals.setParam("ab3c");
        equals.setValue("3");
        rule.setItems(Arrays.<Expressions>asList(equals));
        return rule;
    }

    private List<IfExpression> getUrlRules(String ruleId, String templateDependencyName) {
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
