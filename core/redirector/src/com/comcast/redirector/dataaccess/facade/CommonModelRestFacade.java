/*
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
 * @author Alexander Ievstratiev (oievstratiev@productengine.com)
 */
package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.api.model.AppNames;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.logging.ExecutionStep;
import com.comcast.redirector.common.logging.OperationResult;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.util.IAuthHeaderProducer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.modelupdate.IDataChangePoller;
import com.comcast.redirector.core.modelupdate.NewVersionHandler;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RestDataSourceExeption;
import com.comcast.redirector.dataaccess.dao.*;
import com.comcast.redirector.webserviceclient.IWebServiceClient;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static com.comcast.redirector.common.RedirectorConstants.Logging.OPERATION_RESULT;
import static com.comcast.redirector.common.RedirectorConstants.NAMESPACE_CONTROLLER_PATH;

public class CommonModelRestFacade extends AbstractModelFacade implements ICommonModelFacade {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(CommonModelRestFacade.class);
    
    private IStacksDAO stacksDAO;
    private ISimpleDAO<RedirectorConfig> redirectorConfigDAO;
    private IDAOFactory daoFactory;
    private IDataChangePoller dataChangePoller;
    private IWebServiceClient webServiceClient;

    private Integer currentNamespacedListsVersion = 0;
    private Integer nextNamespacedListsVersion = 0;
    private int currentNamespacedListsVersionUpdateIntervalSeconds = 60;

    @Autowired(required = false)
    IAuthHeaderProducer authHeaderProducer;
    
    @VisibleForTesting
    CommonModelRestFacade(IDataSourceConnector connector, IDAOFactory daoFactory, IDataChangePoller dataChangePoller, IWebServiceClient webServiceClient, ZKConfig zkConfig) {
        super(connector);
        this.daoFactory = daoFactory;
        this.webServiceClient = webServiceClient;
        this.dataChangePoller = dataChangePoller;
        currentNamespacedListsVersionUpdateIntervalSeconds = zkConfig.getNsListsPollIntervalSeconds();

        initDAOs();
        doStart();
    }
    
    public static CommonModelRestFacade cachingModelFacade(IDataSourceConnector connector, Serializer serializer, IDataChangePoller dataChangePoller, IWebServiceClient webServiceClient, ZKConfig zkConfig) {
        return new CommonModelRestFacade(connector, new DAOFactory(connector, true, serializer), dataChangePoller, webServiceClient, zkConfig);
    }
    
    public static CommonModelRestFacade nonCachingModelFacade(IDataSourceConnector connector, Serializer serializer, IDataChangePoller dataChangePoller, IWebServiceClient webServiceClient, ZKConfig zkConfig) {
        return new CommonModelRestFacade(connector, new DAOFactory(connector, false, serializer), dataChangePoller, webServiceClient, zkConfig);
    }
    
    private void initDAOs() {
        redirectorConfigDAO = daoFactory.getSimpleDAO(RedirectorConfig.class, EntityType.CONFIG, BaseDAO.NOT_COMPRESSED);
        stacksDAO = daoFactory.createStacksDAO();
    }
    
    @Override
    public Collection<NamespacedList> getAllNamespacedLists() {
        ThreadLocalLogger.setExecutionFlow("requestingAllNamespcedLists");
        try {
            Namespaces namespaces = webServiceClient.getRequest(Namespaces.class, concatEndpointAndVersion(nextNamespacedListsVersion, RedirectorConstants.NAMESPACE_CONTROLLER_PATH, "getAllNamespacedLists"));
            if (namespaces != null) {
                return namespaces.getNamespaces();
            }
        } catch (RestDataSourceExeption e) {
            log.error("failed to obtain namespaced lists", e);
        }
        return null;
    }
    
    @Override
    public NamespacedList getNamespacedList(String namespacedListName) {
        ThreadLocalLogger.setExecutionFlow("requestingNamespcedList");
        try {
            return webServiceClient.getRequest(NamespacedList.class, concatEndpointAndVersion(nextNamespacedListsVersion, RedirectorConstants.NAMESPACE_CONTROLLER_PATH, "getOne", namespacedListName));
        } catch (RestDataSourceExeption e) {
            log.error("failed to obtain namespaced list", e);
            return null;
        }
    }
    
    @Override
    public int getNamespacedListsVersion() {
        return getCurrentNamespacedListsVersion();
    }

    @Override
    public void initNamespacedListDataChangePolling(NewVersionHandler<Integer> refreshNamespacedLists) {
        dataChangePoller.startDataChangePolling("namespacedLists", NAMESPACE_CONTROLLER_PATH + "/getVersion/",
                currentNamespacedListsVersionUpdateIntervalSeconds,
                refreshNamespacedLists,
                this::getCurrentNamespacedListsVersion,
                this::setCurrentNamespacedListsVersion,
                this::setNextNamespacedListsVersion,
                null /* applicationName */);
    }
    @Override
    public RedirectorConfig getRedirectorConfig() {
        return redirectorConfigDAO.get();
    }
    
    @Override
    public void saveRedirectorConfig(RedirectorConfig config) {
        try {
            redirectorConfigDAO.save(config);
        } catch (SerializerException e) {
            log.error("Cannot save redirector config due to Serializer error");
        }
    }
    
    @Override
    public Set<String> getAllRegisteredApps() {
        ThreadLocalLogger.setExecutionFlow(ExecutionStep.getAllAppsList.toString());
        try {
            AppNames applicationNames = webServiceClient.getRequest(AppNames.class, concatEndpoint(RedirectorConstants.REDIRECTOR_CONTROLLER_PATH, RedirectorConstants.EndpointPath.GET_ALL_REGISTERED_APPS));
            if (applicationNames != null && applicationNames.getAppNames() != null) {
                return new LinkedHashSet<>(applicationNames.getAppNames());
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return new LinkedHashSet<>();
    }
    
    @Override
    public Set<XreStackPath> getAllStackPaths() {
        return stacksDAO.getAllStackPaths();
    }
    
    @Override
    public Set<XreStackPath> getAllStackPaths(Set<String> excludedApps) {
        return stacksDAO.getAllStackPaths(excludedApps);
    }
    
    @Override
    public Collection<HostIPs> getHosts(XreStackPath path) {
        return stacksDAO.getHosts(path);
    }
    
    @Override
    public List<String> getApplications() {
        ThreadLocalLogger.setExecutionFlow(ExecutionStep.getAllAppsList.toString());
        try {
            AppNames applicationNames = webServiceClient.getRequest(AppNames.class, concatEndpoint(RedirectorConstants.REDIRECTOR_CONTROLLER_PATH, RedirectorConstants.EndpointPath.APPLICATION_NAMES));
            if (applicationNames != null && applicationNames.getAppNames() != null) {
                return new LinkedList<>(applicationNames.getAppNames());
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return new LinkedList<>();
    }
    
    public Integer getCurrentNamespacedListsVersion() {
        return currentNamespacedListsVersion;
    }
    
    public void setCurrentNamespacedListsVersion(Integer currentNamespacedListsVersion) {
        this.currentNamespacedListsVersion = currentNamespacedListsVersion;
    }

    public void setNextNamespacedListsVersion(Integer nextNamespacedListsVersion) {
        this.nextNamespacedListsVersion = nextNamespacedListsVersion;
    }

    @Override
    public Integer getNextNamespacedListsVersion() {
        return nextNamespacedListsVersion;
    }

    @Override
    public Boolean isValidModelForAppExists(String appName) {
        try {
            Boolean request = webServiceClient.getRequest(Boolean.class, concatEndpoint(RedirectorConstants.INITIALIZER_CONTROLLER_PATH, RedirectorConstants.EndpointPath.VALID_MODEL_EXISTS, appName), MediaType.TEXT_PLAIN);
            return request == null ? Boolean.FALSE : request;
        } catch (Exception e) {
            log.setExecutionFlow(ExecutionStep.checkIsModelValid.toString());
            log.warn(OPERATION_RESULT + OperationResult.ParsingDataError + " " + e.getMessage());
        }
        return Boolean.FALSE;
    }
}
