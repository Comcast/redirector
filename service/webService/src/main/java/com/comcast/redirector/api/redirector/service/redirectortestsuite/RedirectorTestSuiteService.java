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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.Context;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.TestCaseResult;
import com.comcast.redirector.api.model.testsuite.TestSuiteResponse;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.core.engine.RedirectorEngine;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class RedirectorTestSuiteService implements IRedirectorTestSuiteService {
    private static final String APP_NAME = "appName";

    @Autowired
    private IListServiceDAO<RedirectorTestCase> testSuiteDAO;

    @Autowired
    private IEntityViewService<SelectServer> nextFlavorRulesEntityViewService;

    @Autowired
    private IEntityViewService<SelectServer> currentFlavorRulesEntityViewService;

    @Autowired
    private IEntityViewService<URLRules> nextUrlRulesEntityViewService;

    @Autowired
    private IEntityViewService<URLRules> currentUrlRulesEntityViewService;

    @Autowired
    private INamespacedListsService namespacedListsService;

    @Autowired
    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    @Autowired
    private IEntityViewService<Whitelisted> currentWhitelistedEntityViewService;

    @Autowired
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @Autowired
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    @Autowired
    private IEntityViewService<Server> currentDefaultServerEntityViewService;

    @Autowired
    private IEntityViewService<Server> nextDefaultServerEntityViewService;

    @Autowired
    private IRedirectorEngineFactory redirectorEngineFactory;

    @Autowired
    private IStacksService stacksService;

    public enum TestMode {
        CURRENT,
        NEXT,
        EXTERNAL_REST
    }

    @Override
    public synchronized void saveTestCase(String serviceName, RedirectorTestCase testCase) {
        try {
            testSuiteDAO.saveById(testCase, serviceName, testCase.getName());
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }

    @Override
    public Collection<RedirectorTestCase> getAllTestCasesByServiceName(String serviceName) {
        return testSuiteDAO.getAll(serviceName);
    }

    @Override
    public RedirectorTestCase getTestCase(String serviceName, String testCaseId) {
        return testSuiteDAO.getById(serviceName, testCaseId);
    }

    @Override
    public void deleteTestCasesByIds(Collection<String> testCaseNames, String serviceName) {
        for (String testCaseId : testCaseNames) {
            // TODO: chack if node exists
            testSuiteDAO.deleteById(serviceName, testCaseId);
        }
    }

    @Override
    public TestCaseResult runTestCase(String serviceName, String testName, String mode) {
        MDC.put(APP_NAME, RedirectorConstants.Logging.APP_NAME_PREFIX + serviceName);
        TestMode testMode = TestMode.valueOf(mode.toUpperCase());
        RedirectorTestCase testCase = getTestCase(serviceName, testName);
        if (testCase == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // test case not found
        }

        IRedirectorEnvLoader envLoader = getZookeeperRedirectorEnvLoader(serviceName, testMode);

        SelectServer flavorRules = envLoader.getFlavorRules();
        URLRules urlRules = envLoader.getUrlRules();
        Whitelisted whitelisted = envLoader.getWhitelists();
        NamespacedListRepository namespacedLists = new SimpleNamespacedListsHolder(envLoader.getNamespacedListsBatch());
        IRedirectorEngine engine = redirectorEngineFactory.newRedirectorEngine(
                serviceName, flavorRules, urlRules, whitelisted, namespacedLists, envLoader.getStacks(), new RedirectorEngine.SessionLog());
        Context context = TestSuiteUtils.getRedirectorContext(testCase);
        InstanceInfo instanceInfo = engine.redirect(context.asMap());

        TestSuiteResponse actual = TestSuiteUtils.getRedirectorResponse(instanceInfo);
        TestCaseResult testCaseResult = new TestCaseResult();
        testCaseResult.setStatus(TestCaseResult.Status.fromTestCase(testCase.getExpected(), actual));
        testCaseResult.setActual(actual);
        testCaseResult.setLogs(AutoTestRunner.getSessionLogs(context, engine));
        MDC.remove(APP_NAME);
        return testCaseResult;
    }

    private IRedirectorEnvLoader getZookeeperRedirectorEnvLoader(String serviceName, TestMode testMode) {
        switch (testMode) {
            case CURRENT:
                return new ZookeeperRedirectorEnvLoader(serviceName, currentFlavorRulesEntityViewService,
                    currentUrlRulesEntityViewService, currentWhitelistedEntityViewService, currentDistributionEntityViewService, currentDefaultServerEntityViewService,
                        stacksService, namespacedListsService);
            case NEXT:
                return new ZookeeperRedirectorEnvLoader(serviceName, nextFlavorRulesEntityViewService,
                    nextUrlRulesEntityViewService, nextWhitelistedEntityViewService, nextDistributionEntityViewService, nextDefaultServerEntityViewService,
                        stacksService, namespacedListsService);
            default:
                throw new IllegalStateException("unknown test mode");
        }
    }

    private IRedirectorEnvLoader getRedirectorEnvLoader(String serviceName, TestMode testMode, String baseURL) {
        switch (testMode) {
            case CURRENT:
            case NEXT:
                return getZookeeperRedirectorEnvLoader(serviceName, testMode);
            case EXTERNAL_REST:
                return new ExternalEndpointRedirectorEnvLoader(serviceName, baseURL);
            default:
                throw new IllegalStateException("unknown test mode");
        }
    }

    static NamespacedListsBatch getNamespacedListsBatch(Namespaces namespaces) {
        NamespacedListsBatch namespacedLists = new NamespacedListsBatch();
        if (namespaces.getNamespaces() != null) {
            for (NamespacedList item : namespaces.getNamespaces()) {
                Set<String> values = new HashSet<>(item.getValueSet().size());
                for (NamespacedListValueForWS value : item.getValueSet()) {
                    values.add(item.getType().equals(NamespacedListType.ENCODED) ? value.getEncodedValue() : value.getValue());
                }
                namespacedLists.addValues(item.getName(), values);
            }
        }

        return namespacedLists;
    }

    public Collection<TestCaseResult> autoCreateTestCasesAndRun(String serviceName, String mode, String baseURL){
        AutoTestRunner testRunner = new AutoTestRunner(
            getRedirectorEnvLoader(
                serviceName,
                TestMode.valueOf(mode.toUpperCase()),
                baseURL),
            serviceName,
            redirectorEngineFactory);
        return testRunner.createAndRunTests();
    }

    interface IRedirectorEnvLoader {
        SelectServer getFlavorRules();
        URLRules getUrlRules();
        NamespacedListsBatch getNamespacedListsBatch();
        Whitelisted getWhitelists();
        Set<StackData> getStacks();
    }
}
