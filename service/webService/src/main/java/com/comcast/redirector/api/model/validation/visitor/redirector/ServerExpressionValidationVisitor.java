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

package com.comcast.redirector.api.model.validation.visitor.redirector;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.xrestack.PathItem;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@ModelValidationVisitor(forClass = Server.class)
public class ServerExpressionValidationVisitor extends BaseExpressionValidationVisitor<Server> {

    @Override
    public void visit(Server server) {
        for (Rule rule : getValidationState().getDistribution().getRules()) {
            if (server.getPath().equals(rule.getServer().getPath())) {
                getValidationState().pushError(ValidationState.ErrorType.ServerPathOverridesDistribution);
            }
        }

        if (StringUtils.isBlank(server.getUrl())){
            getValidationState().pushError(ValidationState.ErrorType.ServerUrlMissed);
        }
        else if (!server.getUrl().contains(RedirectorConstants.HOST_PLACEHOLDER) && !server.getUrl().matches("(([a-z]+){1}(:\\/\\/)([\\S]+)([:]*)([0-9]*)[\\/]*([\\S]*))+")) {
            getValidationState().pushError(ValidationState.ErrorType.ServerUrlInvalid);
        }

        boolean simpleMode = StringUtils.isNotBlank(server.getUrl()) && server.getUrl().contains(RedirectorConstants.HOST_PLACEHOLDER); // in simple mode of return server path should be present
        if (simpleMode && StringUtils.isBlank(server.getPath())) {
            getValidationState().pushError(ValidationState.ErrorType.ServerPathMissed);
        }

        if (simpleMode && StringUtils.isNotBlank(server.getPath()) && !containsPath(getValidationState().getActivePaths(), server.getPath())) {
            getValidationState().pushError(ValidationState.ErrorType.InactivePathError);
        }

        if ("false".equals(server.getIsNonWhitelisted())) {
            for (PathItem pathItem : getValidationState().getActivePaths()) {
                if (pathItem.getValue().equals(server.getPath()) && (pathItem.getWhitelistedNodesCount() == 0 )) {
                    getValidationState().pushError(ValidationState.ErrorType.NonWhitelistedPathError);
                }
            }
        }

        // actually this is not good change state of the server in its validation
        // but with current solution this is the simplest and most straightforward solution
        updateServerName(server);

        if (simpleMode && !server.getUrl().equals("{protocol}://{host}:{port}/{urn}")) {
            getValidationState().pushError(ValidationState.ErrorType.SimpleServerInvalidUrl);
        }
    }

    private boolean containsPath (Set<PathItem> items, String path) {
        for (PathItem item : items) {
            if (item.getValue().equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If user did not set server name the server name will be set as the rule name
     * @param server
     */
    private void updateServerName(Server server) {

        if (server.getName() == null && getValidationState().hasParam("ruleName")) {
            server.setName((String)getValidationState().getParams().get("ruleName"));
        }
        else if (server.getName() != null && server.getName().equalsIgnoreCase("default")) {
            server.setDescription("Default server route");
        }

        if (StringUtils.isBlank(server.getDescription()) && getValidationState().hasParam("ruleName")) {
            server.setDescription(getValidationState().getParams().get("ruleName") + " server route");
        }
    }
}
