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

package com.comcast.redirector.core.engine;

import com.comcast.redirector.api.model.testsuite.Event;
import com.comcast.redirector.api.model.testsuite.Session;
import com.comcast.redirector.api.model.testsuite.SessionList;
import com.comcast.redirector.api.model.testsuite.TestSuiteResponse;
import com.comcast.redirector.common.*;
import com.comcast.redirector.common.logging.ExecutionFlow;
import com.comcast.redirector.common.logging.ExecutionStep;
import com.comcast.redirector.common.util.AppLoggingHelper;
import com.comcast.redirector.common.util.LoggerUtils;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.balancer.IBalancer;
import com.comcast.redirector.core.engine.rules.IFlavorRuleSet;
import com.comcast.redirector.core.engine.rules.IURLRuleSet;
import com.comcast.redirector.metrics.Metrics;
import com.comcast.redirector.ruleengine.model.*;
import com.comcast.xre.common.redirector.DataParamName;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;

import static com.comcast.redirector.common.RedirectorConstants.Logging.EXECUTION_STEP_PREFIX;

public class RedirectorEngine implements IRedirectorEngine, ILoggable {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(RedirectorEngine.class);
    private static final String DEFAULT = "default";
    private AppLoggingHelper loggingHelper;

    private HostSelector hostSelector;
    private IURLRuleSet urlRules;
    private String appName;
    private static final ThreadLocal<String> sessionId = new ThreadLocal<>();

    public RedirectorEngine(String appName, IBalancer balancer, IFlavorRuleSet flavorRuleSet, IURLRuleSet urlRuleSet, ISessionLog sessionLog, int modelVersion) {
        loggingHelper = new AppLoggingHelper(log, appName, modelVersion);
        hostSelector = new HostSelector.Builder()
            .setAppName(appName).setBalancer(balancer).setFlavorRules(flavorRuleSet)
            .setSessionLog(sessionLog)
            .setModelVersion(modelVersion).build();
        urlRules = urlRuleSet;

        this.appName = appName;
    }

    @Override
    public InstanceInfo redirect(Map<String, String> context) {
        return doRedirect(
            redirectContext -> {
                InstanceInfo instanceInfo = hostSelector.getHostByContext(redirectContext);
                return updateUrlParams(instanceInfo, redirectContext);
            },
            context);
    }

    private InstanceInfo doRedirect(Function<Map<String, String>, InstanceInfo> redirect,
                                    Map<String, String> context) {
        ThreadLocalLogger.setExecutionFlow(ExecutionFlow.redirect);
        hostSelector.startSession(context);

        InstanceInfo result = redirect.apply(context);

        if (result != null && StringUtils.isNotBlank(result.getUrl())) {
            Metrics.reportGatewayTrafficStats(appName, result.getStack(), result.getFlavor(), result.getRuleName());
        }

        loggingHelper.info(LoggerUtils.getRedirectLog(context, result));

        if (isTestMode(context)) {
            doLogTestSession(result);
        }
        hostSelector.stopSession();
        
        ThreadLocalLogger.clear();
        return result;
    }

    private void doLogTestSession(InstanceInfo instanceInfo) {
        getLog().log(sessionId.get(), TestSuiteResponseReader.get(instanceInfo));
    }

    @Override
    public ISessionLog getLog() {
        return hostSelector.sessionLog;
    }

    public static class SessionLog implements ISessionLog {
        private Map<String, Session> sessions = new HashMap<>();

        @Override
        public Session pollById(String sessionId) {
            return sessions.remove(sessionId);
        }

        @Override
        public SessionList pollAll() {
            Collection<Session> result = sessions.values();
            SessionList sessionList = new SessionList();
            sessionList.setSessions(result);
            return sessionList;
        }

        @Override
        public void write(String sessionId, String message) {
            Session session;
            if ((session = sessions.get(sessionId)) == null) {
                session = new Session(sessionId);
                sessions.put(sessionId, session);
            }

            session.getEvents().add(new Event(message));
        }

        @Override
        public void log(String sessionId, TestSuiteResponse actual) {
            Session session;
            if ((session = sessions.get(sessionId)) == null) {
                session = new Session(sessionId);
                sessions.put(sessionId, session);
            }

            session.setActual(actual);
        }

        @Override
        public void clearAll() {
            sessions.clear();
        }
    }

    public static boolean isTestMode(Map context) {
        String keyTestInfo = DataParamName.testInfo.name();
        return StringUtils.isNotBlank((String)context.get(keyTestInfo));
    }

    private static class TestSuiteResponseReader {
        public static TestSuiteResponse get(InstanceInfo instanceInfo) {
            TestSuiteResponse result = new TestSuiteResponse();
            result.setFlavor(instanceInfo.getFlavor());
            result.setIpVersion(Integer.toString(instanceInfo.getIpProtocolVersion()));
            result.setPort(Integer.toString(instanceInfo.getPort()));
            result.setProtocol(instanceInfo.getProtocol());
            result.setResponseType(instanceInfo.getServer().getReturnStatementType().name());
            result.setRule(instanceInfo.getRuleName());
            result.setAppliedUrlRules(instanceInfo.getAppliedUrlRules());
            result.setUrn(instanceInfo.getUrn());
            result.setXreStack(instanceInfo.getStack());

            return result;
        }
    }

    @Override
    public ServerGroup redirectServerGroup(ServerGroup serverGroup, Map<String, String> context) {
        Iterator<Server> serverIterator = serverGroup.getServers().iterator();
        while (serverIterator.hasNext()) {
            Server server = serverIterator.next();

            InstanceInfo serviceInstance = hostSelector.getHostByServer(server);
            if (serviceInstance == null || StringUtils.isBlank(serviceInstance.getUrl())) {
                // no available hosts or path / flavor not found. exclude this server
                serverIterator.remove();
                continue;
            }
            serviceInstance = updateUrlParams(serviceInstance, context);
            if (serviceInstance != null) {
                server.setURL(serviceInstance.getUrl());
            } else {
                serverIterator.remove();
            }
        }
        return serverGroup;
    }

    private InstanceInfo appendUrlQuery(InstanceInfo instanceInfo, Map<String, String> context) {
        String connectUrl = context.get(DataParamName.connectUrl.name());
        if (StringUtils.isNotBlank(connectUrl)) {
            instanceInfo.appendConnectionUrlToQuery(connectUrl);
        }

        if (instanceInfo.getServer() != null) {
            if (!isDefaultRuleOrDistributionApplied(instanceInfo)) {
                Map<String, String> query = instanceInfo.getServer().getQuery();
                query.put(QueryKeys.RULE_NAME.getKey(), instanceInfo.getRuleName());
        
                query.entrySet().forEach(entry -> {
                    try {
                        String keyValue = entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
                        if (instanceInfo.getUrl() != null && !instanceInfo.getUrl().contains(keyValue)) {
                            instanceInfo.appendUrlQuery(keyValue);
                        }
                    } catch (UnsupportedEncodingException e) {
                        log.error("Failed to append " + entry.getKey() + "=" + entry.getValue(), e);
                    }
                });
            }
        }

        return instanceInfo;
    }

    private boolean isDefaultRuleOrDistributionApplied (InstanceInfo instanceInfo) {
        return ((instanceInfo.getServer() != null && instanceInfo.getServer().getReturnStatementType() == ReturnStatementType.DISTRIBUTION_RULE)
                || (instanceInfo.getRuleName() != null && instanceInfo.getRuleName().toLowerCase().contains(DEFAULT)));
    }
    
    private InstanceInfo updateUrlParams(InstanceInfo serviceInstance, Map<String, String> params) {
        if (serviceInstance != null && serviceInstance.isNeedUrlParams()) {
            if (urlRules.isAvailable()) {
                UrlParams urlParams = urlRules.getUrlParams(params);
                if (serviceInstance.isValid(urlParams)) {
                    serviceInstance.replaceUrlParams(urlParams);
                } else {
                    loggingHelper.info("Invalid URL Params {} and ip address v4={} / v6={} ",
                            urlParams.toString(0, Collections.<String, String>emptyMap()),
                            serviceInstance.getServerIp(), serviceInstance.getServerIpV6());
                    serviceInstance = null;
                }
            } else {
                loggingHelper.warn("URL Rules are not available");
                serviceInstance = null;
            }
        }

        if (serviceInstance != null) {
            serviceInstance = appendUrlQuery(serviceInstance, params);
        }

        return serviceInstance;
    }

    public static class HostSelector implements IHostSelector {
        private AppLoggingHelper loggingHelper;
        private IBalancer balancer;
        private IFlavorRuleSet flavorRules;
        private ISessionLog sessionLog;

        public static class Builder {
            private String appName;
            private IBalancer balancer;
            private IFlavorRuleSet flavorRules;
            private ISessionLog sessionLog = new ISessionLog() {
                @Override
                public SessionList pollAll() {
                    return null;
                }

                @Override
                public Session pollById(String sessionId) {
                    return null;
                }

                @Override
                public void write(String sessionId, String message) {
                }

                @Override
                public void log(String sessionId, TestSuiteResponse actual) {

                }

                @Override
                public void clearAll() {

                }
            };
            private int modelVersion = RedirectorConstants.NO_MODEL_NODE_VERSION;

            public Builder setAppName(String appName) {
                this.appName = appName;
                return this;
            }

            public Builder setBalancer(IBalancer balancer) {
                this.balancer = balancer;
                return this;
            }

            public Builder setFlavorRules(IFlavorRuleSet flavorRules) {
                this.flavorRules = flavorRules;
                return this;
            }

            public Builder setSessionLog(ISessionLog sessionLog) {
                if (sessionLog != null) {
                    this.sessionLog = sessionLog;
                }
                return this;
            }

            public HostSelector build() {
                return new HostSelector(appName, balancer, flavorRules, sessionLog, modelVersion);
            }

            public Builder setModelVersion(int modelVersion) {
                this.modelVersion = modelVersion;
                return this;
            }
        }

        private HostSelector(String appName, IBalancer balancer, IFlavorRuleSet flavorRules, int modelVersion) {
            ThreadLocalLogger.setCustomMessage(null);
            loggingHelper = new AppLoggingHelper(log, appName, modelVersion);
            this.balancer = balancer;
            this.flavorRules = flavorRules;
        }

        public HostSelector(String appName, IBalancer balancer, IFlavorRuleSet flavorRules, ISessionLog sessionLog, int modelVersion) {
            this(appName, balancer, flavorRules, modelVersion);
            this.sessionLog = sessionLog;
        }

        private void startSession(Map<String, String> context) {
            sessionId.set(context.get(Context.SESSION_ID));
        }

        private void stopSession() {
            sessionId.remove();
        }

        @Override
        public InstanceInfo getHostByContext(Map<String, String> context) {
            if (!flavorRules.isAvailable()) {
                loggingHelper.warn("Flavor rules model is not available");
                return null;
            }

            Object result = flavorRules.getResult(context);
            if (result instanceof ServerGroup) {
                return new InstanceInfo((ServerGroup) result);
            }

            writeServerIntoSessionLog((Server) result);

            return getHostByServer((Server) result);
        }

        private void writeServerIntoSessionLog(Server server) {
            StringBuilder sb = new StringBuilder().append("ruleMatched=").append(server.getName());
            if (server.getPath().isEmpty()) {
                sb.append(" url=").append(server.getURL());
            } else {
                sb.append(" path=").append(server.getPath());
            }
            sessionLog.write(sessionId.get(), sb.toString());
            log.info(EXECUTION_STEP_PREFIX + ExecutionStep.redirect + ", " + sb.toString());
        }

        @Override
        public InstanceInfo getHostByServer(Server server) {
            InstanceInfo instanceInfo;
            // TODO: check if redundant
            if (server == null) {
                loggingHelper.warn("No server found");
                return null;
            } else if (StringUtils.isBlank(server.getPath())) { // TODO: should we check that url is without templates instead?
                instanceInfo = getAdvancedServiceInstance(server);
            } else {
                boolean applyFilter = true; // all traffic should go to whitelisted stacks
                instanceInfo = getSimpleHostByServer(server, applyFilter);

                if (isServerInstanceInvalid(instanceInfo)) {
                    // fallback from distribution
                    if (server.getReturnStatementType() == ReturnStatementType.DISTRIBUTION_RULE
                            || server.getReturnStatementType() == ReturnStatementType.PATH_RULE) {
                        String logMessage = "falling back to default server";
                        sessionLog.write(sessionId.get(), logMessage);
                        log.info(logMessage);
                        instanceInfo = getDefaultHost();
                    }
                }
            }
            if (instanceInfo == null) {
                // TODO: builder
                instanceInfo = new InstanceInfo(server, null);
            }

            return instanceInfo;
        }

        private InstanceInfo getSimpleHostByServer(Server server, boolean applyFilter) {
            ServerLookupMode mode;
            if (applyFilter) {
                mode = server.isNonWhitelisted() ? ServerLookupMode.DEFAULT_NON_WHITELISTED : ServerLookupMode.DEFAULT;
            } else {
                mode = ServerLookupMode.NON_FILTERED;
            }
            InstanceInfo instanceInfo = balancer.getServiceInstance(server, mode);
            String logMessage = "balancerResult=" + ((isServerInstanceInvalid(instanceInfo)) ? "not-available" : instanceInfo.getAddress()) + ", byLookupMode=[ applyFilter=" + mode.getFilterMode() + " , forceGetFromBackup=" + mode.isForceGetFromBackup() + " ]";
            sessionLog.write(sessionId.get(), logMessage);
            log.info(EXECUTION_STEP_PREFIX + ExecutionStep.redirect + ", " + logMessage);
            return instanceInfo;
        }

        @Override
        public InstanceInfo getDefaultHost() {
            Server defaultServer = getDefaultServer();
            loggingHelper.debug("Returned defaultServer {}", defaultServer);
            sessionLog.write(sessionId.get(), "Returned defaultServer " + (defaultServer != null ? defaultServer : "null"));

            InstanceInfo instanceInfo = balancer.getServiceInstance(defaultServer,
                    ServerLookupMode.DEFAULT);

            sessionLog.write(sessionId.get(), "balancer result for defaultServer is " + ((isServerInstanceInvalid(instanceInfo)) ? " not-available " : instanceInfo.getAddress()));
            return instanceInfo;
        }

        @Override
        public int getCountOfHostsForDefaultServer() {
            int count = getCountOfHostsForDefaultServerInternal();
            loggingHelper.info("[Get Default. Simple math]. Total count {}", count);
            return count;
        }

        private int getCountOfHostsForDefaultServerInternal() {
            return balancer.getCountOfHostsForPath(
                    getDefaultServer().getPath(),
                    ServerLookupMode.DEFAULT);
        }

        @Override
        public int getCountOfHostsForDistribution() {
            int count = 0;
            List<DistributionServer> distributionServers = flavorRules.getDistributionServers();

            if (distributionServers != null) {
                for (DistributionServer server : distributionServers) {
                    int hosts = balancer.getCountOfHostsForPath(server.getPath(), ServerLookupMode.DEFAULT);
                    loggingHelper.info("[Get Distribution. Simple math]. Server: {}, hosts {}", server.getPath(), hosts);
                    count += hosts;
                }
            }
            loggingHelper.info("[Get Distribution. Simple math]. Total count {}", count);
            return count;
        }

        @Override
        public int getPercentDeviationCountOfHostsForDistribution() {
            double count = 0;
            double percents = 0;
            double countOfHostsOnePercentForDistributions = 0;
            int percentDeviationCountOfHostsForDistribution = 0;
            List<DistributionServer> distributionServers = flavorRules.getDistributionServers();
            if (distributionServers != null) {
                for (DistributionServer server : distributionServers) {
                    percents += server.getPercent();
                    int hosts = balancer.getCountOfHostsForPath(server.getPath(), ServerLookupMode.DEFAULT);
                    count += hosts;
                }
                countOfHostsOnePercentForDistributions = count / percents;
            }
            int defaultFlavorHostsCount = getCountOfHostsForDefaultServerInternal();
            if (percents > 0) {
                double percentDefaultServer = 100 - percents;
                double countOfHostsOnePercentForDefaultServer = defaultFlavorHostsCount / percentDefaultServer;
                percentDeviationCountOfHostsForDistribution = (int) Math.round(100 - (countOfHostsOnePercentForDistributions * 100 / countOfHostsOnePercentForDefaultServer));
            }
            return percentDeviationCountOfHostsForDistribution < 0 ? 0 : percentDeviationCountOfHostsForDistribution;
        }

        private Server getDefaultServer() {
            return (Server) flavorRules.getDefault();
        }

        static boolean isServerInstanceInvalid(InstanceInfo instanceInfo) {
            return (instanceInfo == null || StringUtils.isBlank(instanceInfo.getUrl()));
        }

        private InstanceInfo getAdvancedServiceInstance(Server server) {
            InstanceInfo advancedUrlInstance = null;
            if (StringUtils.isNotBlank(server.getURL())
                    && !server.getURL().contains(RedirectorConstants.HOST_PLACEHOLDER)) {
                String advancedUrl = server.getURL();
                advancedUrlInstance = new InstanceInfo(server, advancedUrl);
            }
            return advancedUrlInstance;
        }
    }
}