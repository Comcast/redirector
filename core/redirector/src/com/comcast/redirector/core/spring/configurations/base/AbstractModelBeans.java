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

package com.comcast.redirector.core.spring.configurations.base;

import com.comcast.redirector.core.applications.IModelRefreshManager;
import com.comcast.redirector.core.applications.ModelRefreshManager;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManagerFactory;
import com.comcast.redirector.core.balancer.serviceprovider.ServiceProviderManagerFactory;
import com.comcast.redirector.core.engine.IRedirector;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.core.engine.RedirectorEngineFactory;
import com.comcast.redirector.core.engine.RedirectorImpl;
import com.comcast.redirector.core.modelupdate.IRedirectorEngineProvider;
import com.comcast.redirector.core.modelupdate.RedirectorEngineProvider;
import com.comcast.redirector.core.modelupdate.holder.IDataStoreAwareNamespacedListsHolder;
import com.comcast.redirector.core.modelupdate.holder.NamespacedListsHolder;
import com.comcast.redirector.core.spring.AppScope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
public abstract class AbstractModelBeans {

    @Bean
    @Scope(AppScope.APP_SCOPE) // TODO: move to common beans
    public IRedirector redirector(String appName) {
        return new RedirectorImpl();
    }

    @Bean
    public IDataStoreAwareNamespacedListsHolder namespacedListsHolder() {
        return new NamespacedListsHolder();
    }

    @Bean
    @Scope(AppScope.APP_SCOPE)
    public IRedirectorEngineProvider modelProvider(String appName) {
        return new RedirectorEngineProvider();
    }

    @Bean
    @Lazy // this annotation is needed to let backups be initialized first so if ZK is down apps list is loaded from backup so edge case of DiscoveryBackupManager which depends on this backup will execute correctly
    public IServiceProviderManagerFactory serviceProviderManagerFactory() {
        return new ServiceProviderManagerFactory();
    }

    @Bean
    @Scope(AppScope.APP_SCOPE)
    public IRedirectorEngineFactory redirectorEngineFactory() {
        return new RedirectorEngineFactory(serviceProviderManagerFactory());
    }

    @Bean
    @Scope(AppScope.APP_SCOPE)
    public IModelRefreshManager initModelRefreshManager() {
        return new ModelRefreshManager();
    }
}
