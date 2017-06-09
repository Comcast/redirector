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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.NamespacedListsHelper;
import com.comcast.redirector.common.serializers.SerializerException;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.*;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static com.comcast.redirector.api.redirector.helpers.NamespacedListsHelper.cleanUpNamespaceData;
import static org.junit.Assert.*;

public class NamespaceControllerIntegrationTest {

    private RestApiFacade apiFacade;

    @Before
    public void before() throws Exception {
        apiFacade = new RestApiFacade((HttpTestServerHelper.BASE_URL));
        setupEnv(getServiceNameForTest());
        namespaces = NamespacedListsHelper.createNamespaces(NamespacedListType.TEXT);
    }

    private Namespaces namespaces;

    @After
    public void cleanUp() throws Exception {
        cleanUpNamespaceData();
    }

    @Test
    public void Namespaces_areSaved_afterPost() throws SerializerException {
        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListIsPresent (namespaces.getNamespaces().get(0)).
                verifyListIsPresent (namespaces.getNamespaces().get(1));
    }

    @Test
    public void EncodedTypedNamespaces_areSaved_afterPost() throws SerializerException {
        NamespacedList encodedNamespacedList = new NamespacedList();
        encodedNamespacedList.setType(NamespacedListType.ENCODED);
        encodedNamespacedList.setName("encodedNamespacedList");
        encodedNamespacedList.setValueSet(new HashSet<>());
        NamespacedListValueForWS value1 = new NamespacedListValueForWS();
        value1.setValue("value1");
        NamespacedListValueForWS value2 = new NamespacedListValueForWS();
        value2.setValue("value2");
        encodedNamespacedList.getValueSet().add(value1);
        encodedNamespacedList.getValueSet().add(value2);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                postOneNamespacedList(encodedNamespacedList).
                getAllNamespacedLists().
                verifyListIsPresent (encodedNamespacedList);
    }

    @Test
    public void IPTypedNamespaces_WithValidValues_areSaved_afterPost() throws SerializerException {
        NamespacedList encodedNamespacedList = new NamespacedList();
        encodedNamespacedList.setType(NamespacedListType.IP);
        encodedNamespacedList.setName("ipNamespacedList");
        encodedNamespacedList.setValueSet(new HashSet<>());
        NamespacedListValueForWS value1 = new NamespacedListValueForWS();
        value1.setValue("2001:db8::/64");
        NamespacedListValueForWS value2 = new NamespacedListValueForWS();
        value2.setValue("192.168.0.0/32");
        NamespacedListValueForWS value3 = new NamespacedListValueForWS();
        value3.setValue("192.168.0.0");
        NamespacedListValueForWS value4 = new NamespacedListValueForWS();
        value4.setValue("2001:db8::");
        encodedNamespacedList.getValueSet().add(value1);
        encodedNamespacedList.getValueSet().add(value2);
        encodedNamespacedList.getValueSet().add(value3);
        encodedNamespacedList.getValueSet().add(value4);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                postOneNamespacedList(encodedNamespacedList).
                getAllNamespacedLists().
                verifyListIsPresent (encodedNamespacedList);
    }

    @Test (expected = WebApplicationException.class)
    public void IPTypedNamespaces_WithInValidValues_areNotSaved_afterPost() throws SerializerException {
        NamespacedList encodedNamespacedList = new NamespacedList();
        encodedNamespacedList.setType(NamespacedListType.IP);
        encodedNamespacedList.setName("ipNamespacedList");
        encodedNamespacedList.setValueSet(new HashSet<>());
        NamespacedListValueForWS value1 = new NamespacedListValueForWS();
        value1.setValue("2001:db8::/129");
        NamespacedListValueForWS value2 = new NamespacedListValueForWS();
        value2.setValue("192.168.0.0/33");
        NamespacedListValueForWS value3 = new NamespacedListValueForWS();
        value3.setValue("192.168.0.0");
        NamespacedListValueForWS value4 = new NamespacedListValueForWS();
        value4.setValue("2001:db8::");
        encodedNamespacedList.getValueSet().add(value1);
        encodedNamespacedList.getValueSet().add(value2);
        encodedNamespacedList.getValueSet().add(value3);
        encodedNamespacedList.getValueSet().add(value4);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                postOneNamespacedList(encodedNamespacedList).
                getAllNamespacedLists().
                verifyListIsNotPresent(encodedNamespacedList);
    }

    @Test
    public void NoNamespaces_areSaved_whenNoPostIsPerformed() throws SerializerException {
        new NamespacedListServiceSteps(getServiceNameForTest()).
                getAllNamespacedLists().
                verifyListIsNotPresent(namespaces.getNamespaces().get(0)).
                verifyListIsNotPresent(namespaces.getNamespaces().get(1));
    }

    @Test
    public void OneNamespace_isReturned_AfterPost() throws SerializerException {
        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getOneNamespacedList(namespaces.getNamespaces().get(0).getName()).
                verifyListIsPresent (namespaces.getNamespaces().get(0)).
                getOneNamespacedList(namespaces.getNamespaces().get(1).getName()).
                verifyListIsPresent (namespaces.getNamespaces().get(1));
    }

    @Test
    public void Namespace_isNotReturned_WhenNoNamespacesArePresent() throws SerializerException {
        new NamespacedListServiceSteps(getServiceNameForTest()).
                getOneNamespacedList(namespaces.getNamespaces().get(0).getName()).
                verifyListIsNotPresent(namespaces.getNamespaces().get(0)).
                getOneNamespacedList(namespaces.getNamespaces().get(1).getName()).
                verifyListIsNotPresent(namespaces.getNamespaces().get(1));
    }


    @Test
    public void Namespace_isFound_WhenItsValueIsSearched() throws SerializerException {
        NamespacedList expectedNamespacedList = getNamespacedListByName(namespaces, "namespacedListTest2");
        String valueForSearch = "value1_2";

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                searchInNamespacedLists(valueForSearch).
                verifySearchResultContainsNamespacedList(expectedNamespacedList);
    }


    @Test
    public void EncodedTypedNamespace_isFound_WhenItsValueIsSearched() throws SerializerException {
        NamespacedList encodedNamespacedList = new NamespacedList();
        encodedNamespacedList.setType(NamespacedListType.ENCODED);
        encodedNamespacedList.setName("encodedNamespacedList");
        encodedNamespacedList.setValueSet(new HashSet<>());
        NamespacedListValueForWS value1 = new NamespacedListValueForWS();
        value1.setValue("value1");
        NamespacedListValueForWS value2 = new NamespacedListValueForWS();
        value2.setValue("value2");
        encodedNamespacedList.getValueSet().add(value1);
        encodedNamespacedList.getValueSet().add(value2);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                postOneNamespacedList(encodedNamespacedList).
                searchInNamespacedLists("value1").
                verifySearchResultContainsNamespacedList(encodedNamespacedList);
    }

    @Test
    public void Namespace_isNotFound_WhenNotPresentValueIsSearched() throws SerializerException {
        NamespacedList expectedNamespacedList = getNamespacedListByName(namespaces, "namespacedListTest2");
        String valueForSearch = "value1_2dummydummy";

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                searchInNamespacedLists(valueForSearch).
                verifySearchResultDoesNotContainNamespacedList(expectedNamespacedList);
    }

    @Test
    public void Namespace_isFound_WhenItsNameIsSearched() throws SerializerException {
        NamespacedList expectedNamespacedList = getNamespacedListByName(namespaces, "namespacedListTest2");
        String valueForSearch = "namespacedListTest2";

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                searchInNamespacedLists(valueForSearch).
                verifySearchResultContainsNamespacedList(expectedNamespacedList);
    }

    @Test
    public void Duplicates_areFound_WhenTheyShouldBe() throws SerializerException {
        NamespacedList namespacedListWithDuplicatedValue = new NamespacedList();
        namespacedListWithDuplicatedValue.setName("listWithDuplicate");
        namespacedListWithDuplicatedValue.setDescription("Description");
        namespacedListWithDuplicatedValue.setValueSet(new HashSet<NamespacedListValueForWS>(Arrays.asList(
                new NamespacedListValueForWS("value1_1"), // this is a duplicated value that is contained in namespacedListTest1
                new NamespacedListValueForWS("value2_2"), // this is a duplicated value that is contained in namespacedListTest2
                new NamespacedListValueForWS("value5_2"))));

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                searchDuplicates(namespacedListWithDuplicatedValue).
                verifyDuplicatesMapIsNotEmpty();
    }

    @Test
    public void Duplicates_areNotFound_WhenEmptyList() throws SerializerException {
        NamespacedList namespacedListWithDuplicatedValue = new NamespacedList();
        namespacedListWithDuplicatedValue.setName("listWithOutDuplicate");
        namespacedListWithDuplicatedValue.setDescription("Description");

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                searchDuplicates(namespacedListWithDuplicatedValue).
                verifyDuplicatesMapIsEmpty();
    }

    @Test
    public void Duplicates_areNotFound_WhenNonEmptyListWithoutDuplicates() throws SerializerException {
        NamespacedList namespacedListWithDuplicatedValue = new NamespacedList();
        namespacedListWithDuplicatedValue.setName("listWithOutDuplicate");
        namespacedListWithDuplicatedValue.setDescription("Description");
        namespacedListWithDuplicatedValue.setValueSet(new HashSet<NamespacedListValueForWS>(Arrays.asList(
                new NamespacedListValueForWS("value33_2"),
                new NamespacedListValueForWS("value5_2"))));

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                searchDuplicates(namespacedListWithDuplicatedValue).
                verifyDuplicatesMapIsEmpty();
    }

    @Test
    public void OneNamespaceIsSaved_AfterItIsPosted() throws SerializerException {
        NamespacedList list = namespaces.getNamespaces().get(0);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                getAllNamespacedLists().
                verifyListIsNotPresent(list).
                postOneNamespacedList(list).
                getAllNamespacedLists().
                verifyListIsPresent(list);
    }

    @Test
    public void OneNamespaceIsDeleted_WhenItWasPresent() throws SerializerException {
        NamespacedList list = namespaces.getNamespaces().get(0);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListIsPresent(list).
                deleteOneNamespacedList(list).
                getAllNamespacedLists().
                verifyListIsNotPresent(list);
    }

    @Test
    public void OneNamespaceDeleteSuccessful_WhenItWasNotPresent() throws SerializerException {
        NamespacedList list = namespaces.getNamespaces().get(0);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                getAllNamespacedLists().
                verifyListIsNotPresent(list).
                deleteOneNamespacedList(list).
                getAllNamespacedLists().
                verifyListIsNotPresent(list);
    }

    @Test
    public void OneNamespaceValue_IsSuccesfullyDeleted_WithAnOldAPI() throws SerializerException {
        NamespacedList list = namespaces.getNamespaces().get(0);
        NamespacedListValueForWS valueToDelete = new NamespacedListValueForWS("value2_1");

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListHasValue(list.getName(), valueToDelete).
                deleteNamespacedListValues(list, valueToDelete.getValue()).
                getAllNamespacedLists().
                verifyListDoesNotHaveValue(list.getName(), valueToDelete);
    }

    @Test
    public void MultipleNamespaceValues_AreSuccesfullyDeleted_WithAnOldAPI() throws SerializerException {
        NamespacedList list = namespaces.getNamespaces().get(0);
        NamespacedListValueForWS value1ToDelete = new NamespacedListValueForWS("value1_1");
        NamespacedListValueForWS value2ToDelete = new NamespacedListValueForWS("value2_1");

        String valueStringToDelete = "value1_1,value2_1,notExistingValue";

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListHasValue(list.getName(), value1ToDelete).
                verifyListHasValue(list.getName(), value2ToDelete).
                deleteNamespacedListValues(list, valueStringToDelete).
                getAllNamespacedLists().
                verifyListDoesNotHaveValue(list.getName(), value1ToDelete).
                verifyListDoesNotHaveValue(list.getName(), value2ToDelete);
    }

    @Test
    public void ValuesAreAddedToNamespacedList_WhenItExists() throws SerializerException {
        NamespacedList list =  new NamespacedList(namespaces.getNamespaces().get(0).getName(), new HashSet<>());
        NamespacedListValueForWS value1ToAdd = new NamespacedListValueForWS("value5_1");
        NamespacedListValueForWS value2ToAdd = new NamespacedListValueForWS("value6_1");
        list.getValueSet().add(value1ToAdd);
        list.getValueSet().add(value2ToAdd);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListDoesNotHaveValue(list.getName(), value1ToAdd).
                verifyListDoesNotHaveValue(list.getName(), value2ToAdd).
                addValuesToNamespacedList(list).
                getAllNamespacedLists().
                verifyListHasValue(list.getName(), value1ToAdd).
                verifyListHasValue(list.getName(), value2ToAdd);
    }

    @Test
    public void DuplicateValuesAreNotAdded_OnAddingValues() throws SerializerException {
        NamespacedList list =  new NamespacedList(namespaces.getNamespaces().get(0).getName(), new HashSet<>());
        NamespacedListValueForWS value1ToAdd = new NamespacedListValueForWS("value5_1");
        NamespacedListValueForWS value2ToAdd = new NamespacedListValueForWS("value6_1");
        list.getValueSet().add(value1ToAdd);
        list.getValueSet().add(value2ToAdd);

        NamespacedList listWithDuplicates =  new NamespacedList(namespaces.getNamespaces().get(1).getName(), new HashSet<>());
        list.getValueSet().add(value1ToAdd);
        list.getValueSet().add(value2ToAdd);

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListDoesNotHaveValue(list.getName(), value1ToAdd).
                verifyListDoesNotHaveValue(list.getName(), value2ToAdd).
                addValuesToNamespacedList(list).
                getAllNamespacedLists().
                verifyListHasValue(list.getName(), value1ToAdd).
                verifyListHasValue(list.getName(), value2ToAdd).
                addValuesToNamespacedList(listWithDuplicates).
                getAllNamespacedLists().
                verifyListDoesNotHaveValue(listWithDuplicates.getName(), value1ToAdd).
                verifyListDoesNotHaveValue(listWithDuplicates.getName(), value2ToAdd);
    }

    @Test
    public void BulkDelete_isSuccessful() throws SerializerException {
        NamespacedList listToDeleteFrom = namespaces.getNamespaces().get(0);
        NamespacedEntities entities = new NamespacedEntities();
        entities.setEntities(new HashSet<>());
        entities.getEntities().add("value1_1");
        entities.getEntities().add("value2_1");
        entities.getEntities().add("shouldNotBeFound");

        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                bulkDeleteNamespacedListValues(listToDeleteFrom, entities).
                getAllNamespacedLists().
                verifyListDoesNotHaveValue(listToDeleteFrom.getName(), new NamespacedListValueForWS("value1_1")).
                verifyListDoesNotHaveValue(listToDeleteFrom.getName(), new NamespacedListValueForWS("value2_1")).
                verifyBulkDeleteResultHasDeletedValue("value1_1").
                verifyBulkDeleteResultHasDeletedValue("value2_1").
                verifyBulkDeleteResultHasNotFoundValue("shouldNotBeFound");
    }

    @Test
    public void DeleteFromMultipleLists_IsSuccessful() throws SerializerException {
        NamespacedValuesToDeleteByName valuesToDeleteByName1 = new NamespacedValuesToDeleteByName();
        valuesToDeleteByName1.setName(namespaces.getNamespaces().get(0).getName());
        valuesToDeleteByName1.getValuesToDelete().add("value2_1");
        valuesToDeleteByName1.getValuesToDelete().add("value3_1");

        NamespacedValuesToDeleteByName valuesToDeleteByName2 = new NamespacedValuesToDeleteByName();
        valuesToDeleteByName2.setName(namespaces.getNamespaces().get(1).getName());
        valuesToDeleteByName2.getValuesToDelete().add("value1_2");
        valuesToDeleteByName2.getValuesToDelete().add("value2_2");

        List<NamespacedValuesToDeleteByName> valueListToDelete = new ArrayList<>(2);
        valueListToDelete.add(valuesToDeleteByName1);
        valueListToDelete.add(valuesToDeleteByName2);


        new NamespacedListServiceSteps(getServiceNameForTest()).
                postNamespacedLists(namespaces).
                getAllNamespacedLists().
                verifyListHasValue(valuesToDeleteByName1.getName(), new NamespacedListValueForWS("value2_1")).
                verifyListHasValue(valuesToDeleteByName2.getName(), new NamespacedListValueForWS("value2_2")).
                deleteValuesFromMultipleNamespacedLists(valueListToDelete).
                getAllNamespacedLists().
                verifyListHasValue(valuesToDeleteByName1.getName(), new NamespacedListValueForWS("value1_1")).
                verifyListDoesNotHaveValue(valuesToDeleteByName1.getName(), new NamespacedListValueForWS("value2_1")).
                verifyListDoesNotHaveValue(valuesToDeleteByName1.getName(), new NamespacedListValueForWS("value3_1")).
                verifyListDoesNotHaveValue(valuesToDeleteByName2.getName(), new NamespacedListValueForWS("value1_2")).
                verifyListDoesNotHaveValue(valuesToDeleteByName2.getName(), new NamespacedListValueForWS("value2_2")).
                verifyListHasValue(valuesToDeleteByName2.getName(), new NamespacedListValueForWS("value3_2"));
    }

    private NamespacedList getNamespacedListByName(Namespaces namespaces, String name) {
        for (NamespacedList namespacedList: namespaces.getNamespaces()) {
            if (name.equals(namespacedList.getName())) {
                return namespacedList;
            }
        }
        return null;
    }

    private class NamespacedListServiceSteps {
        private String serviceName;
        private Namespaces responseNamespaced;

        NamespacedListSearchResult namespacedListSearchResult;

        NamespaceDuplicates namespaceDuplicates;

        NamespacedEntities returnedNamespacedEntities;

        NamespacedListServiceSteps (String serviceName) {
            this.serviceName = serviceName;
        }

        NamespacedListServiceSteps postNamespacedLists(Namespaces namespaces) {
            apiFacade.postNamespacedLists(namespaces);
            this.responseNamespaced = namespaces;
            return this;
        }

        NamespacedListServiceSteps postOneNamespacedList(NamespacedList namespacedList) {
            NamespacedList list = apiFacade.postOneNamespacedList(namespacedList);
            Namespaces namespaces = new Namespaces();
            namespaces.setNamespaces(new LinkedList<>());
            namespaces.getNamespaces().add(list);
            this.responseNamespaced = namespaces;
            return this;
        }

        NamespacedListServiceSteps addValuesToNamespacedList (NamespacedList namespacedListWithAddedValues) {
            NamespacedList list = apiFacade.addValuesToNamespacedList(namespacedListWithAddedValues);
            Namespaces namespaces = new Namespaces();
            namespaces.setNamespaces(new LinkedList<>());
            namespaces.getNamespaces().add(list);
            return this;
        }

        NamespacedListServiceSteps deleteOneNamespacedList(NamespacedList namespacedList) {
            apiFacade.deleteNamespacedList(namespacedList);
            return this;
        }

        NamespacedListServiceSteps deleteNamespacedListValues (NamespacedList list, String values) {
            apiFacade.deleteNamespacedListValues(list, values);
            return this;
        }

        NamespacedListServiceSteps bulkDeleteNamespacedListValues (NamespacedList list, NamespacedEntities values) {
            returnedNamespacedEntities = apiFacade.bulkDeleveValuesFromNamespacedList(list, values);
            return this;
        }

        NamespacedListServiceSteps deleteValuesFromMultipleNamespacedLists (List<NamespacedValuesToDeleteByName> valuesToDeleteByNames) {
            apiFacade.deleteMultipleValuesFromMultipleNSLists(valuesToDeleteByNames);
            return this;
        }

        NamespacedListServiceSteps searchInNamespacedLists (String value) {
            namespacedListSearchResult = apiFacade.searchInNamespacedLists(value);
            return this;
        }

        NamespacedListServiceSteps verifySearchResultContainsNamespacedList (NamespacedList list) {
            if (doesSearchResultContainNamespacedList(list)) {
                return this;
            }
            fail("NS list is not found in response");
            return this;
        }

        NamespacedListServiceSteps verifySearchResultDoesNotContainNamespacedList (NamespacedList list) {
            if (!doesSearchResultContainNamespacedList(list)) {
                return this;
            }
            fail("Not expected NS list is found in response");
            return this;
        }

        NamespacedListServiceSteps verifyListIsPresent(NamespacedList list) {
            NamespacedList listFromResponce = null;
            try {
                listFromResponce = returnResponseListByName(list).get();
            } catch (NoSuchElementException e) {
                fail("List with such a name is not present");
            }

            assertTrue("List is present, but values or description are not equal", areNamespacedListsHoldingTheSameData(listFromResponce, list));
            return this;
        }

        NamespacedListServiceSteps verifyListIsNotPresent(NamespacedList list) {
            try {
                returnResponseListByName(list).get();
                fail("List with such a name" + list.getName() + "is present");
            } catch (NoSuchElementException ignore) { }
            return this;
        }

        NamespacedListServiceSteps searchDuplicates (NamespacedList list) {
            namespaceDuplicates = apiFacade.searchNamespaceDuplicates(list);
            return this;
        }

        NamespacedListServiceSteps verifyBulkDeleteResultHasDeletedValue (String value) {
            if (!returnedNamespacedEntities.getDeletedValues().contains(value)) {
                fail();
            }
            return this;
        }

        NamespacedListServiceSteps verifyBulkDeleteResultHasNotFoundValue (String value) {
            if (!returnedNamespacedEntities.getEntities().contains(value)) {
                fail();
            }
            return this;
        }

        NamespacedListServiceSteps verifyDuplicatesMapIsNotEmpty () {
            assertFalse("Duplicates are not found, should be found", namespaceDuplicates.getNamespaceDuplicatesMap().isEmpty());
            return this;
        }

        NamespacedListServiceSteps verifyDuplicatesMapIsEmpty () {
            assertTrue("Duplicates are found, should not be found", namespaceDuplicates.getNamespaceDuplicatesMap().isEmpty());
            return this;
        }

        NamespacedListServiceSteps verifyListHasValue (String listName, NamespacedListValueForWS valueForWS) {
            assertTrue("Value " + valueForWS.toString() + " is not found in Namespaced list " + listName,
                    listHasValue(getNamespacedListByName(responseNamespaced, listName), valueForWS));
            return this;
        }

        NamespacedListServiceSteps verifyListDoesNotHaveValue (String listName, NamespacedListValueForWS valueForWS) {
            assertFalse("Value" + valueForWS.toString() + " is found in Namespaced list" + listName + ", whether it should not be",
                    listHasValue(getNamespacedListByName(responseNamespaced, listName), valueForWS));
            return this;
        }

        NamespacedListServiceSteps getAllNamespacedLists() {
            this.responseNamespaced = apiFacade.getAllNamespacedLists();
            return this;
        }

        NamespacedListServiceSteps getOneNamespacedList(String namespacedListName) {
            Namespaces namespaces = new Namespaces();
            namespaces.setNamespaces(new LinkedList<>());
            NamespacedList list = null;
            try {
                list = apiFacade.getOneNamespacedList(namespacedListName);
            } catch (Exception ignore) {}
            if (list != null) {
                namespaces.getNamespaces().add(list);
            }
            this.responseNamespaced = namespaces;
            return this;
        }

        /**
         * Is namespaced list with a name of the provided list present in the response?
         * If yes, return it, return empty optional otherwise.
         * @param list
         * @return
         */
        private Optional<NamespacedList> returnResponseListByName(NamespacedList list) {
            if (Objects.isNull(responseNamespaced) || Objects.isNull(responseNamespaced.getNamespaces()) ||
                    responseNamespaced.getNamespaces().isEmpty()) {
                return Optional.empty();
            }
            return responseNamespaced.getNamespaces().stream().
                    filter(responceList -> responceList.getName().equals(list.getName()))
                    .findFirst();
        }

        /**
         * NOT equals, since equals is formally defined in the class
         * This method is about data transferred, it disregards version info and such.
         * @param fromResponse
         * @param insertedList
         * @return
         */
        private boolean areNamespacedListsHoldingTheSameData(NamespacedList fromResponse, NamespacedList insertedList) {
            Boolean valuesAreEqual = true;
            if (NamespacedListType.ENCODED.equals(insertedList.getType())) {
                for (NamespacedListValueForWS valueFromInserted : insertedList.getValueSet()) {
                    if (!listHasValue(fromResponse, valueFromInserted)) {
                        valuesAreEqual = false;
                    }
                }
            }  else {
                valuesAreEqual = fromResponse.getValueSet().equals(insertedList.getValueSet());
            }

            return valuesAreEqual &&
                    Objects.equals(fromResponse.getDescription(), insertedList.getDescription()) &&
                    Objects.equals(fromResponse.getName(), insertedList.getName());
        }

        private boolean doesSearchResultContainNamespacedList(NamespacedList list) {
            for (NamespacedListEntity nsListEntity: namespacedListSearchResult.getNamespacedLists()) {
                if (list.getName().equals(nsListEntity.getName())) {
                    return true;
                }
            }
            return false;
        }

        private boolean listHasValue (NamespacedList list, NamespacedListValueForWS value) {
            for (NamespacedListValueForWS valueForWS : list.getValueSet()) {
                if (NamespacedListType.ENCODED.equals(list.getType())) {
                    if (Objects.equals(value.getValue(), valueForWS.getValue()) &&
                            (StringUtils.isNotBlank(valueForWS.getEncodedValue()))) {
                        return true;
                    }
                } else {
                    if (Objects.equals(value.getValue(), valueForWS.getValue())) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
