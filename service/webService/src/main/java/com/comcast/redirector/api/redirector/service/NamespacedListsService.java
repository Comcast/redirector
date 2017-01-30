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
import com.comcast.redirector.api.auth.NamespacedListsPermissionPostProcessService;
import com.comcast.redirector.api.decider.service.IDeciderRulesService;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingChangeStatusHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.IFlavorRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.IUrlRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.TemplateFlavorRulesService;
import com.comcast.redirector.api.redirector.service.ruleengine.TemplateUrlRulesService;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.util.NamespacedListUtils;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.dao.IEmptyObjectDAO;
import com.comcast.redirector.dataaccess.dao.IListDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class NamespacedListsService implements INamespacedListsService {
    private static Logger log = LoggerFactory.getLogger(NamespacedListsService.class);

    public enum Applications {
        XRE_GUIDE("xreGuide"),
        XRE_APP("xreApp"),
        DECIDER("decider");

        private final String value;

        Applications(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Autowired
    private NamespacedListsPermissionPostProcessService namespacedListsPermissionPostProcessService;

    @Autowired
    private IListDAO<NamespacedList> namespacedListDAO;

    @Autowired
    private IFlavorRulesService flavorRulesService;

    @Autowired
    private IDeciderRulesService deciderRulesService; // TODO: decouple

    @Autowired
    private IUrlRulesService urlRulesService;

    @Autowired
    @Qualifier("changesStatusService")
    private IChangesStatusService pendingChangesService;

    @Autowired
    @Qualifier("coreBackupChangesStatusService")
    private IChangesStatusService coreBackupPendingChangesService;

    @Autowired
    private TemplateFlavorRulesService templateFlavorRulesService;

    @Autowired
    private TemplateUrlRulesService templateUrlRulesService;

    @Autowired
    private IAppsService appsService;

    @Autowired
    private IEmptyObjectDAO namespacedListVersionReloadDAO; // TODO: move to NamespacedListDAO instead

    @Override
    public Namespaces getAllNamespacedLists() {
        Namespaces namespaces = new Namespaces();
        namespaces.setNamespaces(namespacedListDAO.getAll());

        return namespaces;
    }

    @Override
    public Namespaces getAllNamespacedListsFilteredByPermissions() {
        Namespaces namespaces = getAllNamespacedLists();
        namespaces.setNamespaces(namespacedListsPermissionPostProcessService.removeListsWithNoReadPermissionsFromNamespacedLists(namespaces.getNamespaces()));

        return namespaces;
    }

    @Override
    public Namespaces getAllNamespacedListsWithoutValues() {
        Namespaces namespaces = getAllNamespacedListsFilteredByPermissions();
        for (NamespacedList list: namespaces.getNamespaces()) {
            list.setValueCount(list.getValueSet().size());
            list.setValueSet(null);
        }
        return namespaces;
    }

    @Override
    public NamespacedListSearchResult searchNamespacedLists(String value) {
        Namespaces namespaces = getAllNamespacedLists();
        Map<String, RulesWrapper> allRules = new HashMap<>();
        NamespacedListValueForWS searchValue = new NamespacedListValueForWS(value);

        for (String serviceName : appsService.getAppNames().getAppNames()) {
            RulesWrapper rulesWrapper = new RulesWrapper();
            rulesWrapper.addRules(EntityType.RULE.name(), flavorRulesService.getRules(serviceName));
            rulesWrapper.addRules(EntityType.TEMPLATE_RULE.name(), templateFlavorRulesService.getRules(serviceName));
            rulesWrapper.addRules(EntityType.URL_RULE.name(), urlRulesService.getUrlRules(serviceName));
            rulesWrapper.addRules(EntityType.TEMPLATE_URL_RULE.name(), templateUrlRulesService.getUrlRules(serviceName));
            allRules.put(serviceName, rulesWrapper);
        }
        putRulesToMap(Applications.DECIDER.value, allRules,
                new RulesWrapper(EntityType.DECIDER_RULE.name(), deciderRulesService.getRules()));

        NamespacedListSearchResult namespacedListSearchResult = NamespacedListUtils.searchNamespacedLists(searchValue, namespaces, allRules);
        return namespacedListsPermissionPostProcessService.removeListsWithNoReadPermissionsFromSearchResult(namespacedListSearchResult);
    }

    @Override
    public NamespacedListSearchResult searchNamespacedLists(NamespacedListValueForWS searchValue, SnapshotList snapshotList) {

        Map<String, RulesWrapper> allRules = new HashMap<>();
        for (Snapshot snapshot : snapshotList.getItems()) {
            RulesWrapper rulesWrapper = new RulesWrapper();
            rulesWrapper.addRules(EntityType.RULE.name(), snapshot.getFlavorRules().getItems());
            rulesWrapper.addRules(EntityType.TEMPLATE_RULE.name(), snapshot.getTemplatePathRules().getItems());
            rulesWrapper.addRules(EntityType.URL_RULE.name(), snapshot.getUrlRules().getItems());
            rulesWrapper.addRules(EntityType.TEMPLATE_URL_RULE.name(), snapshot.getTemplateUrlRules().getItems());
            allRules.put(snapshot.getApplication(), rulesWrapper);
        }

        NamespacedListSearchResult namespacedListSearchResult =  NamespacedListUtils.searchNamespacedLists(searchValue, snapshotList.getNamespaces(), allRules);
        return namespacedListsPermissionPostProcessService.removeListsWithNoReadPermissionsFromSearchResult(namespacedListSearchResult);
    }

    @Override
    public NamespaceDuplicates getNamespaceDuplicates(NamespacedList newNamespacedList, Namespaces allNamespaces) {
        return getNamespaceDuplicates(newNamespacedList, allNamespaces, false);
    }

    @Override
    public NamespaceDuplicates getNamespaceDuplicatesFilteredByPermissions(NamespacedList newNamespacedList, Namespaces allNamespaces) {
        return getNamespaceDuplicates(newNamespacedList, allNamespaces, true);
    }

    private NamespaceDuplicates getNamespaceDuplicates(NamespacedList newNamespacedList, Namespaces allNamespaces, boolean checkPermissions) {
        NamespaceDuplicates namespaceDuplicates = new NamespaceDuplicates();
        for (NamespacedList list : allNamespaces.getNamespaces()) {
            if (list.getName().equals(newNamespacedList.getName())) {
                continue;
            }

            Set<NamespacedListValueForWS> listTemp = list.getValueSet();
            listTemp.retainAll(newNamespacedList.getValueSet());
            for (NamespacedListValueForWS value : listTemp) {
                if (!checkPermissions || namespacedListsPermissionPostProcessService.isAuthorizedToReadList(list.getName())) {
                    namespaceDuplicates.put(value.getValue(), list.getName());
                } else {
                    namespaceDuplicates.setContainsNamespacedListsWithoutReadRights(true);
                }
            }
        }
        return namespaceDuplicates;
    }

    private void putRulesToMap(String serviceName, Map<String, RulesWrapper> rulesMap,
                               RulesWrapper rulesWrapper) {
        if (!rulesWrapper.getRules().isEmpty()) {
            rulesMap.put(serviceName, rulesWrapper);
        }
    }

    @Override
    public NamespacedList getNamespacedListByName(String name) {
        if (namespacedListsPermissionPostProcessService.isAuthorizedToReadList(name)) {
            return namespacedListDAO.getById(name);
        } else {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @Override
    public synchronized void deleteNamespacedList(String name) {
        Collection<IfExpression> rules = new ArrayList<>();

        for (String app : appsService.getAppNames().getAppNames()) {

            // 1. get all approved flavor and url rules, flavor and url templates
            rules.addAll(flavorRulesService.getRules(app));
            rules.addAll(urlRulesService.getUrlRules(app));
            rules.addAll(templateFlavorRulesService.getRules(app));
            rules.addAll(templateUrlRulesService.getUrlRules(app));

            // 2. get all pending flavor and url rules, flavor and url templates
            rules.addAll(PendingChangeStatusHelper.getNewPendingRulesAndTemplates(pendingChangesService.getPendingChangesStatus(app)));
            rules.addAll(PendingChangeStatusHelper.getNewPendingRulesAndTemplates(coreBackupPendingChangesService.getPendingChangesStatus(app)));
        }

        // 3. get all decider rules
        rules.addAll(deciderRulesService.getRules().getItems());

        NamespacedList  namespacedList = getNamespacedListByName(name);
        if (namespacedList != null) {
            deleteNamespacedList(namespacedList, rules, true);
        }
    }

    private void deleteNamespacedList(NamespacedList namespacedListToDelete, Collection<IfExpression> rules, boolean save) {

        // need to ensure that the namespace we are going to remove is not used in some rule
        validateNamespacedList(namespacedListToDelete, rules);

        if(save) {
            namespacedListDAO.deleteById(namespacedListToDelete.getName());
            namespacedListVersionReloadDAO.save();
        }
    }

    public void validateNamespacedList(NamespacedList namespacedList, Collection<IfExpression> rules) {

        if (!namespacedListsPermissionPostProcessService.isAuthorizedToReadList(namespacedList.getName())) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        try {
            if (namespacedList != null) {
                ModelValidationFacade.validateNamespacedList(namespacedList, rules);
            }
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to remove namespaced list '%s' due to validation error(s). %s",  namespacedList.getName(), ex.getMessage());
            throw new WebApplicationException(error, ex, Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrors()).build());
        }
    }

    @Override
    public synchronized void addNamespacedList(NamespacedList namespace) {
        try {
            ModelValidationFacade.validateNamespacedList(namespace);
            for (NamespacedListValueForWS value : namespace.getValueSet()) { //APPDS-1521: remove leading and trailing spaces in field values
               if (value.getValue() != null) {
                   value.setValue(value.getValue().trim());
               }
            }
            namespace.updateVersion();
            namespacedListDAO.saveById(namespace, namespace.getName());
            namespacedListVersionReloadDAO.save();
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to save namespace '%s' due to validation error(s). %s",  namespace.getName(), ex.getMessage());
            throw new WebApplicationException(error, ex, Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrors()).build());
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }

    @Override
    public NamespacedList addNamespacedList(final String name, final NamespacedList namespacedList, boolean autoResolve) {
        if(!name.equals(namespacedList.getName())) {
            ValidationState validationState = new ValidationState();
            validationState.pushError(ValidationState.ErrorType.NamespacedListsNamesMismatch);
            throw new WebApplicationException(new ExpressionValidationException(ValidationState.ErrorType.NamespacedListsNamesMismatch.toString(), validationState));
        }
        Namespaces allNamespaces = getAllNamespacedLists();
        NamespaceDuplicates namespaceDuplicates = getNamespaceDuplicates(namespacedList, allNamespaces);
        if (namespaceDuplicates.isEmpty()) {
            namespacedList.setName(name);
            addNamespacedList(namespacedList);
            return namespacedList;
        } else {
            if (autoResolve) {
                if (namespacedList.getType() != NamespacedListType.TEXT) {
                     throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                             .entity(new ErrorMessage("Auto-resolving is possible only for text-typed lists")).build());
                }
                Map<String, NamespacedValuesToDeleteByName> namespacedListsAlreadyInMap = new LinkedHashMap<>();
                List<NamespacedValuesToDeleteByName> namespacedValuesToDeleteByNameList = new LinkedList<>();
                Map<String, String> duplicatesMap = namespaceDuplicates.getNamespaceDuplicatesMap();
                for (String value: duplicatesMap.keySet()) {
                    String listName = duplicatesMap.get(value);
                    if (namespacedListsAlreadyInMap.containsKey(listName)) {
                        namespacedListsAlreadyInMap.get(listName).getValuesToDelete().add(value);
                    } else {
                        NamespacedValuesToDeleteByName valuesToDeleteByName = new NamespacedValuesToDeleteByName();
                        valuesToDeleteByName.setName(listName);
                        valuesToDeleteByName.getValuesToDelete().add(value);
                        namespacedListsAlreadyInMap.put(listName,valuesToDeleteByName);
                    }
                }
                for (NamespacedValuesToDeleteByName valuesToDeleteByName : namespacedListsAlreadyInMap.values()) {
                    namespacedValuesToDeleteByNameList.add(valuesToDeleteByName);
                }
                deleteEntitiesFromMultipleNamespacedLists(namespacedValuesToDeleteByNameList);

                namespacedList.setName(name);
                addNamespacedList(namespacedList);
                return namespacedList;
            } else {
                ValidationState validationState = new ValidationState();
                validationState.pushError(ValidationState.ErrorType.NamespacedListsDuplicates);
                throw new WebApplicationException(new ExpressionValidationException("", validationState));
            }
        }
    }

    @Override
    public NamespacedListEntity getRulesDependingOnNamespaced(String namespacedName, SnapshotList snapshotList) {
        Map<String, Collection<IfExpression>> rulesByAppNames = new HashMap<>();
        Map<String, Collection<IfExpression>> templateRulesByAppNames = new HashMap<>();

        AppNames appNames = snapshotList.getApplicationsNames();
        for (Snapshot snapshot : snapshotList.getItems()) {
            rulesByAppNames.put(snapshot.getApplication(), snapshot.getFlavorRules().getItems());
            templateRulesByAppNames.put(snapshot.getApplication(), snapshot.getTemplatePathRules().getItems());
        }
        return getRulesDependingOnNamespacedInternal(namespacedName, appNames, rulesByAppNames, templateRulesByAppNames);
    }

    @Override
    public NamespacedListEntity getRulesDependingOnNamespaced(String namespacedName) {
        Map<String, Collection<IfExpression>> rulesByAppNames = new HashMap<>();
        Map<String, Collection<IfExpression>> templateRulesByAppNames = new HashMap<>();
        for (String serviceName : appsService.getAppNames().getAppNames()) {
            try {
                rulesByAppNames.put(serviceName, flavorRulesService.getRules(serviceName));
            } catch (RedirectorDataSourceException ignore) {
                log.info("No rules for service {}", serviceName);
            }
            try {
                templateRulesByAppNames.put(serviceName, templateFlavorRulesService.getRules(serviceName));
            } catch (RedirectorDataSourceException ignore) {
                log.info("No template rules for service {}", serviceName);
            }
        }
        return getRulesDependingOnNamespacedInternal(namespacedName, appsService.getAppNames(), rulesByAppNames, templateRulesByAppNames);
    }

    @Override
    public NamespacedEntities removeEntitiesFromNSListAndReturnNotFoundAndDeletedValues(NamespacedList list, NamespacedEntities values) {
        NamespacedEntities returnedEntities = new NamespacedEntities();
        Set<String> notFoundSet = new HashSet<>();
        Set<String> deletedSet = new HashSet<>();
        if (list == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Namespaced list is null.")).build());
        }

        notFoundSet.addAll(values.getEntities());
        notFoundSet.removeAll(list.getValues());

        deletedSet.addAll(values.getEntities());
        deletedSet.retainAll(list.getValues());

        for (String item : values.getEntities()) {
            list.getValueSet().remove(new NamespacedListValueForWS(item));
        }
        returnedEntities.setEntities(notFoundSet);
        returnedEntities.setDeletedValues(deletedSet);
        if (!namespacedListsPermissionPostProcessService.isAuthorizedToWriteList(list.getName())) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return returnedEntities;
    }

    private NamespacedListEntity getRulesDependingOnNamespacedInternal (String namespacedName, AppNames appNames, Map<String, Collection<IfExpression>>  rules, Map<String, Collection<IfExpression>>templateRules) {

        if (!namespacedListsPermissionPostProcessService.isAuthorizedToReadList(namespacedName)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Map<String, RulesWrapper> allRules = new HashMap<>();
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(namespacedName);

        for (String serviceName : appNames.getAppNames()) {
            RulesWrapper rulesWrapper = new RulesWrapper();
            rulesWrapper.addRules(EntityType.RULE.name(), rules.get(serviceName));
            rulesWrapper.addRules(EntityType.TEMPLATE_RULE.name(), templateRules.get(serviceName));
            allRules.put(serviceName, rulesWrapper);
        }

        NamespacedListEntity namespacedListEntity = new NamespacedListEntity();
        namespacedListEntity.setDependingFlavorRules(NamespacedListUtils.getDependentRules(EntityType.RULE.name(), namespacedList, allRules));
        namespacedListEntity.setDependingTemplateFlavorRules(NamespacedListUtils.getDependentRules(EntityType.TEMPLATE_RULE.name(), namespacedList, allRules));
        return namespacedListEntity;

    }

    public Namespaces deleteEntitiesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> toDelete) {

        Consumer<NamespacedValuesToDeleteByName> extractNamespacedList = namespacedListConsumer -> {
            try {
                namespacedListConsumer.setCurrentNamespacedList(this.getNamespacedListByName(namespacedListConsumer.getName()));
            } catch (RedirectorDataSourceException e1) {
                throw new WebApplicationException(new RuntimeException("Namespaced list is not found "), Response.Status.BAD_REQUEST);
            }
        };
        
        toDelete.stream().forEach(extractNamespacedList);
        return deleteEntitiesFromMultipleNamespacedLists(toDelete, ApplicationStatusMode.ONLINE);
    }

    public Namespaces deleteEntitiesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> toDelete, ApplicationStatusMode mode) {
        Namespaces namespaces = new Namespaces();
        List<NamespacedList> namespacedLists = toDelete.stream().filter(Objects::nonNull).map(createNewNamespacedListWithoutChoosedValues).collect(Collectors.toList());
        namespaces.setNamespaces(namespacedLists);

        if(mode == ApplicationStatusMode.ONLINE) {
            namespacedLists.forEach(namespacedList -> {
                try {
                    addNamespacedList(namespacedList);
                } catch (RedirectorDataSourceException e) {
                    throw new WebApplicationException(new RuntimeException("Namespaced list is not saved with name: " + namespacedList.getName()), Response.Status.BAD_REQUEST);
                }
            });
        }
        return namespaces;
    }

    private Predicate<NamespacedListValueForWS> nonEqualsSelectedValues(List<String> compareValues) {
        return namespacedListValueForWS -> compareValues.stream().noneMatch(
                compareEntity -> namespacedListValueForWS.getValue().equals(compareEntity));
    }

    private Function<NamespacedValuesToDeleteByName, NamespacedList> createNewNamespacedListWithoutChoosedValues = namespacedValuesToDeleteByName -> {
        if(namespacedValuesToDeleteByName.getCurrentNamespacedList() != null) {
            if(!namespacedListsPermissionPostProcessService.isAuthorizedToWriteList(namespacedValuesToDeleteByName.getName())) {
                throw new WebApplicationException(new RuntimeException("Not authorized to write NS list "), Response.Status.FORBIDDEN);
            }
            namespacedValuesToDeleteByName.getCurrentNamespacedList().setValueSet(
                    namespacedValuesToDeleteByName.getCurrentNamespacedList().getValueSet().stream()
                            .filter(Objects::nonNull)
                            .filter(nonEqualsSelectedValues(namespacedValuesToDeleteByName.getValuesToDelete()))
                            .collect(Collectors.toSet()));

            return namespacedValuesToDeleteByName.getCurrentNamespacedList();
        } else {
            throw new WebApplicationException(new RuntimeException("Namespaced list not found "), Response.Status.BAD_REQUEST);
        }
    };
}
