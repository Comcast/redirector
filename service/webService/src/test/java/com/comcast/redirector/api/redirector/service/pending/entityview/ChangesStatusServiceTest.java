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

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.redirector.helpers.WhitelistedHelper;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.ChangesStatusService;
import com.comcast.redirector.api.redirector.service.ruleengine.IRedirectorConfigService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.balancer.IBalancer;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.core.engine.RedirectorEngine;
import com.comcast.redirector.core.engine.ServerLookupMode;
import com.comcast.redirector.core.engine.rules.IFlavorRuleSet;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.ruleengine.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangesStatusServiceTest extends SelectServerTemplate {
    private final static String SERVICE_NAME = "testServiceName";
    private final static String FULL_PATH_STACK = "/DataCenter1/Region1/Zone12/testServiceName";

    @Mock
    private IEntityViewService<SelectServer> nextFlavorRulesEntityViewService;

    @Mock
    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    @Mock
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    @Mock
    private IEntityViewService<SelectServer> currentFlavorRulesEntityViewService;

    @Mock
    private IEntityViewService<Whitelisted> currentWhitelistedEntityViewService;

    @Mock
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @Mock
    private IEntityViewService<Server> nextDefaultServerEntityViewService;

    @Mock
    private IRedirectorEngineFactory redirectorEngineFactory;

    @Mock
    private IStacksService stacksService;

    @Mock
    private IRedirectorConfigService redirectorConfigService;

    @Mock
    private IBalancer balancer;

    @Mock
    IFlavorRuleSet flavorRuleSet;

    @Mock
    private IServerService serverService;

    @Mock
    private Serializer xmlSerializer;

    @InjectMocks
    private ChangesStatusService testee;

    private Server defaultServer;
    private Whitelisted whitelisted;
    private Whitelisted nextWhitelisted;
    private Distribution distribution;
    private Set<StackData> stackDataSet;
    private SelectServer selectServer;
    private RedirectorEngine.HostSelector hostSelector;
    private RedirectorConfig redirectorConfig;
    private PendingChangesStatus pendingChangesStatus;

    @Before
    public void setUp() {
        defaultServer = ServerHelper.prepareServer("default", "Zone12");
        whitelisted = WhitelistedHelper.createWhitelisted(WhitelistedHelper.defaultWhiteList);
        distribution = createDistribution(defaultServer);
        stackDataSet = createStacksData();
        selectServer = SelectServerTemplate.createDefaultSelectServerObject();
        selectServer.setDistribution(distribution);
        nextWhitelisted = new Whitelisted();
        List<String> paths = new ArrayList<>();
        paths.addAll(whitelisted.getPaths());
        nextWhitelisted.setPaths(paths);
        pendingChangesStatus = new PendingChangesStatus();
        Map<String, PendingChange> whitelistedChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(whitelistedChanges, Integer.toString(WhitelistedHelper.DC1_REGION1.hashCode()), null, new Value(WhitelistedHelper.DC1_REGION1), ActionType.DELETE);
        pendingChangesStatus.setWhitelisted(whitelistedChanges);
        hostSelector = new RedirectorEngine.HostSelector.Builder().setAppName(SERVICE_NAME).setFlavorRules(flavorRuleSet).setBalancer(balancer).build();
        redirectorConfig = getRedirectorConfig(1, 1);

        when(nextWhitelistedEntityViewService.getEntity(pendingChangesStatus, whitelisted)).thenReturn(nextWhitelisted);
        when(redirectorEngineFactory.newHostSelector(anyString(),(IServiceProviderManager)any(),(WhiteList)any(), (Model)any())).thenReturn(hostSelector);
        when(flavorRuleSet.getDefault()).thenReturn(null);
    }

    @Test
    public void validateRefuse_WhenCurrentModelNotValid_AndNextModelHasNotWhitelistdDefaultServer() {
        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, EntityType.WHITELIST, pendingChangesStatus,  selectServer, distribution, whitelisted, stackDataSet, defaultServer, redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.CANT_REDIRECT_TO_DEFAULT, "Can't redirect to Default server");

        assertEquals(excpectedResult, actualResult);
    }

    @Test
    public void validateAccept_WhenCurrentModelNotValid_AndNextModelHasOnlyWhitelist() {
        SelectServer nullSelectServer = new SelectServer();
        Server nullDefaultServer = null;
        Distribution nullDistribution = new DistributionBuilder().withDefaultServer(nullDefaultServer).withRule(null).build();
        EntityType nullEntityType = null;
        prepareNexModel(nullSelectServer, nullDefaultServer, nullDistribution);

        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, nullEntityType, pendingChangesStatus, nullSelectServer, nullDistribution, whitelisted, stackDataSet, nullDefaultServer, redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.SUCCESS, null);

        assertEquals(excpectedResult, actualResult);
    }

    @Test
    public void validateAccept_WhenCurrentModelNotValid_AndNextModelHasWhitelistAndDefaultUrlParam() {
        SelectServer nullSelectServer = new SelectServer();
        Server nullDefaultServer = null;
        Distribution nullDistribution = new DistributionBuilder().withDefaultServer(nullDefaultServer).withRule(null).build();
        EntityType nullEntityType = null;
        Map<String, PendingChange> urlParamsChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(urlParamsChanges,
                RedirectorConstants.DEFAULT_SERVER_NAME,
                UrlRuleExpressionsHelper.prepareUrlParams("changedDefaultProtocol", IpProtocolVersion.IPV6.getVersionString()),
                UrlRuleExpressionsHelper.prepareUrlParams("DefaultProtocol", IpProtocolVersion.IPV4.getVersionString()),
                ActionType.UPDATE);
        pendingChangesStatus.setUrlParams(urlParamsChanges);
        prepareNexModel(nullSelectServer, nullDefaultServer, nullDistribution);

        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, nullEntityType, pendingChangesStatus, nullSelectServer, nullDistribution, whitelisted, stackDataSet, nullDefaultServer, redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.SUCCESS, null);

        assertEquals(excpectedResult, actualResult);
    }

    @Test
    public void validateRefuse_WhenCurrentModelNotValid_AndNextModelHasWhitelistAndOtherPendingChanges() {
        SelectServer nullSelectServer = new SelectServer();
        Server nullDefaultServer = null;
        Distribution nullDistribution = new DistributionBuilder().withDefaultServer(nullDefaultServer).withRule(null).build();
        EntityType nullEntityType = null;
        Map<String, PendingChange> distributionChanges = new HashMap<>();
        PendingChangesHelper.putDistributionChange(distributionChanges, 3, 35.0f, "1.44", ActionType.ADD);
        pendingChangesStatus.setDistributions(distributionChanges);
        prepareNexModel(nullSelectServer, nullDefaultServer, nullDistribution);

        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, nullEntityType, pendingChangesStatus, nullSelectServer, nullDistribution, whitelisted, stackDataSet, nullDefaultServer, redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.CANT_REDIRECT_TO_DEFAULT, "Can't redirect to Default server");

        assertEquals(excpectedResult, actualResult);
    }

    @Test
    public void validateRefuse_WhenCurrentModelNotValid_AndNextModelHasNotDefaultServer() {
        SelectServer nullSelectServer = new SelectServer();
        Distribution nullDistribution = new DistributionBuilder().withDefaultServer(defaultServer).withRule(null).build();
        EntityType nullEntityType = null;
        com.comcast.redirector.ruleengine.model.Server server = new com.comcast.redirector.ruleengine.model.Server();
        server.setName(defaultServer.getName());
        server.setPath(defaultServer.getPath());
        InstanceInfo instanceInfo = new InstanceInfo(server,"");
        Map<String, PendingChange> urlParamsChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(urlParamsChanges,
                RedirectorConstants.DEFAULT_SERVER_NAME,
                UrlRuleExpressionsHelper.prepareUrlParams("changedDefaultProtocol", IpProtocolVersion.IPV6.getVersionString()),
                UrlRuleExpressionsHelper.prepareUrlParams("DefaultProtocol", IpProtocolVersion.IPV4.getVersionString()),
                ActionType.UPDATE);
        pendingChangesStatus.setUrlParams(urlParamsChanges);
        when(flavorRuleSet.getDefault()).thenReturn(server);
        when(balancer.getServiceInstance(server, ServerLookupMode.DEFAULT)).thenReturn(instanceInfo);
        prepareNexModel(nullSelectServer, defaultServer, nullDistribution);

        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, nullEntityType, pendingChangesStatus, nullSelectServer, nullDistribution, whitelisted, stackDataSet, defaultServer, redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.NOT_ENOUGH_HOSTS_FOR_DEFAULT, "1 hosts are required for default + distribution paths, actual: default=0, distribution=0, total=0");

        assertEquals(excpectedResult, actualResult);
    }

    @Test
    public void validateAccept_WhenCurrentModelNotValid_AndNextModelTakesOneStackForWhitelist() {
        Set<StackData> stackDataSet = new HashSet<>();
        List<HostIPs> hostIPsList = new ArrayList<>();
        StackData stackData = new StackData(FULL_PATH_STACK, hostIPsList);
        stackDataSet.add(stackData);
        SelectServer selectServer = SelectServerTemplate.createDefaultSelectServerObject();
        selectServer.setDistribution(distribution);

        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, EntityType.WHITELIST, pendingChangesStatus,  selectServer, distribution, whitelisted, stackDataSet, defaultServer,redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.SUCCESS, null);

        assertEquals(excpectedResult, actualResult);
    }

    @Test
    public void validateRefuse_WhenCurrentModelNotValid_AndNextModelDoesNotHaveDefaultServer() {
        com.comcast.redirector.ruleengine.model.Server server = new com.comcast.redirector.ruleengine.model.Server();
        server.setName(defaultServer.getName());
        server.setPath(defaultServer.getPath());
        InstanceInfo instanceInfo = new InstanceInfo(server,"");
        when(flavorRuleSet.getDefault()).thenReturn(server);
        when(balancer.getServiceInstance(server, ServerLookupMode.DEFAULT)).thenReturn(instanceInfo);

        RedirectorConfig redirectorConfig = getRedirectorConfig(2, 10);
        ValidationReport actualResult = testee.validateModelBeforeApprove(SERVICE_NAME, EntityType.WHITELIST, pendingChangesStatus,  selectServer, distribution, whitelisted, stackDataSet, defaultServer,redirectorConfig);
        ValidationReport excpectedResult = new ValidationReport(Validator.ValidationResultType.NOT_ENOUGH_HOSTS_FOR_DEFAULT, null);

        assertEquals(excpectedResult.getValidationResultType(), actualResult.getValidationResultType());
    }

    private Distribution createDistribution(Server defaultServer) {
        DistributionBuilder builder = new DistributionBuilder();
        builder.withRule(newDistributionRule(0, 10.3f, newSimpleServerForFlavor("/DataCenter1/Region1/Zone1")));
        builder.withRule(newDistributionRule(1, 25.5f, newSimpleServerForFlavor("/DataCenter2/Region1/Zone1")));
        builder.withDefaultServer(defaultServer);
        return builder.build();
    }

    private Set<StackData> createStacksData() {
        List<HostIPs> hostIPsList = new ArrayList<>();
        HostIPs hostIPs = new HostIPs("host1ipv4", "host1ipv6");
        hostIPsList.add(hostIPs);
        Set<StackData> stackDataSet = new HashSet<>();

        StackData stackData = new StackData(FULL_PATH_STACK, hostIPsList);
        stackDataSet.add(stackData);
        StackData stackData1 = new StackData("/DataCenter1/Region2/Zone1/testServiceName", hostIPsList);
        stackDataSet.add(stackData1);
        StackData stackData2 = new StackData("/DataCenter2/Region1/Zone1/testServiceName", hostIPsList);
        stackDataSet.add(stackData2);
        StackData stackData3 = new StackData("/DataCenter2/Region2/Zone1/testServiceName", hostIPsList);
        stackDataSet.add(stackData3);

        return stackDataSet;
    }

    private RedirectorConfig getRedirectorConfig(int minHost, int appMinHosts){
        RedirectorConfig redirectorConfig = new RedirectorConfig();
        redirectorConfig.setMinHosts(minHost);
        redirectorConfig.setAppMinHosts(appMinHosts);
        return redirectorConfig;
    }

    private void prepareNexModel(SelectServer selectServer, Server defaultServer, Distribution distribution) {
        when(nextDefaultServerEntityViewService.getEntity(pendingChangesStatus, eq(any()))).thenReturn(defaultServer);
        when(nextDistributionEntityViewService.getEntity(pendingChangesStatus, distribution)).thenReturn(distribution);
        when(nextFlavorRulesEntityViewService.getEntity(pendingChangesStatus, selectServer)).thenReturn(selectServer);
    }
}
