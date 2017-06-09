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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;

public class Result {
    private ModelContext context;
    private ValidationReport validationReport;
    private boolean success = true;

    private Result(ModelContext context) {
        this.context = context;
    }

    public static Result success(ModelContext context) {
        return new Result(context);
    }

    public static Result failure(ModelContext context) {
        return failure(context, new ValidationReport(Validator.ValidationResultType.FAILURE));
    }

    public static Result failure(ModelContext context, ValidationReport validationReport) {
        Result result = new Result(context);
        result.success = false;
        result.validationReport = validationReport;

        return result;
    }

    public boolean isSuccessful() {
        return success;
    }

    public ValidationReport getValidationReport() {
        return validationReport;
    }

    public IRedirectorEngine getRedirectorEngine() {
        return context.getRedirectorEngine();
    }

    public ModelContext getContext() {
        return context;
    }
}
