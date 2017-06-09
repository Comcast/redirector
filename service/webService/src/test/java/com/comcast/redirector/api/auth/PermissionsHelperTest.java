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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.auth;

import com.comcast.redirector.common.RedirectorConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PermissionsHelperTest {

    private static final String WRITE_SERVICENAME = "write-serviceName";
    private static final String WRITE_ALL = "write-*";
    private static final String READ_WHITELIST = "read-whitelist";
    private static final String WRITE_WHITELIST = "write-whitelist";
    private static final String READ_SERVICENAME = "read-serviceName";
    private static final String READ_ALL = "read-*";
    private static final String RESTONLY_WRITE_SERVICENAME = "RESTOnly-write-serviceRestName";
    private static final String RESTONLY_READ_SERVICENAME = "RESTOnly-read-serviceRestName";
    private static final String ACCESS_APP_XREGUIDE = "redirector-accessApp-xreGuide";

    private static final String ROOT_PATH = "somePath";
    private static final String DATA_ENDPOINT = "data";

    private Set<String> currentPermissions = new LinkedHashSet<>();

    @Before
    public void setUp () {
        fillPermissionsSet();
    }

    private void fillPermissionsSet() {
        currentPermissions.add(WRITE_SERVICENAME);
        currentPermissions.add(READ_SERVICENAME);
        currentPermissions.add(RESTONLY_WRITE_SERVICENAME);
        currentPermissions.add(RESTONLY_READ_SERVICENAME);
        currentPermissions.add(ACCESS_APP_XREGUIDE);
    }

    /**
     * We simplified the granular permission check to  (is not present) and (is allowed)
     * If we will have another element like "error" or "conflict" we need to revisit {@link PermissionHelper}
     * and ensure that it works correctly.
     */
    @Test
    public void granularPermissionResultLength_is3 () {
        assertTrue(PermissionHelper.GranularPermissionResult.values().length == 3);
    }

    @Test
    public void granularWritePermission_IsGranted_When_noOrdinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-allow-write-namespacedLists-ns1");

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void readPermission_IsGranted_OnGetAllNamespacedLists_When_noOrdinaryPermissionsArePresent () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void readPermission_IsGranted_OnPostToDuplicates_NamespacedLists_When_noOrdinaryPermissionsArePresent () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/duplicates", HttpMethod.POST, currentPermissions));
    }


    @Test
    public void readPermission_IsGranted_OnPostToOfflineModePreviewDistribution_When_noOrdinaryPermissionsArePresent () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "changesOffline/xreGuide/preview/distribution/", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void granularWritePermission_IsRejected_When_noOrdinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-deny-write-namespacedLists-ns1");

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void granularWritePermission_IsGranted_When_ordinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-allow-write-namespacedLists-ns1");
        currentPermissions.add("RESTOnly-write-namespacedLists");

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void granularWritePermission_IsGranted_When_WriteAllPermissionsArePresent () {
        currentPermissions.add("RESTOnly-allow-write-namespacedLists-ns1");
        currentPermissions.add(WRITE_ALL);

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void granularWritePermission_IsRejected_When_ordinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-deny-write-namespacedLists-ns1");
        currentPermissions.add("RESTOnly-write-namespacedLists");

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void granularWritePermission_IsRejected_When_WriteAllPermissionsArePresent () {
        currentPermissions.add("RESTOnly-deny-write-namespacedLists-ns1");
        currentPermissions.add(WRITE_ALL);

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void granularReadPermission_IsGranted_When_noOrdinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-allow-read-namespacedLists-ns1");

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void granularReadPermission_IsRejected_When_noOrdinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-deny-read-namespacedLists-ns1");

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void granularReadPermission_IsGranted_When_ordinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-allow-read-namespacedLists-ns1");
        currentPermissions.add("RESTOnly-read-namespacedLists");

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void granularReadPermissionRejects_When_GranularDeny_And_OrdinaryPermissionsArePresent () {
        currentPermissions.add("RESTOnly-deny-read-namespacedLists-ns2");
        currentPermissions.add("RESTOnly-deny-write-namespacedLists-ns1");
        currentPermissions.add("RESTOnly-deny-read-namespacedLists-ns1");
        currentPermissions.add("RESTOnly-read-namespacedLists");

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "namespacedLists/ns1", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryWritePermission_IsGranted () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void ordinaryWritePermission_IsRejected () {
        currentPermissions.remove(WRITE_SERVICENAME);

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted_WhenPermission_IsReadAll () {
        currentPermissions.clear();
        currentPermissions.add(READ_ALL);
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryWritePermission_IsNotGranted_WhenPermission_IsReadAll_AndMethodPost () {
        currentPermissions.clear();
        currentPermissions.add(READ_ALL);
        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void ordinaryWritePermission_IsNotGranted_WhenPermission_IsReadAll_AndMethodDelete () {
        currentPermissions.clear();
        currentPermissions.add(READ_ALL);
        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.DELETE, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsRejected () {
        currentPermissions.remove(READ_SERVICENAME);

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void restOnlyWritePermission_IsGranted () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceRestName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void ordinaryWritePermission_IsGranted_WhenPermission_IsWriteAll () {
        currentPermissions.clear();
        currentPermissions.add(WRITE_ALL);

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void stackCommentsReadPermission_IsGranted_WhenPermission_IsReadStacks() {
        currentPermissions.clear();
        currentPermissions.add(READ_WHITELIST);

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "stackComments", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void stackCommentsWritePermission_IsGranted_WhenPermission_IsWriteStacks() {
        currentPermissions.clear();
        currentPermissions.add(WRITE_WHITELIST);

        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "stackComments", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void restOnlyWritePermission_IsRejected () {
        currentPermissions.remove(RESTONLY_WRITE_SERVICENAME);

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceRestName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void restOnlyReadPermission_IsGranted () {
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceRestName", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void restOnlyReadPermission_IsRejected () {
        currentPermissions.remove(RESTONLY_READ_SERVICENAME);

        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceRestName", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void unparseablePermission_IsNotCausingExceptions_whileDoingGet() {
        currentPermissions.add("someUparseablePermisssion");
        currentPermissions.add("RESTOnly-someUparseablePermisssion2");

        assertNotNull(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceRestName", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void unparseablePermission_IsNotCausingExceptions_whileDoingPost() {
        currentPermissions.add("someUparseablePermisssion");
        currentPermissions.add("RESTOnly-someUparseablePermisssion2");

        assertNotNull(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.DELIMETER + "serviceRestName", HttpMethod.POST, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted_WithAccessAppPermission_WithLeadingAppName () {
        currentPermissions.add("read-changes");
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.PENDING_CONTROLLER_PATH + "/xreGuide", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsRejected_WithOutAccessAppPermission_WithLeadingAppName () {
        currentPermissions.add("read-changes");
        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.PENDING_CONTROLLER_PATH + "/xreApp", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted_WithAccessAppPermission_WithTrailingAppName () {
        currentPermissions.add("read-endToEnd");
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.END_TO_END_PATH +"/someUrl/xreGuide", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsRejected_WithOutAccessAppPermission_WithTrailingAppName () {
        currentPermissions.add("read-endToEnd");
        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.END_TO_END_PATH +"/someUrl/xreApp", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted_WithAccessAppPermission_WithInnerAppName () {
        currentPermissions.add("read-traffic");
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.TRAFFIC_PATH +"/someUrl/xreGuide/someOtherUrl", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsRejected_WithOutAccessAppPermission_WithInnerAppName () {
        currentPermissions.add("read-traffic");
        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.TRAFFIC_PATH +"/someUrl/xreApp/someOtherUrl", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted_WithAccessAppPermission_WithLeadingAppName_WithRunAutoPrefix () {
        currentPermissions.add("read-testSuite");
        assertTrue(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH + "/runAuto/xreGuide", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsRejected_WithOutAccessAppPermission_WithLeadingAppName_WithRunAutoPrefix () {
        currentPermissions.add("read-testSuite");
        assertFalse(PermissionHelper.isAuthorized(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.REDIRECTOR_TEST_SUITE_PATH + "/runAuto/xreApp", HttpMethod.GET, currentPermissions));
    }

    @Test
    public void ordinaryReadPermission_IsGranted_WithOutAccessAppPermission_WithFalseCheckAccessApp () {
        currentPermissions.add("read-traffic");
        assertTrue(PermissionHelper.isAuthorizedDisregardingAccessAppPermissions(ROOT_PATH + RedirectorConstants.DELIMETER +
                DATA_ENDPOINT + RedirectorConstants.TRAFFIC_PATH +"/someUrl/xreApp/someOtherUrl", HttpMethod.GET, currentPermissions));
    }


    @After
    public void tearDown() {
        currentPermissions.clear();
    }
}
