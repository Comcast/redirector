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

package com.comcast.redirector.api.model.validation;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.PathItem;

import javax.xml.bind.annotation.*;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

@XmlRootElement(name="validationState")
@XmlAccessorType(XmlAccessType.NONE)
public class ValidationState implements IVisitorState {

    public enum ErrorType {
        MissedRuleName,
        InvalidRuleName,
        EmptyExpressions,
        ExpressionCouldNotBeDeserialized,
        MissedParamName,
        MissedParamValue,
        ServerUrlMissed,
        ServerUrlInvalid,
        ServerPathMissed,
        ServerGroupCountDown,
        ServerGroupEnablePrivateInvalid,
        ServerGroupEnablePrivateMissed,
        ServerPathOverridesDistribution,
        ServerIsMissed,
        SimpleServerInvalidUrl,
        DistributionPercentageInvalid,
        DistributionDuplicatedServersErr,
        DistributionOnlySimpleServersErr,
        DistributionDeletionError,
        RuleDistributionPercentageInvalid,
        NamespacedListNameIsInvalid,
        NamespacedListNameIsMissed,
        NamespacedListContainsEmptyValue,
        NamespacedListValueIsInvalid,
        NamespacedlistIsInUse,
        NamespacedListsNamesMismatch,
        NamespacedListsDuplicates,
        PartnerIdMissing,
        PartnerPropertiesMissing,
        PartnerIsInUse,
        InactivePathError,
        NonWhitelistedPathError,
        WhitelistEmptyPath,
        WhitelistDeleteError,
        WhitelistInvalidPath,
        DeleteDefaultSeverWhitelistedPathError,
        IpRangeTypeError,
        DuplicatedLessParam,
        DuplicatedGreaterParam,
        DuplicatedLessOrEqualParam,
        DuplicatedGreaterOrEqualParam,
        ValueStringTypeError,
        ValueNumericTypeError,
        ValueVersionTypeError,
        ValueIPV6TypeError,
        ValueIPV6_4TypeError,
        PercentRangeError,
        RandomRangeError,
        AllReturnURLParamsMissed,
        UrlParamsHasEmptyValues,
        ProtocolIsInvalid,
        PortIsInvalid,
        UrnIsInvalid,
        TemplateDependsOnTemplate,
        TemplateIsUsed
    }

    public enum ActionType {
        ADD,
        UPDATE,
        DELETE
    }

    public static Pattern versionPattern = Pattern.compile("([\\da-zA-Z]+([-]?[0-9a-zA-Z]+)*)+([\\.]{1}[\\da-zA-Z]+([0-9a-zA-Z-]+[\\da-zA-Z]+)?)*");
    public static Pattern macPattern = Pattern.compile("[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}");
    public static Pattern alphaNumericPattern = Pattern.compile("[0-9a-zA-z]+");

    @XmlElementWrapper(name="errors")
    private Map<ErrorType, String> errors = new HashMap<>();
    String[] errorToString = new String[ErrorType.values().length];
    private Map<String, Object> params = new HashMap<>(); // any element which this validator validates store params here
    private Set<PathItem> paths = new HashSet<>();
    private Collection<IfExpression> rules;
    private Distribution currentDistribution = new Distribution();
    private Distribution distribution = new Distribution();
    private Whitelisted whitelisted;
    private EntityType entityType;
    private Server defaultServer;
    private PendingChangesStatus pendingChangesStatus;
    private ActionType actionType = null;
    private ServicePaths servicePaths;
    private String serviceName;

    public ValidationState() {

        errorToString[ErrorType.MissedRuleName.ordinal()]   = "Rule name is missed.";
        errorToString[ErrorType.InvalidRuleName.ordinal()]  = "Rule name is invalid. Rule name is required and must contain only word characters, i.e. letters, numbers and _";

        errorToString[ErrorType.EmptyExpressions.ordinal()]                 = "Rule must contain at least one expression";
        errorToString[ErrorType.ExpressionCouldNotBeDeserialized.ordinal()] = "Some rule expression(s) could not be deserialized to any of our expression objects";
        errorToString[ErrorType.MissedParamName.ordinal()]                  = "Expression name is missed.";
        errorToString[ErrorType.MissedParamValue.ordinal()]                 = "Expression value is missed.";

        errorToString[ErrorType.ServerUrlMissed.ordinal()]                 = "Server is invalid: url is missed";
        errorToString[ErrorType.ServerUrlInvalid.ordinal()]                = "Server is invalid: valid service url must match pattern: protocol://host[:port][/urn]";
        errorToString[ErrorType.SimpleServerInvalidUrl.ordinal()]          = "Server with simple url is invalid: valid simple url must match pattern: protocol://{host}:port/urn";
        errorToString[ErrorType.ServerPathMissed.ordinal()]                = "Server is invalid: path is missed";
        errorToString[ErrorType.ServerGroupCountDown.ordinal()]            = "ServerGroup is invalid: countDownTime should be more or equal to -1.";
        errorToString[ErrorType.ServerGroupEnablePrivateInvalid.ordinal()] = "ServerGroup is invalid: EnablePrivate param should have 'true' or 'false' values.";
        errorToString[ErrorType.ServerGroupEnablePrivateMissed.ordinal()]  = "ServerGroup is invalid: EnablePrivate param is missed.";
        errorToString[ErrorType.ServerPathOverridesDistribution.ordinal()] = "Server path overrides distribution one.";

        errorToString[ErrorType.ServerIsMissed.ordinal()]  = "Rule must contain return server.";

        errorToString[ErrorType.DistributionPercentageInvalid.ordinal()]    = "Total percentage value is invalid. Value should be between 0 and 99.99.";
        errorToString[ErrorType.DistributionDuplicatedServersErr.ordinal()] = "Distribution has duplicated servers.";
        errorToString[ErrorType.DistributionOnlySimpleServersErr.ordinal()] = "Distribution can NOT contain 'advanced' url.";
        errorToString[ErrorType.DistributionDeletionError.ordinal()]        = "Deletion of all rules of distribution at once is not allowed. At least one distribution must stay.";

        errorToString[ErrorType.DuplicatedLessParam.ordinal()]           = "Invalid rule. Same parameter name is used more than once in 'lessThan' expression";
        errorToString[ErrorType.DuplicatedGreaterParam.ordinal()]        = "Invalid rule. Same parameter name is used more than once in 'greaterThan' expression";
        errorToString[ErrorType.DuplicatedLessOrEqualParam.ordinal()]    = "Invalid rule. Same parameter name is used more than once in 'lessOrEqualThan' expression";
        errorToString[ErrorType.DuplicatedGreaterOrEqualParam.ordinal()] = "Invalid rule. Same parameter name is used more than once in 'greaterOrEqualThan' expression";

        errorToString[ErrorType.RuleDistributionPercentageInvalid.ordinal()] = "Percentage value is invalid. Value should be between 0 and 99.99.";

        errorToString[ErrorType.NamespacedlistIsInUse.ordinal()]                = "Namespaced list name is used in rule.";
        errorToString[ErrorType.NamespacedListsNamesMismatch.ordinal()]         = "Request isn't  correct (lists names mismatch).";
        errorToString[ErrorType.NamespacedListsDuplicates.ordinal()]         = "List contains values, that are contained in the other list.";
        errorToString[ErrorType.NamespacedListNameIsMissed.ordinal()]           = "Namespaced list name is missed.";
        errorToString[ErrorType.NamespacedListNameIsInvalid.ordinal()]          = "Namespaced list name is invalid. Name is required and must contain only word characters, i.e. letters, numbers and _";
        errorToString[ErrorType.NamespacedListContainsEmptyValue.ordinal()]     = "Namespaced list name is invalid. Namespaced list contains empty value.";
        errorToString[ErrorType.NamespacedListValueIsInvalid.ordinal()]     = "Some namespaced list value is invalid against NS list type.";

        errorToString[ErrorType.PartnerIdMissing.ordinal()]         = "Partner Id missing.";
        errorToString[ErrorType.PartnerPropertiesMissing.ordinal()] = "Properties are absent.";

        errorToString[ErrorType.WhitelistInvalidPath.ordinal()] = "Whitelist is invalid. Path should match pattern: /DC/STACK and contain only word characters and '.', '_', '-' symbols.";
        errorToString[ErrorType.WhitelistDeleteError.ordinal()] = "Whitelist is invalid. More than one entry remains in whitelist.";
        errorToString[ErrorType.WhitelistEmptyPath.ordinal()]   = "Whitelist is invalid. Path can not be empty.";
        errorToString[ErrorType.DeleteDefaultSeverWhitelistedPathError.ordinal()] = "Whitelist is invalid. Whitelisted path which is used by default server can not be deleted or made non-whitelisted";

        errorToString[ErrorType.IpRangeTypeError.ordinal()]   = "IP Range TYPE is invalid.";

        errorToString[ErrorType.InactivePathError.ordinal()]        = "Server path should be active.";
        errorToString[ErrorType.NonWhitelistedPathError.ordinal()]  = "Server path should be whitelisted.";

        errorToString[ErrorType.ValueStringTypeError.ordinal()]     = "Relational expression of 'String' type should contain only word characters.";
        errorToString[ErrorType.ValueNumericTypeError.ordinal()]    = "Relational expression of 'Numeric' type should contain only numbers.";
        errorToString[ErrorType.ValueVersionTypeError.ordinal()]    = "Relational expression of 'Version / IPv4 address' type should be a version / IPv4 address (alphabet/numbers/hyphens separated by dots)";
        errorToString[ErrorType.ValueIPV6TypeError.ordinal()]       = "Relational expression of 'IPv6' type should be valid IPv6 address.";
        errorToString[ErrorType.ValueIPV6_4TypeError.ordinal()]     = "Value 'IPv6/IPv4/Range' should be valid.";
        errorToString[ErrorType.PercentRangeError.ordinal()]        = "Percent range should be valid(> 0 and <= 100)";
        errorToString[ErrorType.RandomRangeError.ordinal()]        = "Random expression value should be in range (0; 100]";

        errorToString[ErrorType.AllReturnURLParamsMissed.ordinal()] = "All return url parameters are empty. At least one return url parameter should be added.";
        errorToString[ErrorType.UrlParamsHasEmptyValues.ordinal()] = "All Default Url Parameters values must be set.";
        errorToString[ErrorType.ProtocolIsInvalid.ordinal()]        = "Protocol is invalid. Allowable protocol versions: " + ProtocolVersion.getProtocols().toString();
        errorToString[ErrorType.PortIsInvalid.ordinal()]            = "Port is invalid. Must be a value between 1 and 65535.";
        errorToString[ErrorType.UrnIsInvalid.ordinal()]             = "Urn is invalid. Allowed symbols are: alphanumeric and - _ ; .";
        errorToString[ErrorType.TemplateDependsOnTemplate.ordinal()] = "Trying to import template, that depends on template";
        errorToString[ErrorType.TemplateIsUsed.ordinal()]           = "Template is used.";
    }

    public ValidationState(EntityType entityType) {
        this();
        this.entityType = entityType;
    }

    public ValidationState(PendingChangesStatus pendingChangesStatus, EntityType entityType) {
        this();
        this.pendingChangesStatus = pendingChangesStatus;
        this.entityType = entityType;
    }

    public ValidationState(Set<PathItem> paths, Distribution currentDistribution, PendingChangesStatus pendingChangesStatus, Server defaultServer) {
        this();
        this.paths = paths;
        this.currentDistribution = currentDistribution;
        this.pendingChangesStatus = pendingChangesStatus;
        this.defaultServer = defaultServer;
    }

    public ValidationState(Set<PathItem> paths, PendingChangesStatus pendingChangesStatus, EntityType entityType) {
        this();
        this.paths = paths;
        this.pendingChangesStatus = pendingChangesStatus;
        this.entityType = entityType;
    }

    public ValidationState(Set<PathItem> paths, PendingChangesStatus pendingChangesStatus, EntityType entityType, Distribution distribution) {
        this();
        this.paths = paths;
        this.pendingChangesStatus = pendingChangesStatus;
        this.entityType = entityType;
        this.distribution = distribution;
    }

    public ValidationState(Whitelisted whitelisted) {
        this();
        this.whitelisted = whitelisted;
    }

    public ValidationState(Collection<IfExpression> rules) {
        this();
        this.rules = rules;
    }

    public ValidationState(Collection<IfExpression> rules, EntityType entityType, ActionType actionType) {
        this(rules);
        this.entityType = entityType;
        this.actionType = actionType;
    }

    public void setParam(String paramName, Object paramValue) {
        params.put(paramName, paramValue);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public boolean hasParam(String paramName) {
        return params.containsKey(paramName);
    }

    public void pushError(ErrorType errorType) {
        if (!errors.containsKey(errorType)) {
            errors.put(errorType, errorToString[errorType.ordinal()]);
        }
    }

    public void pushError(ErrorType errorType, String errorMsg) {
        if (!errors.containsKey(errorType)) {
            errors.put(errorType, errorMsg);
        }
    }

    public Map<ErrorType, String> getErrors() {
        return errors;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Collection<IfExpression> getRules() {
        return rules;
    }

    public Distribution getCurrentDistribution() {
        return currentDistribution;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public Set<PathItem> getActivePaths() {
        return paths;
    }

    public void setActivePaths(Set<PathItem> paths) {
        this.paths = paths;
    }

    public Server getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(Server defaultServer) {
        this.defaultServer = defaultServer;
    }

    public PendingChangesStatus getPendingChangesStatus() {
        return pendingChangesStatus;
    }

    public Whitelisted getWhitelisted() {
        return whitelisted;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public ServicePaths getServicePaths() {
        return servicePaths;
    }

    public void setServicePaths(ServicePaths servicePaths) {
        this.servicePaths = servicePaths;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void validateDuplicates(String operatorName, String expressionName, ErrorType error) {

        if (hasParam(operatorName)) {
            Set<String> names = ((Set<String>)getParams().get(operatorName));
            if (names.contains(expressionName)) {
                pushError(error);
            }
            else {
                names.add(expressionName);
            }
        }else {
            Set<String> paramName = new HashSet<>();
            paramName.add(expressionName);
            setParam(operatorName, paramName);
        }
    }

    public void validateValue(String value, Expressions.ValueType type) {

        if (type.equals(Expressions.ValueType.NUMERIC)){
            try {
                Float.parseFloat(value);
            }
            catch (Exception e){
                pushError(ErrorType.ValueNumericTypeError);
            }
        }
        else if (type.equals(Expressions.ValueType.VERSION) && !versionPattern.matcher(value).matches()){
            pushError(ErrorType.ValueVersionTypeError);
        }
        else if (type.equals(Expressions.ValueType.IPV6)){
            try {
                Inet6Address.getByName(value);
            } catch (UnknownHostException e) {
                pushError(ErrorType.ValueIPV6TypeError);
            }
        }
    }

    public String getErrorMessage() {

        StringBuffer msg = new StringBuffer();

        if (errors.isEmpty()) {
            msg.append("Validation passed successfully.");
        }
        else {
            int errorInd = 0;
            msg.append("Validation failed. Next errors has been found:");
            for (String error : errors.values()) {
                msg.append("\n").append(++errorInd).append(". ").append(error);
            }
        }

        return msg.toString();
    }

    public boolean isExpressionValid() throws ExpressionValidationException {
        if (!errors.isEmpty()) {
            throw new ExpressionValidationException(getErrorMessage(), this);
        }
        return true;
    }
}
