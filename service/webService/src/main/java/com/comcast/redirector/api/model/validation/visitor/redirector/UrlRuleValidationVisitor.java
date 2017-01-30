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

import com.comcast.redirector.api.model.ProtocolVersion;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.common.IpProtocolVersion;
import com.comcast.redirector.dataaccess.EntityType;
import org.apache.commons.lang3.StringUtils;

@ModelValidationVisitor(forClass = UrlRule.class)
public class UrlRuleValidationVisitor extends BaseExpressionValidationVisitor<UrlRule> {
    private static final int MIN_PORT_VALUE = 1;
    private static final int MAX_PORT_VALUE = 65535;

    @Override
    public void visit(UrlRule item) {

        if (EntityType.URL_PARAMS.equals(getValidationState().getEntityType()) && hasEmptyParams(item)) {
            getValidationState().pushError(ValidationState.ErrorType.UrlParamsHasEmptyValues);
            return;
        }

        // one parameter should be filled at least in URL rule return statement: urn, protocol, port, ipProtocolVersion
        if (allParamsAreEmpty(item)) {
            getValidationState().pushError(ValidationState.ErrorType.AllReturnURLParamsMissed);
            return;
        }

        if (StringUtils.isNotBlank(item.getProtocol()) && !ProtocolVersion.getProtocols().contains(item.getProtocol())) {
            getValidationState().pushError(ValidationState.ErrorType.ProtocolIsInvalid);
        }

        if (StringUtils.isNotBlank(item.getPort()) && !isPortValid(item)) {
            getValidationState().pushError(ValidationState.ErrorType.PortIsInvalid);
        }

        if (StringUtils.isNotBlank(item.getUrn()) && !item.getUrn().matches("^[a-zA-Z0-9-_;\\.]+$")) {
            getValidationState().pushError(ValidationState.ErrorType.UrnIsInvalid);
        }

        if (StringUtils.isNotBlank(item.getIpProtocolVersion())
                && !item.getIpProtocolVersion().equals(IpProtocolVersion.IPV4.getVersionString())
                && !item.getIpProtocolVersion().equals(IpProtocolVersion.IPV6.getVersionString())) {
            getValidationState().pushError(ValidationState.ErrorType.ValueIPV6_4TypeError);
        }
    }

    private boolean hasEmptyParams(UrlRule urlRule) {
        return StringUtils.isBlank(urlRule.getUrn()) ||
                StringUtils.isBlank(urlRule.getProtocol()) ||
                urlRule.getPort() == null ||
                urlRule.getIpProtocolVersion() == null;
    }

    private boolean allParamsAreEmpty(UrlRule rule) {
        return (StringUtils.isBlank(rule.getUrn())
                && StringUtils.isBlank(rule.getProtocol())
                && StringUtils.isBlank(rule.getPort())
                && StringUtils.isBlank(rule.getIpProtocolVersion()));
    }

    private boolean isPortValid(final UrlRule rule) {
        try {
            Integer port = Integer.valueOf(rule.getPort());
            return port != null && port > MIN_PORT_VALUE && port < MAX_PORT_VALUE;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
