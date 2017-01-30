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


package com.comcast.redirector.api.redirector.serializers;

import com.comcast.redirector.api.model.builders.*;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ExpressionFactory.newSingleParamExpression;
import static com.comcast.redirector.api.model.factory.ServerFactory.newServerSimple;
import static com.comcast.redirector.api.model.factory.UrlRuleFactory.newUrlParams;
import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;
import static org.junit.Assert.assertEquals;

public class XMLSerializerTest {

    private static final String SELECT_SERVER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><selectServer><if id=\"rule1\"><and><equals><param>receiverType</param><value>Native</value></equals><matches negation=\"false\"><param>platform</param><pattern>Windows</pattern></matches></and><return><server isNonWhitelisted=\"false\"><name>rule1</name><url>{protocol}://{host}:{port}/{urn}</url><path>/PO/POC1/1.0</path><description>rule1 server route</description></server></return></if><distribution><rule><id>0</id><percent>1.0</percent><server><name>distribution server 1</name><url>{protocol}://{host}:{port}/{urn}</url><path>/PO/POC1/2.0</path><description>1.0% distribution server</description></server></rule><rule><id>1</id><percent>1.0</percent><server><name>distribution server 2</name><url>{protocol}://{host}:{port}/{urn}</url><path>/PO/POC1/3.0</path><description>1.0% distribution server</description></server></rule><server><name>Default Server</name><url>{protocol}://{host}:{port}/{urn}</url><path>1.0</path><description>Default server route</description></server></distribution></selectServer>";
    private static final String BACKUP_USAGE_SCHEDULE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><backupUsageSchedule schedule=\"0 55 16 ? * MON,TUE,FRI,SAT,SUN\" duration=\"36000000\"/>";
    private static final String PATHS_SERVICE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><servicePaths><paths serviceName=\"pathServiceTest\"><stack nodes=\"2\" nodesWhitelisted=\"0\">/DataCenter1/Region1/Zone1</stack><stack nodes=\"1\" nodesWhitelisted=\"0\">/DataCenter2/Region2/Zone2</stack><stack nodes=\"0\" nodesWhitelisted=\"0\">/DataCenter2/Region1/Zone1</stack><flavor nodes=\"3\" nodesWhitelisted=\"0\">Zone1</flavor><flavor nodes=\"0\" nodesWhitelisted=\"0\">Zone2</flavor></paths></servicePaths>";

    private static final String DISTRIBUTION_RULE_1 = "<rule><id>0</id><percent>10.3</percent><server><name>distribution server 1</name><url>{protocol}://{host}:{port}/{urn}</url><path>/DataCenter1/Region1/Zone1</path><description>10.3% distribution server</description></server></rule>";
    private static final String DISTRIBUTION_RULE_2 = "<rule><id>1</id><percent>25.5</percent><server><name>distribution server 2</name><url>{protocol}://{host}:{port}/{urn}</url><path>/DataCenter2/Region1/Zone1</path><description>25.5% distribution server</description></server></rule>";
    private static final String DISTRIBUTION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><distribution>" + DISTRIBUTION_RULE_1 + DISTRIBUTION_RULE_2 + "</distribution>";

    private static final String WHITELISTED = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><whitelisted><paths>/DC0/Stack0</paths><paths>/DC1/Stack1</paths></whitelisted>";
    private static final String RULE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><if id=\"Rule\"><equals><param>param1</param><value>value1</value></equals><return><server isNonWhitelisted=\"true\"><name>Rule1</name><url>{protocol}://{host}:{port}/{urn}</url><path>/DataCenter1/Region1/Zone1</path><description>description_test</description></server></return></if>";
    private static final String SERVER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><server isNonWhitelisted=\"false\"><name>ServerTest</name><url>{protocol}://{host}:{port}/{urn}</url><path>/DataCenter1/Region1/Zone1</path><description>description_test</description></server>";
    private static final String URL_RULE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><if id=\"UrlRule\"><equals><param>param1</param><value>value1</value></equals><return><urlRule><urn>urn</urn><protocol>xre</protocol><port>8888</port><ipProtocolVersion>4</ipProtocolVersion></urlRule></return></if>";
    private static final String NAMESPACES = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><namespaces version=\"0\" dataNodeVersion=\"0\"><namespace version=\"0\" name=\"namespacedListTest1\"><description>Description1</description><valueSet><value>value2</value></valueSet><valueSet><value>value1</value></valueSet></namespace><namespace version=\"0\" name=\"namespacedListTest2\"><description>Description2</description><valueSet><value>value3</value></valueSet><valueSet><value>value4</value></valueSet></namespace></namespaces>";

    private SelectServer selectServer;
    private BackupUsageSchedule backupUsageSchedule;
    private ServicePaths servicePaths;
    private Distribution distribution;
    private Whitelisted whitelisted;
    private IfExpression rule;
    private IfExpression urlRule;
    private Server server;
    private Namespaces namespaces;

    private Serializer xmlSerializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());

    @Before
    public void initData() throws IllegalAccessException, InstantiationException {
        selectServer = getSelectServer();
        backupUsageSchedule = getBackupUsageSchedule();
        servicePaths = getServicePaths("pathServiceTest");
        distribution = getDistribution();
        whitelisted = getWhitelisted("/DC0/Stack0", "/DC1/Stack1");
        rule = getRule("Rule", "param1", "value1");
        urlRule = getUrlRule("UrlRule", "param1", "value1");
        server = getServer("ServerTest");
        namespaces = getNamespaces();
    }

    // ************************************* SERIALIZATION TESTS ********************************** //

    @Test
    public void testBackupUsageScheduleSerialization() throws Exception {
        String xmlBackupUsageSchedule = xmlSerializer.serialize(backupUsageSchedule, false);
        Diff diff = new Diff(BACKUP_USAGE_SCHEDULE, xmlBackupUsageSchedule);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testSelectServerSerialization() throws Exception {
        String xmlSelectServer = xmlSerializer.serialize(selectServer, false);
        Diff diff = new Diff(SELECT_SERVER, xmlSelectServer);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testServicePathsSerialization() throws Exception {
        String xmlServicePaths = xmlSerializer.serialize(servicePaths, false);
        Diff diff = new Diff(PATHS_SERVICE, xmlServicePaths);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testDistributionSerialization() throws Exception {
        String xmlDistribution = xmlSerializer.serialize(distribution, false);
        Diff diff = new Diff(DISTRIBUTION, xmlDistribution);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testWhitelistedSerialization() throws Exception {
        String xmlWhitelisted = xmlSerializer.serialize(whitelisted, false);
        Diff diff = new Diff(WHITELISTED, xmlWhitelisted);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testRuleSerialization() throws Exception {
        String xmlRule = xmlSerializer.serialize(rule, false);
        Diff diff = new Diff(RULE, xmlRule);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testUrlRuleSerialization() throws Exception {
        String xmlUrlRule = xmlSerializer.serialize(urlRule, false);
        Diff diff = new Diff(URL_RULE, xmlUrlRule);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testServerSerialization() throws Exception {
        String xmlServer = xmlSerializer.serialize(server, false);
        Diff diff = new Diff(SERVER, xmlServer);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testNamespacesSerialization() throws Exception {
        String xmlNamespaces = xmlSerializer.serialize(namespaces, false);
        Diff diff = new Diff(NAMESPACES, xmlNamespaces);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    // ************************************* DESERIALIZATION TESTS ********************************** //

    @Test
    public void testBackupUsageScheduleDeserialization() throws Exception {
        BackupUsageSchedule backupUsageScheduleObj = xmlSerializer.deserialize(BACKUP_USAGE_SCHEDULE, BackupUsageSchedule.class);
        assertEquals(backupUsageSchedule, backupUsageScheduleObj);
    }

    @Test
    public void testSelectServerDeserialization() throws Exception {
        SelectServer selectServerObj = xmlSerializer.deserialize(SELECT_SERVER, SelectServer.class);
        assertEquals(selectServer, selectServerObj);
    }

    @Test
    public void testServicePathsDeserialization() throws Exception {
        ServicePaths servicePathsObj = xmlSerializer.deserialize(PATHS_SERVICE, ServicePaths.class);
        assertEquals(servicePaths, servicePathsObj);
    }

    @Test
    public void testDistributionDeserialization() throws Exception {
        Distribution distributionObj = xmlSerializer.deserialize(DISTRIBUTION, Distribution.class);
        assertEquals(distribution, distributionObj);
    }

    @Test
    public void testWhitelistedDeserialization() throws Exception {
        Whitelisted whitelistedObj = xmlSerializer.deserialize(WHITELISTED, Whitelisted.class);
        assertEquals(whitelisted, whitelistedObj);
    }

    @Test
    public void testRuleDeserialization() throws Exception {
        IfExpression ruleObj = xmlSerializer.deserialize(RULE, IfExpression.class);
        assertEquals(rule, ruleObj);
    }

    @Test
    public void testUrlRuleDeserialization() throws Exception {
        IfExpression urlObj = xmlSerializer.deserialize(URL_RULE, IfExpression.class);
        assertEquals(urlRule, urlObj);
    }

    @Test
    public void testServerDeserialization() throws Exception {
        Server serverObj = xmlSerializer.deserialize(SERVER, Server.class);
        assertEquals(server, serverObj);
    }

    @Test
    public void testNamespacesDeserialization() throws Exception {
        Namespaces namespacesObj = xmlSerializer.deserialize(NAMESPACES, Namespaces.class);
        assertEquals(namespacesObj, namespacesObj);
    }

    public static SelectServer getSelectServer() {
        Equals receiverType = new Equals();
        receiverType.setParam("receiverType");
        receiverType.setValue("Native");

        Matches platform = new Matches();
        platform.setParam("platform");
        platform.setPattern("Windows");

        AndExpression andExpression = new AndExpression();
        andExpression.setItems(Arrays.asList((Expressions) receiverType, (Expressions) platform));

        Server ret = new Server();
        ret.setName("rule1");
        ret.setIsNonWhitelisted("false");
        ret.setUrl("{protocol}://{host}:{port}/{urn}");
        ret.setDescription("rule1 server route");
        ret.setPath("/PO/POC1/1.0");

        IfExpression ifExpression = new IfExpression();
        ifExpression.setId("rule1");
        ifExpression.setItems(Arrays.asList((Expressions) andExpression));
        ifExpression.setReturn(ret);

        Server server1 = new Server();
        server1.setName("distribution server 1");
        server1.setUrl("{protocol}://{host}:{port}/{urn}");
        server1.setDescription("1.0% distribution server");
        server1.setPath("/PO/POC1/2.0");
        Rule rule1 = new Rule();
        rule1.setId(0);
        rule1.setServer(server1);
        rule1.setPercent(1);

        Server server2 = new Server();
        server2.setName("distribution server 2");
        server2.setUrl("{protocol}://{host}:{port}/{urn}");
        server2.setDescription("1.0% distribution server");
        server2.setPath("/PO/POC1/3.0");
        Rule rule2 = new Rule();
        rule2.setId(1);
        rule2.setServer(server2);
        rule2.setPercent(1);

        Server defaultServer = new Server();
        defaultServer.setPath("1.0");
        defaultServer.setName("Default Server");
        defaultServer.setDescription("Default server route");
        defaultServer.setUrl("{protocol}://{host}:{port}/{urn}");

        Distribution distribution = new Distribution();
        distribution.setRules(Arrays.asList(rule1, rule2));
        distribution.setDefaultServer(defaultServer);

        SelectServer selectServer = new SelectServer();
        selectServer.setItems(Arrays.asList(ifExpression));
        selectServer.setDistribution(distribution);
        return selectServer;
    }

    public static BackupUsageSchedule getBackupUsageSchedule() {
        BackupUsageSchedule backupUsageSchedule = new BackupUsageSchedule();
        backupUsageSchedule.setSchedule("0 55 16 ? * MON,TUE,FRI,SAT,SUN");
        backupUsageSchedule.setDuration(36000000);
        return backupUsageSchedule;
    }

    public static ServicePaths getServicePaths(String serviceName) {
        PathItem stack1_1 = new PathItem(DELIMETER + "DataCenter1" + DELIMETER + "Region1" + DELIMETER + "Zone1", 2, 0);
        PathItem stack1_2 = new PathItem(DELIMETER + "DataCenter2" + DELIMETER + "Region2" + DELIMETER + "Zone2", 1, 0);
        PathItem stack1_3 = new PathItem(DELIMETER + "DataCenter2" + DELIMETER + "Region1" + DELIMETER + "Zone1", 0, 0);
        PathItem flavor_1 = new PathItem("Zone1", 3, 0);
        PathItem flavor_2 = new PathItem("Zone2", 0, 0);

        Paths path = new Paths(serviceName,
                new ArrayList<>(Arrays.asList(stack1_1, stack1_2, stack1_3)),
                new ArrayList<>(Arrays.asList(flavor_1, flavor_2)));

        List<Paths> paths = new ArrayList<>();
        paths.add(path);
        ServicePaths servicePaths = new ServicePaths(paths);
        return servicePaths;
    }

    public static Distribution getDistribution() {
        DistributionBuilder builder = new DistributionBuilder();
        Server server1 = newServerSimple("/DataCenter1/Region1/Zone1", "distribution server 1", "{protocol}://{host}:{port}/{urn}", "10.3% distribution server", null);
        Server server2 = newServerSimple("/DataCenter2/Region1/Zone1", "distribution server 2", "{protocol}://{host}:{port}/{urn}", "25.5% distribution server", null);
        builder.withRule(newDistributionRule(0, 10.3f, server1));
        builder.withRule(newDistributionRule(1, 25.5f, server2));
        return builder.build();
    }

    public static Whitelisted getWhitelisted(String... paths) {
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(Arrays.asList(paths));
        return whitelisted;
    }

    public static IfExpression getRule(String ruleName, String paramName, String value) throws InstantiationException, IllegalAccessException {
        IfExpressionBuilder builder = new IfExpressionBuilder();
        Server server = newServerSimple("/DataCenter1/Region1/Zone1", "Rule1", "{protocol}://{host}:{port}/{urn}", "description_test", "true");
        IfExpression expression = builder.
                withRuleName(ruleName).
                withExpression(newSingleParamExpression(Equals.class, paramName, value)).
                withReturnStatement(server).build();
        return expression;
    }

    public static Server getServer(String serverName) {
        Server server = newServerSimple("/DataCenter1/Region1/Zone1", serverName, "{protocol}://{host}:{port}/{urn}", "description_test", "false");
        return server;
    }

    public static IfExpression getUrlRule(String ruleName, String paramName, String value) throws InstantiationException, IllegalAccessException {
        IfExpressionBuilder builder = new IfExpressionBuilder();
        IfExpression expression = builder.
                withRuleName(ruleName).
                withExpression(newSingleParamExpression(Equals.class, paramName, value)).
                withReturnStatement(newUrlParams("urn", "xre", "8888", "4")).build();
        return expression;
    }

    public static Namespaces getNamespaces() {
        NamespacedListBuilder namespacedListBuilder = new NamespacedListBuilder();
        Namespaces namespaces = new Namespaces();
        List<NamespacedList> namespacedLists = new ArrayList<>();
        NamespacedList namespacedList = namespacedListBuilder.withName("namespacedListTest1").
                withDescription("Description1").
                withValues("value1", "value2").build();
        namespacedLists.add(namespacedList);
        namespacedList = namespacedListBuilder.withName("namespacedListTest2").
                withDescription("Description2").
                withValues("value3", "value4").build();
        namespacedLists.add(namespacedList);
        namespaces.setNamespaces(namespacedLists);
        return namespaces;
    }
}
