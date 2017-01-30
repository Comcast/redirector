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

package com.comcast.redirector.core.modelupdate.chain.validator;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.common.util.ThreadLocalLogger;

public class FlavorRulesValidator implements Validator<SelectServer> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(FlavorRulesValidator.class);

    @Override
    public ValidationReport validate(SelectServer model) {
        ValidationReport validationReport = new ValidationReport(ValidationResultType.SUCCESS);
        // models should be not null
        // flavor rules should have at least default and fallback server
        if (model == null) {
            log.warn("Got null SelectServer in Flavor Rules");
            validationReport.setValidationResultType(ValidationResultType.REQUIRED_MODEL_NULL);
            validationReport.setMessage("Could't get model.");
        } else if (model.getDistribution() == null || model.getDistribution().getDefaultServer() == null) {
            log.warn("Got null Default Server in Flavor Rules");
            validationReport.setValidationResultType(ValidationResultType.REQUIRED_MODEL_INVALID);
            validationReport.setMessage("Flavor Rules are not valid.");
        }
        return validationReport;
    }
}
