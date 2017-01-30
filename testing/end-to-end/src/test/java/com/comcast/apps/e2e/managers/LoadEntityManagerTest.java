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

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.apps.e2e.utils.TestHelper;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class LoadEntityManagerTest {

    private final static String SERVICE_NAME = "testApp";
    private ModelEntityLoader loadEntityManager;
    private Context context;

    @Before
    public void setUp() throws IOException {
        Serializer jsonSerializer = new JsonSerializer();
        Serializer xmlSerializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());
        FileUtil fileUtil = spy(new FileUtil(jsonSerializer, xmlSerializer));
        FilesPathHelper filesPathHelper = new FilesPathHelper(TestHelper.getResourcesPath());
        context = new Context(SERVICE_NAME, "baseUrl");
        loadEntityManager = new ModelEntityLoader(fileUtil, filesPathHelper, context);

        String whitelistedFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.WHITELISTED, SERVICE_NAME);
        String selectServerFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.SELECT_SERVER, SERVICE_NAME);
        String urlRulesFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.URL_RULES, SERVICE_NAME);
        String stackFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.STATIC_STACKS, SERVICE_NAME);

        when(fileUtil.load(whitelistedFileName)).thenReturn(TestHelper.load(whitelistedFileName));
        when(fileUtil.load(selectServerFileName)).thenReturn(TestHelper.load(selectServerFileName));
        when(fileUtil.load(urlRulesFileName)).thenReturn(TestHelper.load(urlRulesFileName));
        when(fileUtil.load(stackFileName)).thenReturn(TestHelper.load(stackFileName));
    }

    @Test
    public void loadTest() {
        assertTrue(loadEntityManager.load());
        assertEquals(getWhitelisted(), context.getWhitelisted());
        assertEquals(getSelectServer(), context.getSelectServer());
        assertEquals(getURLRules(), context.getUrlRules());
        assertEquals(getStackBackup(), context.getStackBackup());
    }

    private Whitelisted getWhitelisted() {
        List<String> paths = Arrays.asList("/BR/BRC9","/BR1/BRC9");
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(paths);
        return whitelisted;
    }

    private SelectServer getSelectServer() {
        Server server = new Server();
        server.setName("Default Server");
        server.setUrl(RedirectorConstants.URL_TEMPLATE);
        server.setPath("1.41");
        server.setDescription("Default Server route");
        Distribution distribution = new Distribution();
        distribution.setDefaultServer(server);
        SelectServer selectServer = new SelectServer();
        selectServer.setDistribution(distribution);
        return selectServer;
    }

    private URLRules getURLRules() {
        List<Expressions> expressionList = new ArrayList<>();
        Equals equals = new Equals();
        equals.setValue("valueTest");
        equals.setParam("paramTest");
        expressionList.add(equals);

        IfExpression expression = getIfExpression("testId", "templateName", getUrlRule("urnTest", "xre", "8088", "4"), expressionList);
        List<Expressions> items1 = new ArrayList<>();
        items1.add(expression);

        IfExpression ifExpression = getIfExpression("testId", null, null, items1);

        Collection<IfExpression> items = new ArrayList<>();
        items.add(ifExpression);

        Default defaultUrl = new Default();
        defaultUrl.setUrlRule(getUrlRule("shell", "xre", "8081", "4"));

        URLRules urlRules = new URLRules();
        urlRules.setItems(items);
        urlRules.setDefaultStatement(defaultUrl);
        return urlRules;
    }

    private UrlRule getUrlRule(String urn, String protocol, String port, String ipProtocolVersion) {
        UrlRule urlRule = new UrlRule();
        urlRule.setUrn(urn);
        urlRule.setProtocol(protocol);
        urlRule.setPort(port);
        urlRule.setIpProtocolVersion(ipProtocolVersion);
        return urlRule;
    }

    private IfExpression getIfExpression(String id, String templateDependencyName, Expressions ret, List<Expressions> items) {
        IfExpression ifExpression = new IfExpression();
        ifExpression.setId(id);
        ifExpression.setTemplateDependencyName(templateDependencyName);
        if (ret != null) {
            ifExpression.setReturn(ret);
        }
        ifExpression.setItems(items);
        return ifExpression;
    }

    private StackBackup getStackBackup() {
        StackSnapshot.Host host = new StackSnapshot.Host("ccpapp-po-c534-p.po.ccp.cable3ff33.comcast.com", null);
        List<StackSnapshot.Host> hosts = new ArrayList<>();
        hosts.add(host);

        StackSnapshot stackSnapshot = new StackSnapshot("/BR/BRC9/1.41/xreGuide", hosts);
        List<StackSnapshot> stackSnapshots = new ArrayList<>();
        stackSnapshots.add(stackSnapshot);

        return new StackBackup(0, stackSnapshots);
    }
}
