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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.auth;

import com.comcast.redirector.api.auth.model.AccessMode;
import com.comcast.redirector.api.auth.model.Operation;
import com.comcast.redirector.api.auth.model.Permission;
import com.comcast.redirector.api.auth.model.Restriction;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.redirector.service.INamespacedListsPermissionPostProcessService;
import com.comcast.redirector.common.RedirectorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NamespacedListsPermissionPostProcessService implements INamespacedListsPermissionPostProcessService {

    @Autowired
    private PermissionProvider permissionProvider;

    @Override
    public boolean isAuthorizedToReadList(String name) {
        return isAuthorized(name, Operation.READ);
    }

    @Override
    public boolean isAuthorizedToWriteList(String name) {
        return isAuthorized(name, Operation.WRITE);
    }

    @Override
    public List<NamespacedList> removeListsWithNoReadPermissionsFromNamespacedLists(List<NamespacedList> namespacedLists) {
        List<Permission> permissions = getPermissions();

        if (Permission.hasPermitAll(permissions)) {
            return namespacedLists;
        }

        if (isGeneralPermissionPresent(Operation.READ, permissions)) {
            return filterOutEntitiesDeniedToRead(namespacedLists, permissions,
                NamespacedListsPermissionPostProcessService::getNameFromNamespacedList);
        } else {
            return filterOutEntitiesAllowedToRead(namespacedLists, permissions,
                NamespacedListsPermissionPostProcessService::getNameFromNamespacedList);
        }
    }

    private static String getNameFromNamespacedList(NamespacedList namespacedList) {
        return namespacedList.getName();
    }

    private static String getNameFromNamespacedListSearch(NamespacedListEntity namespacedList) {
        return namespacedList.getName();
    }

    private <T> List<T> filterOutEntitiesDeniedToRead(List<T> namespacedLists, List<Permission> permissions, Function<T, String> getName) {
        List<String> deniedLists = getNSListsNamesByAccessMode(permissions, AccessMode.DENY_READ);

        return namespacedLists.stream()
            .filter(namespacedList -> ! deniedLists.contains(getName.apply(namespacedList)))
            .collect(Collectors.toList());
    }

    private <T> List<T> filterOutEntitiesAllowedToRead(List<T> namespacedLists, List<Permission> permissions, Function<T, String> getName) {
        List<String> allowedLists = getNSListsNamesByAccessMode(permissions, AccessMode.ALLOW_READ);

        return namespacedLists.stream()
            .filter(namespacedList -> allowedLists.contains(getName.apply(namespacedList)))
            .collect(Collectors.toList());
    }

    @Override
    public NamespacedListSearchResult removeListsWithNoReadPermissionsFromSearchResult(NamespacedListSearchResult namespacedListsSearchResults) {
        List<Permission> permissions = getPermissions();

        if (Permission.hasPermitAll(permissions)) {
            return namespacedListsSearchResults;
        }

        List<NamespacedListEntity> namespacedLists = namespacedListsSearchResults.getNamespacedLists();

        if (isGeneralPermissionPresent(Operation.READ, permissions)) {
            List<NamespacedListEntity> namespacedListsToReturn = filterOutEntitiesDeniedToRead(namespacedLists, permissions,
                NamespacedListsPermissionPostProcessService::getNameFromNamespacedListSearch);

            namespacedListsSearchResults.setNamespacedLists(namespacedListsToReturn);
            return namespacedListsSearchResults;
        } else {
            List<NamespacedListEntity> namespacedListsToReturn = filterOutEntitiesAllowedToRead(namespacedLists, permissions,
                NamespacedListsPermissionPostProcessService::getNameFromNamespacedListSearch);

            namespacedListsSearchResults.setNamespacedLists(namespacedListsToReturn);
            return namespacedListsSearchResults;
        }
    }

    private boolean isAuthorized(String entityName, Operation operation) {
        List<Permission> permissions = getPermissions();

        if (Permission.hasPermitAll(permissions)) {
            return true;
        }

        if (isGeneralPermissionPresent(operation, permissions)) {
            // if we are here it means that we have GENERAL ALLOW permission for a given operation (read/write)
            // since granular permissions have priority over general permissions now we have to look
            // if there is a GRANULAR DENY permission which discards GENERAL ALLOW
            AccessMode accessMode = AccessMode.fromRestrictionAndOperation(Restriction.DENY, operation);
            List<String> deniedNSListsNames = getNSListsNamesByAccessMode(permissions, accessMode);

            if (!deniedNSListsNames.contains(entityName)) {
                return true;
            }
        } else {
            AccessMode accessMode = AccessMode.fromRestrictionAndOperation(Restriction.ALLOW, operation);
            List<String> allowedNSListsNames = getNSListsNamesByAccessMode(permissions, accessMode);

            if (allowedNSListsNames.contains(entityName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGeneralPermissionPresent(Operation operation, List<Permission> permissions) {
        return permissions.contains(Permission.of(operation, RedirectorConstants.NAMESPACED_LISTS))
                || permissions.contains(Permission.of(operation, "*"));
    }

    private List<String> getNSListsNamesByAccessMode(List<Permission> permissions, AccessMode accessMode) {
         return permissions.stream()
                    .filter(Permission::isGranular)
                    .filter(permission -> permission.getAccessMode() == accessMode)
                    .map(Permission::getEntity)
                    .collect(Collectors.toList());
    }

    private List<Permission> getPermissions() {
         return permissionProvider.getPermissions().stream()
                .map(Permission::new)
                .filter(Permission::isValid)
                .collect(Collectors.toList());
    }
}
