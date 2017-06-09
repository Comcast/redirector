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
import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.testsuite.visitor.VisitorFactories;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

@ModelValidationVisitor(forClass = Distribution.class)
public class DistributionValidationVisitor extends BaseExpressionValidationVisitor<Distribution> {
    @Override
    public void visit(Distribution item) {

        // trying to remove all distribution at once is not allowed. At least one should remain in case batch remove.
        // this is needed to avoid possibly erroneous deletion of all distribution at once
        if (getValidationState().getCurrentDistribution().getRules() != null &&
                getValidationState().getCurrentDistribution().getRules().size() > 1 &&
                CollectionUtils.isEmpty(item.getRules())) {
            getValidationState().pushError(ValidationState.ErrorType.DistributionDeletionError);
        }

        Set<Server> servers = new HashSet<>();
        float distributionPercentage = 0;

        Server defaultServer = getValidationState().getDefaultServer();

        int i = 1;
        for (Rule rule : item.getRules()) {
            Server server = rule.getServer();
            PendingChange rulePendingChange = null;
            if (getValidationState().getPendingChangesStatus() != null && getValidationState().getPendingChangesStatus().getDistributions() != null) {
                rulePendingChange = getValidationState().getPendingChangesStatus().getDistributions().get(Integer.toString(i-1));
            }

            if (!(rulePendingChange != null && rulePendingChange.getChangeType() == ActionType.DELETE)) {
                distributionPercentage += rule.getPercent();
            }

            // 1. validate server
            if (rule instanceof IVisitable) {
                rule.accept(VisitorFactories.VALIDATION.getFactory().get(rule.getClass(), getValidationState()));
            }

            // 2. distribution can contain only 'simple' servers
            boolean simpleMode = StringUtils.isNotBlank(server.getUrl()) && server.getUrl().contains(RedirectorConstants.HOST_PLACEHOLDER);
            if (!simpleMode) {
                getValidationState().pushError(ValidationState.ErrorType.DistributionOnlySimpleServersErr);
            }

            // 3. set server name/description according to requirements
            if (rule.getServer().getName() == null) {
                rule.getServer().setName("distribution server " + (i++));
                rule.getServer().setDescription(rule.getPercent() + "% distribution server");
            }

            //4. avoid servers duplicated in distribution
            for (Server distributionServer : servers) {
                if (server.getPath().equals(distributionServer.getPath())) {
                    getValidationState().pushError(ValidationState.ErrorType.DistributionDuplicatedServersErr);
                }
            }
            servers.add(rule.getServer());
            //5. Avoid overriding default server
            if (defaultServer != null && rule.getServer().getPath().equals(defaultServer.getPath())) {
                getValidationState().pushError(ValidationState.ErrorType.ServerPathOverridesDistribution);
            }
        }

        // available range for rule is [0.01,...,99.99] and total range(can be empty) is [0.00,...,99.99]
        if (distributionPercentage < 0 || distributionPercentage >= 100) {
            getValidationState().pushError(ValidationState.ErrorType.DistributionPercentageInvalid);
        }
    }
}
