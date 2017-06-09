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

package com.comcast.redirector.core.spring;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.modelupdate.IDataChangePoller;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.facade.AppModelRestFacade;
import com.comcast.redirector.dataaccess.facade.CommonModelRestFacade;
import com.comcast.redirector.dataaccess.facade.IAppModelFacade;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.webserviceclient.IWebServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;

@Configuration
@Import({CommonBeans.class})
public class IntegrationTestBeans {
    @Inject
    CommonBeans commonBeans;
    
    @Bean(name = "integrationTestChangeListener")
    IntegrationTestChangeListener<StackSnapshot> integrationTestChangeListener() {
        return new IntegrationTestChangeListenerImpl<>(StackSnapshot.class, IntegrationTestEvent.Type.STACKS_UPDATE);
    }

    @Bean
    IntegrationTestChangeListener<String> integrationTestModelRefreshListener() {
        return new IntegrationTestChangeListenerImpl<>(String.class, IntegrationTestEvent.Type.MODEL_UPDATE);
    }

    @Bean
    IntegrationTestChangeListener<String> integrationTestStacksReloadListener() {
        return new IntegrationTestChangeListenerImpl<>(String.class, IntegrationTestEvent.Type.STACKS_RELOAD);
    }

    @Bean
    IntegrationTestChangeListener<String> integrationTestModelInitListener() {
        return new IntegrationTestChangeListenerImpl<>(String.class, IntegrationTestEvent.Type.MODEL_INIT);
    }

    @Bean
    IntegrationTestChangeListener<String> modelFailsInitListener() {
        return new IntegrationTestChangeListenerImpl<>(String.class, IntegrationTestEvent.Type.MODEL_INIT_FAILS);
    }

    @Bean
    IntegrationTestChangeListener<NamespacedListsBatch> newBatchAppliedListener() {
        return new IntegrationTestChangeListenerImpl<>(NamespacedListsBatch.class, IntegrationTestEvent.Type.NAMESPACED_LIST_APPLIED);
    }
    
    @Bean
    ICommonModelFacade commonModelFacade(IDataSourceConnector connector, Serializer xmlSerializer,
                                         IDataChangePoller dataChangePoller,
                                         IWebServiceClient dataFacadeWebServiceClient, ZKConfig config) {

        return CommonModelRestFacade.nonCachingModelFacade(connector, xmlSerializer, dataChangePoller,
                dataFacadeWebServiceClient, config);
    }

    @Bean
    @Scope(AppScope.APP_SCOPE)
    IAppModelFacade modelFacade(String appName, IDataSourceConnector connector, IDataChangePoller dataChangePoller,
                                IWebServiceClient dataFacadeWebServiceClient, ZKConfig config) {
        return new AppModelRestFacade.Builder()
                .withConnector(connector)
                .forApplication(appName)
                .withDataChangePoller(dataChangePoller)
                .withWebServiceClient(dataFacadeWebServiceClient)
                .withZkConfig(config)
                .build();
    }

}
