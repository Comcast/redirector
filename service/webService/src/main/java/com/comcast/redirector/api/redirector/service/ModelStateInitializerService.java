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
 */
package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.model.whitelisted.WhitelistUpdate;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.model.xrestack.HostIPsListWrapper;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.api.redirector.OperationContextHolder;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.IPendingEntityWriteService;
import com.comcast.redirector.api.redirector.service.pending.IPendingSingletonEntityWriteService;
import com.comcast.redirector.api.redirector.service.ruleengine.IDistributionService;
import com.comcast.redirector.api.redirector.service.ruleengine.IRedirectorConfigService;
import com.comcast.redirector.api.redirector.service.ruleengine.IServerService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlParamsService;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static com.comcast.redirector.common.RedirectorConstants.DEFAULT_SERVER_NAME;
import static com.comcast.redirector.common.RedirectorConstants.NO_MODEL_NODE_VERSION;

@Service
public class ModelStateInitializerService implements IModelInitializerService {
    private static Logger log = LoggerFactory.getLogger(ModelStateInitializerService.class);

    private static final String DEFAULT_SERVER_NAME_DESCRIPTION = "Default server route";
    private static final int APP_MIN_HOSTS = 1;
    private static final int MIN_HOSTS = 1;

    private RedirectorConfig redirectorConfig = null;

    @Value("${redirector.protocolHostPortUrn:{protocol}://{host}:{port}/{urn}}")
    private String protocolHostPortUrn;

    @Value("${redirector.urlPartsShell:shell}")
    private String urlPartsShell;

    @Value("${redirector.urlPartsProtocol:http}")
    private String urlPartsProtocol;

    @Value("${redirector.urlPartsPort:10001}")
    private String urlPartsPort;

    @Value("${redirector.urlPartsIPProtocol:4}")
    private String urlPartsIPProtocol;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IWhiteListService whiteListService;
    
    @Autowired
    private IWhiteListStackUpdateService whiteListStackUpdateService;

    @Autowired
    private IDistributionService trafficDistributionService;

    @Autowired
    private IServerService routeService;

    @Autowired
    @Qualifier("pendingWhitelistedWriteService")
    private IPendingSingletonEntityWriteService<Whitelisted> pendingWhitelistedWriteService;

    @Autowired
    @Qualifier("pendingDistributionWriteService")
    private IPendingSingletonEntityWriteService<Distribution> pendingTrafficDistributionWriteService;

    @Autowired
    @Qualifier("urlParamsService")
    private IUrlParamsService urlPartsService;

    @Autowired
    @Qualifier("pendingUrlParamsWriteService")
    private IPendingEntityWriteService<UrlRule> pendingUrlPartsWriteService;

    @Autowired
    IRedirectorConfigService redirectorConfigService;

    @Autowired
    private IChangesStatusService changesStatusService;

    @Autowired
    private OperationContextHolder operationContextHolder;

    @PostConstruct
    public void init() {
        try {
            updateRedirectorConfig();
        } catch (RedirectorDataSourceException e) {
            log.warn("Could not fill config RedirectorConfig");
        }
    }

    @Override
    public ModelStatesWrapper getAllApplications() {

        ModelStatesWrapper allApplications = new ModelStatesWrapper();

        AppNames appNames = getAppNames();

        for (String appName : appNames.getAppNames()) {
            Whitelisted whitelisted = whiteListService.getWhitelistedStacks(appName);

            if (whitelisted.getPaths().size() == 0) {
                allApplications.add(new ModelState(appName, false));
                continue;
            }

            Default defaultUrlParts = urlPartsService.getDefaultUrlParams(appName);

            if (defaultUrlParts.getUrlRule().getUrn() == null) {
                allApplications.add(new ModelState(appName, false));
                continue;
            }

            Server route = routeService.getServer(appName);

            if (route == null) {
                allApplications.add(new ModelState(appName, false));
                continue;
            }

            allApplications.add(new ModelState(appName, true));
        }

        return allApplications;
    }

    private AppNames getAppNames() {
        AppNames appNames = new AppNames();
        stacksService.getAllServiceNames().forEach(appNames::add);
        return appNames;
    }

    @Override
    public Boolean activateModelForService(String appName) {
        try {
            if (redirectorConfig == null) {
                updateRedirectorConfig();
            }

            XreStackPath xreStackPath = getFirstActiveXreStack(appName);

            if (xreStackPath != null) {
                Whitelisted whitelisted = createWhitelisted(appName, xreStackPath.getStackOnlyPath());
                WhitelistedStackUpdates whitelistedStackUpdates = createWhitelistedStackUpdates(xreStackPath.getStackOnlyPath());
                Server route = createDefaultRoute(xreStackPath.getFlavor());
                UrlRule urlRule = createDefaultUrlParams();

                createOperationContextHolder(appName, whitelisted, whitelistedStackUpdates, urlRule);

                if (validateWhitelisted(whitelisted) && validateDefaultRoute(route) && validateDefaultUrlParts(urlRule)) {

                    saveWhitelisted(appName, xreStackPath.getStackOnlyPath());
                    saveTrafficDistribution(appName, route);
                    saveDefaultUrlParts(appName, urlRule);

                    log.info("Application: '" + appName + "' is enabled.");
                    return true;
                }
            }

            String error = String.format("Failed to create new Redirection model for %s application due to validation error(s).", appName);
            log.warn(error);
            throw new WebApplicationException(error, Response.status(Response.Status.SERVICE_UNAVAILABLE).build());

        } catch (RedirectorDataSourceException e) {
            String error = "Could not fill config RedirectorConfig";
            log.warn(error);
            throw new WebApplicationException(error, Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
        }

    }

    @Override
    public Boolean validModelExists() {
        try {
            AppNames appNames = getAppNames();

            if (appNames.getAppNames() == null || appNames.getAppNames().size() == 0) {
                return false;
            }

            for (String appName : appNames.getAppNames()) {
                if (isModelExists(appName)) {
                    return true;
                }
            }

        } catch (Exception e) {
            String error = "Error occurred while getting active hosts for redirect. ";
            log.warn(error);
            throw new WebApplicationException(error + e.getMessage(), Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
        }

        return false;
    }
    
    @Override
    public Boolean isValidModelForAppExists(String appName) {
        if (!stacksService.isServiceExists(appName)) {
            return false;
        }
        return isModelExists(appName);
    }
    
    @Override
    public Boolean validateApplication(String appName) {
        XreStackPath xreStackPath = getFirstActiveXreStack(appName);

        if (xreStackPath != null) {
            Whitelisted whitelisted = createWhitelisted(appName, xreStackPath.getStackOnlyPath());
            WhitelistedStackUpdates whitelistedStackUpdates = createWhitelistedStackUpdates(xreStackPath.getStackOnlyPath());
            Server route = createDefaultRoute(xreStackPath.getFlavor());
            UrlRule urlRule = createDefaultUrlParams();
            createOperationContextHolder(appName, whitelisted, whitelistedStackUpdates, urlRule);
            return  !isModelExists(appName) && validateWhitelisted(whitelisted)
                    && validateDefaultRoute(route)
                    && validateDefaultUrlParts(urlRule);
        }

        String error = String.format("Failed to validate new Redirection model for %s application ", appName);
        log.warn(error);
        throw new WebApplicationException(error, Response.status(Response.Status.SERVICE_UNAVAILABLE).build());

    }

    @Override
    public DefaultModelConstructionDetails defaultModelConstructionDetails(String appName) {
        DefaultModelConstructionDetails defaultModelConstructionDetails = new DefaultModelConstructionDetails();

        XreStackPath xreStackPath = getFirstActiveXreStack(appName);
        String ipAddress = "10.0.0.1";
        String ipProtocol = "4".equals(urlPartsIPProtocol) ? "IPv4" : "IPv6";

        if (xreStackPath != null) {
            HostIPsListWrapper host = stacksService.getHostsForStackAndService(xreStackPath.getStackAndFlavorPath(), appName);

            if (host.getHostIPsList() != null && host.getHostIPsList().size() > 0) {
                ipAddress = host.getHostIPsList().get(0).getIpV4Address();
            }

            String url = urlPartsProtocol + "://" + ipAddress + ":" + urlPartsPort + "/" + urlPartsShell + "?" + "urlRule=default";
            String UrlParts = urlPartsProtocol + "://" + ipProtocol + ":" + urlPartsPort + "/" + urlPartsShell;

            defaultModelConstructionDetails.setUrlForRedirection(url);
            defaultModelConstructionDetails.setDefaultRouteComposition(xreStackPath.getFlavor());
            defaultModelConstructionDetails.setDefaultUrlPartsComposition(UrlParts);
            defaultModelConstructionDetails.setFirstAvailableWhitelisted(xreStackPath.getStackOnlyPath());
        }
        return defaultModelConstructionDetails;
    }

    private void createOperationContextHolder(String appName, Whitelisted whitelisted, WhitelistedStackUpdates whitelistedStackUpdates, UrlRule urlRule) {
        URLRules urlRules = new URLRules();
        urlRules.setDefaultStatement(new Default(urlRule));

        PendingChangesStatus pendingChangesStatus = changesStatusService.getPendingChangesStatus(appName);
        Distribution distribution = trafficDistributionService.getDistribution(appName);

        ServicePaths servicePaths = stacksService.getStacksForService(appName);
        Snapshot snapshot = new Snapshot(appName);
        snapshot.setWhitelist(whitelisted);
        snapshot.setWhitelistedStackUpdates(whitelistedStackUpdates);
        snapshot.setServicePaths(servicePaths);
        snapshot.setPendingChanges(pendingChangesStatus);
        snapshot.setDistribution(distribution);
        snapshot.setUrlRules(urlRules);
        snapshot.setDefaultUrlParams(new Default(urlRule));
        operationContextHolder.buildContextFromOfflineSnapshot(appName, snapshot);
    }

    private UrlRule createDefaultUrlParams() {
        final UrlRule urlRule = new UrlRule();
        urlRule.setUrn(urlPartsShell);
        urlRule.setProtocol(urlPartsProtocol);
        urlRule.setPort(urlPartsPort);
        urlRule.setIpProtocolVersion(urlPartsIPProtocol);
        return urlRule;
    }

    private void saveDefaultUrlParts(String appName, UrlRule urlRule) {
        urlPartsService.saveUrlParams(appName, urlRule, RedirectorConstants.DEFAULT_URL_RULE);
        pendingUrlPartsWriteService.approve(appName, RedirectorConstants.DEFAULT_URL_RULE, NO_MODEL_NODE_VERSION);
    }

    private Server createDefaultRoute(String appVersion) {
        Server route = new Server();

        route.setUrl(protocolHostPortUrn);
        route.setPath(appVersion);
        route.setName(DEFAULT_SERVER_NAME);
        route.setDescription(DEFAULT_SERVER_NAME_DESCRIPTION);
        return route;
    }

    private void saveTrafficDistribution(String appName, Server route) {
        routeService.saveServer(appName, route);

        Distribution distribution = trafficDistributionService.getDistribution(appName);
        distribution.setDefaultServer(route);

        trafficDistributionService.saveDistribution(distribution, appName);
        pendingTrafficDistributionWriteService.approve(appName, NO_MODEL_NODE_VERSION);
    }

    private Whitelisted createWhitelisted(String appName, String path) {
        Whitelisted whitelisted = whiteListService.getWhitelistedStacks(appName);
        whitelisted.setPaths(Collections.singletonList(path));
        return whitelisted;
    }
    
    private WhitelistedStackUpdates createWhitelistedStackUpdates(String path) {
        WhitelistedStackUpdates whitelistedStackUpdates = new WhitelistedStackUpdates();
        whitelistedStackUpdates.addUpdateItem(new WhitelistUpdate(path, new Date().getTime(), ActionType.ADD), path);
        return whitelistedStackUpdates;
    }
    
    private void saveWhitelisted(String appName, String path) {
        Whitelisted whitelisted = whiteListService.getWhitelistedStacks(appName);
        whitelisted.setPaths(Collections.singletonList(path));

        whiteListService.saveWhitelistedStacks(whitelisted, appName);
    
        WhitelistedStackUpdates whitelistedStackUpdates = new WhitelistedStackUpdates();
        whitelistedStackUpdates.addUpdateItem(new WhitelistUpdate(path, new Date().getTime(), ActionType.ADD), path);
        whiteListStackUpdateService.saveWhitelistedStacksUpdates(whitelistedStackUpdates, appName);

        pendingWhitelistedWriteService.approve(appName, NO_MODEL_NODE_VERSION);
    }

    private XreStackPath getFirstActiveXreStack(String appName) {
        Set<PathItem> activeStacksAndFlavors = stacksService.getActiveStacksAndFlavors(appName);

        for (PathItem pathItem : activeStacksAndFlavors) {
            if (pathItem.getActiveNodesCount() > 0) {
                String path = pathItem.getValue();
                if (path.contains("/")) {
                    return new XreStackPath(path, appName);
                }
            }
        }
        return null;
    }

    private void updateRedirectorConfig() throws RedirectorDataSourceException {
        try {

            redirectorConfig = redirectorConfigService.getRedirectorConfig();

            boolean update = false;
            if (redirectorConfig != null) {
                if (redirectorConfig.getAppMinHosts() < 1) {
                    redirectorConfig.setAppMinHosts(APP_MIN_HOSTS);
                    update = true;
                }

                if (redirectorConfig.getMinHosts() < 1) {
                    redirectorConfig.setMinHosts(MIN_HOSTS);
                    update = true;
                }

            } else {
                redirectorConfig = new RedirectorConfig();
                redirectorConfig.setAppMinHosts(APP_MIN_HOSTS);
                redirectorConfig.setMinHosts(MIN_HOSTS);
                update = true;
            }

            if (update) {
                redirectorConfigService.saveRedirectorConfig(redirectorConfig);
                log.info("Create RedirectorConfig: minHosts=" + MIN_HOSTS + ", appMinHosts=" + APP_MIN_HOSTS);
            }

        } catch (RedirectorDataSourceException e) {
            log.warn("Could not read RedirectorConfig");
        }
    }

    private Boolean validateWhitelisted(Whitelisted whitelisted) {
        try {
            ModelValidationFacade.validateWhitelistedStacks(whitelisted, OperationContextHolder.getCurrentContext());
        } catch (ExpressionValidationException ex) {
            return false;
        }
        return true;
    }

    private Boolean validateDefaultRoute(Server route) {
        try {
            ModelValidationFacade.validateServer(route, OperationContextHolder.getCurrentContext());
        } catch (ExpressionValidationException e) {
            return false;
        }
        return true;
    }

    private Boolean validateDefaultUrlParts(UrlRule urlRule) {
        try {
            ModelValidationFacade.validateUrlParams(urlRule, EntityType.URL_PARAMS);
        } catch (ExpressionValidationException e) {
            return false;
        }
        return true;
    }

    private Boolean isModelExists(String appName) {
        Whitelisted whitelisted = whiteListService.getWhitelistedStacks(appName);

        if (whitelisted.getPaths().size() == 0) {
            return false;
        }

        Default defaultUrlParts = urlPartsService.getDefaultUrlParams(appName);

        if (defaultUrlParts.getUrlRule().getUrn() == null) {
            return false;
        }

        Server route = routeService.getServer(appName);
        return route != null;
    }
}
