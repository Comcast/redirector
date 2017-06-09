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

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NamespacedPermissionsPostProcessTest {
    private static final String WRITE_NAMESPACED = "write-namespacedLists";
    private static final String READ_NAMESPACED = "read-namespacedLists";
    private static final String RESTONLY_DENY = "RESTOnly-deny";
    private static final String RESTONLY_ALLOW = "RESTOnly-allow";
    private static final String PERMISSION_DELIMITER = "-";


    private NamespacedListsPermissionPostProcessService namespacedListsPermissionPostProcessService;
    private MockPermissionProviderWithChangeablePermissions permissionProvider;
    private List<NamespacedList> namespacedLists = new ArrayList<>(10);
    private NamespacedListSearchResult namespacedListSearchResult;

    @Before
    public void setUp() {
        namespacedLists = createNamespacedLists();
        namespacedListSearchResult = createNamespacedListSearchResult();

        permissionProvider = new MockPermissionProviderWithChangeablePermissions();
        LinkedList<String> permissions = new LinkedList<>();

        permissions.add(RESTONLY_DENY + PERMISSION_DELIMITER + WRITE_NAMESPACED + PERMISSION_DELIMITER + "ns1");
        permissions.add(RESTONLY_DENY + PERMISSION_DELIMITER + READ_NAMESPACED + PERMISSION_DELIMITER + "ns2");
        permissions.add(RESTONLY_ALLOW + PERMISSION_DELIMITER + WRITE_NAMESPACED + PERMISSION_DELIMITER + "ns3");
        permissions.add(RESTONLY_ALLOW + PERMISSION_DELIMITER + READ_NAMESPACED + PERMISSION_DELIMITER + "ns4");

        permissionProvider.setPermissions(permissions);

        namespacedListsPermissionPostProcessService = new NamespacedListsPermissionPostProcessService();
        Whitebox.setInternalState(namespacedListsPermissionPostProcessService, "permissionProvider", permissionProvider);
    }

    @Test
    public void isAuthorized_toReadNamespaced_withGranularAllow_withoutGeneralPermission() {
        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns4"));
    }

    @Test
    public void isAuthorized_toReadNamespaced_withGranularAllow_withGeneralPermission() {
        permissionProvider.getPermissions().add(READ_NAMESPACED);

        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns4"));
    }

    @Test
    public void isAuthorized_toReadNamespaced_withoutGranularAllow_withGeneralPermission() {
        permissionProvider.getPermissions().clear();
        permissionProvider.getPermissions().add(READ_NAMESPACED);

        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns4"));
    }

    @Test
    public void isNotAuthorized_toReadNamespaced_withGranularDeny_withoutGeneralPermission() {
        Assert.assertFalse(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns2"));
    }

    @Test
    public void isNotAuthorized_toReadNamespaced_withGranularDeny_withGeneralPermission() {
        permissionProvider.getPermissions().add(READ_NAMESPACED);

        Assert.assertFalse(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns2"));
    }

    @Test
    public void isAuthorized_toReadNamespaced_withoutGranularDeny_withGeneralPermission() {
        permissionProvider.getPermissions().clear();
        permissionProvider.getPermissions().add(READ_NAMESPACED);

        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns2"));
    }

    @Test
    public void isAuthorized_toWriteNamespaced_withGranularAllow_withoutGeneralPermission() {
        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns3"));
    }

    @Test
    public void isAuthorized_toWriteNamespaced_withGranularAllow_withGeneralPermission() {
        permissionProvider.getPermissions().add(WRITE_NAMESPACED);

        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns3"));
    }

    @Test
    public void isAuthorized_toWriteNamespaced_withoutGranularAllow_withGeneralPermission() {
        permissionProvider.getPermissions().clear();
        permissionProvider.getPermissions().add(WRITE_NAMESPACED);

        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns3"));
    }

    @Test
    public void isNotAuthorized_toWriteNamespaced_withGranularDeny_withoutGeneralPermission() {
        Assert.assertFalse(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns1"));
    }

    @Test
    public void isNotAuthorized_toWriteNamespaced_withGranularDeny_withGeneralPermission() {
        permissionProvider.getPermissions().add(WRITE_NAMESPACED);

        Assert.assertFalse(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns1"));
    }

    @Test
    public void isAuthorized_toWriteNamespaced_withoutGranularDeny_withGeneralPermission() {
        permissionProvider.getPermissions().clear();
        permissionProvider.getPermissions().add(WRITE_NAMESPACED);

        Assert.assertTrue(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns1"));
    }

    @Test
    public void isNotAuthorized_toWriteNamespaced_withoutPermissions() {
        permissionProvider.getPermissions().clear();

        Assert.assertFalse(namespacedListsPermissionPostProcessService.isAuthorizedToWriteList("ns1"));
    }

    @Test
    public void isNotAuthorized_toReadNamespaced_withoutPermissions() {
        permissionProvider.getPermissions().clear();

        Assert.assertFalse(namespacedListsPermissionPostProcessService.isAuthorizedToReadList("ns1"));
    }

    @Test
    public void onlyOneNSList_whichIsDeniedGranularly_isFiltered_withGeneralPermissions() {
        permissionProvider.getPermissions().add(READ_NAMESPACED);

        List<NamespacedList> filteredLists = namespacedListsPermissionPostProcessService.
                removeListsWithNoReadPermissionsFromNamespacedLists(namespacedLists);

        Assert.assertTrue(filteredLists.size() == 3);
    }

    @Test
    public void onlyOneNSList_whichIsAllowedGranularly_isLeftAfterFiltering_withoutGeneralPermissions() {
        List<NamespacedList> filteredLists = namespacedListsPermissionPostProcessService.
                removeListsWithNoReadPermissionsFromNamespacedLists(namespacedLists);

        Assert.assertTrue(filteredLists.size() == 1);
    }

    @Test
    public void noNSLists_areLeftAfterFiltering_withoutGeneralPermissions_withoutGranularAllow() {
        permissionProvider.getPermissions().clear();

        List<NamespacedList> filteredLists = namespacedListsPermissionPostProcessService.
                removeListsWithNoReadPermissionsFromNamespacedLists(namespacedLists);

        Assert.assertTrue(filteredLists.size() == 0);
    }

    @Test
    public void onlyOneNSListSearchResult_whichIsDeniedGranularly_isFiltered_withGeneralPermissions() {
        permissionProvider.getPermissions().add(READ_NAMESPACED);

        NamespacedListSearchResult filteredSearchResult = namespacedListsPermissionPostProcessService.
                removeListsWithNoReadPermissionsFromSearchResult(namespacedListSearchResult);

        Assert.assertTrue(filteredSearchResult.getNamespacedLists().size() == 3);
    }

    @Test
    public void onlyOneNSListSearchResult_whichIsAllowedGranularly_isLeftAfterFiltering_withoutGeneralPermissions() {
        NamespacedListSearchResult filteredSearchResult = namespacedListsPermissionPostProcessService.
                removeListsWithNoReadPermissionsFromSearchResult(namespacedListSearchResult);

        Assert.assertTrue(filteredSearchResult.getNamespacedLists().size() == 1);
    }

    @Test
    public void noNSListsSearchResult_areLeftAfterFiltering_withoutGeneralPermissions_withoutGranularAllow() {
        permissionProvider.getPermissions().clear();

        NamespacedListSearchResult filteredSearchResult = namespacedListsPermissionPostProcessService.
                removeListsWithNoReadPermissionsFromSearchResult(namespacedListSearchResult);

        Assert.assertTrue(filteredSearchResult.getNamespacedLists().size() == 0);
    }

    private List<NamespacedList> createNamespacedLists () {
        List<NamespacedList> namespacedLists = new ArrayList<>(6);

        NamespacedList ns1 = new NamespacedList();
        NamespacedList ns2 = new NamespacedList();
        NamespacedList ns3 = new NamespacedList();
        NamespacedList ns4 = new NamespacedList();
        ns1.setName("ns1");
        ns2.setName("ns2");
        ns3.setName("ns3");
        ns4.setName("ns4");
        namespacedLists.add(ns1);
        namespacedLists.add(ns2);
        namespacedLists.add(ns3);
        namespacedLists.add(ns4);
        return namespacedLists;
    }

    private NamespacedListSearchResult createNamespacedListSearchResult () {
        List<NamespacedListEntity> namespacedLists = new ArrayList<>(6);
        NamespacedListSearchResult result = new NamespacedListSearchResult();

        NamespacedListEntity ns1 = new NamespacedListEntity();
        NamespacedListEntity ns2 = new NamespacedListEntity();
        NamespacedListEntity ns3 = new NamespacedListEntity();
        NamespacedListEntity ns4 = new NamespacedListEntity();
        ns1.setName("ns1");
        ns2.setName("ns2");
        ns3.setName("ns3");
        ns4.setName("ns4");
        namespacedLists.add(ns1);
        namespacedLists.add(ns2);
        namespacedLists.add(ns3);
        namespacedLists.add(ns4);

        result.setNamespacedLists(namespacedLists);
        return result;
    }
}
