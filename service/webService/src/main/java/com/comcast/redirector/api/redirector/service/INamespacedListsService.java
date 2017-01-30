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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;

import java.util.Collection;
import java.util.List;

public interface INamespacedListsService {

    Namespaces getAllNamespacedListsFilteredByPermissions();

    Namespaces getAllNamespacedLists();

    Namespaces getAllNamespacedListsWithoutValues();

    NamespacedListSearchResult searchNamespacedLists(String value);

    NamespacedListSearchResult searchNamespacedLists(NamespacedListValueForWS searchValue, SnapshotList snapshotList);

    NamespaceDuplicates getNamespaceDuplicatesFilteredByPermissions(NamespacedList newNamespacedList, Namespaces allNamespaces);

    NamespaceDuplicates getNamespaceDuplicates(NamespacedList newNamespacedList, Namespaces allNamespaces);

    NamespacedList getNamespacedListByName(String name);

    void deleteNamespacedList(String name);

    void addNamespacedList(NamespacedList namespace);

    NamespacedListEntity getRulesDependingOnNamespaced(String namespacedName);

    NamespacedList addNamespacedList(String name, NamespacedList namespacedList, boolean autoResolve);

    NamespacedListEntity getRulesDependingOnNamespaced(String namespacedName, SnapshotList snapshotList);

    void validateNamespacedList(NamespacedList namespacedList, Collection<IfExpression> rules);

    NamespacedEntities removeEntitiesFromNSListAndReturnNotFoundAndDeletedValues(NamespacedList list, NamespacedEntities values);

    Namespaces deleteEntitiesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> toDelete);

    Namespaces deleteEntitiesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> toDelete, ApplicationStatusMode status);
}
