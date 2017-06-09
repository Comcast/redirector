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
package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.namespaced.NamespaceChangesStatus;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class NamespacesChangesService {
    private static final String FAILED_TO_SAVE_NAMESPACE_DUE_TO_VALIDATION_ERROR = "Failed to save namespace due to validation error(s).";

    private static Logger log = LoggerFactory.getLogger(NamespacesChangesService.class);

    @Autowired
    @Qualifier("coreBackupNamespaceChangesDAO")
    private ISimpleServiceDAO<NamespaceChangesStatus> coreBackupNamespaceChangesDAO;

    @Autowired
    private NamespacedListsService namespacedListsService;

    private List<String> errorList;

    private Comparator<Map.Entry<NamespacedList, ActionType>> timespampComparator = (entryComparator, nextEntryComparator) -> Long.compare(entryComparator.getKey().getVersion(), nextEntryComparator.getKey().getVersion());

    private Predicate<Map.Entry<NamespacedList, ActionType>> filterToSave = (mapEntry) -> translatedChanges(mapEntry.getKey(), mapEntry.getValue());

    private Predicate<Map.Entry<NamespacedList, ActionType>> filterToSaveAlone(NamespacedList namespacedList) {
        return entryPredicate -> !entryPredicate.getKey().equals(namespacedList) || translatedChanges(entryPredicate.getKey(), entryPredicate.getValue());
    }

    public void save(String serviceName, NamespaceChangesStatus namespaceChangesStatus) {
        try {
            coreBackupNamespaceChangesDAO.save(namespaceChangesStatus, serviceName);
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }

    public NamespaceChangesStatus getNamespaceChangesStatus(String serviceName) {
        log.info("getNamespaceChangesStatus {}", serviceName);
        NamespaceChangesStatus namespaceChangesStatus = coreBackupNamespaceChangesDAO.get(serviceName);
        if (namespaceChangesStatus == null) {
            log.info("getNamespaceChangesStatus null");
        } else {
            log.info("getNamespaceChangesStatus {}", namespaceChangesStatus.getNamespaceChanges().size());
        }
        return namespaceChangesStatus;
    }

    public NamespaceChangesStatus approve(String serviceName, NamespacedList namespacedList) {

        NamespaceChangesStatus namespaceChangesStatus = getNamespaceChangesStatus(serviceName);

        errorList = null;

        if (namespaceChangesStatus != null && namespaceChangesStatus.getNamespaceChanges() != null && namespacedList != null) {

            Map<NamespacedList, ActionType> removedNamespacesChangesMap = namespaceChangesStatus.getNamespaceChanges().entrySet().stream()
                    .filter(filterToSaveAlone(namespacedList))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            namespaceChangesStatus.setNamespaceChanges(removedNamespacesChangesMap);
            save(serviceName, namespaceChangesStatus);
        }

        if (errorList != null) {
            throw new WebApplicationException(FAILED_TO_SAVE_NAMESPACE_DUE_TO_VALIDATION_ERROR,
                    Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage(FAILED_TO_SAVE_NAMESPACE_DUE_TO_VALIDATION_ERROR)).build());
        }

        return namespaceChangesStatus;
    }

    public NamespaceChangesStatus cancelAll(String serviceName) {
        NamespaceChangesStatus namespaceChangesStatus = getNamespaceChangesStatus(serviceName);
        namespaceChangesStatus.clear();
        save(serviceName, namespaceChangesStatus);
        return namespaceChangesStatus;
    }

    public NamespaceChangesStatus cancel(String serviceName, NamespacedList namespacedList) {
        NamespaceChangesStatus namespaceChangesStatus = getNamespaceChangesStatus(serviceName);
        namespaceChangesStatus.getNamespaceChanges().remove(namespacedList);
        save(serviceName, namespaceChangesStatus);
        return namespaceChangesStatus;
    }

    public NamespaceChangesStatus approveAll(String serviceName) {

        NamespaceChangesStatus namespaceChangesStatus = getNamespaceChangesStatus(serviceName);

        errorList = new ArrayList<>();

        if (namespaceChangesStatus != null && namespaceChangesStatus.getNamespaceChanges() != null) {

            Map<NamespacedList, ActionType> removedNamespacesChangesMap = namespaceChangesStatus.getNamespaceChanges().entrySet().stream()
                    .sorted(timespampComparator)
                    .filter(filterToSave)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            namespaceChangesStatus.setNamespaceChanges(removedNamespacesChangesMap);

            save(serviceName, namespaceChangesStatus);
        }

        if (errorList.size() > 0) {

            String commaSeparatedFailedNamespacedList = errorList.stream()
                    .map(stringFunction -> stringFunction.toString())
                    .collect(Collectors.joining(", "));

            throw new WebApplicationException(commaSeparatedFailedNamespacedList,
                    Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessage(commaSeparatedFailedNamespacedList)).build());
        }

        return namespaceChangesStatus;
    }

    //todo: think about better implementation, it returns bool and modifies global list at the same time.
    private boolean translatedChanges(NamespacedList namespacedList, ActionType type) {

        try {
            switch (type) {
                case ADD:
                case UPDATE:
                    if (!areNamespacedDuplicatesPresent(namespacedList)) {
                        namespacedListsService.addNamespacedList(namespacedList);
                    } else {
                        errorList.add("Value is duplicate in namespace " + namespacedList.getName() + ". ");
                        return true;
                    }
                    break;
                case DELETE:
                    namespacedListsService.deleteNamespacedList(namespacedList.getName());
                    break;
            }

        } catch (WebApplicationException ex) {
            if (ex != null && ex.getCause() != null ) {
                errorList.add(ex.getCause().getMessage());
            }
            return true;

        } catch (RedirectorDataSourceException ex) {
            return true;
        }

        return false;
    }

    private boolean areNamespacedDuplicatesPresent(final NamespacedList newNamespacedList) {
        for (NamespacedList list : namespacedListsService.getAllNamespacedLists().getNamespaces()) {
            if (newNamespacedList.getName().equals(list.getName())) {
                continue;
            }
            Set<NamespacedListValueForWS> listTemp = list.getValueSet();
            listTemp.retainAll(newNamespacedList.getValueSet());
            if (!listTemp.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
