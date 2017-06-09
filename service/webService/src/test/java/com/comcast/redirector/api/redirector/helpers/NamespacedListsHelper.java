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

package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.common.RedirectorConstants;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class NamespacedListsHelper {

    private static final String NAMESPACE_SERVICE_PATH = RedirectorConstants.NAMESPACE_CONTROLLER_PATH;

    private static final String DEPENDING_RULES_API = "dependingRulesMultiple";

    /**
     *
     * @return Namespaces object that needed for post and comparing with all responses: <p>
     * <pre>
     *  {@code
     *    <namespaces>
     *      <namespace name="namespacedListTest1">
     *        <description>Description1</description>
     *        <value>value1_1</value>
     *        <value>value2_1</value>
     *        <value>value3_1</value>
     *      </namespace>
     *      <namespace name="namespacedListTest2">
     *        <description>Description2</description>
     *        <value>value1_2</value>
     *        <value>value2_2</value>
     *        <value>value3_2</value>
     *      </namespace>
     *    </namespaces>
     *  }
     * </pre>
     *
     *
     */
    public static Namespaces createNamespaces(NamespacedListType type) {
        Namespaces namespaces = new Namespaces();
        List<NamespacedList> namespacedLists = new ArrayList<NamespacedList>();
        for (int i = 1; i <= 2; i++) {
            NamespacedList namespacedList = new NamespacedList();
            namespacedList.setName("namespacedListTest" + i);
            namespacedList.setDescription("Description" + i);
            namespacedList.setType(type);
            namespacedList.setValueSet(new HashSet<>(Arrays.asList(
                    new NamespacedListValueForWS("value1_" + i),
                    new NamespacedListValueForWS("value2_" + i),
                    new NamespacedListValueForWS("value3_" + i))));
            namespacedLists.add(namespacedList);
        }
        namespaces.setNamespaces(namespacedLists);
        return namespaces;
    }


    public static <T> T getDependentRulesOfNamespace(WebTarget target, String namespacedName, String responseMediaType,
                                                     Class<T> responseClassType) {
        WebTarget webTarget = target.path(NAMESPACE_SERVICE_PATH).path(DEPENDING_RULES_API).path(namespacedName);
        return ServiceHelper.get(webTarget, responseMediaType, responseClassType);
    }

    public static void postNamespaces(Namespaces namespaces) {
        for (NamespacedList namespacedList: namespaces.getNamespaces()) {
            ServiceHelper.post(getWebTarget_Post(namespacedList.getName()), namespacedList, MediaType.APPLICATION_JSON);
        }
    }

    public static WebTarget getWebTarget_Post(String namespacedListName) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH + "/addNewNamespaced").path(namespacedListName);
    }

    public static WebTarget getWebTarget_Get(String namespacedListName) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH + "/getOne").path(namespacedListName);
    }

    public static WebTarget getWebTarget_Delete_multiple(String namespacedListName) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path("deleteNamespacedEntities").path(namespacedListName);
    }

    public static WebTarget getWebTarget_GetAll() {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH + "/getAllNamespacedLists");
    }

    public static WebTarget getWebTarget_Delete(String namespacedListName) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedListName);
    }

    public static WebTarget getWebTarget_DeleteValues(String namespacedListName, String namespacedListValues) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedListName).path(namespacedListValues);
    }

    public static WebTarget getWebTarget_Search(String searchValue) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path("search").path(searchValue);
    }

    public static WebTarget getWebTarget_SearchDuplicated() {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path("duplicates");
    }

    public static WebTarget getWebTarget_AddValues(String namespacedListName) {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path(namespacedListName).path("addValues");
    }

    public static WebTarget getWebTargetDeleteEntitiesFromMultipleNamespacedLists() {
        return HttpTestServerHelper.target().path(NAMESPACE_SERVICE_PATH).path("deleteEntitiesFromNamespacedLists");
    }

    public static void cleanUpNamespaceData() {
        Namespaces allNamespaces = ServiceHelper.get(NamespacedListsHelper.getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Namespaces.class);
        for (NamespacedList namespacedList: allNamespaces.getNamespaces()) {
            ServiceHelper.delete(NamespacedListsHelper.getWebTarget_Delete(namespacedList.getName()));
        }
    }
}
