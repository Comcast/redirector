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

package com.comcast.redirector.api.redirector.serializers;

import com.comcast.redirector.api.model.BackupUsageSchedule;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.JSONSerializer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

public class JSONSerializerTest {
    private static JSONSerializer jsonSerializer;

    private static final String BACKUP_USAGE_SCHEDULE = "{\"schedule\":\"0 55 16 ? * MON,TUE,FRI,SAT,SUN\",\"duration\":36000000}";
    private static final String PATHS_SERVICE = "{\"paths\":[" + "{\"serviceName\":\"pathServiceTest\",\"stack\":[{\"nodes\":2,\"nodesWhitelisted\":0,\"value\":\"/DataCenter1/Region1/Zone1\"},{\"nodes\":1,\"nodesWhitelisted\":0,\"value\":\"/DataCenter2/Region2/Zone2\"},{\"nodes\":0,\"nodesWhitelisted\":0,\"value\":\"/DataCenter2/Region1/Zone1\"}],\"flavor\":[{\"nodes\":3,\"nodesWhitelisted\":0,\"value\":\"Zone1\"},{\"nodes\":0,\"nodesWhitelisted\":0,\"value\":\"Zone2\"}]}" + "]}";

    private static final String DISTRIBUTION_RULE_1 = "{\"id\":0,\"percent\":10.3,\"server\":{\"name\":\"distribution server 1\",\"url\":\"{protocol}://{host}:{port}/{urn}\",\"path\":\"/DataCenter1/Region1/Zone1\",\"description\":\"10.3% distribution server\"}}";
    private static final String DISTRIBUTION_RULE_2 = "{\"id\":1,\"percent\":25.5,\"server\":{\"name\":\"distribution server 2\",\"url\":\"{protocol}://{host}:{port}/{urn}\",\"path\":\"/DataCenter2/Region1/Zone1\",\"description\":\"25.5% distribution server\"}}";
    private static final String DISTRIBUTION = "{\"rule\":[" + DISTRIBUTION_RULE_1 + "," + DISTRIBUTION_RULE_2 + "]}";
    private static final String WHITELISTED = "{\"paths\":[\"/DC0/Stack0\",\"/DC1/Stack1\"]}";
    private static final String RULE = "{\"id\":\"Rule\",\"equals\":[{\"param\":\"param1\",\"value\":\"value1\"}],\"return\":{\"server\":[{\"isNonWhitelisted\":\"true\",\"name\":\"Rule1\",\"url\":\"{protocol}://{host}:{port}/{urn}\",\"path\":\"/DataCenter1/Region1/Zone1\",\"description\":\"description_test\"}]}}";
    private static final String SERVER = "{\"isNonWhitelisted\":\"false\",\"name\":\"ServerTest\",\"url\":\"{protocol}://{host}:{port}/{urn}\",\"path\":\"/DataCenter1/Region1/Zone1\",\"description\":\"description_test\"}";
    private static final String URL_RULE = "{\"id\":\"UrlRule\",\"equals\":[{\"param\":\"param1\",\"value\":\"value1\"}],\"return\":{\"urlRule\":[{\"urn\":\"urn\",\"protocol\":\"xre\",\"port\":\"8888\",\"ipProtocolVersion\":\"4\"}]}}";
    private static final String NAMESPACES =  "{\"version\":0,\"dataNodeVersion\":0,\"namespace\":[{\"version\":0,\"name\":\"namespacedListTest1\",\"description\":\"Description1\",\"value\":[],\"valueSet\":[{\"value\":\"value2\"},{\"value\":\"value1\"}]},{\"version\":0,\"name\":\"namespacedListTest2\",\"description\":\"Description2\",\"value\":[],\"valueSet\":[{\"value\":\"value4\"},{\"value\":\"value3\"}]}]}";

    private BackupUsageSchedule backupUsageSchedule;
    private ServicePaths servicePaths;
    private Distribution distribution;
    private Whitelisted whitelisted;
    private IfExpression rule;
    private IfExpression urlRule;
    private Server server;
    private Namespaces namespaces;

    @BeforeClass
    public static void initJABXContext() {
        JAXBContextBuilder jaxbContextBuilder = new JAXBContextBuilder();
        jsonSerializer = new JSONSerializer(jaxbContextBuilder.createContextForJSON());
    }
    
    @Before
    public void initData() throws IllegalAccessException, InstantiationException {
        backupUsageSchedule = XMLSerializerTest.getBackupUsageSchedule();
        servicePaths = XMLSerializerTest.getServicePaths("pathServiceTest");
        distribution = XMLSerializerTest.getDistribution();
        whitelisted = XMLSerializerTest.getWhitelisted("/DC0/Stack0", "/DC1/Stack1");
        rule = XMLSerializerTest.getRule("Rule", "param1", "value1");
        urlRule = XMLSerializerTest.getUrlRule("UrlRule", "param1", "value1");
        server = XMLSerializerTest.getServer("ServerTest");
        namespaces = XMLSerializerTest.getNamespaces();
    }

    // ************************************* SERIALIZATION TESTS ********************************** //

    @Test
    public void testBackupUsageScheduleSerialization() throws Exception {
        byte[] backupUsageScheduleBytes = jsonSerializer.serializeToByteArray(backupUsageSchedule);
        String actualResult  = new String(backupUsageScheduleBytes);
        JSONAssert.assertEquals(BACKUP_USAGE_SCHEDULE, actualResult, false);
    }

    @Test
    public void testServicePathsSerialization() throws Exception {
        byte[] servicePathsBytes = jsonSerializer.serializeToByteArray(servicePaths);
        String actualResult  = new String(servicePathsBytes);
        JSONAssert.assertEquals(PATHS_SERVICE, actualResult, false);
    }

    @Test
    public void testDistributionSerialization() throws Exception {
        byte[] distributionBytes = jsonSerializer.serializeToByteArray(distribution);
        String actualResult  = new String(distributionBytes);
        JSONAssert.assertEquals(DISTRIBUTION, actualResult, false);
    }

    @Test
    public void testWhitelistedSerialization() throws Exception {
        byte[] whitelistedBytes = jsonSerializer.serializeToByteArray(whitelisted);
        String actualResult  = new String(whitelistedBytes);
        JSONAssert.assertEquals(WHITELISTED, actualResult, false);
    }

    @Test
    public void testRuleSerialization() throws Exception {
        byte[] ruleBytes = jsonSerializer.serializeToByteArray(rule);
        String actualResult  = new String(ruleBytes);
        JSONAssert.assertEquals(RULE, actualResult, false);
    }

    @Test
    public void testUrlRuleSerialization() throws Exception {
        byte[] urlRuleBytes = jsonSerializer.serializeToByteArray(urlRule);
        String actualResult  = new String(urlRuleBytes);
        JSONAssert.assertEquals(URL_RULE, actualResult, false);
    }

    @Test
    public void testServerSerialization() throws Exception {
        byte[] serverBytes = jsonSerializer.serializeToByteArray(server);
        String actualResult  = new String(serverBytes);
        JSONAssert.assertEquals(SERVER, actualResult, false);
    }

    @Test
    public void testNamespacesSerialization() throws Exception {
        byte[] namespacesBytes = jsonSerializer.serializeToByteArray(namespaces);
        String actualResult  = new String(namespacesBytes);
        JSONAssert.assertEquals(NAMESPACES, actualResult, false);
    }

    // ************************************* DESERIALIZATION TESTS ********************************** //
    
    @Test
    public void testBackupUsageScheduleDeserializationFromString() throws Exception {
        BackupUsageSchedule backupUsageScheduleObj = jsonSerializer.deserialize(BACKUP_USAGE_SCHEDULE, BackupUsageSchedule.class);
        assertEquals(backupUsageSchedule, backupUsageScheduleObj);
    }
    
    @Test
    public void testServicePathsDeserialization() throws Exception {
        ServicePaths servicePathsObj = jsonSerializer.deserialize(PATHS_SERVICE.getBytes(), ServicePaths.class);
        assertEquals(servicePaths, servicePathsObj);
    }
    
    @Test
    public void testServicePathsDeserializationFromString() throws Exception {
        ServicePaths servicePathsObj = jsonSerializer.deserialize(PATHS_SERVICE, ServicePaths.class);
        assertEquals(servicePaths, servicePathsObj);
    }
    
    @Test
    public void testDistributionDeserialization() throws Exception {
        Distribution distributionObj = jsonSerializer.deserialize(DISTRIBUTION.getBytes(), Distribution.class);
        assertEquals(distribution, distributionObj);
    }
    
    @Test
    public void testDistributionDeserializationFromString() throws Exception {
        Distribution distributionObj = jsonSerializer.deserialize(DISTRIBUTION, Distribution.class);
        assertEquals(distribution, distributionObj);
    }
    
    @Test
    public void testWhitelistedDeserialization() throws Exception {
        Whitelisted whitelistedObj = jsonSerializer.deserialize(WHITELISTED.getBytes(), Whitelisted.class);
        assertEquals(whitelisted, whitelistedObj);
    }
    
    @Test
    public void testWhitelistedDeserializationFromString() throws Exception {
        Whitelisted whitelistedObj = jsonSerializer.deserialize(WHITELISTED, Whitelisted.class);
        assertEquals(whitelisted, whitelistedObj);
    }
    
    @Test
    public void testRuleDeserialization() throws Exception {
        IfExpression ruleObj = jsonSerializer.deserialize(RULE.getBytes(), IfExpression.class);
        assertEquals(rule, ruleObj);
    }
    
    @Test
    public void testRuleDeserializationFromString() throws Exception {
        IfExpression ruleObj = jsonSerializer.deserialize(RULE, IfExpression.class);
        assertEquals(rule, ruleObj);
    }
    
    @Test
    public void testUrlRuleDeserialization() throws Exception {
        IfExpression urlRuleObj = jsonSerializer.deserialize(URL_RULE.getBytes(), IfExpression.class);
        assertEquals(urlRule, urlRuleObj);
    }
    
    @Test
    public void testUrlRuleDeserializationFrom() throws Exception {
        IfExpression urlRuleObj = jsonSerializer.deserialize(URL_RULE, IfExpression.class);
        assertEquals(urlRule, urlRuleObj);
    }
    
    @Test
    public void testServerDeserialization() throws Exception {
        Server serverObj = jsonSerializer.deserialize(SERVER.getBytes(), Server.class);
        assertEquals(server, serverObj);
    }
    
    @Test
    public void testServerDeserializationFrom() throws Exception {
        Server serverObj = jsonSerializer.deserialize(SERVER, Server.class);
        assertEquals(server, serverObj);
    }
    
    @Test
    public void testNamespacesDeserialization() throws Exception {
        Namespaces namespacesObj = jsonSerializer.deserialize(NAMESPACES.getBytes(), Namespaces.class);
        assertEquals(namespaces, namespacesObj);
    }
    
    @Test
    public void testNamespacesDeserializationFromString() throws Exception {
        Namespaces namespacesObj = jsonSerializer.deserialize(NAMESPACES, Namespaces.class);
        assertEquals(namespaces, namespacesObj);
    }
}
