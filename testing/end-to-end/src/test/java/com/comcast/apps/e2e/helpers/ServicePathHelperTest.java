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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.helpers;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServicePathHelperTest {

    private static ServicePathHelper servicePathHelper;

    @BeforeClass
    public static void setUp() {
        servicePathHelper = new ServicePathHelper("testServiceName");
    }

    @Test
    public void getRedirectorConfigServicePathTest() {
        assertEquals("/settings/redirectorConfig", servicePathHelper.getRedirectorConfigServicePath());
    }

    @Test
    public void getWhitelistedServicePathTest() {
        assertEquals("/whitelist/testServiceName", servicePathHelper.getWhitelistedServicePath());
    }

    @Test
    public void getDefaultServerServicePathTest() {
        assertEquals("/servers/testServiceName/default", servicePathHelper.getDefaultServerServicePath());
    }

    @Test
    public void getDistributionServicePathTest() {
        assertEquals("/distributions/testServiceName", servicePathHelper.getDistributionServicePath());
    }

    @Test
    public void getFlavorRulesServicePathTest() {
        assertEquals("/rules/testServiceName/ruleIdTest", servicePathHelper.getFlavorRulesServicePath("ruleIdTest"));
    }

    @Test
    public void getDefaultUrlParamsServicePathTest() {
        assertEquals("/urlRules/testServiceName/urlParams/default", servicePathHelper.getDefaultUrlParamsServicePath());
    }

    @Test
    public void getUrlRulesServicePathTest() {
        assertEquals("/urlRules/testServiceName/urlRuleIdTest", servicePathHelper.getUrlRulesServicePath("urlRuleIdTest"));
    }

    @Test
    public void getPendingChangesServicePathTest() {
        assertEquals("/changes/testServiceName", servicePathHelper.getPendingChangesServicePath());
    }

    @Test
    public void getPendingChangesApproveServicePathTest() {
        assertEquals("/changes/testServiceName/approve/25", servicePathHelper.getPendingChangesApproveServicePath("25"));
    }

    @Test
    public void getPendingChangesApproveRuleServicePathTest() {
        assertEquals("/changes/testServiceName/rule/ruleIdTest/31", servicePathHelper.getPendingChangesApproveRuleServicePath("ruleIdTest", "31"));
    }

    @Test
    public void getPendingChangesApproveUrlRuleServicePathTest() {
        assertEquals("/changes/testServiceName/urlRule/urlRuleIdTest/31", servicePathHelper.getPendingChangesApproveUrlRuleServicePath("urlRuleIdTest", "31"));
    }

    @Test
    public void getPendingChangesApproveDefaultUrlParamsServicePathTest() {
        assertEquals("/urlRules/testServiceName/urlParams/default/31", servicePathHelper.getPendingChangesApproveDefaultUrlParamsServicePath("31"));
    }

    @Test
    public void getPendingChangesApproveWhitelistedServicePathTest() {
        assertEquals("/changes/testServiceName/stackmanagement/31", servicePathHelper.getPendingChangesApproveWhitelistedServicePath("31"));
    }

    @Test
    public void getPendingChangesApproveDefaultServerServicePathTest() {
        assertEquals("/changes/testServiceName/server/31", servicePathHelper.getPendingChangesApproveDefaultServerServicePath("31"));
    }

    @Test
    public void getPendingChangesApproveDistributionServicePathTest() {
        assertEquals("/changes/testServiceName/distribution/31", servicePathHelper.getPendingChangesApproveDistributionServicePath("31"));
    }

    @Test
    public void getNamespacedListServicePathTest() {
        assertEquals("/namespacedLists/addNewNamespaced/namespacedListTest", servicePathHelper.getNamespacedListPostOneNamespacedServicePath("namespacedListTest"));
    }
}
