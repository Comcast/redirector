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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model.validation.visitor.redirector;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ModelValidationVisitor(forClass = Whitelisted.class)
public class WhitelistedValidationVisitor extends BaseExpressionValidationVisitor<Whitelisted> {

    @Override
    public void visit(Whitelisted newWhitelist) {

        Whitelisted currStacks = getValidationState().getWhitelisted();
        Server defaultServer = getValidationState().getDefaultServer();
        List<PathItem> servicePaths = getValidationState().getServicePaths().getPaths(getValidationState().getServiceName()).getStacks();

        boolean isDefaultPathWhitelisted = true;

        if (defaultServer != null && StringUtils.isNotBlank(defaultServer.getPath())) {

            List<String> stacksOfDefaultServer = servicePaths
                    .stream()
                    .filter(pathItem -> pathItem.getValue().contains(defaultServer.getPath()))
                    .map(pathItem -> pathItem.getValue().substring(0, pathItem.getValue().lastIndexOf("/")))
                    .collect(Collectors.toList());

            Set<String> activeStackPaths = getValidationState().getActivePaths().stream()
                .filter(item -> item.getValue().startsWith("/"))
                .map(item -> item.getValue().substring(0, item.getValue().lastIndexOf("/")))
                .collect(Collectors.toSet());

            if (! CollectionUtils.intersection(stacksOfDefaultServer, activeStackPaths).isEmpty()) {
                isDefaultPathWhitelisted = CollectionUtils.isNotEmpty(CollectionUtils.intersection(newWhitelist.getPaths(), stacksOfDefaultServer));
            }
        }

        if (!isDefaultPathWhitelisted) {
            getValidationState().pushError(ValidationState.ErrorType.DeleteDefaultSeverWhitelistedPathError);
            return;
        }



        // Whitelist is too critical to allow a DELETE to mess things up. So DELETE API should always be unsupported.
        // For POST with empty data, let us only allow it when there is only one entry in the whitelist (Response BODY
        // should mention "More than one entry remains in whitelist"). This prevents any call being made by mistake causing
        // a major issue. Of course user can send two different POSTs (first to save just one entry, and second to empty)
        // - but that would be a backdoor way to delete the whitelist in case we need to use it.

        if (CollectionUtils.isEmpty(newWhitelist.getPaths()) &&
                currStacks != null && CollectionUtils.isNotEmpty(currStacks.getPaths()) && currStacks.getPaths().size() > 1) {
            getValidationState().pushError(ValidationState.ErrorType.WhitelistDeleteError);
        }

        for (String path : newWhitelist.getPaths()) {

            if (StringUtils.isBlank(path)) {
                getValidationState().pushError(ValidationState.ErrorType.WhitelistEmptyPath);
            }

            if (StringUtils.isNotBlank(path) && !path.matches("^/[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$")) {
                getValidationState().pushError(ValidationState.ErrorType.WhitelistInvalidPath);
            }
        }
    }
}
