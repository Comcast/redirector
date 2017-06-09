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

package com.comcast.redirector.api.redirector.service.pending;


import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.redirector.service.IStacksService;
import com.comcast.redirector.api.redirector.service.pending.entityview.IEntityViewService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.api.redirector.service.redirectortestsuite.SimpleNamespacedListsHolder;
import com.comcast.redirector.api.redirector.service.ruleengine.IRedirectorConfigService;
import com.comcast.redirector.api.redirector.utils.CoreUtils;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.util.StacksHelper;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManagerFactory;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.core.modelupdate.chain.validator.CheckIfAbleToRedirect;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import com.comcast.xre.redirector.utils.XreGuideAppNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Set;

@Service
public class ChangesStatusService implements IChangesStatusService {
    private static final Logger log = LoggerFactory.getLogger(ChangesStatusService.class);

    private static final int NO_VERSION = -1;

    private ISimpleServiceDAO<PendingChangesStatus> pendingChangeDAO;

    private static final String DEFAULT_SERVER = "default";

    @Autowired
    private Serializer xmlSerializer;

    @Autowired
    @Qualifier("coreBackupPendingChangeDAO")
    private ISimpleServiceDAO<PendingChangesStatus> coreBackupPendingChangeDAO;

    private EntityType pendingChangeType;

    @Autowired
    private IEntityViewService<Distribution> nextDistributionEntityViewService;

    public void setPendingChangeType(EntityType pendingChangeType) {
        if (EntityType.PENDING_CHANGES_STATUS.equals(pendingChangeType) || EntityType.CORE_BACKUP_PENDING_CHANGES_STATUS.equals(pendingChangeType)) {
            this.pendingChangeType = pendingChangeType;
        }
        else {
            throw new IllegalArgumentException("Pending change can be only of type" + EntityType.PENDING_CHANGES_STATUS + " or " + EntityType.CORE_BACKUP_PENDING_CHANGES_STATUS);
        }
    }

    @Override
    public EntityType getPendingChangeType() {
        return pendingChangeType;
    }

    @Autowired
    private IEntityViewService<SelectServer> nextFlavorRulesEntityViewService;

    @Autowired
    private IEntityViewService<Whitelisted> nextWhitelistedEntityViewService;

    @Autowired
    private IEntityViewService<SelectServer> currentFlavorRulesEntityViewService;

    @Autowired
    private IEntityViewService<Whitelisted> currentWhitelistedEntityViewService;

    @Autowired
    private IEntityViewService<Distribution> currentDistributionEntityViewService;

    @Autowired
    private IEntityViewService<Server> currentDefaultServerEntityViewService;

    @Autowired
    private IEntityViewService<Server> nextDefaultServerEntityViewService;

    @Autowired
    private IRedirectorEngineFactory redirectorEngineFactory;

    @Autowired
    private IStacksService stacksService;

    @Autowired
    private IRedirectorConfigService redirectorConfigService;

    @Override
    public PendingChangesStatus getPendingChangesStatus(String serviceName){
        return getPendingChangesStatus(serviceName, NO_VERSION);
    }

    @Override
    public PendingChangesStatus getPendingChangesStatus(String serviceName, int version){
        PendingChangesStatus pendingChangesStatus;

        pendingChangesStatus = pendingChangeDAO.get(serviceName);
        if (pendingChangesStatus != null) {
            pendingChangesStatus.setVersion(pendingChangeDAO.getObjectVersion(serviceName));
        } else {
            pendingChangesStatus = new PendingChangesStatus();
        }

        if (NO_VERSION != version && version < pendingChangesStatus.getVersion()) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }

        return pendingChangesStatus;
    }

    @Override
    public Collection<String> getNewRulesIds(String serviceName, String objectType){
        PendingChangesStatus pendingChangesStatus = getPendingChangesStatus(serviceName, NO_VERSION);
        return PendingChangeStatusHelper.getNewRulesIds(objectType, pendingChangesStatus);
    }

    @Override
    public void savePendingChangesStatus(String serviceName, PendingChangesStatus pendingChangesStatus){

        if (EntityType.PENDING_CHANGES_STATUS.equals(pendingChangeType) &&
                coreBackupPendingChangeDAO.get(serviceName) != null && !coreBackupPendingChangeDAO.get(serviceName).isPendingChangesEmpty()) {
            throw new WebApplicationException("Resolve OFFLINE pending changes before updating data model.", Response.Status.CONFLICT);
        }
        try {
            pendingChangeDAO.save(pendingChangesStatus, serviceName);
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }

    @Override
    public PendingChange getPendingChangeByTypeAndId(String serviceName, String objectType, String changeId){
        PendingChange result;
        PendingChangesStatus pendingChangesStatus = getPendingChangesStatus(serviceName);
        switch (objectType) {
            case RedirectorConstants.PENDING_STATUS_PATH_RULES: {
                result = pendingChangesStatus.getPathRules().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_URL_RULES: {
                result = pendingChangesStatus.getUrlRules().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_TEMPLATE_PATH_RULES: {
                result = pendingChangesStatus.getTemplatePathRules().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_TEMPLATE_URL_PATH_RULES: {
                result = pendingChangesStatus.getTemplateUrlPathRules().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_URL_PARAMS: {
                result = pendingChangesStatus.getUrlParams().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_SERVERS: {
                result = pendingChangesStatus.getServers().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_DISTRIBUTIONS: {
                result = pendingChangesStatus.getDistributions().get(changeId);
                break;
            }
            case RedirectorConstants.PENDING_STATUS_STACKMANAGEMENT: {
                result = pendingChangesStatus.getWhitelisted().get(changeId);
                break;
            }
            default: {
                throw new WebApplicationException(Response.Status.NOT_FOUND); // change not found
            }
        }
        return  result;
    }

    public ValidationReport validateModelBeforeApprove(String serviceName, EntityType entityType){
        Set<StackData> stackDataSet = stacksService.getAllStacksAndHosts(serviceName);
        RedirectorConfig config = redirectorConfigService.getRedirectorConfig();
        Server currentDefaultServer = currentDefaultServerEntityViewService.getEntity(serviceName);
        PendingChangesStatus pendingChangesStatus = getPendingChangesStatus(serviceName);
        Whitelisted currentWhitelisted = currentWhitelistedEntityViewService.getEntity(serviceName);
        Distribution currentDistribution = currentDistributionEntityViewService.getEntity(serviceName);
        SelectServer currentRules = currentFlavorRulesEntityViewService.getEntity(serviceName);
        return validateModelBeforeApprove(serviceName, entityType, pendingChangesStatus, currentRules, currentDistribution, currentWhitelisted, stackDataSet, currentDefaultServer, config);
    }

    public ValidationReport validateModelBeforeApprove(String serviceName, EntityType entityType, PendingChangesStatus pendingChangesStatus, SelectServer currentRules, Distribution currentDistribution, Whitelisted currentWhitelisted, Set<StackData> stackDataSet, Server currentDefaultServer, RedirectorConfig config) {
        Server defaultServer = getDefaultServer(pendingChangesStatus, currentDefaultServer, entityType);
        Distribution distribution = getDistribution(pendingChangesStatus, defaultServer, currentDistribution, entityType);
        SelectServer selectServer = getFlavorRules(pendingChangesStatus, currentRules, distribution, entityType);
        Whitelisted whitelisted = getWhitelisted(pendingChangesStatus, currentWhitelisted, entityType);

        IServiceProviderManagerFactory serviceProviderManagerFactory = CoreUtils.newServiceProviderManagerFactory();
        IServiceProviderManager serviceProviderManager = serviceProviderManagerFactory.newStaticServiceProviderManager(stackDataSet);

        if (config == null) {
            throw new WebApplicationException("Validation failed because does not set settings for RedirectorConfig.", Response.Status.NOT_ACCEPTABLE);
        }
        int minHost = XreGuideAppNames.xreGuide.toString().equals(serviceName) ? config.getMinHosts() : config.getAppMinHosts();

        ModelTranslationService modelTranslationService = new ModelTranslationService(xmlSerializer);

        ValidationReport validationCurrentReport = new CheckIfAbleToRedirect.Builder()
                .setAppName(serviceName)
                .setFlavorRulesModel(modelTranslationService.translateFlavorRules(currentRules, new SimpleNamespacedListsHolder()))
                .setWhiteList(modelTranslationService.translateWhitelistedStacks(currentWhitelisted))
                .setServiceProviderManager(serviceProviderManager)
                .setRedirectorEngineFactory(redirectorEngineFactory)
                .setMinHosts(minHost).build().validate();

        ValidationReport validationReport = new CheckIfAbleToRedirect.Builder()
                .setAppName(serviceName)
                .setFlavorRulesModel(modelTranslationService.translateFlavorRules(selectServer, new SimpleNamespacedListsHolder()))
                .setWhiteList(modelTranslationService.translateWhitelistedStacks(whitelisted))
                .setServiceProviderManager(serviceProviderManager)
                .setRedirectorEngineFactory(redirectorEngineFactory)
                .setMinHosts(minHost).build().validate();

        if (Validator.ValidationResultType.CANT_REDIRECT_TO_DEFAULT.equals(validationReport.getValidationResultType())) {

            // in case then current model and next model are not valid
            if (Validator.ValidationResultType.CANT_REDIRECT_TO_DEFAULT.equals(validationCurrentReport.getValidationResultType()) && entityType == null) {
                if (pendingChangesStatus.onlyWhitelistedOrDefaultUrlParamsNotEmpty()) {

                    validationReport.setValidationResultType(Validator.ValidationResultType.SUCCESS);
                    validationReport.setMessage(null);

                    log.debug("Current model and next model are not valid now!");
                }

            } else {

                if (EntityType.WHITELIST.equals(entityType)) {
                    if(defaultServer == null || defaultServer.getPath() == null || !StacksHelper.isActiveAndWhitelistedHostsForFlavor(defaultServer.getPath(), stackDataSet, currentWhitelisted)) {
                        validationReport.setValidationResultType(Validator.ValidationResultType.SUCCESS);
                        validationReport.setMessage(null);
                    }
                } else {
                    log.info("Can't redirect to Default server. Default server: {}", defaultServer);
                }
            }
        }
        return validationReport;
    }

    private SelectServer getFlavorRules(PendingChangesStatus pendingChangesStatus, SelectServer currentRules, Distribution distribution, EntityType entityType) {
        SelectServer selectServer = currentRules;
        if (EntityType.RULE.equals(entityType) || entityType == null) {
            selectServer = nextFlavorRulesEntityViewService.getEntity(pendingChangesStatus, currentRules);
        }
        if (EntityType.DISTRIBUTION.equals(entityType) || entityType == null) {
            selectServer.setDistribution(distribution);
        }
        return selectServer;
    }

    private Whitelisted getWhitelisted(PendingChangesStatus pendingChangesStatus, Whitelisted currentWhitelisted, EntityType entityType) {
        Whitelisted whitelisted = currentWhitelisted;
        if (EntityType.WHITELIST.equals(entityType) || entityType == null) {
            whitelisted = nextWhitelistedEntityViewService.getEntity(pendingChangesStatus, currentWhitelisted);
        }
        return whitelisted;
    }

    private Server getDefaultServer(PendingChangesStatus pendingChangesStatus, Server currentDefaultServer, EntityType entityType) {
        Server server = currentDefaultServer;
        if (EntityType.DISTRIBUTION.equals(entityType) || entityType == null) {
            server = nextDefaultServerEntityViewService.getEntity(pendingChangesStatus, currentDefaultServer);
        }
        return server;
    }

    private Distribution getDistribution(PendingChangesStatus pendingChangesStatus, Server defaultServer, Distribution currentDistribution, EntityType entityType) {
        Distribution distribution = Distribution.newInstance(currentDistribution);
        if (EntityType.DISTRIBUTION.equals(entityType) || entityType == null) {
            distribution = nextDistributionEntityViewService.getEntity(pendingChangesStatus, currentDistribution);
        }
        distribution.setDefaultServer(defaultServer);
        return distribution;
    }

    public void setPendingChangeDAO(ISimpleServiceDAO<PendingChangesStatus> pendingChangeDAO) {
        this.pendingChangeDAO = pendingChangeDAO;
    }

}
