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

package com.comcast.redirector.core.spring.configurations.base;

import com.comcast.redirector.common.serializers.*;
import com.comcast.redirector.core.modelupdate.StacksChangePoller;
import com.comcast.redirector.core.IDynamicAppsAwareRedirectorFactory;
import com.comcast.redirector.core.applications.ApplicationsManager;
import com.comcast.redirector.core.applications.IApplicationsManager;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.IRedirector;
import com.comcast.redirector.core.modelupdate.DataChangePoller;
import com.comcast.redirector.core.modelupdate.IDataChangePoller;
import com.comcast.redirector.core.spring.AppScope;
import com.comcast.redirector.core.spring.AppsContextHolder;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorCuratorFramework;
import com.comcast.redirector.dataaccess.client.ZookeeperConnector;
import com.comcast.redirector.webserviceclient.IWebServiceClient;
import com.comcast.redirector.webserviceclient.WebServiceClient;
import com.comcast.tvx.cloud.MetaData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.GzipCompressionProvider;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.function.Predicate;

@Configuration
public abstract class AbstractCommonBeans {
    private static final Logger log = LoggerFactory.getLogger(AbstractCommonBeans.class);

    @Bean
    public ProviderStrategy<MetaData> providerStrategy() {
        return new RoundRobinStrategy<>();
    }

    @Bean
    public abstract ZKConfig config();

    @Bean
    public CuratorFramework curatorFramework() {
        ZKConfig config = config();

        if (config.useZooKeeperWaitTimePolicy()) {
            return new RedirectorCuratorFramework(config);
        }

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(config.getZooKeeperConnection())
                .connectionTimeoutMs(config.getZooKeeperConnectionTimeout())
                .sessionTimeoutMs(config.getZooKeeperSessionTimeout())
                .retryPolicy(new RetryNTimes(config.getZooKeeperRetryAttempts(), config.getZooKeeperRetryInterval()))
                .compressionProvider(new GzipCompressionProvider());

        return builder.build();
    }

    @Bean
    @Qualifier("dataChangePollerWebServiceClient")
    public IWebServiceClient dataChangePollerWebServiceClient() {
        ZKConfig config = config();

        return new WebServiceClient(config.getRestBasePath(),
                config.getPollingConnectionTimeoutMs(),
                config.getPollingRequestTimeoutMs(),
                config.getPollingSocketTimeoutMs(),
                config.getPollingClientsMaxNumberOfConnectionsTotal(),
                config.getPollingClientsMaxNumberOfConnectionsPerRoute());
    }

    @Bean
    @Qualifier("dataFacadeWebServiceClient")
    public IWebServiceClient dataFacadeWebServiceClient() {
        ZKConfig config = config();

        return new WebServiceClient(config.getRestBasePath(),
                config.getDataRestConnectionTimeoutMs(),
                config.getDataRestRequestTimeoutMs(),
                config.getDataRestSocketTimeoutMs(),
                config.getDataRestClientsMaxNumberOfConnectionsTotal(),
                config.getDataRestClientsMaxNumberOfConnectionsPerRoute());
    }

    @Bean
    public IDataChangePoller dataChangePoller() {
        return new DataChangePoller(dataChangePollerWebServiceClient());
    }

    @Bean
    public StacksChangePoller stacksChangePoller() {
        return new StacksChangePoller(config(), dataChangePoller());
    }

    @Bean
    public IDataSourceConnector connector() {
        ZKConfig config = config();
        CuratorFramework curatorFramework = curatorFramework();
        ZookeeperConnector zookeeperConnector;
        try {
            zookeeperConnector = new ZookeeperConnector(
                    curatorFramework,
                    config.getZooKeeperBasePath(),
                    config.isCacheHosts(),
                    stacksChangePoller()::initStacksPolling
            );

            zookeeperConnector.connect();
        } catch (Exception e) {
            log.error("Failed to create connector", e);
            throw new RuntimeException(e);
        }

        return zookeeperConnector;
    }

    @Bean
    @Scope(AppScope.APP_SCOPE)
    public String appName() {
        return AppsContextHolder.getCurrentApp();
    }

    @Bean
    public Predicate<String> isStaticDiscoveryNeededForApp() {
        ZKConfig config = config();
        return appName -> config.getExcludedAppsFromStackAutoDiscovery().contains(appName);
    }

    @Bean
    public Serializer xmlSerializer() {
        JAXBContextBuilder jaxbContextBuilder = new JAXBContextBuilder();
        return new XMLSerializer(jaxbContextBuilder.createContextForXML());
    }

    @Bean
    public Serializer jsonSerializer() {
        JAXBContextBuilder jaxbContextBuilder = new JAXBContextBuilder();
        return new JSONSerializer(jaxbContextBuilder.createContextForJSON());
    }
    
    @Bean
    public IApplicationsManager applicationsManager() {
        int appsRetrievingIntervalInSeconds = config().getAppsRetrievingIntervalInSeconds();
        return new ApplicationsManager(appsRetrievingIntervalInSeconds, config().getBackupBasePath());
    }

    @Bean
    public AppScope appScope() {
        return new AppScope();
    }

    @Bean
    public AbstractCommonBeans.AppScopeBeanFactoryPostProcessor appScopeBeanFactoryPostProcessor() {
        return new AbstractCommonBeans.AppScopeBeanFactoryPostProcessor();
    }

    public class AppScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            beanFactory.registerScope(AppScope.APP_SCOPE, AbstractCommonBeans.this.appScope());
        }
    }

    @Bean
    public IDynamicAppsAwareRedirectorFactory redirectorEngine() {
        return new AbstractCommonBeans.ContextAwareRedirectorFactory();
    }

    public static class ContextAwareRedirectorFactory implements IDynamicAppsAwareRedirectorFactory, ApplicationContextAware {
        private ApplicationContext context;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            context = applicationContext;
        }

        @Override
        public IRedirector createRedirector(String appName) {
            AppsContextHolder.setCurrentApp(appName);
            IRedirector redirector = context.getBean(IRedirector.class, appName);
            AppsContextHolder.clear();
            return redirector;
        }

        @Override
        public void startLookingForAppsChanges() {
            context.getBean(IApplicationsManager.class).start();
        }
    }
}
