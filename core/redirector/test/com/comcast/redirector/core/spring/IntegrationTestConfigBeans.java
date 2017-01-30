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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.core.spring;

import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.engine.RedirectorEngineFactory;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import com.google.common.io.Files;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class IntegrationTestConfigBeans {
    public static class ProfileNames {
        public static final String MULTIPLE_APPS_STATIC_DISCOVERY = "multipleappsStaticDiscovery";
        public static final String MULTIPLE_APPS_DISCOVERY_CONCURRENT_TEST = "multipleappsDiscoveryConcurrent";
        public static final String MULTIPLE_APPS_DYNAMIC_DISCOVERY = "multipleappsDynamicDiscovery";
        public static final String SANITY_STATIC_DISCOVERY = "sanityStaticDiscovery";
        public static final String SANITY_DYNAMIC_DISCOVERY = "sanityDynamicDiscovery";
        public static final String STACKS_RELOAD_TEST = "stacksReload";
        public static final String OFFLINE_DYNAMIC_APP = "offlineDynamicApp";
        public static final String OFFLINE_NAMESPACED_LISTS = "offlineNamespaced";
        public static final String SERVICE_AVAILABILITY = "serviceAvailability";
        public static final String SERVICE_AVAILABILITY_REGISTRY = "serviceAvailabilityRegistry";
        public static final String OFFLINE_STATIC_APP = "offlineStaticApp";
        public static final String ZK_CACHE_LISTENER = "zkCacheListener";
        public static final String CURATOR_RETRY = "curatorRetry";
    }

    public enum Profiles {
        MULTIPLE_APPS_STATIC_DISCOVERY(ProfileNames.MULTIPLE_APPS_STATIC_DISCOVERY, 21824),
        MULTIPLE_APPS_DYNAMIC_DISCOVERY(ProfileNames.MULTIPLE_APPS_DYNAMIC_DISCOVERY, 21824),
        MULTIPLE_APPS_DISCOVERY_CONCURRENT_TEST(ProfileNames.MULTIPLE_APPS_DISCOVERY_CONCURRENT_TEST, 21824),
        SANITY_STATIC_DISCOVERY(ProfileNames.SANITY_STATIC_DISCOVERY, 21824),
        SANITY_DYNAMIC_DISCOVERY(ProfileNames.SANITY_DYNAMIC_DISCOVERY, 21824),
        STACKS_RELOAD_TEST(ProfileNames.STACKS_RELOAD_TEST, 21824),
        SERVICE_AVAILABILITY_REGISTRY(ProfileNames.SERVICE_AVAILABILITY_REGISTRY, 51823),
        OFFLINE_DYNAMIC_APP(ProfileNames.OFFLINE_DYNAMIC_APP, 51824),
        OFFLINE_NAMESPACED_LISTS(ProfileNames.OFFLINE_NAMESPACED_LISTS, 51825),
        SERVICE_AVAILABILITY(ProfileNames.SERVICE_AVAILABILITY, 51826),
        OFFLINE_STATIC_APP(ProfileNames.OFFLINE_STATIC_APP, 51827),
        ZK_CACHE_LISTENER(ProfileNames.ZK_CACHE_LISTENER, 51828),
        CURATOR_RETRY(ProfileNames.CURATOR_RETRY, 51834),
        NO_CONNECTION("NO_CONNECTION", -1);

        private String name;
        private int dataSourceConnectionPort;

        Profiles(String name, int dataSourceConnectionPort) {
            this.name = name;
            this.dataSourceConnectionPort = dataSourceConnectionPort;
        }

        public int getDataSourceConnectionPort() {
            return dataSourceConnectionPort;
        }

        public String getName() {
            return name;
        }

        private static Config loadConfigForProfile(Profiles profile) {
            String zookeeperBasePath = getBasePath(profile.getName());
            Config config = ConfigLoader.doParse(Config.class);
            config.setZooKeeperBasePath(zookeeperBasePath);
            config.setZooKeeperConnection("localhost:" + profile.getDataSourceConnectionPort());
            return config;
        }
    }



    private static String getBasePath(String profile) {
        return "/tests/" + profile;
    }

    @Configuration
    @Profile(ProfileNames.MULTIPLE_APPS_STATIC_DISCOVERY)
    public static class MultiAppsStaticDiscoveryTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.MULTIPLE_APPS_STATIC_DISCOVERY);
        }
    }

    @Configuration
    @Profile(ProfileNames.MULTIPLE_APPS_DYNAMIC_DISCOVERY)
    public static class MultiAppsDynamicDiscoveryTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.MULTIPLE_APPS_DYNAMIC_DISCOVERY);
        }
    }

    @Configuration
    @Profile(ProfileNames.MULTIPLE_APPS_DISCOVERY_CONCURRENT_TEST)
    public static class MultiAppsDiscoveryConcurrentTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.MULTIPLE_APPS_DISCOVERY_CONCURRENT_TEST);
        }
    }

    @Configuration
    @Profile(ProfileNames.SANITY_STATIC_DISCOVERY)
    public static class StaticDiscoverySanityTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.SANITY_STATIC_DISCOVERY);
        }
    }

    @Configuration
    @Profile(ProfileNames.SANITY_DYNAMIC_DISCOVERY)
    public static class DynamicDiscoverySanityTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.SANITY_DYNAMIC_DISCOVERY);
        }
    }

    @Configuration
    @Profile(ProfileNames.OFFLINE_DYNAMIC_APP)
    public static class OfflineDynamicAppTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.OFFLINE_DYNAMIC_APP);
        }
    }

    @Configuration
    @Profile(ProfileNames.OFFLINE_NAMESPACED_LISTS)
    public static class OfflineNamespacedListsTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.OFFLINE_NAMESPACED_LISTS);
        }
    }

    @Configuration
    @Profile(ProfileNames.OFFLINE_STATIC_APP)
    public static class OfflineStaticAppTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.OFFLINE_STATIC_APP);
        }
    }

    @Configuration
    @Profile(ProfileNames.SERVICE_AVAILABILITY)
    public static class ServiceAvailabilityTestConfig {
        @Bean
        public ZKConfig config() {
            return Profiles.loadConfigForProfile(Profiles.SERVICE_AVAILABILITY);
        }
    }

    @Configuration
    @Profile(ProfileNames.CURATOR_RETRY)
    public static class CuratorRetryTestConfig {
        @Bean
        public ZKConfig config() throws Exception {
            Config config =  Profiles.loadConfigForProfile(Profiles.CURATOR_RETRY);
            config.setZooKeeperRetryAttempts(1);
            config.setZooKeeperConnectionTimeout(100);

            config.setZooKeeperWaitTimeBeforeReconnectMin(1);
            config.setZooKeeperWaitTimeBeforeReconnectMax(5);
            config.setUseZooKeeperWaitTimePolicy(true);
            config.setBackupBasePath(getTempDirName());
            return config;
        }

        @Bean
        RedirectorEngineFactory.BackupMode backupMode() {
            return RedirectorEngineFactory.BackupMode.FILE_SYSTEM;
        }

        private String getTempDirName () {
            return Files.createTempDir().getName();
        }
    }
}
