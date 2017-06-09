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

package com.comcast.redirector.api.redirector.offlineserivces;

import com.comcast.redirector.api.OfflineRestApiFacade;
import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.comcast.redirector.api.NamespaceServiceIntegrationTestUtils.*;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NamespaceOfflineControllerIntegrationTest {

    private OfflineRestApiFacade apiFacade;

    @Before
    public void before() throws Exception {
        setupEnv(getServiceNameForTest());
        apiFacade = new OfflineRestApiFacade((HttpTestServerHelper.BASE_URL));
    }

    @Test
    public void testValidateNamespacedList() throws SerializerException, UnsupportedEncodingException {
        new NamespaceOfflineServiceSteps()
                .createSnapshotListForSaving()
                .postNamespacedList()
                .verifyNamespacedListSavingResponse();
    }

    @Test
    public void testSearchNamespaceValue() {
        new NamespaceOfflineServiceSteps()
                .createSnapshotListForSearching()
                .searchNamespacedListsByNonexistentData()
                .verifySearchResultIsEmpty()
                .searchNamespacedListsByExistingData()
                .verifySearchWasSuccessful();
    }

    @Test
    public void testSearchNamespaceDuplicates() {
        new NamespaceOfflineServiceSteps()
                .createNamespacesWithEmptyNewNamespace()
                .searchNamespaceDuplicates()
                .verifyNoDuplicatesWereFound()
                .createNamespacesWithNewNamespaceWithDuplicatedData()
                .searchNamespaceDuplicates()
                .verifyDuplicatesWereSuccessfullyFound();
    }

    @Test
    public void testDeleteNamespacedList() throws SerializerException {
        new NamespaceOfflineServiceSteps()
                .createSnapshotListForDeleting()
                .deleteNamespacedList()
                .verifyResponseIs200();
    }

    @Test
    public void testDeleteValuesFromNamespacedList() throws SerializerException {
        new NamespaceOfflineServiceSteps()
                .createSnapshotListForNamespacedListValuesDeleting()
                .deleteNamespacedListValues()
                .verifyValuesDeletionWasSuccessful();
    }

    @Test
    public void testDeleteValuesFromMultipleNamespacedLists() throws SerializerException {
        new NamespaceOfflineServiceSteps()
                .createListOfValuesToDeleteFromMultipleNamespacedLists()
                .deleteValuesFromMultipleNamespacedLists()
                .verifyValuesDeletionFromMultipleListsWasSuccessful();
    }

    private class NamespaceOfflineServiceSteps {
        String LIST_NAME_1 = "namespacedListTest1";
        String LIST_NAME_2 = "namespacedListTest2";
        SnapshotList snapshotList;
        NamespacedList savingRuleResponse;
        NamespacedListSearchResult searchingRuleResponse;
        NamespaceDuplicates searchingDuplicatesResponse;
        Namespaces namespaces;
        Response rawResponse;
        OperationResult operationResult;
        List<NamespacedValuesToDeleteByName> namespacedValuesToDeleteByNames;
        List<NamespacedList> multipleDeletionResult;

        NamespaceOfflineServiceSteps createSnapshotListForSaving() {
            NamespacedList listToSave = getNamespacedList();

            SnapshotList result = new SnapshotList();
            result.setNamespaces(createNamespaces());
            result.setEntityToSave(listToSave);
            snapshotList = result;

            return this;
        }

        NamespaceOfflineServiceSteps createSnapshotListForSearching() {
            Namespaces namespaces = new Namespaces();
            namespaces.setNamespaces((Collections.singletonList(createNamespacedList(String.valueOf(2)))));
            SnapshotList result = new SnapshotList();
            result.setNamespaces(namespaces);
            snapshotList = result;

            return this;
        }

        NamespaceOfflineServiceSteps createSnapshotListForDeleting() {
            Namespaces namespaces = createNamespaces();
            namespaces.setNewNamespace(namespaces.getNamespaces().get(0));
            SnapshotList result = new SnapshotList();
            result.setNamespaces(namespaces);
            snapshotList = result;

            return this;
        }

        NamespaceOfflineServiceSteps createSnapshotListForNamespacedListValuesDeleting() {
            SnapshotList result = new SnapshotList();
            result.setNamespaces(createNamespaces());
            NamespacedEntities entries = new NamespacedEntities();
            Set<String> entriesSet = new LinkedHashSet<>();
            entries.setEntities(entriesSet);
            entries.getEntities().add("value1_1");
            entries.getEntities().add("0value1_1");
            entries.getEntities().add("00value1_1");
            entries.getEntities().add("000value1_1");
            result.setEntityToSave(entries);
            snapshotList = result;

            return this;
        }

        NamespaceOfflineServiceSteps createListOfValuesToDeleteFromMultipleNamespacedLists() {
            NamespacedValuesToDeleteByName namespacedValuesToDeleteByName1 = new NamespacedValuesToDeleteByName();
            NamespacedList listTest1 = getNamespacedListByName(createNamespaces(), LIST_NAME_1);
            namespacedValuesToDeleteByName1.setName(LIST_NAME_1);
            namespacedValuesToDeleteByName1.setValuesToDelete(Collections.singletonList("value1_1"));
            namespacedValuesToDeleteByName1.setCurrentNamespacedList(listTest1);

            NamespacedValuesToDeleteByName namespacedValuesToDeleteByName2 = new NamespacedValuesToDeleteByName();
            NamespacedList listTest2 = getNamespacedListByName(createNamespaces(), LIST_NAME_2);
            namespacedValuesToDeleteByName2.setName(LIST_NAME_2);
            namespacedValuesToDeleteByName2.setValuesToDelete(Collections.singletonList("value3_2"));
            namespacedValuesToDeleteByName2.setCurrentNamespacedList(listTest2);

            List<NamespacedValuesToDeleteByName> result = new ArrayList<>();
            result.add(namespacedValuesToDeleteByName1);
            result.add(namespacedValuesToDeleteByName2);

            namespacedValuesToDeleteByNames = result;

            return this;
        }

        NamespaceOfflineServiceSteps postNamespacedList() {
            savingRuleResponse = apiFacade.postNamespacedListOffline(snapshotList);
            return this;
        }

        NamespaceOfflineServiceSteps deleteNamespacedList() {
            rawResponse = apiFacade.deleteNamespacedList(snapshotList, LIST_NAME_1);
            return this;
        }

        NamespaceOfflineServiceSteps deleteNamespacedListValues() {
            operationResult = apiFacade.deleteNamespacedListValues(snapshotList, LIST_NAME_1);
            return this;
        }

        NamespaceOfflineServiceSteps deleteValuesFromMultipleNamespacedLists() {
            multipleDeletionResult = apiFacade.deleteValuesFromMultipleNamespacedLists(namespacedValuesToDeleteByNames).getNamespaces();
            return this;
        }

        NamespaceOfflineServiceSteps createNamespacesWithEmptyNewNamespace() {
            Namespaces result = createNamespaces();
            result.setNewNamespace(new NamespacedList());
            namespaces = result;

            return this;
        }

        NamespaceOfflineServiceSteps createNamespacesWithNewNamespaceWithDuplicatedData() {
            Namespaces result = createNamespaces();
            NamespacedList namespacedListWithDuplicatedValue = new NamespacedList();
            namespacedListWithDuplicatedValue.setName("listWithDuplicate");
            namespacedListWithDuplicatedValue.setDescription("Description");
            namespacedListWithDuplicatedValue.setValueSet(new HashSet<>(Arrays.asList(
                    new NamespacedListValueForWS("value1_1"),
                    new NamespacedListValueForWS("value2_2"),
                    new NamespacedListValueForWS("value5_2"))));
            result.setNewNamespace(namespacedListWithDuplicatedValue);
            namespaces = result;

            return this;
        }

        NamespaceOfflineServiceSteps searchNamespacedListsByNonexistentData() {
            searchingRuleResponse = apiFacade.searchForNamespacedListOffline(snapshotList, "value1_1");
            return this;
        }

        NamespaceOfflineServiceSteps searchNamespacedListsByExistingData() {
            searchingRuleResponse = apiFacade.searchForNamespacedListOffline(snapshotList, "value1_2");
            return this;
        }

        NamespaceOfflineServiceSteps searchNamespaceDuplicates() {
            searchingDuplicatesResponse = apiFacade.searchForNamespaceDuplicates(namespaces);
            return this;
        }

        NamespaceOfflineServiceSteps verifySearchResultIsEmpty() {
            Assert.assertEquals("value1_1", searchingRuleResponse.getSearchItem());
            Assert.assertEquals(0, searchingRuleResponse.getNamespacedLists().size());
            return this;
        }

        NamespaceOfflineServiceSteps verifySearchWasSuccessful() {
            Assert.assertEquals("value1_2", searchingRuleResponse.getSearchItem());
            Assert.assertEquals(1, searchingRuleResponse.getNamespacedLists().size());
            return this;
        }

        NamespaceOfflineServiceSteps verifyNoDuplicatesWereFound() {
            assertTrue(searchingDuplicatesResponse.isEmpty());
            return this;
        }

        NamespaceOfflineServiceSteps verifyDuplicatesWereSuccessfullyFound() {
            Assert.assertEquals(2, searchingDuplicatesResponse.getNamespaceDuplicatesMap().size());
            Assert.assertEquals(LIST_NAME_1, searchingDuplicatesResponse.get("value1_1"));
            Assert.assertEquals(LIST_NAME_2, searchingDuplicatesResponse.get("value2_2"));
            return this;
        }

        NamespaceOfflineServiceSteps verifyNamespacedListSavingResponse() {
            NamespacedList savedList = getNamespacedList();
            Assert.assertEquals(savedList.getName(), savingRuleResponse.getName());
            Assert.assertEquals(savedList.getDescription(), savingRuleResponse.getDescription());
            Assert.assertEquals(savedList.getValueSet(), savingRuleResponse.getValueSet());
            Assert.assertNotEquals(savedList.getVersion(), savingRuleResponse.getVersion());

            return this;
        }

        NamespaceOfflineServiceSteps verifyResponseIs200() {
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            return this;
        }

        NamespaceOfflineServiceSteps verifyValuesDeletionWasSuccessful() {
            NamespacedEntities entities = (NamespacedEntities) operationResult.getMethodResponce();
            Assert.assertEquals(entities.getEntities().size(), 3);
            return this;
        }

        NamespaceOfflineServiceSteps verifyValuesDeletionFromMultipleListsWasSuccessful() {
            Assert.assertEquals(2, multipleDeletionResult.size());

            Assert.assertTrue(multipleDeletionResult.get(1).getValues().contains("value1_2"));
            Assert.assertTrue(multipleDeletionResult.get(1).getValues().contains("value2_2"));
            Assert.assertFalse(multipleDeletionResult.get(1).getValues().contains("value3_2"));

            Assert.assertTrue(multipleDeletionResult.get(0).getValues().contains("value3_1"));
            Assert.assertTrue(multipleDeletionResult.get(0).getValues().contains("value2_1"));
            Assert.assertFalse(multipleDeletionResult.get(0).getValues().contains("value1_1"));

            return this;
        }

        private NamespacedList getNamespacedList() {
            return createNamespacedList(String.valueOf(3));
        }

        private NamespacedList getNamespacedListByName(Namespaces namespaces, String name) {
            for (NamespacedList namespacedList: namespaces.getNamespaces()) {
                if (name.equals(namespacedList.getName())) {
                    return namespacedList;
                }
            }
            return null;
        }
    }
}
