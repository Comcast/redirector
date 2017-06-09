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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.core.modelupdate.chain.validator;

import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import org.apache.commons.lang3.StringUtils;

public class UrlRulesValidator implements Validator<URLRules> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(UrlRulesValidator.class);

    @Override
    public ValidationReport validate(URLRules model) {
        ValidationReport validationReport = new ValidationReport(ValidationResultType.SUCCESS);

        if (model == null) {
            log.warn("Got null URLRules");

            validationReport.setValidationResultType(ValidationResultType.REQUIRED_MODEL_NULL);
            validationReport.setMessage("Could't get URLRules.");

        } else if (! isValid(model) ) {
            validationReport.setValidationResultType(ValidationResultType.REQUIRED_MODEL_INVALID);
            validationReport.setMessage("Url Rules are not valid.");
        }

        return validationReport;
    }

    private boolean isValid(URLRules urlRules) {
        return urlRules.getDefaultStatement() != null
            && urlRules.getDefaultStatement().getUrlRule() != null
            && areDefaultUrlParamsValid(urlRules.getDefaultStatement().getUrlRule());
    }

    private static boolean areDefaultUrlParamsValid(UrlRule params) {
        return isValidNumericValue(params.getIpProtocolVersion())
            && isValidNumericValue(params.getPort())
            && StringUtils.isNotBlank(params.getProtocol())
            && StringUtils.isNotBlank(params.getUrn());
    }

    public static boolean isValidNumericValue(String numericValue) {
        Integer numeric;

        if (StringUtils.isNotBlank(numericValue)) {
            try {
                numeric = Integer.valueOf(numericValue);
                return numeric != null && numeric > 0;
            } catch (NumberFormatException ex) {
                log.warn("Wrong numeric value is found - [{}] ", numericValue);
                return false;
            }
        }

        return false;
    }
}
