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
package com.comcast.apps.e2e.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class E2EConfigLoader implements E2EConfig {

    private static final Logger log = LoggerFactory.getLogger(E2EConfigLoader.class);

    private static final String[] EXCLUDED_APPS_FOR_STACK_AUTO_DISCOVERY = new String[] {"xreGuide"};
    private static final String PROPERTIES_FILE_PROP_NAME = "appConfig";

    private Configuration config;


    public static final String DEFAULT_PROPERTIES_FILE_NAME = "end-to-end.properties";
    public static final String EXTRA_TESTS_CLASSES_NAMES_DELIMETER = "/";

    private static final class InstanceHolder {
        static E2EConfig instance = new E2EConfigLoader();
    }

    public static E2EConfig getDefaultInstance() {
        return InstanceHolder.instance;
    }

    public E2EConfigLoader(String defaultFileName) {
        String fileName = getFileNameFromVmOptions();
        if (fileName == null) {
         fileName = defaultFileName;
        }
        PropertiesConfiguration propCfg = new PropertiesConfiguration();
        try {
            propCfg.load(getClass().getClassLoader().getResourceAsStream(fileName));
        } catch (Exception e) {
            log.error("Failed to load " + fileName, e);
        }
        config = propCfg;
    }

    public E2EConfigLoader() {
        this(DEFAULT_PROPERTIES_FILE_NAME);
    }

    @Override
    public String getWebApplicationBasePort() {
        return config.getString("e2e.testResultsEndpoint").split(":")[1];
    }

    @Override
    public String getWebApplicationBaseUrl() {
        return config.getString("e2e.testResultsEndpoint").split(":")[0];
    }

    @Override
    public String getBasePath() {
        return config.getString("e2e.basePath");
    }

    @Override
    public String getServiceBaseUrl() {
        return config.getString("e2e.redirectorWsBaseUrl");
    }

    @Override
    public String getRedirectorEndpoint() {
        return config.getString("e2e.redirectorEndpoint");
    }

    @Override
    public String getReportBaseUrl() {
        return config.getString("e2e.reportBaseUrl");
    }

    @Override
    public String getZooKeeperBasePath() {
        return config.getString("e2e.zookeeperBasePath");
    }

    @Override
    public String getZooKeeperConnection() {
        return config.getString("e2e.zooKeeperConnection");
    }

    @Override
    public Set<String> getExcludedAppsFromStackAutoDiscovery() {
        String[] excludedAppsForStackAutoDiscoveryArray = config.getStringArray("e2e.excludedAppsForStackAutoDiscovery");
        if (excludedAppsForStackAutoDiscoveryArray.length < 1) {
            excludedAppsForStackAutoDiscoveryArray = EXCLUDED_APPS_FOR_STACK_AUTO_DISCOVERY;
        }
        return Arrays.stream(excludedAppsForStackAutoDiscoveryArray).collect(toSet());
    }

    @Override
    public String getTestCasesRunner() {
        return config.getString("e2e.testcasesRunner");
    }

    @Override
    public List<String> getExtraTestsClasses() {
        String extraTests =  config.getString("e2e.extraTests");
        if (StringUtil.isNotBlank(extraTests)) {
            return Arrays.asList(extraTests.split(EXTRA_TESTS_CLASSES_NAMES_DELIMETER));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Long getDiscoveryPullInterval() {
        return config.getLong("e2e.discoveryPullInterval");
    }

    protected Configuration getConfig() {
        return this.config;
    }

    private String getFileNameFromVmOptions() {
        return System.getProperty(PROPERTIES_FILE_PROP_NAME);
    }
}
