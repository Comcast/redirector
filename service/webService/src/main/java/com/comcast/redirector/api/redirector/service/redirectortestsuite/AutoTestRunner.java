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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.testsuite.*;
import com.comcast.redirector.common.Context;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.engine.ILoggable;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.core.engine.RedirectorEngine;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class AutoTestRunner {
    private static final Logger log = LoggerFactory.getLogger(AutoTestRunner.class);

    private static ExecutorService autoTestExecutor = Executors.newCachedThreadPool();
    private RedirectorTestSuiteService.IRedirectorEnvLoader envLoader;
    private String serviceName;
    private IRedirectorEngineFactory redirectorEngineFactory;

    private SelectServer flavorRules;
    private URLRules urlRules;
    private IRedirectorEngine engine;
    private NamespacedListRepository namespacedLists;

    public AutoTestRunner(RedirectorTestSuiteService.IRedirectorEnvLoader envLoader, String serviceName, IRedirectorEngineFactory redirectorEngineFactory) {
        this.envLoader = envLoader;
        this.serviceName = serviceName;
        this.redirectorEngineFactory = redirectorEngineFactory;

        init();
    }

    Collection<TestCaseResult> createAndRunTests(){
        List<RedirectorTestCase> testCases = createTestCases();
        return executeTestCases(testCases);
    }

    private Collection<TestCaseResult> executeTestCases(List<RedirectorTestCase> testCases) {
        try {
            List<Future<TestCaseResult>> testResultFutures = autoTestExecutor.invokeAll(
                FluentIterable
                    .from(testCases)
                    .transform(new Function<RedirectorTestCase, Callable<TestCaseResult>>() {
                        @Override
                        public Callable<TestCaseResult> apply(final RedirectorTestCase testCase) {
                            return new Callable<TestCaseResult>() {
                                @Override
                                public TestCaseResult call() throws Exception {
                                    Context context = TestSuiteUtils.getRedirectorContext(testCase);
                                    InstanceInfo instanceInfo = engine.redirect(context.asMap());

                                    TestSuiteResponse actual = TestSuiteUtils.getRedirectorResponse(instanceInfo);
                                    TestCaseResult testCaseResult = new TestCaseResult();
                                    testCaseResult.setStatus(TestCaseResult.Status.fromTestCase(testCase.getExpected(), actual));
                                    testCaseResult.setActual(actual);
                                    testCaseResult.setTestCase(testCase);
                                    testCaseResult.setLogs(getSessionLogs(context, engine));

                                    return testCaseResult;
                                }
                            };
                        }
                    }).toList()
            );

            return FluentIterable.from(testResultFutures)
                .transform(new Function<Future<TestCaseResult>, TestCaseResult>() {
                    @Override
                    public TestCaseResult apply(Future<TestCaseResult> input) {
                        try {
                            return input.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("Failed to get TestCaseResult from future. Skipping", e);
                            return null;
                        }
                    }
                }).filter(Predicates.notNull()).toList();
        } catch (InterruptedException e) {
            log.error("Failed to execute auto TestCases", e);
        }
        return null;
    }

    public static List<String> getSessionLogs(Context context, IRedirectorEngine engine) {
        List<String> logs = new ArrayList<>();
        ILoggable.ISessionLog sessionLog = ((ILoggable) engine).getLog();
        Session session = sessionLog.pollById(context.asMap().get(Context.SESSION_ID));
        logs.addAll(session.getEvents().stream().map(Event::getMessage).collect(Collectors.toList()));
        return logs;
    }

    public List<RedirectorTestCase> createTestCases() {
        final IToTestConverter flavorRuleConverter = new FlavorRuleToTestConverter(serviceName, namespacedLists);
        final IToTestConverter distributionConverter = new DistributionRuleToTestConverter(serviceName);
        final IToTestConverter urlRuleConverter = new UrlRuleToTestConverter(serviceName, namespacedLists);
        try {
            List<Future<RedirectorTestCase>> testFutures = new ArrayList<>();

            if (flavorRules.getItems() != null)
                testFutures.addAll(autoTestExecutor.invokeAll(
                    FluentIterable.from(flavorRules.getItems())
                    .transform(new Function<IfExpression, Callable<RedirectorTestCase>>() {

                        @Override
                        public Callable<RedirectorTestCase> apply(final IfExpression expression) {
                            return new Callable<RedirectorTestCase>() {
                                @Override
                                public RedirectorTestCase call() throws Exception {
                                    return flavorRuleConverter.toTestCase(expression);
                                }
                            };
                        }
                    })
                    .toList()
                ));

            testFutures.addAll(autoTestExecutor.invokeAll(
                FluentIterable.from(flavorRules.getDistribution().getRules())
                    .transform(new Function<Rule, Callable<RedirectorTestCase>>() {

                        @Override
                        public Callable<RedirectorTestCase> apply(final Rule expression) {
                            return new Callable<RedirectorTestCase>() {
                                @Override
                                public RedirectorTestCase call() throws Exception {
                                    return distributionConverter.toTestCase(expression);
                                }
                            };
                        }
                    })
                    .toList()
            ));

            if (urlRules.getItems() != null) {
                testFutures.addAll(autoTestExecutor.invokeAll(
                    FluentIterable.from(urlRules.getItems())
                        .transform(new Function<IfExpression, Callable<RedirectorTestCase>>() {

                            @Override
                            public Callable<RedirectorTestCase> apply(final IfExpression expression) {
                                return new Callable<RedirectorTestCase>() {
                                    @Override
                                    public RedirectorTestCase call() throws Exception {
                                        RedirectorTestCase result = urlRuleConverter.toTestCase(expression);
                                        return result;
                                    }
                                };
                            }
                        })
                        .toList()
                ));
            }

            return FluentIterable.from(testFutures)
                .transform(new Function<Future<RedirectorTestCase>, RedirectorTestCase>() {
                    @Override
                    public RedirectorTestCase apply(Future<RedirectorTestCase> input) {
                        try {
                            if (input != null)
                                return input.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("Failed to get TestCase from future. Skipping", e);
                        }

                        return null;
                    }
                }).filter(Predicates.notNull()).toList();
        } catch (InterruptedException e) {
            log.error("Failed to get TestCases from rules", e);
            return Collections.emptyList();
        }
    }

    private void init() {
        flavorRules = envLoader.getFlavorRules();

        // TODO: later those local variables can become members if needed
        urlRules = envLoader.getUrlRules();
        Whitelisted whitelisted = envLoader.getWhitelists();
        namespacedLists = new SimpleNamespacedListsHolder(envLoader.getNamespacedListsBatch());
        Set<StackData> stacks = envLoader.getStacks();

        engine = redirectorEngineFactory.newRedirectorEngine(
            serviceName, flavorRules, urlRules, whitelisted, namespacedLists, stacks, new RedirectorEngine.SessionLog());
    }

}
