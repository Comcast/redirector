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
 */
package com.comcast.apps.e2e.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class E2EConfigLoaderTest {

    @Test
    public void getBasePathTest() {
        assertEquals("path", E2EConfigLoader.getDefaultInstance().getBasePath());
    }

    @Test
    public void getServiceBaseUrlTest() {
        assertEquals("http://localhost:10540/redirectorWebService/data", E2EConfigLoader.getDefaultInstance().getServiceBaseUrl());
    }

    @Test
    public void getRedirectorEndpointTest() {
        assertEquals("xre://192.168.201.202:8180", E2EConfigLoader.getDefaultInstance().getRedirectorEndpoint());
    }

    @Test
    public void getZookeeperBasePathTest() {
        assertEquals("/endtoend", E2EConfigLoader.getDefaultInstance().getZooKeeperBasePath());
    }

    @Test
    public void getZooKeeperConnectionTest() {
        assertEquals("localhost:2181", E2EConfigLoader.getDefaultInstance().getZooKeeperConnection());
    }

    @Test
    public void getExcludedAppsFromStackAutoDiscoverTest() {
        assertEquals(Collections.singleton("xreGuide"), E2EConfigLoader.getDefaultInstance().getExcludedAppsFromStackAutoDiscovery());
    }

    @Test
    public void getReportBaseUrlTest() {
        assertEquals("http://localhost:10601", E2EConfigLoader.getDefaultInstance().getReportBaseUrl());
    }

    @Test
    public void getTestcasesRunnerTest() {
        assertEquals("HttpTestCasesRunner", E2EConfigLoader.getDefaultInstance().getTestCasesRunner());
    }

    @Test
    public void getWebApplicationBaseUrlTest() {
        assertEquals("localhost", E2EConfigLoader.getDefaultInstance().getWebApplicationBaseUrl());
    }

    @Test
    public void getWebApplicationBasePortTest() {
        assertEquals("8000", E2EConfigLoader.getDefaultInstance().getWebApplicationBasePort());
    }

    @Test
    public void getExtraTestsClassesTest() {
        assertEquals(Arrays.asList("SomeExtraTestClass1", "SomeExtraTestClass2"), E2EConfigLoader.getDefaultInstance().getExtraTestsClasses());
    }

    @Test
    public void getDiscoveryPullIntervalTest(){
        assertEquals("1000", E2EConfigLoader.getDefaultInstance().getDiscoveryPullInterval().toString());
    }
}
